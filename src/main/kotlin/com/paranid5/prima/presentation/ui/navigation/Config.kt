package com.paranid5.prima.presentation.ui.navigation

import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize

sealed interface Config : Parcelable {
    sealed interface MainMenuConfig : Config {
        @Parcelize
        object Tracks : MainMenuConfig

        @Parcelize
        object TrackCollections : MainMenuConfig

        @Parcelize
        object Artists : MainMenuConfig

        @Parcelize
        object Favourites : MainMenuConfig

        @Parcelize
        object MP3Converter : MainMenuConfig

        @Parcelize
        object GTM : MainMenuConfig

        @Parcelize
        object Statistics : MainMenuConfig

        @Parcelize
        object Settings : MainMenuConfig

        @Parcelize
        object AboutApp : MainMenuConfig
    }

    sealed interface TrackCollectionsConfig : Config {
        @Parcelize
        object Albums : TrackCollectionsConfig

        @Parcelize
        object CustomPlaylists : TrackCollectionsConfig
    }

    sealed interface FavouritesConfig : Config {
        @Parcelize
        object Tracks : FavouritesConfig

        @Parcelize
        object Artists : FavouritesConfig

        @Parcelize
        object TrackCollections : FavouritesConfig
    }

    sealed interface GTMConfig : Config {
        @Parcelize
        object AboutGame : GTMConfig

        @Parcelize
        object Game : GTMConfig
    }

    sealed interface StatisticsConfig : Config {
        @Parcelize
        object AllTime : StatisticsConfig

        @Parcelize
        object Year : StatisticsConfig

        @Parcelize
        object Weak : StatisticsConfig

        @Parcelize
        object Day : StatisticsConfig
    }

    sealed interface SettingsConfig : Config {
        @Parcelize
        object Fonts : SettingsConfig

        @Parcelize
        object Themes : SettingsConfig

        @Parcelize
        object FilesLocation : SettingsConfig

        @Parcelize
        object HiddenTracks : SettingsConfig
    }

    sealed interface PlaybarConfig : Config {
        @Parcelize
        object CurrentPlaylist : SettingsConfig

        @Parcelize
        object TrimTrack : SettingsConfig

        @Parcelize
        object Equalizer : SettingsConfig
    }

    @Parcelize
    object ArtistTracks : Config
}