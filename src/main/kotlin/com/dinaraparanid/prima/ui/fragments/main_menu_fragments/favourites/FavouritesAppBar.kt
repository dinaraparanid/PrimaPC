package com.dinaraparanid.prima.ui.fragments.main_menu_fragments.favourites

import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.dinaraparanid.prima.entities.Track
import com.dinaraparanid.prima.ui.fragments.main_menu_fragments.favourites.tracks.FavouriteTracksAppBar

@Composable
fun FavouritesAppBar(
    favouriteTracksState: SnapshotStateList<Track>,
    filteredFavouriteTracksState: SnapshotStateList<Track>
) = FavouriteTracksAppBar(favouriteTracksState, filteredFavouriteTracksState)