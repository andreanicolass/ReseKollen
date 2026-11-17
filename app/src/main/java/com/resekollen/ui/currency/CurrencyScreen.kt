package com.resekollen.ui.currency

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.resekollen.R
import com.resekollen.data.Countries
import com.resekollen.ui.resolve
import androidx.compose.ui.res.stringResource
import com.resekollen.ui.components.ErrorCard
import com.resekollen.ui.components.SectionCard
import com.resekollen.ui.formatAmount
import com.resekollen.ui.formatTimestamp
import com.resekollen.ui.parseAmount

@Composable
fun CurrencyScreen(vm: CurrencyViewModel, contentPadding: PaddingValues) {
    val state by vm.state.collectAsState()
    var picking by remember { mutableStateOf<String?>(null) } // "from" / "to"

    val rates = state.rates

    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (state.error != null) {
            item { ErrorCard(state.error!!.resolve(), onRetry = { vm.refresh(force = true) }) }
        }

        item {
            SectionCard(title = stringResource(R.string.converter_title)) {
                OutlinedTextField(
                    value = state.amount,
                    onValueChange = vm::onAmountChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.amount)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CurrencyButton(state.from, Modifier.weight(1f)) { picking = "from" }
                    FilledTonalIconButton(onClick = vm::swap, modifier = Modifier.padding(horizontal = 8.dp)) {
                        Icon(Icons.Outlined.SwapVert, contentDescription = stringResource(R.string.swap))
                    }
                    CurrencyButton(state.to, Modifier.weight(1f)) { picking = "to" }
                }
                Spacer(Modifier.height(18.dp))

                val amount = parseAmount(state.amount)
                val result = if (rates != null && amount != null) rates.convert(amount, state.from, state.to) else null
                when {
                    state.loading && rates == null -> Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                    result != null -> {
                        Text(
                            "${formatAmount(amount!!)} ${state.from} =",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            "${formatAmount(result)} ${state.to}",
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        val unit = rates!!.convert(1.0, state.from, state.to)
                        if (unit != null) {
                            Text(
                                "1 ${state.from} = ${formatAmount(unit)} ${state.to}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    amount == null -> Text(stringResource(R.string.invalid_amount), color = MaterialTheme.colorScheme.error)
                }
            }
        }

        if (rates != null) {
            item {
                SectionCard(title = stringResource(R.string.popular_currencies_title)) {
                    Column {
                        Countries.popularCurrencies.forEachIndexed { index, code ->
                            val inSek = rates.convert(1.0, code, "SEK") ?: return@forEachIndexed
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { vm.setFrom(code); vm.setTo("SEK") }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(Countries.flagForCurrency(code), fontSize = 26.sp)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(code, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        Countries.currencyName(code),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Text(stringResource(R.string.amount_in_sek, formatAmount(inSek)), style = MaterialTheme.typography.bodyLarge)
                            }
                            if (index < Countries.popularCurrencies.lastIndex) HorizontalDivider()
                        }
                    }
                }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.rates_footer, formatTimestamp(rates.timestamp * 1000)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = { vm.refresh(force = true) }, enabled = !state.loading) {
                        Icon(Icons.Outlined.Refresh, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.refresh))
                    }
                }
            }
        }
    }

    val target = picking
    if (target != null && rates != null) {
        CurrencyPickerDialog(
            currencies = rates.currencies,
            onDismiss = { picking = null },
            onPick = { code ->
                if (target == "from") vm.setFrom(code) else vm.setTo(code)
                picking = null
            },
        )
    }
}

@Composable
private fun CurrencyButton(code: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = modifier.height(56.dp), shape = MaterialTheme.shapes.medium) {
        Text(Countries.flagForCurrency(code), fontSize = 20.sp)
        Spacer(Modifier.width(8.dp))
        Text(code, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Icon(Icons.Outlined.ArrowDropDown, contentDescription = null)
    }
}

@Composable
private fun CurrencyPickerDialog(currencies: List<String>, onDismiss: () -> Unit, onPick: (String) -> Unit) {
    var filter by remember { mutableStateOf("") }
    // Populära valutor först, sedan resten i bokstavsordning.
    val ordered = remember(currencies) {
        val popular = (listOf("SEK") + Countries.popularCurrencies).filter { it in currencies }
        popular + currencies.filterNot { it in popular }
    }
    val shown = ordered.filter {
        filter.isBlank() ||
            it.contains(filter, ignoreCase = true) ||
            Countries.currencyName(it).contains(filter, ignoreCase = true)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) } },
        title = { Text(stringResource(R.string.choose_currency)) },
        text = {
            Column {
                OutlinedTextField(
                    value = filter,
                    onValueChange = { filter = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.currency_search_hint)) },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    singleLine = true,
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(Modifier.heightIn(max = 380.dp)) {
                    items(shown, key = { it }) { code ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { onPick(code) }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(Countries.flagForCurrency(code), fontSize = 22.sp)
                            Spacer(Modifier.width(12.dp))
                            Text(code, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(48.dp))
                            Text(
                                Countries.currencyName(code),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        },
    )
}
