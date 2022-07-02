package com.dinaraparanid.prima.ui.tracks

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
            tracksState.run {
                clear()
                addAll(tracksTask.await())
            }

            filteredTracksState.run {
                clear()
                addAll(tracksState)
            }
        }
    }