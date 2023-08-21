package com.paranid5.prima.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.paranid5.prima.data.Track
import com.paranid5.prima.di.*
import com.paranid5.prima.domain.StorageHandler
import com.paranid5.prima.presentation.screens.artists.ArtistTracksAppBar
import com.paranid5.prima.presentation.screens.main_menu_fragments.artists.ArtistsAppBar
import com.paranid5.prima.presentation.screens.main_menu_fragments.favourites.FavouritesAppBar
import com.paranid5.prima.presentation.screens.main_menu_fragments.tracks.TracksAppBar
import com.paranid5.prima.presentation.screens.playbar_screens.current_playlist.CurrentPlaylistAppBar
import com.paranid5.prima.presentation.ui.navigation.Config
import com.paranid5.prima.presentation.ui.navigation.FavouriteNavigator
import com.paranid5.prima.presentation.ui.navigation.RootNavigator
import com.paranid5.prima.presentation.ui.navigation.RootScreen
import com.paranid5.prima.presentation.ui.navigation.composition_locals.LocalFavouriteNavigator
import com.paranid5.prima.presentation.ui.navigation.composition_locals.LocalRootNavigator
import com.paranid5.prima.presentation.ui.playing_bar.PlayingBar
import com.paranid5.prima.rust.RustLibs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject(),
    selectedTrackState: MutableStateFlow<Track?> = koinInject(named(KOIN_SELECTED_TRACK)),
    playbackPositionState: MutableStateFlow<Float> = koinInject(named(KOIN_PLAYBACK_POS)),
    loopingState: MutableStateFlow<Int> = koinInject(named(KOIN_LOOPING)),
    speedState: MutableStateFlow<Float> = koinInject(named(KOIN_SPEED)),
    volumeState: MutableStateFlow<Float> = koinInject(named(KOIN_VOLUME)),
    isLikedState: MutableStateFlow<Boolean> = koinInject(named(KOIN_IS_LIKED))
) {
    val primaryColor by storageHandler.primaryColorState.collectAsState()
    val secondaryColor by storageHandler.secondaryColorState.collectAsState()
    val theme by storageHandler.themeState.collectAsState()

    val currentTrack by selectedTrackState.collectAsState()

    // Navigators
    val rootNavigator = RootNavigator(
        componentContext = DefaultComponentContext(LifecycleRegistry()),
        initialConfig = Config.MainMenuConfig.Tracks
    )

    val favouriteNavigator = FavouriteNavigator(
        componentContext = DefaultComponentContext(LifecycleRegistry()),
        initialConfig = Config.FavouritesConfig.Tracks
    )

    LaunchedEffect(Unit) {
        val curTrackTask = async(Dispatchers.IO) { RustLibs.getCurTrackBlocking() }
        val playbackPosTask = async(Dispatchers.IO) { RustLibs.getPlaybackPositionBlocking().toFloat() }

        val loopingTask = async(Dispatchers.IO) { RustLibs.getLoopingStateBlocking() }
        val speedTask = async(Dispatchers.IO) { RustLibs.getSpeedBlocking() }
        val volumeTask = async(Dispatchers.IO) { RustLibs.getVolumeBlocking() }

        selectedTrackState.update { curTrackTask.await() }
        playbackPositionState.update { playbackPosTask.await() }

        loopingState.update { loopingTask.await() }
        speedState.update { speedTask.await() }
        volumeState.update { volumeTask.await() }
    }

    LaunchedEffect(currentTrack) {
        isLikedState.update {
            currentTrack
                ?.let { withContext(Dispatchers.IO) { RustLibs.isTrackLiked(it) } }
                ?: false
        }
    }

    MaterialTheme(
        colors = Colors(
            primary = primaryColor,
            primaryVariant = primaryColor,
            secondary = secondaryColor,
            secondaryVariant = secondaryColor,
            background = secondaryColor,
            surface = secondaryColor,
            error = Color.Red,
            onPrimary = primaryColor,
            onSecondary = secondaryColor,
            onBackground = secondaryColor,
            onSurface = secondaryColor,
            onError = Color.Red,
            isLight = theme.isNight
        )
    ) {
        Surface(color = secondaryColor, modifier = modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                CompositionLocalProvider(
                    LocalRootNavigator provides rootNavigator,
                    LocalFavouriteNavigator provides favouriteNavigator
                ) {
                    Scaffold(
                        topBar = { TopBar() },
                        bottomBar = { PlayingBar() }
                    ) {
                        Row(Modifier.fillMaxSize()) {
                            NavigationMenu(Modifier.padding(bottom = 160.dp))

                            Spacer(
                                Modifier
                                    .fillMaxHeight()
                                    .width(3.dp)
                                    .background(primaryColor)
                            )

                            RootScreen()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBar(modifier: Modifier = Modifier) {
    val navigator = LocalRootNavigator.current
    val screenConfig by navigator.currentConfigState.collectAsState()

    when (screenConfig) {
        Config.MainMenuConfig.Tracks -> TracksAppBar(modifier)
        Config.MainMenuConfig.Artists -> ArtistsAppBar(modifier)
        Config.PlaybarConfig.CurrentPlaylist -> CurrentPlaylistAppBar(modifier)
        Config.MainMenuConfig.Favourites -> FavouritesAppBar(modifier)
        Config.ArtistTracks -> ArtistTracksAppBar(modifier)

        // TODO: other app bars
        Config.GTMConfig.AboutGame -> Unit
        Config.GTMConfig.Game -> Unit
        Config.MainMenuConfig.AboutApp -> Unit
        Config.MainMenuConfig.GTM -> Unit
        Config.MainMenuConfig.MP3Converter -> Unit
        Config.MainMenuConfig.Settings -> Unit
        Config.MainMenuConfig.Statistics -> Unit
        Config.MainMenuConfig.TrackCollections -> Unit
        Config.PlaybarConfig.Equalizer -> Unit
        Config.PlaybarConfig.TrimTrack -> Unit
        Config.SettingsConfig.FilesLocation -> Unit
        Config.SettingsConfig.Fonts -> Unit
        Config.SettingsConfig.HiddenTracks -> Unit
        Config.SettingsConfig.Themes -> Unit
        else -> Unit
    }
}