package com.dinaraparanid.prima.ui.fragments.main_menu_fragments.tracks

import androidx.compose.runtime.snapshots.SnapshotStateList
import com.dinaraparanid.prima.entities.Track
import com.dinaraparanid.prima.rust.RustLibs
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