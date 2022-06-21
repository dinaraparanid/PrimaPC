package com.dinaraparanid.prima.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.dinaraparanid.prima.entities.Track
import com.dinaraparanid.prima.rust.RustLibs
import com.dinaraparanid.prima.ui.tracks.Tracks
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
        val isPlayingState = remember { mutableStateOf(false) }
        val tracksState = remember { mutableStateListOf<Track>() }
        val currentTrackState = remember { mutableStateOf(RustLibs.getCurTrack()) }
        val isPlayingCoverLoadedState = remember { mutableStateOf(false) }
        val isPlaybackTrackDraggingState = remember { mutableStateOf(false) }
        val playbackPositionState = remember { mutableStateOf(0F) } // TODO: load position
        val loopingState = remember { mutableStateOf(0) }             // TODO: Load looping status

        Surface(color = secondary, modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                Scaffold(
                    topBar = { AppBar() },
                    bottomBar = {
                        PlayingBar(
                            tracksState,
                            currentTrackState,
                            isPlayingCoverLoadedState,
                            playbackPositionState,
                            isPlayingState,
                            isPlaybackTrackDraggingState,
                            loopingState
                        )
                    }
                ) {
                    // TODO: Load first screen

                    Tracks(
                        currentTrackState,
                        isPlayingState,
                        isPlayingCoverLoadedState,
                        playbackPositionState,
                        loopingState,
                        tracksState,
                        isPlaybackTrackDraggingState
                    )
                }
            }
        }
    }
}

private var playbackControlTasks: Job? = null

fun CoroutineScope.startPlaybackControlTasks(
    currentTrackState: MutableState<Track?>,
    isPlayingState: MutableState<Boolean>,
    isPlayingCoverLoadedState: MutableState<Boolean>,
    playbackPositionState: MutableState<Float>,
    loopingState: MutableState<Int>,
    tracksState: SnapshotStateList<Track>,
    isPlaybackTrackDraggingState: State<Boolean>
) {
    playbackControlTasks = launch(Dispatchers.Default) {
        runCalculationOfSliderPos(
            isPlaybackTrackDraggingState,
            playbackPositionState,
        )

        onPlaybackCompletition(
            currentTrackState,
            isPlayingState,
            isPlayingCoverLoadedState,
            playbackPositionState,
            loopingState,
            tracksState,
            isPlaybackTrackDraggingState
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

fun CoroutineScope.switchToNextTrack(
    currentTrackState: MutableState<Track?>,
    isPlayingState: MutableState<Boolean>,
    isPlayingCoverLoadedState: MutableState<Boolean>,
    playbackPositionState: MutableState<Float>,
    loopingState: MutableState<Int>,
    tracksState: SnapshotStateList<Track>,
    isPlaybackTrackDraggingState: State<Boolean>
) {
    RustLibs.onNextTrackClickedAsync()
    currentTrackState.value = RustLibs.getCurTrack()
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
        isPlaybackTrackDraggingState
    )
}

private fun CoroutineScope.replayCurrentTrack(
    currentTrackState: MutableState<Track?>,
    isPlayingState: MutableState<Boolean>,
    isPlayingCoverLoadedState: MutableState<Boolean>,
    playbackPositionState: MutableState<Float>,
    loopingState: MutableState<Int>,
    tracksState: SnapshotStateList<Track>,
    isPlaybackTrackDraggingState: State<Boolean>
) {
    RustLibs.replayCurrentTrackAsync()

    startPlaybackControlTasks(
        currentTrackState,
        isPlayingState,
        isPlayingCoverLoadedState,
        playbackPositionState,
        loopingState,
        tracksState,
        isPlaybackTrackDraggingState
    )
}

fun CoroutineScope.onPlaybackCompletition(
    currentTrackState: MutableState<Track?>,
    isPlayingState: MutableState<Boolean>,
    isPlayingCoverLoadedState: MutableState<Boolean>,
    playbackPositionState: MutableState<Float>,
    loopingState: MutableState<Int>,
    tracksState: SnapshotStateList<Track>,
    isPlaybackTrackDraggingState: State<Boolean>
) = when (loopingState.value) {
    // Playlist looping
    0 -> switchToNextTrack(
        currentTrackState,
        isPlayingState,
        isPlayingCoverLoadedState,
        playbackPositionState,
        loopingState,
        tracksState,
        isPlaybackTrackDraggingState
    )

    // Track looping
    1 -> replayCurrentTrack(
        currentTrackState,
        isPlayingState,
        isPlayingCoverLoadedState,
        playbackPositionState,
        loopingState,
        tracksState,
        isPlaybackTrackDraggingState
    )

    // No looping
    else -> when {
        RustLibs.getCurTrackIndex() == tracksState.size - 1 -> {
            isPlayingState.value = false
            playbackPositionState.value = RustLibs.getCurTrack()?.duration?.toFloat() ?: 0F
        }

        else -> switchToNextTrack(
            currentTrackState,
            isPlayingState,
            isPlayingCoverLoadedState,
            playbackPositionState,
            loopingState,
            tracksState,
            isPlaybackTrackDraggingState
        )
    }
}