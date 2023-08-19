package com.paranid5.prima.presentation.ui.navigation

sealed interface Screen {
    sealed interface MainMenuScreen : Screen {
        data object Tracks : MainMenuScreen
        data object TrackCollections : MainMenuScreen
        data object Artists : MainMenuScreen
        data object Favourites : MainMenuScreen
        data object MP3Converter : MainMenuScreen
        data object GTM : MainMenuScreen
        data object Statistics : MainMenuScreen
        data object Settings : MainMenuScreen
        data object AboutApp : MainMenuScreen
    }

    sealed interface TrackCollectionsScreen : Screen {
        data object Albums : TrackCollectionsScreen
        data object CustomPlaylists : TrackCollectionsScreen
    }

    sealed interface FavouritesScreen : Screen {
        data object Tracks : FavouritesScreen
        data object Artists : FavouritesScreen
        data object TrackCollections : FavouritesScreen
    }

    sealed interface GTMScreen : Screen {
        data object AboutGame : GTMScreen
        data object Game : GTMScreen
    }

    sealed interface StatisticsScreen : Screen {
        data object AllTime : StatisticsScreen
        data object Year : StatisticsScreen
        data object Weak : StatisticsScreen
        data object Day : StatisticsScreen
    }

    sealed interface SettingsScreen : Screen {
        data object Fonts : SettingsScreen
        data object Themes : SettingsScreen
        data object FilesLocation : SettingsScreen
        data object HiddenTracks : SettingsScreen
    }

    sealed interface PlaybarScreen : Screen {
        data object CurrentPlaylist : SettingsScreen
        data object TrimTrack : SettingsScreen
        data object Equalizer : SettingsScreen
    }

    data object ArtistTracks : Screen
}