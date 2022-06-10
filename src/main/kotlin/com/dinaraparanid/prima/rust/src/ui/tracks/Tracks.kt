package com.dinaraparanid.prima.rust.src.ui.tracks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dinaraparanid.prima.entities.Track
import com.dinaraparanid.prima.rust.RustLibs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

@Composable
fun Tracks() {
    val coroutineScope = rememberCoroutineScope()
    val tracks = remember { mutableStateListOf<Track>() }
    val tracksTask = coroutineScope.async(Dispatchers.IO) {
        RustLibs.getAllTracksAsync()
    }

    coroutineScope.launch {
        tracks.clear()
        tracks.addAll(tracksTask.await())
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        items(tracks) {
            println(it.title)
            TrackItem(it)
        }
    }
}