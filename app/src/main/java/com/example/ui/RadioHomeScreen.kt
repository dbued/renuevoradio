package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
import com.example.model.AppThemeMode
import com.example.model.BibleVerse
import com.example.model.PlaybackStatus
import com.example.model.SongTrack
import com.example.ui.components.AudioVisualizer
import com.example.ui.components.DailyVerseMiniCard
import com.example.ui.components.DailyVerseScreen
import com.example.ui.components.PlayerSleepTimerCard
import com.example.ui.components.SleepTimerDialog
import com.example.ui.components.SocialShareDialog
import com.example.ui.components.SongHistoryList
import com.example.ui.components.VolumeControl
import com.example.ui.theme.AmbientGlowYellow
import com.example.ui.theme.DarkPrimary
import com.example.ui.theme.LiveIndicatorRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadioHomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val status by viewModel.playbackStatus.collectAsStateWithLifecycle()
    val currentTrack by viewModel.currentTrack.collectAsStateWithLifecycle()
    val volume by viewModel.volume.collectAsStateWithLifecycle()
    val isMuted by viewModel.isMuted.collectAsStateWithLifecycle()
    val sessionDuration by viewModel.sessionDurationSeconds.collectAsStateWithLifecycle()
    val sleepTimerSeconds by viewModel.sleepTimerRemainingSeconds.collectAsStateWithLifecycle()
    val sleepTimerInitialSeconds by viewModel.sleepTimerInitialSeconds.collectAsStateWithLifecycle()
    val isFadeOutEnabled by viewModel.isFadeOutEnabled.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val songHistory by viewModel.songHistory.collectAsStateWithLifecycle()
    val verseOfTheDay by viewModel.verseOfTheDay.collectAsStateWithLifecycle()
    val isVerseLoading by viewModel.isVerseLoading.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val isSleepTimerOpen by viewModel.isSleepTimerDialogOpen.collectAsStateWithLifecycle()
    val isStreamInfoOpen by viewModel.isStreamInfoDialogOpen.collectAsStateWithLifecycle()

    var isPlayerShareOpen by remember { mutableStateOf(false) }
    var isTopMenuOpen by remember { mutableStateOf(false) }

    // Handle back press to return to Player tab if in another tab
    BackHandler(enabled = selectedTab != MainTab.PLAYER) {
        viewModel.selectTab(MainTab.PLAYER)
    }

    if (isPlayerShareOpen) {
        val trackToShare = currentTrack ?: SongTrack(
            title = "Transmisión en Vivo",
            artist = viewModel.streamInfo.stationName,
            rawTitle = "${viewModel.streamInfo.stationName} - En Vivo"
        )
        SocialShareDialog(
            track = trackToShare,
            isCurrentlyPlaying = true,
            onDismiss = { isPlayerShareOpen = false }
        )
    }

    if (isSleepTimerOpen) {
        SleepTimerDialog(
            remainingSeconds = sleepTimerSeconds,
            initialSeconds = sleepTimerInitialSeconds,
            isFadeOutEnabled = isFadeOutEnabled,
            onSetTimer = { minutes, fadeOut -> viewModel.setSleepTimer(minutes, fadeOut) },
            onAddMinutes = { viewModel.addSleepTimerMinutes(it) },
            onCancelTimer = { viewModel.cancelSleepTimer() },
            onToggleFadeOut = { viewModel.setFadeOutEnabled(it) },
            onDismiss = { viewModel.closeSleepTimerDialog() }
        )
    }

    if (isStreamInfoOpen) {
        StreamInfoDialog(
            streamInfo = viewModel.streamInfo,
            status = status,
            onDismiss = { viewModel.closeStreamInfoDialog() },
            onCopyUrl = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("URL de Radio", viewModel.streamInfo.streamUrl)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "URL copiada al portapapeles", Toast.LENGTH_SHORT).show()
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Small circular logo matching screenshot top left
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_renuevo_artwork),
                                contentDescription = "Logo Renuevo Radio",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Column {
                            Text(
                                text = "Renuevo Radio",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "102.4 FM • ¡Música y Vida!",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                actions = {
                    // Live Indicator Badge
                    LivePillBadge(isPlaying = status == PlaybackStatus.PLAYING)

                    Spacer(modifier = Modifier.width(4.dp))

                    // Minimalist single "Más" menu matching the reference screenshot
                    Box {
                        IconButton(
                            onClick = { isTopMenuOpen = true },
                            modifier = Modifier.testTag("top_bar_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Menú Más opciones",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = isTopMenuOpen,
                            onDismissRequest = { isTopMenuOpen = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Versículo del Día (RVR1960)") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.AutoStories,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                onClick = {
                                    isTopMenuOpen = false
                                    viewModel.selectTab(MainTab.VERSE)
                                },
                                modifier = Modifier.testTag("menu_item_verse")
                            )

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        when (themeMode) {
                                            AppThemeMode.DARK -> "Tema: Oscuro (cambiar a Claro)"
                                            AppThemeMode.LIGHT -> "Tema: Claro (cambiar a Sistema)"
                                            AppThemeMode.SYSTEM -> "Tema: Sistema (cambiar a Oscuro)"
                                        }
                                    )
                                },
                                leadingIcon = {
                                    val icon = when (themeMode) {
                                        AppThemeMode.DARK -> Icons.Default.DarkMode
                                        AppThemeMode.LIGHT -> Icons.Default.LightMode
                                        AppThemeMode.SYSTEM -> Icons.Default.SettingsBrightness
                                    }
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                onClick = {
                                    isTopMenuOpen = false
                                    val nextMode = when (themeMode) {
                                        AppThemeMode.DARK -> AppThemeMode.LIGHT
                                        AppThemeMode.LIGHT -> AppThemeMode.SYSTEM
                                        AppThemeMode.SYSTEM -> AppThemeMode.DARK
                                    }
                                    viewModel.setThemeMode(nextMode)
                                    scope.launch {
                                        val label = when (nextMode) {
                                            AppThemeMode.DARK -> "Modo oscuro activado"
                                            AppThemeMode.LIGHT -> "Modo claro activado"
                                            AppThemeMode.SYSTEM -> "Tema del sistema activado"
                                        }
                                        snackbarHostState.showSnackbar(label)
                                    }
                                },
                                modifier = Modifier.testTag("menu_item_theme")
                            )

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (sleepTimerSeconds > 0L) {
                                            "Apagado en ${viewModel.formatSleepTimer(sleepTimerSeconds)}"
                                        } else {
                                            "Temporizador de Apagado"
                                        }
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Bedtime,
                                        contentDescription = null,
                                        tint = if (sleepTimerSeconds > 0L) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                onClick = {
                                    isTopMenuOpen = false
                                    viewModel.openSleepTimerDialog()
                                },
                                modifier = Modifier.testTag("menu_item_sleep_timer")
                            )

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                            DropdownMenuItem(
                                text = { Text("Detalles de Transmisión") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                onClick = {
                                    isTopMenuOpen = false
                                    viewModel.openStreamInfoDialog()
                                },
                                modifier = Modifier.testTag("menu_item_stream_info")
                            )

                            DropdownMenuItem(
                                text = { Text("Compartir Renuevo Radio") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                onClick = {
                                    isTopMenuOpen = false
                                    isPlayerShareOpen = true
                                },
                                modifier = Modifier.testTag("menu_item_share")
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                windowInsets = WindowInsets.statusBars
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = selectedTab == MainTab.PLAYER,
                    onClick = { viewModel.selectTab(MainTab.PLAYER) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Headphones,
                            contentDescription = "Radio en Vivo"
                        )
                    },
                    label = { Text("Listen") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        indicatorColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("nav_tab_player")
                )
                NavigationBarItem(
                    selected = selectedTab == MainTab.VERSE,
                    onClick = { viewModel.selectTab(MainTab.VERSE) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.AutoStories,
                            contentDescription = "Versículo del Día RVR1960"
                        )
                    },
                    label = { Text("Versículo") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        indicatorColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("nav_tab_verse")
                )
                NavigationBarItem(
                    selected = selectedTab == MainTab.HISTORY,
                    onClick = { viewModel.selectTab(MainTab.HISTORY) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (songHistory.isNotEmpty()) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ) {
                                        Text("${songHistory.size}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.QueueMusic,
                                contentDescription = "Playlist e Historial de Canciones"
                            )
                        }
                    },
                    label = { Text("Playlist") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        indicatorColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("nav_tab_history")
                )
                NavigationBarItem(
                    selected = selectedTab == MainTab.SETTINGS,
                    onClick = { viewModel.selectTab(MainTab.SETTINGS) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Ajustes y Conexión"
                        )
                    },
                    label = { Text("Ajustes") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        indicatorColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("nav_tab_settings")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                MainTab.PLAYER -> {
                    ScreenshotInspiredPlayer(
                        status = status,
                        currentTrack = currentTrack,
                        volume = volume,
                        isMuted = isMuted,
                        sessionDurationFormatted = viewModel.formatDuration(sessionDuration),
                        errorMessage = errorMessage,
                        sleepTimerRemainingSeconds = sleepTimerSeconds,
                        sleepTimerInitialSeconds = sleepTimerInitialSeconds,
                        sleepTimerFormatted = viewModel.formatSleepTimer(sleepTimerSeconds),
                        currentVerse = verseOfTheDay,
                        onOpenVerse = { viewModel.selectTab(MainTab.VERSE) },
                        onTogglePlayPause = { viewModel.togglePlayPause() },
                        onStop = { viewModel.stop() },
                        onVolumeChange = { viewModel.setVolume(it) },
                        onToggleMute = { viewModel.toggleMute() },
                        onFavoriteCurrent = { currentTrack?.let { viewModel.toggleFavorite(it) } },
                        onViewHistory = { viewModel.selectTab(MainTab.HISTORY) },
                        onSetSleepTimer = { minutes ->
                            viewModel.setSleepTimer(minutes)
                            scope.launch {
                                snackbarHostState.showSnackbar("Temporizador de apagado fijado en $minutes minutos")
                            }
                        },
                        onOpenSleepTimer = { viewModel.openSleepTimerDialog() },
                        onAddSleepTimerMinutes = { extraMinutes ->
                            viewModel.addSleepTimerMinutes(extraMinutes)
                            scope.launch {
                                snackbarHostState.showSnackbar("+$extraMinutes minutos añadidos al temporizador")
                            }
                        },
                        onCancelSleepTimer = {
                            viewModel.cancelSleepTimer()
                            scope.launch {
                                snackbarHostState.showSnackbar("Temporizador de apagado cancelado")
                            }
                        },
                        onShareStream = { isPlayerShareOpen = true }
                    )
                }
                MainTab.VERSE -> {
                    DailyVerseScreen(
                        verse = verseOfTheDay,
                        isLoading = isVerseLoading,
                        onRefreshVerse = { viewModel.refreshVerse() },
                        onFetchRandomVerse = { viewModel.fetchRandomVerse() },
                        onNavigateToPlayer = { viewModel.selectTab(MainTab.PLAYER) }
                    )
                }
                MainTab.HISTORY -> {
                    SongHistoryList(
                        songs = songHistory,
                        currentTrackId = currentTrack?.id,
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onDeleteSong = { viewModel.deleteSong(it) },
                        onClearHistory = { viewModel.clearHistory() }
                    )
                }
                MainTab.SETTINGS -> {
                    SettingsTabContent(
                        currentThemeMode = themeMode,
                        onThemeModeSelect = { viewModel.setThemeMode(it) },
                        sleepTimerSeconds = sleepTimerSeconds,
                        sleepTimerFormatted = viewModel.formatSleepTimer(sleepTimerSeconds),
                        onOpenSleepTimer = { viewModel.openSleepTimerDialog() },
                        streamInfo = viewModel.streamInfo,
                        onOpenStreamInfo = { viewModel.openStreamInfoDialog() }
                    )
                }
            }
        }
    }
}

@Composable
fun ScreenshotInspiredPlayer(
    status: PlaybackStatus,
    currentTrack: SongTrack?,
    volume: Float,
    isMuted: Boolean,
    sessionDurationFormatted: String,
    errorMessage: String?,
    sleepTimerRemainingSeconds: Long,
    sleepTimerInitialSeconds: Long,
    sleepTimerFormatted: String,
    currentVerse: BibleVerse? = null,
    onOpenVerse: () -> Unit = {},
    onTogglePlayPause: () -> Unit,
    onStop: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onToggleMute: () -> Unit,
    onFavoriteCurrent: () -> Unit,
    onViewHistory: () -> Unit,
    onSetSleepTimer: (Int) -> Unit,
    onOpenSleepTimer: () -> Unit,
    onAddSleepTimerMinutes: (Int) -> Unit,
    onCancelSleepTimer: () -> Unit,
    onShareStream: () -> Unit
) {
    val context = LocalContext.current
    val isPlaying = status == PlaybackStatus.PLAYING
    val isBuffering = status == PlaybackStatus.BUFFERING

    val infiniteTransition = rememberInfiniteTransition(label = "halo_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.22f,
        targetValue = if (isPlaying) 0.50f else 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Atmospheric Ambient Halo behind album art (exact style of screenshot)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 20.dp)
                .size(340.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            AmbientGlowYellow.copy(alpha = glowAlpha),
                            AmbientGlowYellow.copy(alpha = glowAlpha * 0.4f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Error banner if any
            AnimatedVisibility(visible = errorMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onTogglePlayPause) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reintentar",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Prominent Square Artwork Card (Styled after screenshot)
            Card(
                modifier = Modifier
                    .size(270.dp)
                    .shadow(
                        elevation = if (isPlaying) 20.dp else 8.dp,
                        shape = RoundedCornerShape(26.dp),
                        spotColor = AmbientGlowYellow
                    )
                    .testTag("station_artwork_card"),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF141518)
                ),
                border = BorderStroke(1.dp, Color(0xFF2E3036))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (!currentTrack?.artworkUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = currentTrack.artworkUrl,
                            contentDescription = "Carátula de ${currentTrack.displayTitle}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.ic_renuevo_artwork),
                            contentDescription = "Renuevo Radio Logo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    if (isBuffering) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.55f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    color = DarkPrimary,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(46.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Conectando al servidor...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Station & Title (Like screenshot: bold main title + subtitle)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = currentTrack?.displayTitle ?: "Renuevo Radio",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = currentTrack?.displayArtist?.let {
                        if (it == "Renuevo Radio") "102.4 FM • En Vivo" else it
                    } ?: "102.4 FM • En Vivo",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (sessionDurationFormatted != "00:00") {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Sintonizando: $sessionDurationFormatted",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Big Iconic Center Play Button (Electric violet with clean white icon)
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { onTogglePlayPause() }
                    .shadow(
                        elevation = 16.dp,
                        shape = CircleShape,
                        spotColor = MaterialTheme.colorScheme.primary
                    )
                    .testTag("play_pause_button"),
                contentAlignment = Alignment.Center
            ) {
                if (isBuffering) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(36.dp)
                    )
                } else {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Waveform Visualizer
            AudioVisualizer(
                status = status,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                height = 42.dp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Quick actions: Favorite, Share, Sleep timer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledIconButton(
                    onClick = onFavoriteCurrent,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("player_favorite_button"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = if (currentTrack?.isFavorite == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorito",
                        tint = if (currentTrack?.isFavorite == true) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FilledIconButton(
                    onClick = onShareStream,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("player_share_button"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Compartir en redes",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FilledIconButton(
                    onClick = onOpenSleepTimer,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("player_timer_button"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (sleepTimerRemainingSeconds > 0L) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Bedtime,
                        contentDescription = "Temporizador",
                        tint = if (sleepTimerRemainingSeconds > 0L) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Volume Control Card
            VolumeControl(
                volume = volume,
                isMuted = isMuted,
                onVolumeChange = onVolumeChange,
                onToggleMute = onToggleMute,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Dedicated Sleep Timer in Player View (Presets 15m, 30m, 60m + Live countdown)
            PlayerSleepTimerCard(
                remainingSeconds = sleepTimerRemainingSeconds,
                initialSeconds = sleepTimerInitialSeconds,
                formattedTime = sleepTimerFormatted,
                onSetTimer = onSetSleepTimer,
                onAddMinutes = onAddSleepTimerMinutes,
                onCancelTimer = onCancelSleepTimer,
                onOpenCustomDialog = onOpenSleepTimer,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Versículo del Día (RVR1960) Mini Card
            DailyVerseMiniCard(
                verse = currentVerse,
                onOpenFullVerse = onOpenVerse,
                onShare = {
                    currentVerse?.let { v ->
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Versículo del Día - ${v.reference}")
                            putExtra(Intent.EXTRA_TEXT, v.formattedShareText)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Compartir Versículo (RVR1960)"))
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quick access to History
            OutlinedCard(
                onClick = onViewHistory,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quick_view_history_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                ),
                border = BorderStroke(1.dp, Color(0xFF27272A))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.QueueMusic,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Playlist e Historial de Canciones",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsTabContent(
    currentThemeMode: AppThemeMode,
    onThemeModeSelect: (AppThemeMode) -> Unit,
    sleepTimerSeconds: Long,
    sleepTimerFormatted: String,
    onOpenSleepTimer: () -> Unit,
    streamInfo: com.example.model.StreamInfo,
    onOpenStreamInfo: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Personalización y Tema",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // Dark Mode Options Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Modo de Pantalla",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Selecciona el aspecto visual que prefieras para la aplicación.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                val options = listOf(
                    Triple(AppThemeMode.DARK, "Modo Oscuro (Renuevo Radio)", Icons.Default.DarkMode),
                    Triple(AppThemeMode.LIGHT, "Modo Claro", Icons.Default.LightMode),
                    Triple(AppThemeMode.SYSTEM, "Seguir el Sistema", Icons.Default.SettingsBrightness)
                )

                options.forEach { (mode, title, icon) ->
                    val isSelected = currentThemeMode == mode
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onThemeModeSelect(mode) }
                            .testTag("theme_option_${mode.name.lowercase()}"),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        Text(
            text = "Reproducción y Transmisión",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // Sleep Timer Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenSleepTimer() }
                .testTag("settings_sleep_timer_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bedtime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Temporizador de Apagado",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (sleepTimerSeconds > 0L) "Activo: se apagará en $sleepTimerFormatted" else "Detener reproducción automáticamente",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (sleepTimerSeconds > 0L) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Stream Info Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenStreamInfo() }
                .testTag("settings_stream_info_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Headphones,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "${streamInfo.stationName} (${streamInfo.frequency})",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${streamInfo.bitrate} • ${streamInfo.codec} (ExoPlayer Engine)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LivePillBadge(isPlaying: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live_alpha"
    )

    Surface(
        color = if (isPlaying) LiveIndicatorRed.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            if (isPlaying) LiveIndicatorRed.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(
                        if (isPlaying) LiveIndicatorRed.copy(alpha = alphaAnim) else Color.Gray
                    )
            )
            Text(
                text = if (isPlaying) "EN VIVO" else "RADIO",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPlaying) LiveIndicatorRed else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StreamInfoDialog(
    streamInfo: com.example.model.StreamInfo,
    status: PlaybackStatus,
    onDismiss: () -> Unit,
    onCopyUrl: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Detalles de la Transmisión",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DetailItem("Estación", streamInfo.stationName)
                DetailItem("Frecuencia", streamInfo.frequency)
                DetailItem("Motor de Audio", "Media3 ExoPlayer 1.5.0")
                DetailItem("Calidad de Audio", "${streamInfo.bitrate} (${streamInfo.codec})")
                DetailItem(
                    "Estado Actual",
                    when (status) {
                        PlaybackStatus.PLAYING -> "Transmitiendo en vivo ✓"
                        PlaybackStatus.BUFFERING -> "Conectando al servidor..."
                        PlaybackStatus.PAUSED -> "Pausado"
                        PlaybackStatus.IDLE -> "Listo para reproducir"
                        PlaybackStatus.ERROR -> "Error de conexión"
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Servidor SurferNetwork:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "stream-177.surfernetwork.com/f6ks13hmdwzuv",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onCopyUrl,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copiar link",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Entendido")
            }
        }
    )
}

@Composable
fun DetailItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
