package com.resekollen.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

/** En sparad resdestination, med senast kända väder så listan kan visas utan nya API-anrop. */
data class Favorite(
    val city: String,
    val country: String,
    val countryCode: String?,
    val temperature: Int?,
    val weatherCode: Int?,
    val isDay: Boolean,
    val updatedAt: Long,
)

class FavoritesStore(context: Context) {
    private val prefs = context.getSharedPreferences("resekollen_favorites", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _favorites = MutableStateFlow(load())
    val favorites: StateFlow<List<Favorite>> = _favorites.asStateFlow()

    fun isFavorite(city: String): Boolean =
        _favorites.value.any { it.city.equals(city, ignoreCase = true) }

    fun toggle(report: WeatherReport) {
        if (isFavorite(report.city)) remove(report.city) else save(_favorites.value + report.toFavorite())
    }

    fun remove(city: String) {
        save(_favorites.value.filterNot { it.city.equals(city, ignoreCase = true) })
    }

    /** Uppdaterar sparat väder om staden redan är en favorit. */
    fun updateSnapshot(report: WeatherReport) {
        if (!isFavorite(report.city)) return
        save(_favorites.value.map { if (it.city.equals(report.city, ignoreCase = true)) report.toFavorite() else it })
    }

    private fun WeatherReport.toFavorite() = Favorite(
        city = city,
        country = country,
        countryCode = countryCode,
        temperature = temperature.roundToInt(),
        weatherCode = weatherCode,
        isDay = isDay,
        updatedAt = System.currentTimeMillis(),
    )

    private fun save(list: List<Favorite>) {
        _favorites.value = list
        prefs.edit().putString(KEY, gson.toJson(list)).apply()
    }

    private fun load(): List<Favorite> {
        val json = prefs.getString(KEY, null) ?: return emptyList()
        return try {
            gson.fromJson(json, object : TypeToken<List<Favorite>>() {}.type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private companion object {
        // v2: väderkoder från Open-Meteo (WMO)
        const val KEY = "favorites_v2"
    }
}
