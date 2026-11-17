package com.resekollen.ui.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.resekollen.R
import com.resekollen.data.Countries
import androidx.compose.ui.res.stringResource
import com.resekollen.data.Favorite
import com.resekollen.data.WeatherInfo
import com.resekollen.ui.components.EmptyState
import com.resekollen.ui.destination.DestinationViewModel
import com.resekollen.ui.formatTimestamp

@Composable
fun FavoritesScreen(
    vm: DestinationViewModel,
    contentPadding: PaddingValues,
    onOpen: (String) -> Unit,
) {
    val favorites by vm.favorites.collectAsState()

    if (favorites.isEmpty()) {
        EmptyState(
            emoji = "⭐",
            title = stringResource(R.string.favorites_empty_title),
            text = stringResource(R.string.favorites_empty_text),
            modifier = Modifier.padding(contentPadding),
        )
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                stringResource(R.string.favorites_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        items(favorites, key = { it.city }) { fav ->
            FavoriteRow(fav, onClick = { onOpen(fav.city) }, onDelete = { vm.removeFavorite(fav.city) })
        }
    }
}

@Composable
private fun FavoriteRow(fav: Favorite, onClick: () -> Unit, onDelete: () -> Unit) {
    val look = WeatherInfo.look(fav.weatherCode, fav.isDay)
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(Countries.flag(fav.countryCode), fontSize = 30.sp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(fav.city, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "${fav.country} • ${formatTimestamp(fav.updatedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(look.emoji, fontSize = 24.sp)
            Spacer(Modifier.width(6.dp))
            Text(
                fav.temperature?.let { "$it°" } ?: "–",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.favorite_delete, fav.city))
            }
        }
    }
}
