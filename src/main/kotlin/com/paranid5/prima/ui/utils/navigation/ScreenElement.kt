package com.dinaraparanid.prima.ui.utils.navigation

import com.arkivanov.decompose.router.RouterState
import com.arkivanov.decompose.value.Value

interface ScreenElement {
    val routerState: Value<RouterState<*, Screen>>

    sealed interface Screen {
        sealed interface MainMenuScreen : Screen {
            object Tracks : MainMenuScreen
            object TrackCollections : MainMenuScreen
            object Artists : MainMenuScreen
            object Favourites : MainMenuScreen
            object MP3Converter : MainMenuScreen
            object GTM : MainMenuScreen
            object Statistics : MainMenuScreen
            object Settings : MainMenuScreen
            object AboutApp : MainMenuScreen
        }

        sealed interface TrackCollectionsScreen : Screen {
            object Albums : TrackCollectionsScreen
            object CustomPlaylists : TrackCollectionsScreen
        }

        sealed interface FavouritesScreen : Screen {
            object Tracks : FavouritesScreen
            object Artists : FavouritesScreen
            object TrackCollections : FavouritesScreen
        }

        sealed interface GTMScreen : Screen {
            object AboutGame : GTMScreen
            object Game : GTMScreen
        }

        sealed interface StatisticsScreen : Screen {
            object AllTime : StatisticsScreen
            object Year : StatisticsScreen
            object Weak : StatisticsScreen
            object Day : StatisticsScreen
        }

        sealed interface SettingsScreen : Screen {
            object Fonts : SettingsScreen
            object Themes : SettingsScreen
            object FilesLocation : SettingsScreen
            object HiddenTracks : SettingsScreen
        }

        sealed interface PlaybarScreen : Screen {
            object CurrentPlaylist : SettingsScreen
            object TrimTrack : SettingsScreen
            object Equalizer : SettingsScreen
        }

        object ArtistTracks : Screen
    }
}