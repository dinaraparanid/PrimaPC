package com.paranid5.prima.presentation.fragments.main_menu_fragments.artists

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.paranid5.prima.data.Artist
import com.paranid5.prima.presentation.ui.AwaitDialog
import com.paranid5.prima.presentation.ui.artists.ArtistsList
import com.paranid5.prima.presentation.ui.navigation.RootScreen
import kotlinx.coroutines.launch

@Composable
fun ArtistsFragment(
    rootScreen: RootScreen,
    curArtistState: MutableState<Artist?>,
    artistsState: SnapshotStateList<Artist>,
    filteredArtistsState: SnapshotStateList<Artist>
) {
    val isLoadingState = mutableStateOf(true)

    rememberCoroutineScope().launch {
        scanArtists(artistsState, filteredArtistsState)
        isLoadingState.value = false
    }

    AwaitDialog(isDialogShownState = isLoadingState)
    ArtistsList(rootScreen, curArtistState, filteredArtistsState)
}