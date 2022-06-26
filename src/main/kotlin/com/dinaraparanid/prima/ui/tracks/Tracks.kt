package com.dinaraparanid.prima.ui.tracks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dinaraparanid.prima.entities.Track
import com.dinaraparanid.prima.rust.RustLibs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

@Composable
fun Tracks(
    currentTrackState: MutableState<Track?>,
    isPlayingState: MutableState<Boolean>,
    isPlayingCoverLoadedState: MutableState<Boolean>,
    playbackPositionState: MutableState<Float>,
    loopingState: MutableState<Int>,
    tracksState: SnapshotStateList<Track>,
    filteredTracksState: SnapshotStateList<Track>,
    isPlaybackTrackDraggingState: State<Boolean>,
    speedState: State<Float>
) {
    val coroutineScope = rememberCoroutineScope()
    val tracksTask = coroutineScope.async(Dispatchers.IO) {
        RustLibs.getAllTracksAsync()
    }

    coroutineScope.launch {
        tracksState.run {
            clear()
            addAll(tracksTask.await())
        }

        filteredTracksState.run {
            clear()
            addAll(tracksState)
        }
    }

    val listState = rememberLazyListState()

    Column(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
        TracksBar(filteredTracksState, listState)

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
                    speedState
                )
            }
        }
    }
}