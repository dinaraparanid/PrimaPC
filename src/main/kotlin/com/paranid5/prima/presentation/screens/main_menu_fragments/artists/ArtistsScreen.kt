package com.paranid5.prima.presentation.screens.main_menu_fragments.artists

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.paranid5.prima.data.Artist
import com.paranid5.prima.di.KOIN_ALL_ARTISTS
import com.paranid5.prima.di.KOIN_FILTERED_ALL_ARTISTS
import com.paranid5.prima.di.KOIN_SELECTED_ARTIST
import com.paranid5.prima.domain.StorageHandler
import com.paranid5.prima.domain.scanArtists
import com.paranid5.prima.presentation.ui.AwaitDialog
import com.paranid5.prima.presentation.ui.artists.ArtistsList
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
fun ArtistsScreen(
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject(),
    selectedArtistState: MutableStateFlow<Artist?> = koinInject(named(KOIN_SELECTED_ARTIST)),
    artistsState: MutableStateFlow<List<Artist>> = koinInject(named(KOIN_ALL_ARTISTS)),
    filteredArtistsState: MutableStateFlow<List<Artist>> = koinInject(named(KOIN_FILTERED_ALL_ARTISTS)),
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