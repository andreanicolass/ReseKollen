package com.resekollen.data

import java.time.LocalDate

/** Appens egen modell för väder på ett resmål (byggd av Open-Meteos två svar). */
data class WeatherReport(
    val city: String,
    val region: String?,
    val country: String,
    val countryCode: String?,
    val latitude: Double,
    val longitude: Double,
    val timezone: String?,
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val precipitation: Double,
    val weatherCode: Int,
    val isDay: Boolean,
    val windSpeed: Double,
    val windDirection: Int,
    val uvIndex: Double,
    val visibilityKm: Double,
    val daily: List<DailyForecast>,
)

data class DailyForecast(
    val date: LocalDate,
    val weatherCode: Int,
    val maxTemp: Double,
    val minTemp: Double,
    val precipChance: Int?,
    val uvMax: Double?,
)
