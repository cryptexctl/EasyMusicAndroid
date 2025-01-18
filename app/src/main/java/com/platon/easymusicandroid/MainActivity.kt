@file:OptIn(ExperimentalMaterial3Api::class)

package com.platon.easymusicandroid

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
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
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.pager.*
import androidx.compose.material.icons.Icons
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.times
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.times
import kotlin.math.*

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

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        window.isNavigationBarContrastEnforced = false

        // Initialize ExoPlayer
        player = ExoPlayer.Builder(this).build()

        // Start the MusicService
        val musicServiceIntent = Intent(this, MusicService::class.java)
        startService(musicServiceIntent)

        setContent {
            MusicUI(
                stations = stations,
                currentStationIndex = currentStationIndex.value,
                isPlaying = isPlaying.value,
                onPlayPauseToggle = {
                    if (isPlaying.value) {
                        player.pause()
                        isPlaying.value = false
                    }
                    else {
                        playStation(stations[currentStationIndex.value])
                        isPlaying.value = true
                    }
                },
                onStationChange = { newIndex ->
                    currentStationIndex.value = newIndex
                    if (isPlaying.value) playStation(stations[newIndex])
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

@OptIn(ExperimentalTextApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MusicUI(
    stations: List<Station>,
    currentStationIndex: Int,
    isPlaying: Boolean,
    onPlayPauseToggle: () -> Unit,
    onStationChange: (Int) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { stations.size })
    val pagesSizes = List(
        stations.size,
        {i ->
            48.dp + (350 - 48).dp * min(max(1f - pagerState.currentPageOffsetFraction * 2f, 0f), 1f)
        }
    )

    val robotoFlex =
        Font(
            R.font.robotoflex,
            variationSettings = FontVariation.Settings(
                FontVariation.weight(700),
                FontVariation.width(150f)
            )
        )

    val animatedStationGradient = listOf(
        animateColorAsState(
            targetValue = stations[pagerState.currentPage].gradientColors[0], // Изменяем прозрачность
            animationSpec = tween(durationMillis = 300),
            label = "gradientcolorone" // Добавляем анимацию
        ).value,
        animateColorAsState(
            targetValue = stations[pagerState.currentPage].gradientColors[1], // Изменяем прозрачность
            animationSpec = tween(durationMillis = 300),
            label = "gradientcolortwo" // Добавляем анимацию
        ).value
    )

    val bottomSheetState = rememberStandardBottomSheetState(initialValue = SheetValue.PartiallyExpanded)
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = bottomSheetState)

    val context = LocalContext.current
    val intent = remember { Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/easymusicplatonoferon/")) }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        modifier = Modifier.background(
            brush = Brush.verticalGradient(
                colors = animatedStationGradient
            )
        ),
        contentColor = Color.Transparent,
        containerColor = Color.Transparent,
        sheetContainerColor = Color(0xff1f1f1f),
        sheetContent = {
            Column(
                modifier = Modifier.fillMaxWidth().height(300.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = { onPlayPauseToggle() },
                    shape = CircleShape,
                    modifier = Modifier.size(96.dp),
                    colors = ButtonColors(
                        Color.White,
                        Color(0xff1f1f1f),
                        Color.White,
                        Color(0xff1f1f1f)
                    )
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Play",
                        tint = Color(0xff1f1f1f),
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.height(72.dp))
                Button(
                    onClick = {
                        try {
                            context.startActivity(intent)
                        }
                        catch (e: Exception) {
                            Toast.makeText(context, "Ошибка. Ищи вручную:\n@easymusicplatonoferon", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonColors(
                        Color.White,
                        Color(0xff1f1f1f),
                        Color.White,
                        Color(0xff1f1f1f)
                    )
                ) {
                    Text(
                        text = "ТГ чат",
                        color = Color(0xff1f1f1f)
                    )
                }
            }
        },
        sheetPeekHeight = 200.dp
    ) {
        Box {
            VerticalPager(
                state = pagerState,
                contentPadding = PaddingValues(bottom = 300.dp,
                    top = WindowInsets.systemBars.asPaddingValues(LocalDensity.current)
                        .calculateTopPadding()
                ),
                beyondViewportPageCount = pagerState.pageCount
            ) { page ->
                Column {
                    Text(
                        text = stations[page].name,
                        fontFamily = FontFamily(robotoFlex),
                        fontSize = 40.sp,
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                if (pagerState.offsetForPage(page) >= 0) {
                                    translationY = pagerState.offsetForPage(page) * (pagerState.layoutInfo.pageSize - 40.sp.toPx() - 4.dp.toPx())
                                }
                                else {
                                    translationY = minOf(0f, pagerState.offsetForPage(page) + 1f) * (pagerState.layoutInfo.pageSize - 40.sp.toPx() - 4.dp.toPx())
                                }
                            }
                            .alpha(maxOf(0f, (1f - abs(pagerState.offsetForPage(page))) / 2) + 0.5f)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                    Text(
                        text = stations[page].description,
                        fontSize = 18.sp,
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                if (pagerState.offsetForPage(page) >= 0) {
                                    translationY = pagerState.offsetForPage(page) * (pagerState.layoutInfo.pageSize - 40.sp.toPx() - 4.dp.toPx())
                                }
                                else {
                                    translationY = minOf(0f, pagerState.offsetForPage(page) + 1f) * (pagerState.layoutInfo.pageSize - 40.sp.toPx() - 4.dp.toPx())
                                }
                            }
                            .alpha(maxOf(0f, 1f - abs(pagerState.offsetForPage(page))))
                            .padding(horizontal = 8.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }
    if (pagerState.settledPage != currentStationIndex) {
        onStationChange(pagerState.settledPage)
    }
}

//следующее нагло стырено из инета, да, я сам бы до такого не додумался, извините
fun PagerState.offsetForPage(page: Int) = (currentPage - page) + currentPageOffsetFraction
