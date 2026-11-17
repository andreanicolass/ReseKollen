package com.resekollen.ui.destination

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.resekollen.R
import com.resekollen.ReseKollenApp
import com.resekollen.data.ApiException
import com.resekollen.data.Favorite
import com.resekollen.data.Rates
import com.resekollen.data.WeatherReport
import com.resekollen.ui.AppLanguage
import com.resekollen.ui.UiText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DestinationUiState(
    val query: String = "",
    val loading: Boolean = false,
    val weather: WeatherReport? = null,
    val error: UiText? = null,
    val rates: Rates? = null,
    val ratesLoading: Boolean = false,
    val ratesError: UiText? = null,
    val sekAmount: String = "1000",
)

class DestinationViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as ReseKollenApp
    private val repository = app.repository
    private val favoritesStore = app.favorites

    private val _state = MutableStateFlow(DestinationUiState())
    val state: StateFlow<DestinationUiState> = _state.asStateFlow()

    val favorites: StateFlow<List<Favorite>> = favoritesStore.favorites

    private var searchJob: Job? = null
    private var language = AppLanguage.get(application)

    fun onQueryChange(text: String) = _state.update { it.copy(query = text) }

    fun onSekAmountChange(text: String) = _state.update { it.copy(sekAmount = text) }

    /** Anropas när användaren byter språk – hämtar om vädret så att land/region får rätt språk. */
    fun onLanguageChanged(newLanguage: String) {
        if (newLanguage == language) return
        language = newLanguage
        val current = _state.value
        if (current.weather != null || current.error != null) search(current.query)
    }

    fun search(city: String = _state.value.query, forceRefresh: Boolean = false) {
        val query = city.trim()
        if (query.isEmpty()) {
            _state.update { it.copy(error = UiText(R.string.error_enter_city)) }
            return
        }
        searchJob?.cancel()
        _state.update { it.copy(query = query, loading = true, error = null) }
        searchJob = viewModelScope.launch {
            try {
                val weather = repository.getWeather(query, language, forceRefresh)
                favoritesStore.updateSnapshot(weather)
                _state.update { it.copy(loading = false, weather = weather) }
                loadRates()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, weather = null, error = e.toUiText()) }
            }
        }
    }

    fun loadRates(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _state.update { it.copy(ratesLoading = true, ratesError = null) }
            try {
                val rates = repository.getRates(forceRefresh)
                _state.update { it.copy(rates = rates, ratesLoading = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(ratesLoading = false, ratesError = e.toUiText()) }
            }
        }
    }

    fun toggleFavorite() {
        _state.value.weather?.let { favoritesStore.toggle(it) }
    }

    fun removeFavorite(city: String) = favoritesStore.remove(city)
}

fun Exception.toUiText(): UiText = (this as? ApiException)?.text ?: UiText(R.string.error_unknown)
