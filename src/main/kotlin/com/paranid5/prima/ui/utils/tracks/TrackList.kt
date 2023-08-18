package com.dinaraparanid.prima.ui.utils.tracks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dinaraparanid.prima.entities.Track

@Composable
inline fun TrackList(
    tracksState: SnapshotStateList<Track>,
    filteredTracksState: SnapshotStateList<Track>,
    currentTrackState: MutableState<Track?>,
    isPlayingState: MutableState<Boolean>,
    isPlayingCoverLoadedState: MutableState<Boolean>,
    playbackPositionState: MutableState<Float>,
    loopingState: MutableState<Int>,
    isPlaybackTrackDraggingState: State<Boolean>,
    speedState: State<Float>,
    isLikedState: MutableState<Boolean>,
    trackBarCaller: @Composable (SnapshotStateList<Track>, SnapshotStateList<Track>, LazyListState) -> Unit
) {
    val listState = rememberLazyListState()

    Column(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
        trackBarCaller(tracksState, filteredTracksState, listState)

        LazyColumn(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            contentPadding = PaddingValues(top = 20.dp, bottom = 180.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp),
            state = listState
        ) {
            itemsIndexed(filteredTracksState, key = { _, track -> track }) { ind, _ ->
                TrackItem(
                    filteredTracksState,
                    ind,
                    currentTrackState,
                    isPlayingState,
                    isPlayingCoverLoadedState,
                    playbackPositionState,
                    loopingState,
                    tracksState,
                    isPlaybackTrackDraggingState,
                    speedState,
                    isLikedState
                )
            }
        }
    }
}