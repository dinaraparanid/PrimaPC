package com.dinaraparanid.prima.ui.fragments.main_menu_fragments.artists

import androidx.compose.runtime.snapshots.SnapshotStateList
import com.dinaraparanid.prima.entities.Artist
import com.dinaraparanid.prima.rust.RustLibs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

suspend fun scanArtists(artistsState: SnapshotStateList<Artist>, filteredArtistsState: SnapshotStateList<Artist>) =
    coroutineScope {
        val artistsTask = async(Dispatchers.IO) {
            RustLibs.getAllArtistsBlocking().map(::Artist)
        }

        launch {
            artistsState.clear()
            filteredArtistsState.clear()

            artistsState.addAll(artistsTask.await())
            filteredArtistsState.addAll(artistsState)
        }
    }