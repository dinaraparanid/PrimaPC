package com.dinaraparanid.prima.ui.fragments.main_menu_fragments.artists

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.dinaraparanid.prima.entities.Artist
import com.dinaraparanid.prima.ui.utils.AwaitDialog
import com.dinaraparanid.prima.ui.utils.artists.ArtistsList
import com.dinaraparanid.prima.ui.utils.navigation.RootScreen
import com.dinaraparanid.prima.utils.localization.Localization
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