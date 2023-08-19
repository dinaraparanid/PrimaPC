package com.paranid5.prima.presentation.screens.main_menu_fragments.favourites.artists

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.paranid5.prima.data.Artist
import com.paranid5.prima.presentation.ui.AwaitDialog
import com.paranid5.prima.presentation.ui.artists.ArtistsList
import com.paranid5.prima.rust.RustLibs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun FavouriteArtistsScreen(
    selectedArtistState: MutableState<Artist?>,
    artistsState: MutableState<List<Artist>>,
    filteredArtistsState: MutableState<List<Artist>>
) {
    val isLoadingState = mutableStateOf(true)

    LaunchedEffect(Unit) {
        val artists = withContext(Dispatchers.IO) {
            RustLibs.getFavouriteArtists().map(::Artist)
        }

        artistsState.value = artists
        filteredArtistsState.value = artists
        isLoadingState.value = false
    }

    AwaitDialog(isDialogShownState = isLoadingState)

    ArtistsList(
        selectedArtistState = selectedArtistState,
        artistsState = filteredArtistsState
    )
}