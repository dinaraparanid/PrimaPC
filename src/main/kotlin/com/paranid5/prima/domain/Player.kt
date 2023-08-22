package com.paranid5.prima.domain

import com.paranid5.prima.data.Track
import com.paranid5.prima.rust.RustLibs
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

private var playbackControlTasksState = MutableStateFlow<Job?>(null)

suspend fun startPlaybackControlTasks(
    selectedTrackState: MutableStateFlow<Track?>,
    isPlayingState: MutableStateFlow<Boolean>,
    isPlayingCoverLoadedState: MutableStateFlow<Boolean>,
    playbackPositionState: MutableStateFlow<Float>,
    loopingState: MutableStateFlow<Int>,
    currentPlaylistTracksState: MutableStateFlow<List<Track>>,
    isPlaybackTrackDraggingState: StateFlow<Boolean>,
    speedState: StateFlow<Float>
): Unit = coroutineScope {
    playbackControlTasksState.update {
        launch(Dispatchers.Default) {
            runCalculationOfSliderPos(
                isPlaybackTrackDraggingState = isPlaybackTrackDraggingState,
                playbackPositionState = playbackPositionState,
            )

            val dif = RustLibs.getPlaybackPositionBlocking() - (selectedTrackState.value?.duration ?: 0)

            if (dif <= 50)
                onPlaybackCompletion(
                    selectedTrackState = selectedTrackState,
                    isPlayingState = isPlayingState,
                    isPlayingCoverLoadedState = isPlayingCoverLoadedState,
                    playbackPositionState = playbackPositionState,
                    loopingState = loopingState,
                    currentPlaylistTracksState = currentPlaylistTracksState,
                    isPlaybackTrackDraggingState = isPlaybackTrackDraggingState,
                    speedState = speedState
                )
        }
    }
}

fun cancelPlaybackControlTasks() =
    playbackControlTasksState.update {
        it?.cancel()
        println("Slider pos calc canceled")
        null
    }

private suspend inline fun runCalculationOfSliderPos(
    isPlaybackTrackDraggingState: StateFlow<Boolean>,
    playbackPositionState: MutableStateFlow<Float>,
) {
    while (!RustLibs.isPlaying())
        delay(100)

    while (RustLibs.isPlaying() && !isPlaybackTrackDraggingState.value) {
        val duration = RustLibs.getPlaybackPositionBlocking().toFloat()
        playbackPositionState.update { duration }
        delay(50)
    }
}

private suspend fun switchToTrack(
    selectedTrackState: MutableStateFlow<Track?>,
    isPlayingState: MutableStateFlow<Boolean>,
    isPlayingCoverLoadedState: MutableStateFlow<Boolean>,
    playbackPositionState: MutableStateFlow<Float>,
    loopingState: MutableStateFlow<Int>,
    currentPlaylistTracksState: MutableStateFlow<List<Track>>,
    isPlaybackTrackDraggingState: StateFlow<Boolean>,
    speedState: StateFlow<Float>,
    onSwitch: suspend () -> Unit
) = coroutineScope {
    launch(Dispatchers.IO) { onSwitch() }

    launch(Dispatchers.IO) {
        delay(500) // wait for Rust player to switch to next track
        selectedTrackState.update { RustLibs.getCurTrackBlocking() }
        isPlayingState.update { true }
        isPlayingCoverLoadedState.update { false }
        playbackPositionState.update { 0F }
    }

    launch {
        startPlaybackControlTasks(
            selectedTrackState = selectedTrackState,
            isPlayingState = isPlayingState,
            isPlayingCoverLoadedState = isPlayingCoverLoadedState,
            playbackPositionState = playbackPositionState,
            loopingState = loopingState,
            currentPlaylistTracksState = currentPlaylistTracksState,
            isPlaybackTrackDraggingState = isPlaybackTrackDraggingState,
            speedState = speedState
        )
    }
}

internal suspend inline fun switchToPrevTrack(
    selectedTrackState: MutableStateFlow<Track?>,
    isPlayingState: MutableStateFlow<Boolean>,
    isPlayingCoverLoadedState: MutableStateFlow<Boolean>,
    playbackPositionState: MutableStateFlow<Float>,
    loopingState: MutableStateFlow<Int>,
    currentPlaylistTracksState: MutableStateFlow<List<Track>>,
    isPlaybackTrackDraggingState: StateFlow<Boolean>,
    speedState: StateFlow<Float>
) = switchToTrack(
    selectedTrackState = selectedTrackState,
    isPlayingState = isPlayingState,
    isPlayingCoverLoadedState = isPlayingCoverLoadedState,
    playbackPositionState = playbackPositionState,
    loopingState = loopingState,
    currentPlaylistTracksState = currentPlaylistTracksState,
    isPlaybackTrackDraggingState = isPlaybackTrackDraggingState,
    speedState = speedState
) { RustLibs.onPreviousTrackClickedBlocking() }

internal suspend inline fun switchToNextTrack(
    selectedTrackState: MutableStateFlow<Track?>,
    isPlayingState: MutableStateFlow<Boolean>,
    isPlayingCoverLoadedState: MutableStateFlow<Boolean>,
    playbackPositionState: MutableStateFlow<Float>,
    loopingState: MutableStateFlow<Int>,
    currentPlaylistTracksState: MutableStateFlow<List<Track>>,
    isPlaybackTrackDraggingState: StateFlow<Boolean>,
    speedState: StateFlow<Float>
) = switchToTrack(
    selectedTrackState = selectedTrackState,
    isPlayingState = isPlayingState,
    isPlayingCoverLoadedState = isPlayingCoverLoadedState,
    playbackPositionState = playbackPositionState,
    loopingState = loopingState,
    currentPlaylistTracksState = currentPlaylistTracksState,
    isPlaybackTrackDraggingState = isPlaybackTrackDraggingState,
    speedState = speedState
) { RustLibs.onNextTrackClickedBlocking() }

private suspend inline fun onPlaybackCompletion(
    selectedTrackState: MutableStateFlow<Track?>,
    isPlayingState: MutableStateFlow<Boolean>,
    isPlayingCoverLoadedState: MutableStateFlow<Boolean>,
    playbackPositionState: MutableStateFlow<Float>,
    loopingState: MutableStateFlow<Int>,
    currentPlaylistTracksState: MutableStateFlow<List<Track>>,
    isPlaybackTrackDraggingState: StateFlow<Boolean>,
    speedState: StateFlow<Float>
) = coroutineScope {
    when {
        isPlaybackTrackDraggingState.value -> Unit

        else -> when (loopingState.value) {
            // Playlist looping
            0 -> switchToNextTrack(
                selectedTrackState = selectedTrackState,
                isPlayingState = isPlayingState,
                isPlayingCoverLoadedState = isPlayingCoverLoadedState,
                playbackPositionState = playbackPositionState,
                loopingState = loopingState,
                currentPlaylistTracksState = currentPlaylistTracksState,
                isPlaybackTrackDraggingState = isPlaybackTrackDraggingState,
                speedState = speedState
            )

            // Track looping
            1 -> replayCurrentTrack(
                selectedTrackState = selectedTrackState,
                isPlayingState = isPlayingState,
                isPlayingCoverLoadedState = isPlayingCoverLoadedState,
                playbackPositionState = playbackPositionState,
                loopingState = loopingState,
                currentPlaylistTracksState = currentPlaylistTracksState,
                isPlaybackTrackDraggingState = isPlaybackTrackDraggingState,
                speedState = speedState
            )

            // No looping
            else -> launch(Dispatchers.IO) {
                when {
                    RustLibs.getCurTrackIndexBlocking() == currentPlaylistTracksState.value.size - 1 -> {
                        isPlayingState.update { false }

                        playbackPositionState.update {
                            withContext(Dispatchers.IO) {
                                RustLibs.getCurTrackBlocking()?.duration?.toFloat() ?: 0F
                            }
                        }
                    }

                    else -> switchToNextTrack(
                        selectedTrackState = selectedTrackState,
                        isPlayingState = isPlayingState,
                        isPlayingCoverLoadedState = isPlayingCoverLoadedState,
                        playbackPositionState = playbackPositionState,
                        loopingState = loopingState,
                        currentPlaylistTracksState = currentPlaylistTracksState,
                        isPlaybackTrackDraggingState = isPlaybackTrackDraggingState,
                        speedState = speedState
                    )
                }
            }
        }
    }
}

private suspend inline fun replayCurrentTrack(
    selectedTrackState: MutableStateFlow<Track?>,
    isPlayingState: MutableStateFlow<Boolean>,
    isPlayingCoverLoadedState: MutableStateFlow<Boolean>,
    playbackPositionState: MutableStateFlow<Float>,
    loopingState: MutableStateFlow<Int>,
    currentPlaylistTracksState: MutableStateFlow<List<Track>>,
    isPlaybackTrackDraggingState: StateFlow<Boolean>,
    speedState: StateFlow<Float>
) = coroutineScope {
    launch(Dispatchers.IO) {
        RustLibs.replayCurTrackBlocking()
    }

    launch {
        startPlaybackControlTasks(
            selectedTrackState = selectedTrackState,
            isPlayingState = isPlayingState,
            isPlayingCoverLoadedState = isPlayingCoverLoadedState,
            playbackPositionState = playbackPositionState,
            loopingState = loopingState,
            currentPlaylistTracksState = currentPlaylistTracksState,
            isPlaybackTrackDraggingState = isPlaybackTrackDraggingState,
            speedState = speedState
        )
    }
}