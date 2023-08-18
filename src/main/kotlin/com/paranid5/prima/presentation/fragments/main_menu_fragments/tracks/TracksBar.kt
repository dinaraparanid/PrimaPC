package com.paranid5.prima.presentation.fragments.main_menu_fragments.tracks

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.paranid5.prima.data.Track
import com.paranid5.prima.presentation.ui.tracks.DefaultTracksBar

@Composable
fun TracksBar(
    tracksState: SnapshotStateList<Track>,
    filteredTracksState: SnapshotStateList<Track>,
    listState: LazyListState
) = DefaultTracksBar(tracksState, filteredTracksState, listState)