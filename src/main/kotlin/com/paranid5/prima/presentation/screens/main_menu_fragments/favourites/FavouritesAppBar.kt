package com.paranid5.prima.presentation.screens.main_menu_fragments.favourites

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.paranid5.prima.presentation.screens.main_menu_fragments.favourites.artists.FavouriteArtistsAppBar
import com.paranid5.prima.presentation.screens.main_menu_fragments.favourites.tracks.FavouriteTracksAppBar
import com.paranid5.prima.presentation.ui.navigation.Config
import com.paranid5.prima.presentation.ui.navigation.composition_locals.LocalFavouriteNavigator

@Composable
fun FavouritesAppBar(modifier: Modifier = Modifier) {
    val navigator = LocalFavouriteNavigator.current
    val favouriteScreen by navigator.currentConfigState.collectAsState()

    when (favouriteScreen) {
        Config.FavouritesConfig.Tracks -> FavouriteTracksAppBar(modifier)
        Config.FavouritesConfig.Artists -> FavouriteArtistsAppBar(modifier)
        Config.FavouritesConfig.TrackCollections -> Unit // TODO: Favourite Track Collections Fragment
    }
}