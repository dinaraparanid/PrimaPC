package com.paranid5.prima.presentation.screens.main_menu_fragments.favourites.artists

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.paranid5.prima.data.Artist
import com.paranid5.prima.di.KOIN_FAVOURITE_ARTISTS
import com.paranid5.prima.di.KOIN_FILTERED_FAVOURITE_ARTISTS
import com.paranid5.prima.di.KOIN_SELECTED_ARTIST
import com.paranid5.prima.presentation.ui.AwaitDialog
import com.paranid5.prima.presentation.ui.artists.ArtistsList
import com.paranid5.prima.rust.RustLibs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
fun FavouriteArtistsScreen(
    modifier: Modifier = Modifier,
    selectedArtistState: MutableStateFlow<Artist?> = koinInject(named(KOIN_SELECTED_ARTIST)),
    artistsState: MutableStateFlow<List<Artist>> = koinInject(named(KOIN_FAVOURITE_ARTISTS)),
    filteredArtistsState: MutableStateFlow<List<Artist>> = koinInject(named(KOIN_FILTERED_FAVOURITE_ARTISTS)),
) {
    val isLoadingState = remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val artists = withContext(Dispatchers.IO) {
            RustLibs.getFavouriteArtists().map(::Artist)
        }

        artistsState.update { artists }
        filteredArtistsState.update { artists }
        isLoadingState.value = false
    }

    AwaitDialog(isDialogShownState = isLoadingState)

    ArtistsList(
        selectedArtistState = selectedArtistState,
        artistsState = filteredArtistsState,
        modifier = modifier
    )
}