package com.resekollen.ui

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import java.util.Locale

/** Sparar valt språk (sv/en) och applicerar det på appens resurser. */
object AppLanguage {
    private const val PREFS = "resekollen_settings"
    private const val KEY = "language"
    const val SWEDISH = "sv"
    const val ENGLISH = "en"

    fun get(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, SWEDISH) ?: SWEDISH

    fun set(context: Context, language: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, language).apply()
    }

    /** Returnerar en Context vars resurser (strings.xml) använder det valda språket. */
    fun wrap(context: Context): Context {
        val locale = Locale.forLanguageTag(get(context))
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}

/** En text som ska slås upp i strings.xml först i UI:t (så den följer valt språk). */
class UiText(@StringRes val id: Int, vararg val args: Any)

@Composable
fun UiText.resolve(): String = stringResource(id, *args)
