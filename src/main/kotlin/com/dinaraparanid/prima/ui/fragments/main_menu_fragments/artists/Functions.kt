package com.dinaraparanid.prima.ui.fragments.main_menu_fragments.artists

import androidx.compose.runtime.snapshots.SnapshotStateList
import com.dinaraparanid.prima.entities.Artist
import com.dinaraparanid.prima.entities.Track
import com.dinaraparanid.prima.rust.RustLibs
import com.dinaraparanid.prima.utils.localization.Localization
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.streams.toList

suspend fun scanArtists(artistsState: SnapshotStateList<Artist>, filteredArtistsState: SnapshotStateList<Artist>) =
    coroutineScope {
        val artistsTask = async(Dispatchers.IO) {
            RustLibs
                .getAllTracksBlocking()
                .mapNotNull(Track::artist)
                .distinct()
                .map { it.ifEmpty { Localization.unknownArtist.resource } }
                .sorted()
                .map(::Artist)
        }

        launch {
            artistsState.run {
                clear()
                addAll(artistsTask.await())
            }

            filteredArtistsState.run {
                clear()
                addAll(artistsState)
            }
        }
    }