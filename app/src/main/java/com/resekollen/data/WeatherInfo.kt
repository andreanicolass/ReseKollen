package com.resekollen.data

import androidx.annotation.StringRes
import com.resekollen.R
import com.resekollen.ui.UiText
import kotlin.math.abs
import kotlin.math.roundToInt

/** Emoji + text (från strings.xml) för en väderkod. */
data class WeatherLook(val emoji: String, @StringRes val text: Int, val kind: Kind) {
    enum class Kind { CLEAR, CLOUDY, FOG, RAIN, SNOW, STORM }
}

object WeatherInfo {

    /** WMO-väderkoder som Open-Meteo använder: https://open-meteo.com/en/docs */
    fun look(code: Int?, isDay: Boolean = true): WeatherLook = when (code) {
        0 -> if (isDay) WeatherLook("☀️", R.string.weather_clear_day, WeatherLook.Kind.CLEAR)
             else WeatherLook("🌙", R.string.weather_clear_night, WeatherLook.Kind.CLEAR)
        1 -> WeatherLook(if (isDay) "🌤️" else "🌙", R.string.weather_mainly_clear, WeatherLook.Kind.CLEAR)
        2 -> WeatherLook(if (isDay) "⛅" else "☁️", R.string.weather_partly_cloudy, WeatherLook.Kind.CLOUDY)
        3 -> WeatherLook("☁️", R.string.weather_overcast, WeatherLook.Kind.CLOUDY)
        45, 48 -> WeatherLook("🌫️", R.string.weather_fog, WeatherLook.Kind.FOG)
        51, 53, 55 -> WeatherLook("🌦️", R.string.weather_drizzle, WeatherLook.Kind.RAIN)
        56, 57 -> WeatherLook("🧊", R.string.weather_freezing_drizzle, WeatherLook.Kind.RAIN)
        61 -> WeatherLook("🌦️", R.string.weather_light_rain, WeatherLook.Kind.RAIN)
        63 -> WeatherLook("🌧️", R.string.weather_rain, WeatherLook.Kind.RAIN)
        65 -> WeatherLook("🌧️", R.string.weather_heavy_rain, WeatherLook.Kind.RAIN)
        66, 67 -> WeatherLook("🧊", R.string.weather_freezing_rain, WeatherLook.Kind.RAIN)
        71 -> WeatherLook("🌨️", R.string.weather_light_snow, WeatherLook.Kind.SNOW)
        73 -> WeatherLook("❄️", R.string.weather_snow, WeatherLook.Kind.SNOW)
        75 -> WeatherLook("❄️", R.string.weather_heavy_snow, WeatherLook.Kind.SNOW)
        77 -> WeatherLook("🌨️", R.string.weather_snow_grains, WeatherLook.Kind.SNOW)
        80 -> WeatherLook("🌦️", R.string.weather_rain_showers, WeatherLook.Kind.RAIN)
        81 -> WeatherLook("🌧️", R.string.weather_heavy_showers, WeatherLook.Kind.RAIN)
        82 -> WeatherLook("⛈️", R.string.weather_violent_showers, WeatherLook.Kind.RAIN)
        85, 86 -> WeatherLook("🌨️", R.string.weather_snow_showers, WeatherLook.Kind.SNOW)
        95 -> WeatherLook("⛈️", R.string.weather_thunder, WeatherLook.Kind.STORM)
        96, 99 -> WeatherLook("⛈️", R.string.weather_thunder_hail, WeatherLook.Kind.STORM)
        else -> WeatherLook("🌡️", R.string.weather_unknown, WeatherLook.Kind.CLOUDY)
    }

    /** Packtips baserade på vädret just nu OCH prognosen för veckan. */
    fun packingTips(w: WeatherReport): List<UiText> {
        val tips = mutableListOf<UiText>()
        val now = w.feelsLike.roundToInt()
        val weekMin = (w.daily.minOfOrNull { it.minTemp } ?: w.temperature).roundToInt()
        val weekMax = (w.daily.maxOfOrNull { it.maxTemp } ?: w.temperature).roundToInt()
        val kinds = w.daily.map { look(it.weatherCode).kind } + look(w.weatherCode, w.isDay).kind
        val rainyDays = w.daily.count { (it.precipChance ?: 0) >= 50 }
        val maxUv = maxOf(w.uvIndex, w.daily.maxOfOrNull { it.uvMax ?: 0.0 } ?: 0.0)

        tips += when {
            weekMin <= -5 -> UiText(R.string.tip_winter)
            weekMin <= 5 -> UiText(R.string.tip_warm_jacket)
            weekMin <= 12 -> UiText(R.string.tip_jacket)
            else -> UiText(R.string.tip_light_sweater)
        }
        if (weekMax >= 25) tips += UiText(R.string.tip_shorts)
        if (rainyDays > 0) tips += UiText(R.string.tip_umbrella_days, rainyDays, w.daily.size)
        else if (kinds.contains(WeatherLook.Kind.RAIN)) tips += UiText(R.string.tip_umbrella)
        if (kinds.contains(WeatherLook.Kind.SNOW)) tips += UiText(R.string.tip_snow_shoes)
        if (kinds.contains(WeatherLook.Kind.STORM)) tips += UiText(R.string.tip_thunder)
        if (maxUv >= 6) tips += UiText(R.string.tip_sunscreen)
        else if (maxUv >= 3) tips += UiText(R.string.tip_sunglasses)
        if (weekMax >= 27) tips += UiText(R.string.tip_water)
        if (w.windSpeed >= 35) tips += UiText(R.string.tip_wind)
        if (abs(now - w.temperature.roundToInt()) >= 4) tips += UiText(R.string.tip_feels_like, now)
        return tips
    }

    /** 270° -> index 6 i wind_directions ("V" / "W"). */
    fun windDirectionIndex(degrees: Int): Int = ((degrees % 360 + 360) % 360 + 22) / 45 % 8
}
