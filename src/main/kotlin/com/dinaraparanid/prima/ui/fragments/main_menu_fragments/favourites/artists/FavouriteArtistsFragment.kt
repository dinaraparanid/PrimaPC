package com.dinaraparanid.prima.ui.fragments.main_menu_fragments.favourites.artists

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.dinaraparanid.prima.entities.Artist
import com.dinaraparanid.prima.rust.RustLibs
import com.dinaraparanid.prima.ui.utils.artists.ArtistsList
import com.dinaraparanid.prima.ui.utils.navigation.RootScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

@Composable
fun FavouriteArtistsFragment(
    rootScreen: RootScreen,
    curArtistState: MutableState<Artist?>,
    artistsState: SnapshotStateList<Artist>,
    filteredArtistsState: SnapshotStateList<Artist>
) {
    rememberCoroutineScope().launch {
        val artistsTask = async(Dispatchers.IO) { RustLibs.getFavouriteArtists().map(::Artist) }

        artistsState.clear()
        filteredArtistsState.clear()

        artistsState.addAll(artistsTask.await())
        filteredArtistsState.addAll(artistsState)
    }

    ArtistsList(rootScreen, curArtistState, artistsState)
}