package com.resekollen.ui.currency

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.resekollen.ReseKollenApp
import com.resekollen.data.Rates
import com.resekollen.ui.UiText
import com.resekollen.ui.destination.toUiText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CurrencyUiState(
    val amount: String = "100",
    val from: String = "SEK",
    val to: String = "EUR",
    val rates: Rates? = null,
    val loading: Boolean = false,
    val error: UiText? = null,
)

class CurrencyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as ReseKollenApp).repository

    private val _state = MutableStateFlow(CurrencyUiState())
    val state: StateFlow<CurrencyUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh(force: Boolean = false) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val rates = repository.getRates(force)
                _state.update { it.copy(rates = rates, loading = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.toUiText()) }
            }
        }
    }

    fun onAmountChange(text: String) = _state.update { it.copy(amount = text) }
    fun setFrom(code: String) = _state.update { it.copy(from = code) }
    fun setTo(code: String) = _state.update { it.copy(to = code) }
    fun swap() = _state.update { it.copy(from = it.to, to = it.from) }
}
