package com.dinaraparanid.prima.ui.fragments.main_menu_fragments.artists

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.dinaraparanid.prima.entities.Artist
import com.dinaraparanid.prima.ui.utils.artists.ArtistsList
import kotlinx.coroutines.launch

@Composable
fun ArtistsFragment(artistsState: SnapshotStateList<Artist>, filteredArtistsState: SnapshotStateList<Artist>) {
    rememberCoroutineScope().launch { scanArtists(artistsState, filteredArtistsState) }
    ArtistsList(filteredArtistsState)
}