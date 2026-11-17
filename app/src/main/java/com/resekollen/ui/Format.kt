package com.resekollen.ui

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlin.math.abs

// Locale.getDefault() sätts av AppLanguage, så tal och datum följer valt språk.

/** Formaterar ett belopp snyggt: små tal får fler decimaler. */
fun formatAmount(value: Double): String {
    val decimals = when {
        abs(value) >= 1 -> 2
        abs(value) >= 0.01 -> 4
        else -> 6
    }
    return NumberFormat.getNumberInstance(Locale.getDefault()).apply {
        minimumFractionDigits = if (abs(value) >= 1000) 0 else 2
        maximumFractionDigits = decimals
    }.format(value)
}

/** "1 234,5" eller "12.5" -> 1234.5 */
fun parseAmount(text: String): Double? =
    text.replace(" ", "").replace(" ", "").replace(',', '.').toDoubleOrNull()

fun formatTimestamp(millis: Long): String =
    SimpleDateFormat("d MMM HH:mm", Locale.getDefault()).format(Date(millis))

/** Aktuell klocktid i resmålets tidszon, t.ex. "Europe/Madrid" -> "22:17". */
fun localClock(timezone: String?): String = try {
    LocalTime.now(ZoneId.of(timezone ?: return "–")).format(DateTimeFormatter.ofPattern("HH:mm"))
} catch (e: Exception) {
    "–"
}
