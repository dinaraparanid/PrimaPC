package com.dinaraparanid.prima.ui.tracks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dinaraparanid.prima.entities.Track
import com.dinaraparanid.prima.rust.RustLibs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

@Composable
fun Tracks(currentTrackState: MutableState<Track?>, isPlayingCoverLoadedState: MutableState<Boolean>) {
    val coroutineScope = rememberCoroutineScope()
    val tracks = remember { mutableStateListOf<Track>() }
    val tracksTask = coroutineScope.async(Dispatchers.IO) {
        RustLibs.getAllTracksAsync()
    }

    coroutineScope.launch {
        tracks.clear()
        tracks.addAll(tracksTask.await())
    }

    val listState = rememberLazyListState()

    Column(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
        TracksBar(tracks, listState)

        LazyColumn(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            contentPadding = PaddingValues(top = 20.dp, bottom = 180.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp),
            state = listState
        ) {
            items(tracks, key = { it }) {
                TrackItem(
                    it,
                    currentTrackState,
                    isPlayingCoverLoadedState
                )
            }
        }
    }
}