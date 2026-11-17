package com.resekollen.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.resekollen.BuildConfig
import com.resekollen.R
import com.resekollen.ui.UiText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.time.LocalDate

/** Fel som visas för användaren. Texten slås upp i strings.xml så den följer valt språk. */
class ApiException(val text: UiText) : Exception()

/** Valutakurser där basen är USD (gratisplanen tillåter bara USD som källa). */
data class Rates(
    val quotes: Map<String, Double>,
    /** Unix-tid i sekunder när kurserna publicerades */
    val timestamp: Long,
) {
    private fun usdTo(code: String): Double? = if (code == "USD") 1.0 else quotes["USD$code"]

    val currencies: List<String>
        get() = (quotes.keys.map { it.removePrefix("USD") } + "USD").distinct().sorted()

    /** Räknar om via USD: belopp i [from] -> belopp i [to]. */
    fun convert(amount: Double, from: String, to: String): Double? {
        val f = usdTo(from) ?: return null
        val t = usdTo(to) ?: return null
        return amount / f * t
    }
}

/**
 * Hämtar live-data från API:erna. Valuta-API:ts gratisplan har en månadsgräns på antal anrop,
 * därför cachas svaren en stund så att appen inte slösar anrop.
 */
class TravelRepository(context: Context) {

    private val prefs = context.getSharedPreferences("resekollen_cache", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val weatherCache = mutableMapOf<String, Pair<Long, WeatherReport>>()
    private var ratesCache: Rates? = loadCachedRates()

    /**
     * 1) Slår upp staden (Open-Meteo Geocoding)  2) Hämtar väder + prognos (Open-Meteo Forecast).
     * [language] styr språket på land- och regionnamn ("sv" -> "Spanien", "en" -> "Spain").
     */
    suspend fun getWeather(city: String, language: String, forceRefresh: Boolean = false): WeatherReport =
        withContext(Dispatchers.IO) {
            val query = city.trim()
            if (query.isEmpty()) throw ApiException(UiText(R.string.error_enter_city))

            val cacheKey = "$language|${query.lowercase()}"
            val cached = weatherCache[cacheKey]
            if (!forceRefresh && cached != null && System.currentTimeMillis() - cached.first < WEATHER_TTL_MS) {
                return@withContext cached.second
            }

            val place = openMeteoCall { ApiClient.geocoding.search(query, language = language) }.results?.firstOrNull()
            if (place?.latitude == null || place.longitude == null) {
                throw ApiException(UiText(R.string.error_place_not_found, query))
            }

            val forecast = openMeteoCall { ApiClient.forecast.forecast(place.latitude, place.longitude) }
            val report = forecast.toReport(place)
                ?: throw ApiException(UiText(R.string.error_incomplete_weather, place.name ?: query))

            weatherCache[cacheKey] = System.currentTimeMillis() to report
            report
        }

    private suspend fun <T> openMeteoCall(block: suspend () -> T): T = try {
        block()
    } catch (e: HttpException) {
        val reason = parseErrorBody(e, OpenMeteoError::class.java)?.reason
        throw ApiException(
            if (reason != null) UiText(R.string.error_weather_reason, reason)
            else UiText(R.string.error_weather_http, e.code())
        )
    } catch (e: IOException) {
        throw ApiException(UiText(R.string.error_weather_offline))
    }

    private fun ForecastResponse.toReport(place: GeoPlace): WeatherReport? {
        val c = current ?: return null
        val d = daily
        val days = d?.time.orEmpty().mapIndexedNotNull { i, date ->
            DailyForecast(
                date = runCatching { LocalDate.parse(date) }.getOrNull() ?: return@mapIndexedNotNull null,
                weatherCode = d?.weatherCode?.getOrNull(i) ?: 0,
                maxTemp = d?.maxTemp?.getOrNull(i) ?: return@mapIndexedNotNull null,
                minTemp = d?.minTemp?.getOrNull(i) ?: return@mapIndexedNotNull null,
                precipChance = d?.precipChance?.getOrNull(i),
                uvMax = d?.uvMax?.getOrNull(i),
            )
        }
        return WeatherReport(
            city = place.name ?: return null,
            region = place.admin1,
            country = place.country.orEmpty(),
            countryCode = place.countryCode,
            latitude = place.latitude ?: 0.0,
            longitude = place.longitude ?: 0.0,
            timezone = timezone ?: place.timezone,
            temperature = c.temperature ?: return null,
            feelsLike = c.apparentTemperature ?: c.temperature,
            humidity = c.humidity ?: 0,
            precipitation = c.precipitation ?: 0.0,
            weatherCode = c.weatherCode ?: 0,
            isDay = c.isDay != 0,
            windSpeed = c.windSpeed ?: 0.0,
            windDirection = c.windDirection ?: 0,
            uvIndex = c.uvIndex ?: 0.0,
            visibilityKm = (c.visibility ?: 10_000.0) / 1000.0,
            daily = days,
        )
    }

    suspend fun getRates(forceRefresh: Boolean = false): Rates = withContext(Dispatchers.IO) {
        val cached = ratesCache
        val cacheAgeMs = cached?.let { System.currentTimeMillis() - prefs.getLong(KEY_RATES_FETCHED, 0) }
        if (!forceRefresh && cached != null && cacheAgeMs != null && cacheAgeMs < RATES_TTL_MS) {
            return@withContext cached
        }
        if (BuildConfig.EXCHANGERATE_KEY.isBlank()) {
            throw ApiException(UiText(R.string.error_rates_missing_key))
        }

        val response = try {
            ApiClient.rates.live(BuildConfig.EXCHANGERATE_KEY)
        } catch (e: HttpException) {
            parseErrorBody(e, LiveRatesResponse::class.java)?.error?.let { throw ApiException(ratesError(it)) }
            throw ApiException(UiText(R.string.error_rates_http, e.code()))
        } catch (e: IOException) {
            // Visa senast hämtade kurser hellre än ingenting.
            cached?.let { return@withContext it }
            throw ApiException(UiText(R.string.error_rates_offline))
        }

        response.error?.let { throw ApiException(ratesError(it)) }
        val quotes = response.quotes
        if (quotes.isNullOrEmpty()) throw ApiException(UiText(R.string.error_rates_empty))

        val rates = Rates(quotes, response.timestamp ?: (System.currentTimeMillis() / 1000))
        ratesCache = rates
        prefs.edit()
            .putString(KEY_RATES_JSON, gson.toJson(rates.quotes))
            .putLong(KEY_RATES_TIMESTAMP, rates.timestamp)
            .putLong(KEY_RATES_FETCHED, System.currentTimeMillis())
            .apply()
        rates
    }

    private fun <T> parseErrorBody(e: HttpException, type: Class<T>): T? = try {
        e.response()?.errorBody()?.string()?.let { gson.fromJson(it, type) }
    } catch (ex: Exception) {
        null
    }

    private fun loadCachedRates(): Rates? {
        val json = prefs.getString(KEY_RATES_JSON, null) ?: return null
        return try {
            val type = object : TypeToken<Map<String, Double>>() {}.type
            val quotes: Map<String, Double> = gson.fromJson(json, type)
            Rates(quotes, prefs.getLong(KEY_RATES_TIMESTAMP, 0))
        } catch (e: Exception) {
            null
        }
    }

    private fun ratesError(error: ApiError): UiText = when (error.code) {
        101 -> UiText(R.string.error_rates_invalid_key)
        102 -> UiText(R.string.error_rates_inactive)
        104 -> UiText(R.string.error_rates_limit)
        105 -> UiText(R.string.error_rates_not_in_plan)
        else -> UiText(R.string.error_rates_other, error.info ?: "code ${error.code}")
    }

    private companion object {
        const val WEATHER_TTL_MS = 15 * 60 * 1000L        // 15 min
        const val RATES_TTL_MS = 6 * 60 * 60 * 1000L      // 6 h
        const val KEY_RATES_JSON = "rates_json"
        const val KEY_RATES_TIMESTAMP = "rates_timestamp"
        const val KEY_RATES_FETCHED = "rates_fetched_at"
    }
}
