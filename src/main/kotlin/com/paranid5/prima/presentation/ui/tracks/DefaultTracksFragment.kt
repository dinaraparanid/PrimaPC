package com.paranid5.prima.presentation.ui.tracks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.paranid5.prima.data.Track
import com.paranid5.prima.presentation.fragments.main_menu_fragments.tracks.TracksBar

@Composable
fun DefaultTracksFragment(
    currentTrackState: MutableState<Track?>,
    isPlayingState: MutableState<Boolean>,
    isPlayingCoverLoadedState: MutableState<Boolean>,
    playbackPositionState: MutableState<Float>,
    loopingState: MutableState<Int>,
    tracksState: SnapshotStateList<Track>,
    filteredTracksState: SnapshotStateList<Track>,
    isPlaybackTrackDraggingState: State<Boolean>,
    speedState: State<Float>,
    isLikedState: MutableState<Boolean>
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