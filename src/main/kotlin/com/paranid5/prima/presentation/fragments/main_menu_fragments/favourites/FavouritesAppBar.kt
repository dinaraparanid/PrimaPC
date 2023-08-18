package com.paranid5.prima.presentation.fragments.main_menu_fragments.favourites

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.paranid5.prima.data.Artist
import com.paranid5.prima.data.Track
import com.paranid5.prima.presentation.fragments.main_menu_fragments.favourites.artists.FavouriteArtistsAppBar
import com.paranid5.prima.presentation.fragments.main_menu_fragments.favourites.tracks.FavouriteTracksAppBar

@Composable
fun FavouritesAppBar(
    favouriteFragmentState: State<FavouriteFragments>,
    favouriteTracksState: SnapshotStateList<Track>,
    filteredFavouriteTracksState: SnapshotStateList<Track>,
    favouriteArtistsState: SnapshotStateList<Artist>,
    filteredFavouriteArtistsState: SnapshotStateList<Artist>
) = when (favouriteFragmentState.value) {
    FavouriteFragments.TRACKS -> FavouriteTracksAppBar(favouriteTracksState, filteredFavouriteTracksState)
    FavouriteFragments.ARTISTS -> FavouriteArtistsAppBar(favouriteArtistsState, filteredFavouriteArtistsState)
    FavouriteFragments.TRACK_COLLECTIONS -> Unit // TODO: Favourite Track Collections Fragment
}