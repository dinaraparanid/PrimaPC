package com.paranid5.prima.presentation.screens.main_menu_fragments.tracks

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.paranid5.prima.data.Track
import com.paranid5.prima.di.KOIN_ALL_TRACKS
import com.paranid5.prima.di.KOIN_FILTERED_ALL_TRACKS
import com.paranid5.prima.domain.scanTracks
import com.paranid5.prima.presentation.ui.AwaitDialog
import com.paranid5.prima.presentation.ui.tracks.DefaultTracksFragment
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
fun TracksScreen(
    modifier: Modifier = Modifier,
    allTracksState: MutableStateFlow<List<Track>> = koinInject(named(KOIN_ALL_TRACKS)),
    filteredAllTracksState: MutableStateFlow<List<Track>> = koinInject(named(KOIN_FILTERED_ALL_TRACKS))
) {
    val isLoadingState = mutableStateOf(true)

    LaunchedEffect(Unit) {
        scanTracks(
            tracksState = allTracksState,
            filteredTracksState = filteredAllTracksState
        )

        isLoadingState.value = false
    }

    AwaitDialog(isDialogShownState = isLoadingState)

    DefaultTracksFragment(
        tracksState = allTracksState,
        filteredTracksState = filteredAllTracksState,
        modifier = modifier
    )
}