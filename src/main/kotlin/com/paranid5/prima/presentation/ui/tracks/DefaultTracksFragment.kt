package com.paranid5.prima.presentation.ui.tracks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import com.paranid5.prima.data.Track
import com.paranid5.prima.presentation.screens.main_menu_fragments.tracks.TracksBar

@Composable
fun DefaultTracksFragment(
    currentTrackState: MutableState<Track?>,
    isPlayingState: MutableState<Boolean>,
    isPlayingCoverLoadedState: MutableState<Boolean>,
    playbackPositionState: MutableState<Float>,
    loopingState: MutableState<Int>,
    tracksState: MutableState<List<Track>>,
    filteredTracksState: MutableState<List<Track>>,
    isPlaybackTrackDraggingState: State<Boolean>,
    speedState: State<Float>,
    isLikedState: MutableState<Boolean>,
    modifier: Modifier = Modifier
) = TrackList(
    tracksState,
    filteredTracksState,
    currentTrackState,
    isPlayingState,
    isPlayingCoverLoadedState,
    playbackPositionState,
    loopingState,
    isPlaybackTrackDraggingState,
    speedState,
    isLikedState
) { allTracksState, filteredTracksState, listState ->
    TracksBar(allTracksState, filteredTracksState, listState)
}