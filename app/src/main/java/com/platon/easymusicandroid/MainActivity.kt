package com.platon.easymusicandroid

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem

data class Station(
    val name: String,
    val description: String,
    val streamUrl: String,
    val gradientColors: List<Color>
)


private val stations = listOf(
    Station(
        name = "Tabris FM",
        description = "Радио, на котором крутятся отобранные, нормальные, приятные треки многих жанров. Плейлист пополняется ежедневно в районе от 18:00 до 22:00.",
        streamUrl = "https://stream.zeno.fm/uzrnuzqmen6tv",
        gradientColors = listOf(Color.Red, Color(0xFFFFA500)) // Красный и оранжевый
    ),
    Station(
        name = "Night FM",
        description = "Радио для концентрации, успокоения, наслаждения. На радио играет спокойная музыка различных исполнителей. Идеально подойдет для ночных прогулок и поездок.",
        streamUrl = "https://stream.zeno.fm/lgxpsux5v9avv",
        gradientColors = listOf(Color.Blue, Color.Cyan)
    ),
    Station(
        name = "Penis FM",
        description = "Радио для тех, кого обычные треки не устраивают и они слушают альтернативные жанры. На радио играют треки таких исполнителей как Dekma, Кишлак и тд.",
        streamUrl = "https://stream.zeno.fm/hfrwlmkuux4uv",
        gradientColors = listOf(Color.Magenta, Color(0xFF8B008B)) // Фиолетовые тона
    ),
    Station(
        name = "Platon FM",
        description = "Радио, на котором собрана вся музыка, все жанры, характеры и исполнители. Идеально подойдет для меломанов.",
        streamUrl = "http://45.95.234.91:8000/music",
        gradientColors = listOf(Color(0xFFFFA500), Color.Yellow) // Оранжевый и жёлтый (инвертировано)
    ),
    Station(
        name = "Memschol FM",
        description = "Полная сборная солянка от красивых и уникальных жанров до рофл гей ремиксов и блатных треков, часто проводятся подкасты на разные темы в прямом эфире от простого общения до политики.",
        streamUrl = "https://stream.zeno.fm/hydtchh8maguv.m3u",
        gradientColors = listOf(Color(0xFF6A0DAD), Color(0xFF9400D3)) // Темно-фиолетовый градиент
    )
)

class MainActivity : ComponentActivity() {
    private lateinit var player: ExoPlayer
    private var isPlaying = mutableStateOf(false)
    private var currentStationIndex = mutableStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ExoPlayer
        player = ExoPlayer.Builder(this).build()

        // Start the MusicService
        val musicServiceIntent = Intent(this, MusicService::class.java)
        startService(musicServiceIntent)

        setContent {
            EasyMusicApp(
                stations = stations,
                currentStationIndex = currentStationIndex.value,
                isPlaying = isPlaying.value,
                onPlayPauseToggle = {
                    if (isPlaying.value) {
                        player.pause()
                        isPlaying.value = false
                    } else {
                        playStation(stations[currentStationIndex.value])
                        isPlaying.value = true
                    }
                },
                onStationChange = { newIndex ->
                    currentStationIndex.value = newIndex
                    playStation(stations[newIndex])
                    isPlaying.value = true
                }
            )
        }
    }

    private fun playStation(station: Station) {
        val mediaItem = MediaItem.fromUri(station.streamUrl)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}

@Composable
fun EasyMusicApp(
    stations: List<Station>,
    currentStationIndex: Int,
    isPlaying: Boolean,
    onPlayPauseToggle: () -> Unit,
    onStationChange: (Int) -> Unit
) {
    val station = stations[currentStationIndex]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = station.gradientColors
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = station.name,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                ),
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = station.description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.SansSerif
                ),
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = if (isPlaying) "Now Playing: Streaming" else "Now Playing: Paused",
                color = Color.White,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (currentStationIndex > 0) onStationChange(currentStationIndex - 1)
                }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Previous",
                        tint = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.White, CircleShape)
                        .clickable { onPlayPauseToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Close else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.Red
                    )
                }

                IconButton(onClick = {
                    if (currentStationIndex < stations.size - 1) onStationChange(currentStationIndex + 1)
                }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowForward,
                        contentDescription = "Next",
                        tint = Color.White
                    )
                }
            }
        }
    }
}