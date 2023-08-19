package com.paranid5.prima.presentation.screens.main_menu_fragments.tracks

import androidx.compose.runtime.snapshots.SnapshotStateList
import com.paranid5.prima.data.Track
import com.paranid5.prima.rust.RustLibs
import kotlinx.coroutines.*

suspend fun scanTracks(tracksState: SnapshotStateList<Track>, filteredTracksState: SnapshotStateList<Track>) =
    coroutineScope {
        val tracksTask = async(Dispatchers.IO) {
            RustLibs.getAllTracksBlocking()
        }

        launch {
            tracksState.clear()
            filteredTracksState.clear()

            tracksState.addAll(tracksTask.await())
            filteredTracksState.addAll(tracksState)
        }
    }

suspend fun scanTracks(tracksState: SnapshotStateList<Track>) = coroutineScope {
    val tracksTask = async(Dispatchers.IO) {
        RustLibs.getAllTracksBlocking()
    }

    launch {
        tracksState.clear()
        tracksState.addAll(tracksTask.await())
    }
}