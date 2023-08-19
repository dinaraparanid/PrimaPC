package com.paranid5.prima.presentation.screens.main_menu_fragments.artists

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.paranid5.prima.data.Artist
import com.paranid5.prima.domain.StorageHandler
import com.paranid5.prima.presentation.ui.AwaitDialog
import com.paranid5.prima.presentation.ui.artists.ArtistsList
import org.koin.compose.koinInject

@Composable
fun ArtistsScreen(
    selectedArtistState: MutableState<Artist?>,
    artistsState: MutableState<List<Artist>>,
    filteredArtistsState: MutableState<List<Artist>>,
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject()
) {
    val lang by storageHandler.languageState.collectAsState()

    val isLoadingState = mutableStateOf(true)

    LaunchedEffect(Unit) {
        scanArtists(
            artistsState = artistsState,
            filteredArtistsState = filteredArtistsState,
            lang = lang
        )

        isLoadingState.value = false
    }

    AwaitDialog(isDialogShownState = isLoadingState)

    ArtistsList(
        selectedArtistState = selectedArtistState,
        artistsState = filteredArtistsState,
        modifier = modifier
    )
}