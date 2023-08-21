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

            if (RustLibs.getPlaybackPositionBlocking() == selectedTrackState.value?.duration)
                onPlaybackCompletition(
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
        null
    }

private suspend inline fun runCalculationOfSliderPos(
    isPlaybackTrackDraggingState: StateFlow<Boolean>,
    playbackPositionState: MutableStateFlow<Float>,
) {
    println("Start slider pos calc")

    while (RustLibs.isPlaying() && !isPlaybackTrackDraggingState.value) {
        val duration = RustLibs.getPlaybackPositionBlocking().toFloat()
        playbackPositionState.update { duration }
        delay(50)
    }

    println("Stop slider pos calc")
}

suspend fun switchToNextTrack(
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
        RustLibs.onNextTrackClickedBlocking()

        selectedTrackState.update { RustLibs.getCurTrackBlocking() }
        isPlayingState.update { true }
        isPlayingCoverLoadedState.update { false }
        playbackPositionState.update { 0F }

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

private suspend fun replayCurrentTrack(
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

suspend fun onPlaybackCompletition(
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