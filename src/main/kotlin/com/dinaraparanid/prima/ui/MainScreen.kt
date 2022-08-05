package com.dinaraparanid.prima.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
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
import com.dinaraparanid.prima.entities.Artist
import com.dinaraparanid.prima.entities.Track
import com.dinaraparanid.prima.rust.RustLibs
import com.dinaraparanid.prima.ui.fragments.artists.ArtistTracksAppBar
import com.dinaraparanid.prima.ui.fragments.main_menu_fragments.favourites.FavouritesAppBar
import com.dinaraparanid.prima.ui.fragments.main_menu_fragments.favourites.FavouritesScreen
import com.dinaraparanid.prima.ui.fragments.main_menu_fragments.artists.ArtistsAppBar
import com.dinaraparanid.prima.ui.fragments.main_menu_fragments.favourites.FavouriteFragments
import com.dinaraparanid.prima.ui.fragments.main_menu_fragments.tracks.TracksAppBar
import com.dinaraparanid.prima.ui.fragments.playbar_fragments.current_playlist.CurrentPlaylistAppBar
import com.dinaraparanid.prima.ui.utils.navigation.Config
import com.dinaraparanid.prima.ui.utils.navigation.RootScreen
import com.dinaraparanid.prima.ui.utils.navigation.RootView
import com.dinaraparanid.prima.utils.Params
import kotlinx.coroutines.*
import java.awt.Window

@Composable
@Preview
fun MainScreen(owner: Window) {
    val primary = Params.primaryColor
    val secondary = Params.secondaryColor

    MaterialTheme(
        colors = Colors(
            primary = primary,
            primaryVariant = primary,
            secondary = secondary,
            secondaryVariant = secondary,
            background = secondary,
            surface = secondary,
            error = Color.Red,
            onPrimary = primary,
            onSecondary = secondary,
            onBackground = secondary,
            onSurface = secondary,
            onError = Color.Red,
            isLight = !Params.theme.isNight
        )
    ) {
        val coroutineScope = rememberCoroutineScope()
        val currentTrackState: MutableState<Track?> = remember { mutableStateOf(null) }
        val playbackPositionState = remember { mutableStateOf(0F) }
        val isLikedState = remember { mutableStateOf(false) }

        coroutineScope.launch {
            currentTrackState.value = withContext(Dispatchers.IO) {
                RustLibs.getCurTrackBlocking()
            }

            playbackPositionState.value = RustLibs.getPlaybackPosition().toFloat()
            isLikedState.value = currentTrackState.value?.let(RustLibs::isTrackLiked) ?: false
        }

        val isPlayingState = remember { mutableStateOf(false) }
        val isPlayingCoverLoadedState = remember { mutableStateOf(false) }
        val isPlaybackTrackDraggingState = remember { mutableStateOf(false) }
        val loopingState = remember { mutableStateOf(RustLibs.getLoopingState()) }
        val speedState = remember { mutableStateOf(RustLibs.getSpeed()) }
        val volumeState = remember { mutableStateOf(RustLibs.getVolume()) }

        // All tracks
        val allTracksState = remember { mutableStateListOf<Track>() }
        val filteredAllTracksState = remember { mutableStateListOf<Track>() }

        // All artists
        val allArtistsState = remember { mutableStateListOf<Artist>() }
        val filteredAllArtistsState = remember { mutableStateListOf<Artist>() }

        val curArtist = remember { mutableStateOf<Artist?>(null) }

        // Artist tracks
        val artistTrackListState = remember { mutableStateListOf<Track>() }
        val filteredArtistTrackListState = remember { mutableStateListOf<Track>() }

        // Current Playlist
        val currentPlaylistTracksState = mutableStateListOf<Track>()
        val currentPlaylistFilteredTracksState = mutableStateListOf<Track>()

        val favouriteFragmentState = mutableStateOf(FavouriteFragments.TRACKS)

        // Favourite Tracks
        val favouriteTracksState = mutableStateListOf<Track>()
        val filteredFavouriteTracksState = mutableStateListOf<Track>()

        // Favourite Artists
        val favouriteArtistsState = remember { mutableStateListOf<Artist>() }
        val filteredFavouriteArtistsState = remember { mutableStateListOf<Artist>() }

        val rootScreen = RootScreen(DefaultComponentContext(LifecycleRegistry())).apply { start() }

        val favouritesScreen = FavouritesScreen(
            DefaultComponentContext(LifecycleRegistry()),
            favouriteFragmentState
        ).apply { start() }

        Surface(color = secondary, modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                Scaffold(
                    topBar = {
                        when (rootScreen.currentConfigState.value) {
                            Config.MainMenuConfig.Tracks -> TracksAppBar(allTracksState, filteredAllTracksState)

                            Config.MainMenuConfig.Artists -> ArtistsAppBar(allArtistsState, filteredAllArtistsState)

                            Config.PlaybarConfig.CurrentPlaylist -> CurrentPlaylistAppBar(
                                currentPlaylistTracksState,
                                currentPlaylistFilteredTracksState
                            )

                            Config.MainMenuConfig.Favourites -> FavouritesAppBar(
                                favouriteFragmentState,
                                favouriteTracksState,
                                filteredFavouriteTracksState,
                                favouriteArtistsState,
                                filteredFavouriteArtistsState
                            )

                            Config.ArtistTracks -> ArtistTracksAppBar(
                                curArtist.value!!,
                                artistTrackListState,
                                filteredArtistTrackListState
                            )

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
                            else -> Unit // View Pager's fragments
                        }
                    },
                    bottomBar = {
                        PlayingBar(
                            rootScreen,
                            allTracksState,
                            currentTrackState,
                            isPlayingCoverLoadedState,
                            playbackPositionState,
                            isPlayingState,
                            isPlaybackTrackDraggingState,
                            loopingState,
                            speedState,
                            volumeState,
                            isLikedState
                        )
                    }
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        NavigationMenu(rootScreen)

                        Spacer(Modifier.fillMaxHeight().width(3.dp).background(Params.primaryColor))

                        RootView(
                            rootScreen,
                            favouritesScreen,
                            currentTrackState,
                            isPlayingState,
                            isPlayingCoverLoadedState,
                            playbackPositionState,
                            loopingState,
                            allTracksState,
                            filteredAllTracksState,
                            currentPlaylistTracksState,
                            currentPlaylistFilteredTracksState,
                            allArtistsState,
                            filteredAllArtistsState,
                            isPlaybackTrackDraggingState,
                            speedState,
                            isLikedState,
                            curArtist
                        )
                    }
                }
            }
        }
    }
}

private var playbackControlTasks: Job? = null

suspend fun startPlaybackControlTasks(
    currentTrackState: MutableState<Track?>,
    isPlayingState: MutableState<Boolean>,
    isPlayingCoverLoadedState: MutableState<Boolean>,
    playbackPositionState: MutableState<Float>,
    loopingState: MutableState<Int>,
    allTracksState: SnapshotStateList<Track>,
    isPlaybackTrackDraggingState: State<Boolean>,
    speedState: State<Float>
): Unit = coroutineScope {
    playbackControlTasks = launch(Dispatchers.Default) {
        runCalculationOfSliderPos(
            isPlaybackTrackDraggingState,
            playbackPositionState,
        )

        if (RustLibs.getPlaybackPosition() == currentTrackState.value?.duration)
            onPlaybackCompletition(
                currentTrackState,
                isPlayingState,
                isPlayingCoverLoadedState,
                playbackPositionState,
                loopingState,
                allTracksState,
                isPlaybackTrackDraggingState,
                speedState
            )
    }
}

fun cancelPlaybackControlTasks() {
    playbackControlTasks?.cancel()
    playbackControlTasks = null
}

private suspend fun runCalculationOfSliderPos(
    isPlaybackTrackDraggingState: State<Boolean>,
    playbackPositionState: MutableState<Float>,
) {
    while (RustLibs.isPlaying() && !isPlaybackTrackDraggingState.value) {
        val duration = RustLibs.getPlaybackPosition().toFloat()
        playbackPositionState.value = duration
        delay(50)
    }
}

suspend fun switchToNextTrack(
    currentTrackState: MutableState<Track?>,
    isPlayingState: MutableState<Boolean>,
    isPlayingCoverLoadedState: MutableState<Boolean>,
    playbackPositionState: MutableState<Float>,
    loopingState: MutableState<Int>,
    allTracksState: SnapshotStateList<Track>,
    isPlaybackTrackDraggingState: State<Boolean>,
    speedState: State<Float>
) = coroutineScope {
    launch(Dispatchers.IO) {
        RustLibs.onNextTrackClickedBlocking()
        currentTrackState.value = RustLibs.getCurTrackBlocking()
        isPlayingState.value = true
        isPlayingCoverLoadedState.value = false
        playbackPositionState.value = 0F

        startPlaybackControlTasks(
            currentTrackState,
            isPlayingState,
            isPlayingCoverLoadedState,
            playbackPositionState,
            loopingState,
            allTracksState,
            isPlaybackTrackDraggingState,
            speedState
        )
    }
}

private suspend fun replayCurrentTrack(
    currentTrackState: MutableState<Track?>,
    isPlayingState: MutableState<Boolean>,
    isPlayingCoverLoadedState: MutableState<Boolean>,
    playbackPositionState: MutableState<Float>,
    loopingState: MutableState<Int>,
    allTracksState: SnapshotStateList<Track>,
    isPlaybackTrackDraggingState: State<Boolean>,
    speedState: State<Float>
) = coroutineScope {
    launch(Dispatchers.IO) {
        RustLibs.replayCurTrackBlocking()

        startPlaybackControlTasks(
            currentTrackState,
            isPlayingState,
            isPlayingCoverLoadedState,
            playbackPositionState,
            loopingState,
            allTracksState,
            isPlaybackTrackDraggingState,
            speedState
        )
    }
}

suspend fun onPlaybackCompletition(
    currentTrackState: MutableState<Track?>,
    isPlayingState: MutableState<Boolean>,
    isPlayingCoverLoadedState: MutableState<Boolean>,
    playbackPositionState: MutableState<Float>,
    loopingState: MutableState<Int>,
    allTracksState: SnapshotStateList<Track>,
    isPlaybackTrackDraggingState: State<Boolean>,
    speedState: State<Float>
) = coroutineScope {
    when {
        isPlaybackTrackDraggingState.value -> Unit

        else -> when (loopingState.value) {
            // Playlist looping
            0 -> switchToNextTrack(
                currentTrackState,
                isPlayingState,
                isPlayingCoverLoadedState,
                playbackPositionState,
                loopingState,
                allTracksState,
                isPlaybackTrackDraggingState,
                speedState
            )

            // Track looping
            1 -> replayCurrentTrack(
                currentTrackState,
                isPlayingState,
                isPlayingCoverLoadedState,
                playbackPositionState,
                loopingState,
                allTracksState,
                isPlaybackTrackDraggingState,
                speedState
            )

            // No looping
            else -> launch(Dispatchers.IO) {
                when {
                    RustLibs.getCurTrackIndexBlocking() == allTracksState.size - 1 -> {
                        isPlayingState.value = false

                        launch(Dispatchers.IO) {
                            playbackPositionState.value =
                                RustLibs.getCurTrackBlocking()?.duration?.toFloat() ?: 0F
                        }
                    }

                    else -> switchToNextTrack(
                        currentTrackState,
                        isPlayingState,
                        isPlayingCoverLoadedState,
                        playbackPositionState,
                        loopingState,
                        allTracksState,
                        isPlaybackTrackDraggingState,
                        speedState
                    )
                }
            }
        }
    }
}