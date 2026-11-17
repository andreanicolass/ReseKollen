package com.resekollen.ui.destination

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.Umbrella
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.resekollen.R
import com.resekollen.data.Countries
import com.resekollen.data.DailyForecast
import com.resekollen.data.Rates
import com.resekollen.data.WeatherInfo
import com.resekollen.data.WeatherReport
import com.resekollen.ui.components.EmptyState
import com.resekollen.ui.components.ErrorCard
import com.resekollen.ui.components.SectionCard
import com.resekollen.ui.formatAmount
import com.resekollen.ui.formatTimestamp
import com.resekollen.ui.localClock
import com.resekollen.ui.parseAmount
import com.resekollen.ui.UiText
import com.resekollen.ui.resolve
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.resekollen.ui.theme.weatherGradient
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt


@Composable
fun DestinationScreen(vm: DestinationViewModel, contentPadding: PaddingValues) {
    val state by vm.state.collectAsState()
    val favorites by vm.favorites.collectAsState()
    val focus = LocalFocusManager.current
    val popularDestinations = stringArrayResource(R.array.popular_destinations)

    val weather = state.weather
    val isFavorite = weather != null && favorites.any { it.city.equals(weather.city, ignoreCase = true) }

    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            OutlinedTextField(
                value = state.query,
                onValueChange = vm::onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.search_hint)) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        IconButton(onClick = { vm.onQueryChange("") }) {
                            Icon(Icons.Outlined.Clear, contentDescription = stringResource(R.string.clear))
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.large,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    focus.clearFocus()
                    vm.search()
                }),
            )
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(popularDestinations) { city ->
                    SuggestionChip(
                        onClick = {
                            focus.clearFocus()
                            vm.search(city)
                        },
                        label = { Text(city) },
                    )
                }
            }
        }

        when {
            state.loading -> item {
                Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            state.error != null -> item {
                ErrorCard(state.error!!.resolve(), onRetry = { vm.search() })
            }

            weather != null -> {
                item {
                    WeatherHeader(
                        weather = weather,
                        isFavorite = isFavorite,
                        onToggleFavorite = vm::toggleFavorite,
                        onRefresh = { vm.search(state.query.ifBlank { weather.city }, forceRefresh = true) },
                    )
                }
                if (weather.daily.isNotEmpty()) item { ForecastCard(weather.daily) }
                item { PackingTips(weather) }
                item { WeatherDetails(weather) }
                item {
                    CurrencyCard(
                        countryName = weather.country,
                        countryCode = weather.countryCode,
                        rates = state.rates,
                        loading = state.ratesLoading,
                        error = state.ratesError,
                        sekAmount = state.sekAmount,
                        onSekAmountChange = vm::onSekAmountChange,
                        onRetry = { vm.loadRates(forceRefresh = true) },
                    )
                }
                item {
                    Text(
                        stringResource(R.string.data_sources),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            else -> item {
                EmptyState(
                    emoji = "🧳",
                    title = stringResource(R.string.empty_destination_title),
                    text = stringResource(R.string.empty_destination_text),
                )
            }
        }
    }
}

@Composable
private fun WeatherHeader(
    weather: WeatherReport,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onRefresh: () -> Unit,
) {
    val look = WeatherInfo.look(weather.weatherCode, weather.isDay)

    Box(
        Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(weatherGradient(look.kind, weather.isDay))
            .padding(20.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "${Countries.flag(weather.countryCode)}  ${weather.city}",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        listOfNotNull(weather.region?.ifBlank { null }, weather.country.ifBlank { null }).joinToString(", "),
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Outlined.Refresh, contentDescription = stringResource(R.string.refresh), tint = Color.White)
                }
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = stringResource(if (isFavorite) R.string.favorite_remove else R.string.favorite_add),
                        tint = if (isFavorite) Color(0xFFFFD54F) else Color.White,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(look.emoji, fontSize = 64.sp)
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        "${weather.temperature.roundToInt()}°",
                        color = Color.White,
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Light,
                        lineHeight = 64.sp,
                    )
                    Text(stringResource(look.text), color = Color.White, style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(Modifier.height(12.dp))

            val today = weather.daily.firstOrNull()
            val localTime = stringResource(R.string.local_time, localClock(weather.timezone))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Schedule, contentDescription = null, tint = Color.White, modifier = Modifier.padding(end = 6.dp))
                Text(
                    buildString {
                        append(localTime)
                        if (today != null) append("  •  ↑${today.maxTemp.roundToInt()}°  ↓${today.minTemp.roundToInt()}°")
                    },
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun ForecastCard(days: List<DailyForecast>) {
    val today = LocalDate.now()
    val todayLabel = stringResource(R.string.today)
    val tomorrowLabel = stringResource(R.string.tomorrow)
    SectionCard(title = stringResource(R.string.forecast_title, days.size)) {
        Column {
            days.forEachIndexed { index, day ->
                val look = WeatherInfo.look(day.weatherCode)
                val label = when (day.date) {
                    today -> todayLabel
                    today.plusDays(1) -> tomorrowLabel
                    else -> day.date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()).replaceFirstChar { it.uppercase() }
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1.4f)) {
                        Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        Text(
                            stringResource(look.text),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(look.emoji, fontSize = 24.sp, modifier = Modifier.padding(horizontal = 8.dp))
                    Text(
                        day.precipChance?.let { "💧$it%" } ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = if ((day.precipChance ?: 0) >= 50) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(0.8f),
                    )
                    Text(
                        "${day.maxTemp.roundToInt()}°",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        " / ${day.minTemp.roundToInt()}°",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (index < days.lastIndex) HorizontalDivider()
            }
        }
    }
}

@Composable
private fun WeatherDetails(weather: WeatherReport) {
    val directions = stringArrayResource(R.array.wind_directions)
    val wind = "${weather.windSpeed.roundToInt()} km/h ${directions[WeatherInfo.windDirectionIndex(weather.windDirection)]}"
    SectionCard(title = stringResource(R.string.now_title)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailTile(Icons.Outlined.Thermostat, stringResource(R.string.feels_like), "${weather.feelsLike.roundToInt()}°C", Modifier.weight(1f))
                DetailTile(Icons.Outlined.Air, stringResource(R.string.wind), wind, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailTile(Icons.Outlined.WaterDrop, stringResource(R.string.humidity), "${weather.humidity} %", Modifier.weight(1f))
                DetailTile(Icons.Outlined.Umbrella, stringResource(R.string.precipitation), "${weather.precipitation} mm", Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailTile(Icons.Outlined.WbSunny, stringResource(R.string.uv_index), uvText(weather.uvIndex), Modifier.weight(1f))
                DetailTile(Icons.Outlined.Visibility, stringResource(R.string.visibility), "${weather.visibilityKm.roundToInt()} km", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun uvText(uv: Double): String {
    val v = uv.roundToInt()
    val id = when {
        v <= 2 -> R.string.uv_low
        v <= 5 -> R.string.uv_moderate
        v <= 7 -> R.string.uv_high
        else -> R.string.uv_very_high
    }
    return stringResource(id, v)
}

@Composable
private fun DetailTile(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun PackingTips(weather: WeatherReport) {
    val tips = WeatherInfo.packingTips(weather)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.packing_title, weather.city), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            tips.forEach { Text(it.resolve(), style = MaterialTheme.typography.bodyLarge) }
        }
    }
}

@Composable
private fun CurrencyCard(
    countryName: String,
    countryCode: String?,
    rates: Rates?,
    loading: Boolean,
    error: UiText?,
    sekAmount: String,
    onSekAmountChange: (String) -> Unit,
    onRetry: () -> Unit,
) {
    val code = Countries.currencyFor(countryCode)
    SectionCard(title = stringResource(R.string.local_currency_title)) {
        when {
            code == null -> Text(
                stringResource(R.string.currency_unknown, countryName.ifBlank { stringResource(R.string.currency_unknown_country) }),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            code == "SEK" -> Text(
                stringResource(R.string.currency_same_as_home),
                style = MaterialTheme.typography.bodyLarge,
            )

            loading && rates == null -> Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(Modifier.padding(8.dp))
            }

            error != null && rates == null -> Column {
                Text(error.resolve(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
            }

            rates != null -> {
                val perSek = rates.convert(1.0, "SEK", code)
                val per100Local = rates.convert(100.0, code, "SEK")
                if (perSek == null || per100Local == null) {
                    Text(stringResource(R.string.currency_no_rate, code), style = MaterialTheme.typography.bodyMedium)
                    return@SectionCard
                }
                Text(
                    "${Countries.flagForCurrency(code)}  ${Countries.currencyName(code)} ($code)",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(4.dp))
                Text("1 SEK = ${formatAmount(perSek)} $code", style = MaterialTheme.typography.bodyMedium)
                Text("100 $code = ${formatAmount(per100Local)} SEK", style = MaterialTheme.typography.bodyMedium)

                HorizontalDivider(Modifier.padding(vertical = 12.dp))

                val sek = parseAmount(sekAmount)
                OutlinedTextField(
                    value = sekAmount,
                    onValueChange = onSekAmountChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.travel_money_label)) },
                    suffix = { Text(stringResource(R.string.kr_suffix)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    if (sek != null) "≈ ${formatAmount(sek * perSek)} $code" else stringResource(R.string.enter_amount),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.rate_source, formatTimestamp(rates.timestamp * 1000)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
