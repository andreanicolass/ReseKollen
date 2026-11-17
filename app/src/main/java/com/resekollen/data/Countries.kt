package com.resekollen.data

import java.util.Currency
import java.util.Locale

object Countries {

    /** ISO-landskod (från Open-Meteo) -> valutakod. */
    private val currencyByIso: Map<String, String> = mapOf(
        "SE" to "SEK", "NO" to "NOK", "DK" to "DKK", "IS" to "ISK", "FI" to "EUR",
        "GB" to "GBP", "IE" to "EUR", "DE" to "EUR", "FR" to "EUR", "ES" to "EUR",
        "PT" to "EUR", "IT" to "EUR", "GR" to "EUR", "NL" to "EUR", "BE" to "EUR",
        "AT" to "EUR", "LU" to "EUR", "MT" to "EUR", "CY" to "EUR", "EE" to "EUR",
        "LV" to "EUR", "LT" to "EUR", "SI" to "EUR", "SK" to "EUR", "HR" to "EUR",
        "ME" to "EUR", "MC" to "EUR", "AD" to "EUR", "SM" to "EUR", "VA" to "EUR",
        "XK" to "EUR", "CH" to "CHF", "LI" to "CHF", "PL" to "PLN", "CZ" to "CZK",
        "HU" to "HUF", "RO" to "RON", "BG" to "BGN", "RS" to "RSD", "BA" to "BAM",
        "AL" to "ALL", "MK" to "MKD", "MD" to "MDL", "TR" to "TRY", "UA" to "UAH",
        "RU" to "RUB", "GE" to "GEL", "AM" to "AMD", "AZ" to "AZN",
        "US" to "USD", "CA" to "CAD", "MX" to "MXN", "BR" to "BRL", "AR" to "ARS",
        "CL" to "CLP", "CO" to "COP", "PE" to "PEN", "CU" to "CUP", "DO" to "DOP",
        "CR" to "CRC", "PA" to "USD", "EC" to "USD", "PR" to "USD", "JM" to "JMD",
        "JP" to "JPY", "CN" to "CNY", "HK" to "HKD", "TW" to "TWD", "KR" to "KRW",
        "TH" to "THB", "VN" to "VND", "ID" to "IDR", "MY" to "MYR", "SG" to "SGD",
        "PH" to "PHP", "KH" to "KHR", "IN" to "INR", "LK" to "LKR", "MV" to "MVR",
        "NP" to "NPR", "AE" to "AED", "QA" to "QAR", "SA" to "SAR", "OM" to "OMR",
        "JO" to "JOD", "IL" to "ILS", "EG" to "EGP", "MA" to "MAD", "TN" to "TND",
        "ZA" to "ZAR", "KE" to "KES", "TZ" to "TZS", "NG" to "NGN", "GH" to "GHS",
        "MU" to "MUR", "SC" to "SCR", "AU" to "AUD", "NZ" to "NZD", "FJ" to "FJD",
    )

    fun currencyFor(countryCode: String?): String? = countryCode?.uppercase()?.let { currencyByIso[it] }

    /** "SE" -> 🇸🇪 */
    fun flag(iso: String?): String {
        if (iso == null || iso.length != 2 || !iso.all { it.isLetter() }) return "🌍"
        val base = 0x1F1E6
        return iso.uppercase().map { String(Character.toChars(base + (it - 'A'))) }.joinToString("")
    }

    /** Valutakoder börjar oftast med landets ISO-kod: SEK -> SE, EUR -> EU. */
    fun flagForCurrency(code: String): String = when {
        code == "BTC" -> "₿"
        code.startsWith("X") -> "💰"
        else -> flag(code.take(2))
    }

    /**
     * Valutans namn på valt språk, t.ex. "EUR" -> "euro" (sv) / "Euro" (en).
     * Använder Javas inbyggda valutalista så att alla ~170 valutor får ett namn.
     */
    fun currencyName(code: String): String {
        val name = try {
            Currency.getInstance(code).getDisplayName(Locale.getDefault())
        } catch (e: Exception) {
            null
        }
        val result = name?.takeIf { it != code } ?: fallbackNames[code] ?: code
        return result.replaceFirstChar { it.titlecase(Locale.getDefault()) }
    }

    // Valutor som inte finns i Javas ISO-lista.
    private val fallbackNames: Map<String, String> = mapOf("BTC" to "Bitcoin")


    /** Valutor som visas i "populära"-listan. */
    val popularCurrencies = listOf("EUR", "USD", "GBP", "NOK", "DKK", "CHF", "JPY", "THB", "TRY", "AUD")
}
