package com.platon.easymusicandroid

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun animatedGradient(currentStation: Station?): Brush {
    val gradientColors = currentStation?.gradientColors ?: listOf(Color.Gray, Color.DarkGray)
    return Brush.linearGradient(gradientColors)
}

@Composable
fun EasyMusicApp(
    stations: List<Station>,
    onStationClick: (Station) -> Unit
) {
    var currentStation by remember { mutableStateOf<Station?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(animatedGradient(currentStation))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(stations.size) { index ->
                StationCard(
                    station = stations[index],
                    onClick = {
                        currentStation = stations[index]
                        onStationClick(stations[index])
                    }
                )
            }
        }
    }
}

@Composable
fun StationCard(station: Station, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = station.name, style = MaterialTheme.typography.titleLarge)
            Text(text = station.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}