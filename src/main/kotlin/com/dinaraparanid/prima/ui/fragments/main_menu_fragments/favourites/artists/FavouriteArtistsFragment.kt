package com.dinaraparanid.prima.ui.fragments.main_menu_fragments.favourites.artists

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.dinaraparanid.prima.entities.Artist
import com.dinaraparanid.prima.rust.RustLibs
import com.dinaraparanid.prima.ui.utils.artists.ArtistsList
import kotlinx.coroutines.launch

@Composable
fun FavouriteArtistsFragment(artistsState: SnapshotStateList<Artist>, filteredArtistsState: SnapshotStateList<Artist>) {
    rememberCoroutineScope().launch {
        val artists = RustLibs.getFavouriteArtists().map(::Artist)

        artistsState.clear()
        artistsState.addAll(artists)

        filteredArtistsState.clear()
        filteredArtistsState.addAll(artists)
    }

    ArtistsList(filteredArtistsState)
}