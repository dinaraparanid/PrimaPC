package com.paranid5.prima.presentation.fragments.main_menu_fragments.favourites.tracks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.paranid5.prima.data.Track
import com.paranid5.prima.presentation.ui.tracks.DefaultTracksAppBar

@Composable
fun FavouriteTracksAppBar(
    tracksState: SnapshotStateList<Track>,
    filteredTracksState: SnapshotStateList<Track>,
) = DefaultTracksAppBar(tracksState, filteredTracksState, mainLabel = Localization.favourites.resource)