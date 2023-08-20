package com.paranid5.prima.presentation.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.Children
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.stackAnimation
import com.paranid5.prima.presentation.screens.artists.ArtistTracksScreen
import com.paranid5.prima.presentation.screens.main_menu_fragments.artists.ArtistsScreen
import com.paranid5.prima.presentation.screens.main_menu_fragments.favourites.FavouritesScreen
import com.paranid5.prima.presentation.screens.main_menu_fragments.tracks.TracksScreen
import com.paranid5.prima.presentation.screens.playbar_screens.current_playlist.CurrentPlaylistScreen
import com.paranid5.prima.presentation.ui.navigation.composition_locals.LocalRootNavigator

@Composable
fun RootScreen(modifier: Modifier = Modifier) {
    val navigator = LocalRootNavigator.current

    Children(
        stack = navigator.stack,
        modifier = modifier,
        animation = stackAnimation(fade())
    ) {
        when (it.instance) {
            Screen.MainMenuScreen.Tracks -> TracksScreen()
            Screen.PlaybarScreen.CurrentPlaylist -> CurrentPlaylistScreen()
            Screen.MainMenuScreen.Favourites -> FavouritesScreen()
            Screen.MainMenuScreen.Artists -> ArtistsScreen()
            Screen.ArtistTracks -> ArtistTracksScreen()

            // TODO: Other screens
            Screen.MainMenuScreen.TrackCollections -> Unit
            Screen.MainMenuScreen.AboutApp -> Unit
            Screen.MainMenuScreen.MP3Converter -> Unit
            Screen.MainMenuScreen.GTM -> Unit
            Screen.MainMenuScreen.Statistics -> Unit
            Screen.MainMenuScreen.Settings -> Unit
            Screen.FavouritesScreen.Artists -> Unit
            Screen.FavouritesScreen.TrackCollections -> Unit
            Screen.FavouritesScreen.Tracks -> Unit
            Screen.GTMScreen.AboutGame -> Unit
            Screen.GTMScreen.Game -> Unit
            Screen.PlaybarScreen.Equalizer -> Unit
            Screen.SettingsScreen.FilesLocation -> Unit
            Screen.SettingsScreen.Fonts -> Unit
            Screen.SettingsScreen.HiddenTracks -> Unit
            Screen.SettingsScreen.Themes -> Unit
            Screen.PlaybarScreen.TrimTrack -> Unit
            Screen.StatisticsScreen.AllTime -> Unit
            Screen.StatisticsScreen.Day -> Unit
            Screen.StatisticsScreen.Weak -> Unit
            Screen.StatisticsScreen.Year -> Unit
            Screen.TrackCollectionsScreen.Albums -> Unit
            Screen.TrackCollectionsScreen.CustomPlaylists -> Unit
        }
    }
}