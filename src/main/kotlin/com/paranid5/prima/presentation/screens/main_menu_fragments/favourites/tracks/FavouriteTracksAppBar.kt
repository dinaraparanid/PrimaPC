package com.paranid5.prima.presentation.screens.main_menu_fragments.favourites.tracks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.paranid5.prima.data.Track
import com.paranid5.prima.domain.StorageHandler
import com.paranid5.prima.presentation.ui.tracks.DefaultTracksAppBar
import org.koin.compose.koinInject

@Composable
fun FavouriteTracksAppBar(
    tracksState: MutableState<List<Track>>,
    filteredTracksState: MutableState<List<Track>>,
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject()
) {
    val lang by storageHandler.languageState.collectAsState()

    DefaultTracksAppBar(
        mainLabel = lang.favourites,
        tracksState = tracksState,
        filteredTracksState = filteredTracksState,
        modifier = modifier
    )
}