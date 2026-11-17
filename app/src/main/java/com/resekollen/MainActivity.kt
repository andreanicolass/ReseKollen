package com.resekollen

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.outlined.CurrencyExchange
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.resekollen.ui.AppLanguage
import com.resekollen.ui.currency.CurrencyScreen
import com.resekollen.ui.currency.CurrencyViewModel
import com.resekollen.ui.destination.DestinationScreen
import com.resekollen.ui.destination.DestinationViewModel
import com.resekollen.ui.favorites.FavoritesScreen
import com.resekollen.ui.theme.ReseKollenTheme

private enum class Tab(
    @StringRes val label: Int,
    @StringRes val subtitle: Int,
    val selected: ImageVector,
    val unselected: ImageVector,
) {
    Destination(R.string.tab_destination, R.string.subtitle_destination, Icons.Filled.TravelExplore, Icons.Outlined.TravelExplore),
    Currency(R.string.tab_currency, R.string.subtitle_currency, Icons.Filled.CurrencyExchange, Icons.Outlined.CurrencyExchange),
    Favorites(R.string.tab_favorites, R.string.subtitle_favorites, Icons.Filled.Star, Icons.Outlined.StarBorder),
}

class MainActivity : ComponentActivity() {

    // Gör så att strings.xml följer språket som valts i appen.
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLanguage.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val language = AppLanguage.get(this)
        setContent {
            ReseKollenTheme {
                ReseKollenRoot(
                    language = language,
                    onToggleLanguage = {
                        val next = if (language == AppLanguage.SWEDISH) AppLanguage.ENGLISH else AppLanguage.SWEDISH
                        AppLanguage.set(this, next)
                        recreate() // laddar om skärmen med det nya språket
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReseKollenRoot(language: String, onToggleLanguage: () -> Unit) {
    var tab by rememberSaveable { mutableStateOf(Tab.Destination) }
    val destinationVm: DestinationViewModel = viewModel()
    val currencyVm: CurrencyViewModel = viewModel()

    // Hämta om väder när språket byts så att land/region visas på rätt språk.
    LaunchedEffect(language) { destinationVm.onLanguageChanged(language) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.app_name), fontWeight = FontWeight.Bold)
                        Text(
                            stringResource(tab.subtitle),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    val description = stringResource(R.string.change_language)
                    TextButton(
                        onClick = onToggleLanguage,
                        modifier = Modifier.semantics { contentDescription = description },
                    ) {
                        Text(stringResource(R.string.language_toggle_label), fontWeight = FontWeight.SemiBold)
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { t ->
                    NavigationBarItem(
                        selected = tab == t,
                        onClick = { tab = t },
                        icon = { Icon(if (tab == t) t.selected else t.unselected, contentDescription = null) },
                        label = { Text(stringResource(t.label)) },
                    )
                }
            }
        },
    ) { padding ->
        when (tab) {
            Tab.Destination -> DestinationScreen(destinationVm, padding)
            Tab.Currency -> CurrencyScreen(currencyVm, padding)
            Tab.Favorites -> FavoritesScreen(destinationVm, padding, onOpen = { city ->
                destinationVm.search(city)
                tab = Tab.Destination
            })
        }
    }
}
