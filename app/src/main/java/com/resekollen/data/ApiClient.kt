package com.resekollen.data

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/** https://open-meteo.com/en/docs/geocoding-api – stad -> koordinater */
interface GeocodingApi {
    @GET("v1/search")
    suspend fun search(
        @Query("name") name: String,
        @Query("count") count: Int = 1,
        @Query("language") language: String = "sv",
        @Query("format") format: String = "json",
    ): GeocodingResponse
}

/** https://open-meteo.com/en/docs – väder just nu + 7-dagarsprognos */
interface ForecastApi {
    @GET("v1/forecast")
    suspend fun forecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String =
            "temperature_2m,apparent_temperature,relative_humidity_2m,precipitation,weather_code," +
                "wind_speed_10m,wind_direction_10m,is_day,uv_index,visibility",
        @Query("daily") daily: String =
            "weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max,uv_index_max",
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") days: Int = 7,
    ): ForecastResponse
}

/** https://exchangerate.host/documentation */
interface ExchangeRateApi {
    @GET("live")
    suspend fun live(
        @Query("access_key") accessKey: String,
    ): LiveRatesResponse
}

object ApiClient {
    private val http = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private fun retrofit(baseUrl: String): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(http)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val geocoding: GeocodingApi = retrofit("https://geocoding-api.open-meteo.com/").create(GeocodingApi::class.java)
    val forecast: ForecastApi = retrofit("https://api.open-meteo.com/").create(ForecastApi::class.java)

    // Exchangerate Hosts gratisplan kräver http (inte https).
    val rates: ExchangeRateApi = retrofit("http://api.exchangerate.host/").create(ExchangeRateApi::class.java)
}
