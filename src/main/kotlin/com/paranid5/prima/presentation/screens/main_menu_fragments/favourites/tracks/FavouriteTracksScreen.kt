package com.paranid5.prima.presentation.screens.main_menu_fragments.favourites.tracks

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.paranid5.prima.data.Track
import com.paranid5.prima.di.KOIN_FAVOURITE_TRACKS
import com.paranid5.prima.di.KOIN_FILTERED_FAVOURITE_TRACKS
import com.paranid5.prima.presentation.ui.AwaitDialog
import com.paranid5.prima.presentation.ui.tracks.DefaultTracksFragment
import com.paranid5.prima.rust.RustLibs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
fun FavouriteTracksScreen(
    modifier: Modifier = Modifier,
    tracksState: MutableStateFlow<List<Track>> = koinInject(named(KOIN_FAVOURITE_TRACKS)),
    filteredTracksState: MutableStateFlow<List<Track>> = koinInject(named(KOIN_FILTERED_FAVOURITE_TRACKS)),
) {
    val isLoadingState = remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val favouriteTracks = withContext(Dispatchers.IO) {
            RustLibs.getFavouriteTracks().toList()
        }

        tracksState.update { favouriteTracks }
        filteredTracksState.update { favouriteTracks }
        isLoadingState.value = false
    }

    AwaitDialog(isDialogShownState = isLoadingState)

    DefaultTracksFragment(
        tracksState = tracksState,
        filteredTracksState = filteredTracksState,
        modifier = modifier
    )
}