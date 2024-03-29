package com.paranid5.prima.presentation.screens.main_menu_fragments.favourites.tracks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.paranid5.prima.data.Track
import com.paranid5.prima.di.KOIN_FAVOURITE_TRACKS
import com.paranid5.prima.di.KOIN_FILTERED_FAVOURITE_TRACKS
import com.paranid5.prima.domain.StorageHandler
import com.paranid5.prima.presentation.ui.tracks.DefaultTracksAppBar
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
fun FavouriteTracksAppBar(
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject(),
    tracksState: MutableStateFlow<List<Track>> = koinInject(named(KOIN_FAVOURITE_TRACKS)),
    filteredTracksState: MutableStateFlow<List<Track>> = koinInject(named(KOIN_FILTERED_FAVOURITE_TRACKS)),
) {
    val lang by storageHandler.languageState.collectAsState()

    DefaultTracksAppBar(
        mainLabel = lang.favourites,
        tracksState = tracksState,
        filteredTracksState = filteredTracksState,
        modifier = modifier
    )
}