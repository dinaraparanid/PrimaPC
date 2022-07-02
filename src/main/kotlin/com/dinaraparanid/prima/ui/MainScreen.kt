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
import com.dinaraparanid.prima.entities.Track
import com.dinaraparanid.prima.rust.RustLibs
import com.dinaraparanid.prima.ui.tracks.appbar.TracksAppBar
import com.dinaraparanid.prima.ui.utils.navigation.RootScreen
import com.dinaraparanid.prima.ui.utils.navigation.RootView
import com.dinaraparanid.prima.utils.Params
import kotlinx.coroutines.*

@Composable
@Preview
fun MainScreen() {
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

        coroutineScope.launch {
            currentTrackState.value = withContext(Dispatchers.IO) {
                RustLibs.getCurTrackBlocking()
            }
        }

        val isPlayingState = remember { mutableStateOf(false) }
        val isPlayingCoverLoadedState = remember { mutableStateOf(false) }
        val isPlaybackTrackDraggingState = remember { mutableStateOf(false) }
        val playbackPositionState = remember { mutableStateOf(RustLibs.getPlaybackPosition().toFloat()) }
        val loopingState = remember { mutableStateOf(RustLibs.getLoopingState()) }
        val speedState = remember { mutableStateOf(RustLibs.getSpeed()) }
        val volumeState = remember { mutableStateOf(RustLibs.getVolume()) }

        val tracksState = remember { mutableStateListOf<Track>() }
        val filteredTracksState = remember { mutableStateListOf<Track>() }

        val rootScreen = RootScreen(DefaultComponentContext(LifecycleRegistry())).apply { start() }

        Surface(color = secondary, modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                Scaffold(
                    topBar = {
                        // TODO: other app bars
                        when (rootScreen.currentConfigState.value) {
                            RootScreen.Config.Tracks -> TracksAppBar(tracksState, filteredTracksState)
                            RootScreen.Config.TrackCollections -> Unit
                            RootScreen.Config.Artists -> Unit
                            RootScreen.Config.Favourites -> Unit
                            RootScreen.Config.MP3Converter -> Unit
                            RootScreen.Config.GTM -> Unit
                            RootScreen.Config.Statistics -> Unit
                            RootScreen.Config.Settings -> Unit
                            RootScreen.Config.AboutApp -> Unit
                        }
                    },
                    bottomBar = {
                        PlayingBar(
                            tracksState,
                            currentTrackState,
                            isPlayingCoverLoadedState,
                            playbackPositionState,
                            isPlayingState,
                            isPlaybackTrackDraggingState,
                            loopingState,
                            speedState,
                            volumeState
                        )
                    }
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        NavigationMenu(rootScreen)

                        Spacer(Modifier.fillMaxHeight().width(3.dp).background(Params.primaryColor))

                        RootView(
                            rootScreen,
                            currentTrackState,
                            isPlayingState,
                            isPlayingCoverLoadedState,
                            playbackPositionState,
                            loopingState,
                            tracksState,
                            filteredTracksState,
                            isPlaybackTrackDraggingState,
                            speedState
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
    tracksState: SnapshotStateList<Track>,
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
                tracksState,
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
    tracksState: SnapshotStateList<Track>,
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
            tracksState,
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
    tracksState: SnapshotStateList<Track>,
    isPlaybackTrackDraggingState: State<Boolean>,
    speedState: State<Float>
) = coroutineScope {
    launch(Dispatchers.IO) {
        RustLibs.replayCurrentTrackBlocking()

        startPlaybackControlTasks(
            currentTrackState,
            isPlayingState,
            isPlayingCoverLoadedState,
            playbackPositionState,
            loopingState,
            tracksState,
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
    tracksState: SnapshotStateList<Track>,
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
                tracksState,
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
                tracksState,
                isPlaybackTrackDraggingState,
                speedState
            )

            // No looping
            else -> launch(Dispatchers.IO) {
                when {
                    RustLibs.getCurTrackIndexBlocking() == tracksState.size - 1 -> {
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
                        tracksState,
                        isPlaybackTrackDraggingState,
                        speedState
                    )
                }
            }
        }
    }
}