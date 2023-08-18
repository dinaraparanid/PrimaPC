package com.paranid5.prima.presentation.fragments.main_menu_fragments.favourites.artists

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.paranid5.prima.data.Artist
import com.paranid5.prima.rust.RustLibs
import com.paranid5.prima.presentation.ui.AwaitDialog
import com.paranid5.prima.presentation.ui.artists.ArtistsList
import com.paranid5.prima.presentation.ui.navigation.RootScreen
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
    val isLoadingState = mutableStateOf(true)

    rememberCoroutineScope().launch {
        val artistsTask = async(Dispatchers.IO) { RustLibs.getFavouriteArtists().map(::Artist) }

        artistsState.clear()
        filteredArtistsState.clear()

        artistsState.addAll(artistsTask.await())
        filteredArtistsState.addAll(artistsState)
        isLoadingState.value = false
    }

    AwaitDialog(isDialogShownState = isLoadingState)
    ArtistsList(rootScreen, curArtistState, artistsState)
}