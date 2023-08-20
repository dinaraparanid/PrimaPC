package com.paranid5.prima.presentation.ui.playing_bar

import androidx.compose.foundation.layout.*
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paranid5.prima.data.Track
import com.paranid5.prima.di.*
import com.paranid5.prima.domain.StorageHandler
import com.paranid5.prima.domain.cancelPlaybackControlTasks
import com.paranid5.prima.domain.extensions.timeString
import com.paranid5.prima.domain.extensions.triple
import com.paranid5.prima.domain.startPlaybackControlTasks
import com.paranid5.prima.rust.RustLibs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
fun PlaybackTrack(modifier: Modifier = Modifier) = Column(modifier) {
    PlaybackTrackSlider()
    Spacer(Modifier.height(10.dp))
    DurationLabels()
}

@Composable
private fun PlaybackTrackSlider(
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject(),
    selectedTrackState: MutableStateFlow<Track?> = koinInject(named(KOIN_SELECTED_TRACK)),
    playbackPositionState: MutableStateFlow<Float> = koinInject(named(KOIN_PLAYBACK_POS)),
    isPlayingState: MutableStateFlow<Boolean> = koinInject(named(KOIN_IS_PLAYING)),
    isPlayingCoverLoadedState: MutableStateFlow<Boolean> = koinInject(named(KOIN_IS_PLAYING_COVER_LOADED)),
    isPlaybackTrackDraggingState: MutableStateFlow<Boolean> = koinInject(named(KOIN_IS_PLAYBACK_TRACK_DRAGGING)),
    loopingState: MutableStateFlow<Int> = koinInject(named(KOIN_LOOPING)),
    currentPlaylistTracksState: MutableStateFlow<List<Track>> = koinInject(named(KOIN_CURRENT_PLAYLIST_TRACKS)),
    speedState: MutableStateFlow<Float> = koinInject(named(KOIN_SPEED))
) {
    val coroutineScope = rememberCoroutineScope()
    val currentTrack by selectedTrackState.collectAsState()
    val playbackPos by playbackPositionState.collectAsState()

    val secondaryColor by storageHandler.secondaryColorState.collectAsState()
    val secondaryAlternativeColor by storageHandler.secondaryAlternativeColorState.collectAsState()

    val endPoint by remember {
        derivedStateOf {
            currentTrack
                ?.duration
                ?.toFloat()
                ?.coerceAtLeast(1F)
                ?: 1F
        }
    }

    Slider(
        valueRange = (0F..endPoint),
        value = playbackPos,
        colors = SliderDefaults.colors(
            thumbColor = secondaryAlternativeColor,
            activeTrackColor = secondaryAlternativeColor,
            inactiveTrackColor = secondaryColor
        ),
        modifier = modifier.fillMaxWidth().height(20.dp),
        onValueChange = { duration ->
            isPlaybackTrackDraggingState.update { true }
            playbackPositionState.update { duration }
            cancelPlaybackControlTasks()
        },
        onValueChangeFinished = {
            isPlayingState.update { true }
            isPlaybackTrackDraggingState.update { false }

            coroutineScope.launch(Dispatchers.IO) {
                RustLibs.seekToBlocking(playbackPos.toLong())
            }

            coroutineScope.launch {
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
    )
}

@Composable
private fun DurationLabels(
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject(),
    selectedTrackState: MutableStateFlow<Track?> = koinInject(named(KOIN_SELECTED_TRACK)),
    playbackPositionState: MutableStateFlow<Float> = koinInject(named(KOIN_PLAYBACK_POS))
) {
    val currentTrack by selectedTrackState.collectAsState()
    val playbackPos by playbackPositionState.collectAsState()
    val secondaryAlternativeColor by storageHandler.secondaryAlternativeColorState.collectAsState()

    Row(modifier.fillMaxWidth()) {
        val currentPosText = RustLibs
            .calcTrackTime(playbackPos.toInt())
            .triple
            .timeString

        val totalDurationText = RustLibs
            .calcTrackTime(currentTrack?.duration?.toInt() ?: 0)
            .triple
            .timeString

        Text(
            text = currentPosText,
            color = secondaryAlternativeColor,
            fontSize = 14.sp
        )

        Spacer(Modifier.weight(1F))

        Text(
            text = totalDurationText,
            color = secondaryAlternativeColor,
            fontSize = 14.sp
        )
    }
}