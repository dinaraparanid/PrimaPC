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
    tracksState: SnapshotStateList<Track>,
    currentTrackState: MutableState<Track?>,
    isPlayingCoverLoadedState: MutableState<Boolean>,
    playbackPositionState: MutableState<Float>,
    isPlayingState: MutableState<Boolean>,
) {
    val coroutineScope = rememberCoroutineScope()
    val tracksTask = coroutineScope.async(Dispatchers.IO) {
        RustLibs.getAllTracksAsync()
    }

    coroutineScope.launch {
        tracksState.clear()
        tracksState.addAll(tracksTask.await())
    }

    val listState = rememberLazyListState()

    Column(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
        TracksBar(tracksState, listState)

        LazyColumn(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            contentPadding = PaddingValues(top = 20.dp, bottom = 180.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp),
            state = listState
        ) {
            itemsIndexed(tracksState, key = { _, track -> track }) { ind, _ ->
                TrackItem(
                    tracksState,
                    ind,
                    currentTrackState,
                    isPlayingCoverLoadedState,
                    playbackPositionState,
                    isPlayingState
                )
            }
        }
    }
}