package com.paranid5.prima.presentation.ui.navigation

import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize

sealed interface Config : Parcelable {
    sealed interface MainMenuConfig : Config {
        @Parcelize
        data object Tracks : MainMenuConfig

        @Parcelize
        data object TrackCollections : MainMenuConfig

        @Parcelize
        data object Artists : MainMenuConfig

        @Parcelize
        data object Favourites : MainMenuConfig

        @Parcelize
        data object MP3Converter : MainMenuConfig

        @Parcelize
        data object GTM : MainMenuConfig

        @Parcelize
        data object Statistics : MainMenuConfig

        @Parcelize
        data object Settings : MainMenuConfig

        @Parcelize
        data object AboutApp : MainMenuConfig
    }

    sealed interface TrackCollectionsConfig : Config {
        @Parcelize
        data object Albums : TrackCollectionsConfig

        @Parcelize
        data object CustomPlaylists : TrackCollectionsConfig
    }

    sealed interface FavouritesConfig : Config {
        @Parcelize
        data object Tracks : FavouritesConfig

        @Parcelize
        data object Artists : FavouritesConfig

        @Parcelize
        data object TrackCollections : FavouritesConfig
    }

    sealed interface GTMConfig : Config {
        @Parcelize
        data object AboutGame : GTMConfig

        @Parcelize
        data object Game : GTMConfig
    }

    sealed interface StatisticsConfig : Config {
        @Parcelize
        data object AllTime : StatisticsConfig

        @Parcelize
        data object Year : StatisticsConfig

        @Parcelize
        data object Weak : StatisticsConfig

        @Parcelize
        data object Day : StatisticsConfig
    }

    sealed interface SettingsConfig : Config {
        @Parcelize
        data object Fonts : SettingsConfig

        @Parcelize
        data object Themes : SettingsConfig

        @Parcelize
        data object FilesLocation : SettingsConfig

        @Parcelize
        data object HiddenTracks : SettingsConfig
    }

    sealed interface PlaybarConfig : Config {
        @Parcelize
        data object CurrentPlaylist : SettingsConfig

        @Parcelize
        data object TrimTrack : SettingsConfig

        @Parcelize
        data object Equalizer : SettingsConfig
    }

    @Parcelize
    data object ArtistTracks : Config
}