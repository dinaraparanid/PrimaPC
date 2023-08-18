package com.dinaraparanid.prima.ui.fragments.main_menu_fragments.favourites.tracks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.dinaraparanid.prima.entities.Track
import com.dinaraparanid.prima.ui.utils.tracks.DefaultTracksAppBar
import com.dinaraparanid.prima.utils.localization.Localization

@Composable
fun FavouriteTracksAppBar(
    tracksState: SnapshotStateList<Track>,
    filteredTracksState: SnapshotStateList<Track>,
) = DefaultTracksAppBar(tracksState, filteredTracksState, mainLabel = Localization.favourites.resource)