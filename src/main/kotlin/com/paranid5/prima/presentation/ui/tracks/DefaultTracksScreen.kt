package com.paranid5.prima.presentation.ui.tracks

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.paranid5.prima.data.Track
import com.paranid5.prima.presentation.screens.main_menu_fragments.tracks.TracksBar
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun DefaultTracksFragment(
    tracksState: MutableStateFlow<List<Track>>,
    filteredTracksState: MutableStateFlow<List<Track>>,
    modifier: Modifier = Modifier,
) = TrackList(
    tracksState = tracksState,
    filteredTracksState = filteredTracksState,
    modifier = modifier
) { tracksState, filteredTracksState, listState ->
    TracksBar(tracksState, filteredTracksState, listState)
}