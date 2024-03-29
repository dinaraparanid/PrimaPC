package com.paranid5.prima.presentation.screens.main_menu_fragments.tracks

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.paranid5.prima.data.Track
import com.paranid5.prima.presentation.ui.tracks.DefaultTracksBar
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun TracksBar(
    tracksState: MutableStateFlow<List<Track>>,
    filteredTracksState: MutableStateFlow<List<Track>>,
    listState: LazyListState,
    modifier: Modifier = Modifier
) = DefaultTracksBar(
    tracksState = tracksState,
    filteredTracksState = filteredTracksState,
    listState = listState,
    modifier = modifier
)