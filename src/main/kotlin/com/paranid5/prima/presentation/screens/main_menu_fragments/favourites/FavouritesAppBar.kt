package com.paranid5.prima.presentation.screens.main_menu_fragments.favourites

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.paranid5.prima.data.Artist
import com.paranid5.prima.data.Track
import com.paranid5.prima.presentation.screens.main_menu_fragments.favourites.artists.FavouriteArtistsAppBar
import com.paranid5.prima.presentation.screens.main_menu_fragments.favourites.tracks.FavouriteTracksAppBar

@Composable
fun FavouritesAppBar(
    favouriteFragmentState: State<FavouriteScreens>,
    favouriteTracksState: SnapshotStateList<Track>,
    filteredFavouriteTracksState: SnapshotStateList<Track>,
    favouriteArtistsState: SnapshotStateList<Artist>,
    filteredFavouriteArtistsState: SnapshotStateList<Artist>
) = when (favouriteFragmentState.value) {
    FavouriteScreens.TRACKS -> FavouriteTracksAppBar(favouriteTracksState, filteredFavouriteTracksState)
    FavouriteScreens.ARTISTS -> FavouriteArtistsAppBar(favouriteArtistsState, filteredFavouriteArtistsState)
    FavouriteScreens.TRACK_COLLECTIONS -> Unit // TODO: Favourite Track Collections Fragment
}