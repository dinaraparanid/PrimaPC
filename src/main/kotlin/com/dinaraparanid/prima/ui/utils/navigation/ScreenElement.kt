package com.dinaraparanid.prima.ui.utils.navigation

import com.arkivanov.decompose.router.RouterState
import com.arkivanov.decompose.value.Value

sealed interface ScreenElement {
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

        sealed interface TrackCollections : Screen {
            object Albums : TrackCollections
            object CustomPlaylists : TrackCollections
        }

        sealed interface Favourites : Screen {
            object Tracks : Favourites
            object Artists : Favourites
            object TrackCollections : Favourites
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
            object Font : SettingsScreen
            object Theme : SettingsScreen
            object FilesLocation : SettingsScreen
            object HiddenTracks : SettingsScreen
        }
    }
}