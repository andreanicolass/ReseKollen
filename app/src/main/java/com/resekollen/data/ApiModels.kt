package com.resekollen.data

import com.google.gson.annotations.SerializedName

/* ---------- Open-Meteo Geocoding: /v1/search ---------- */

data class GeocodingResponse(
    val results: List<GeoPlace>?,
)

data class GeoPlace(
    val name: String?,
    val latitude: Double?,
    val longitude: Double?,
    val country: String?,
    @SerializedName("country_code") val countryCode: String?,
    val admin1: String?,
    val timezone: String?,
)

/* ---------- Open-Meteo Forecast: /v1/forecast ---------- */

data class ForecastResponse(
    val timezone: String?,
    val current: CurrentBlock?,
    val daily: DailyBlock?,
)

data class CurrentBlock(
    val time: String?,
    @SerializedName("temperature_2m") val temperature: Double?,
    @SerializedName("apparent_temperature") val apparentTemperature: Double?,
    @SerializedName("relative_humidity_2m") val humidity: Int?,
    val precipitation: Double?,
    @SerializedName("weather_code") val weatherCode: Int?,
    @SerializedName("wind_speed_10m") val windSpeed: Double?,
    @SerializedName("wind_direction_10m") val windDirection: Int?,
    @SerializedName("is_day") val isDay: Int?,
    @SerializedName("uv_index") val uvIndex: Double?,
    /** meter */
    val visibility: Double?,
)

data class DailyBlock(
    val time: List<String>?,
    @SerializedName("weather_code") val weatherCode: List<Int?>?,
    @SerializedName("temperature_2m_max") val maxTemp: List<Double?>?,
    @SerializedName("temperature_2m_min") val minTemp: List<Double?>?,
    @SerializedName("precipitation_probability_max") val precipChance: List<Int?>?,
    @SerializedName("uv_index_max") val uvMax: List<Double?>?,
)

/** Open-Meteo skickar {"error": true, "reason": "..."} vid fel. */
data class OpenMeteoError(
    val error: Boolean?,
    val reason: String?,
)

/* ---------- Exchangerate Host: /live ---------- */

data class ApiError(
    val code: Int?,
    val type: String?,
    val info: String?,
)

data class LiveRatesResponse(
    val success: Boolean?,
    val error: ApiError?,
    val timestamp: Long?,
    val source: String?,
    /** T.ex. "USDSEK" -> 10.52 */
    val quotes: Map<String, Double>?,
)
