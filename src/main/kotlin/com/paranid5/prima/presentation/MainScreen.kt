package com.paranid5.prima.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.paranid5.prima.data.Artist
import com.paranid5.prima.data.Track
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
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.koin.compose.koinInject
import java.awt.Window

@Composable
fun MainScreen(
    owner: Window,
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject()
) {
    val primaryColor by storageHandler.primaryColorState.collectAsState()
    val secondaryColor by storageHandler.secondaryColorState.collectAsState()
    val theme by storageHandler.themeState.collectAsState()

    // Playing track
    val currentTrackState = remember { mutableStateOf<Track?>(null) }
    val playbackPositionState = remember { mutableStateOf(0F) }
    val isLikedState = remember { mutableStateOf(false) }

    // Playback
    val isPlayingState = remember { mutableStateOf(false) }
    val isPlayingCoverLoadedState = remember { mutableStateOf(false) }
    val isPlaybackTrackDraggingState = remember { mutableStateOf(false) }
    val loopingState = remember { mutableStateOf(0) }
    val speedState = remember { mutableStateOf(0F) }
    val volumeState = remember { mutableStateOf(0F) }

    // All tracks
    val allTracksState = remember { mutableStateListOf<Track>() }
    val filteredAllTracksState = remember { mutableStateListOf<Track>() }

    // All artists
    val allArtistsState = remember { mutableStateListOf<Artist>() }
    val filteredAllArtistsState = remember { mutableStateListOf<Artist>() }

    val selectedArtist = remember { mutableStateOf<Artist?>(null) }

    // Artist tracks
    val artistTrackListState = remember { mutableStateListOf<Track>() }
    val filteredArtistTrackListState = remember { mutableStateListOf<Track>() }

    // Current Playlist
    val currentPlaylistTracksState = mutableStateListOf<Track>()
    val currentPlaylistFilteredTracksState = mutableStateListOf<Track>()

    // Favourite Tracks
    val favouriteTracksState = mutableStateListOf<Track>()
    val filteredFavouriteTracksState = mutableStateListOf<Track>()

    // Favourite Artists
    val favouriteArtistsState = remember { mutableStateListOf<Artist>() }
    val filteredFavouriteArtistsState = remember { mutableStateListOf<Artist>() }

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

        currentTrackState.value = curTrackTask.await()
        playbackPositionState.value = playbackPosTask.await()

        loopingState.value = loopingTask.await()
        speedState.value = speedTask.await()
        volumeState.value = volumeTask.await()
    }

    LaunchedEffect(currentTrackState.value) {
        isLikedState.value = currentTrackState.value
            ?.let { withContext(Dispatchers.IO) { RustLibs.isTrackLiked(it) } }
            ?: false
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
            Column(modifier = Modifier.fillMaxSize()) {
                CompositionLocalProvider(
                    LocalRootNavigator provides rootNavigator,
                    LocalFavouriteNavigator provides favouriteNavigator
                ) {
                    Scaffold(
                        topBar = { TopBar() },
                        bottomBar = { PlayingBar() }
                    ) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            NavigationMenu(
                                modifier = Modifier.padding(bottom = 160.dp)
                            )

                            Spacer(
                                modifier = Modifier
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
        Config.MainMenuConfig.Tracks -> TracksAppBar()
        Config.MainMenuConfig.Artists -> ArtistsAppBar()
        Config.PlaybarConfig.CurrentPlaylist -> CurrentPlaylistAppBar()
        Config.MainMenuConfig.Favourites -> FavouritesAppBar()
        Config.ArtistTracks -> ArtistTracksAppBar()

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