package com.dinaraparanid.prima.ui.fragments.main_menu_fragments.tracks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.dinaraparanid.prima.entities.Track
import com.dinaraparanid.prima.ui.tracks.TrackList
import kotlinx.coroutines.launch

@Composable
fun TracksFragment(
    currentTrackState: MutableState<Track?>,
    isPlayingState: MutableState<Boolean>,
    isPlayingCoverLoadedState: MutableState<Boolean>,
    playbackPositionState: MutableState<Float>,
    loopingState: MutableState<Int>,
    tracksState: SnapshotStateList<Track>,
    filteredTracksState: SnapshotStateList<Track>,
    isPlaybackTrackDraggingState: State<Boolean>,
    speedState: State<Float>,
) {
    val coroutineScope = rememberCoroutineScope()
    coroutineScope.launch { scanTracks(tracksState, filteredTracksState) }

    TrackList(
        tracksState,
        filteredTracksState,
        currentTrackState,
        isPlayingState,
        isPlayingCoverLoadedState,
        playbackPositionState,
        loopingState,
        isPlaybackTrackDraggingState,
        speedState,
    ) { allTracksState, filteredTracksState, listState -> TracksBar(allTracksState, filteredTracksState, listState) }
}