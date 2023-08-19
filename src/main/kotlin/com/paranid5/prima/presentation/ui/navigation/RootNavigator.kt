package com.paranid5.prima.presentation.ui.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.push
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.updateAndGet

class RootNavigator(componentContext: ComponentContext, initialConfig: Config) :
    AbstractNavigator<Config>(componentContext, initialConfig) {
    override val stack = childStack(
        source = navigation,
        initialConfiguration = initialConfig,
        handleBackButton = true,
        childFactory = { config, _ ->
            getScreenFromConfig(config)
        }
    )

    override fun getScreenFromConfig(config: Config) = when (config) {
        Config.MainMenuConfig.Tracks -> Screen.MainMenuScreen.Tracks
        Config.MainMenuConfig.TrackCollections -> Screen.MainMenuScreen.TrackCollections
        Config.MainMenuConfig.Artists -> Screen.MainMenuScreen.Artists
        Config.MainMenuConfig.Favourites -> Screen.MainMenuScreen.Favourites
        Config.MainMenuConfig.MP3Converter -> Screen.MainMenuScreen.MP3Converter
        Config.MainMenuConfig.GTM -> Screen.MainMenuScreen.GTM
        Config.MainMenuConfig.Statistics -> Screen.MainMenuScreen.Statistics
        Config.MainMenuConfig.Settings -> Screen.MainMenuScreen.Settings
        Config.MainMenuConfig.AboutApp -> Screen.MainMenuScreen.AboutApp
        Config.GTMConfig.AboutGame -> Screen.GTMScreen.AboutGame
        Config.GTMConfig.Game -> Screen.GTMScreen.Game
        Config.PlaybarConfig.CurrentPlaylist -> Screen.PlaybarScreen.CurrentPlaylist
        Config.PlaybarConfig.Equalizer -> Screen.PlaybarScreen.Equalizer
        Config.PlaybarConfig.TrimTrack -> Screen.PlaybarScreen.TrimTrack
        Config.SettingsConfig.FilesLocation -> Screen.SettingsScreen.FilesLocation
        Config.SettingsConfig.Fonts -> Screen.SettingsScreen.Fonts
        Config.SettingsConfig.HiddenTracks -> Screen.SettingsScreen.HiddenTracks
        Config.SettingsConfig.Themes -> Screen.SettingsScreen.Themes
        Config.ArtistTracks -> Screen.ArtistTracks
        else -> throw IllegalArgumentException("$config is not a root screen config")
    }

    fun changeConfigToTracks() = changeConfig(Config.MainMenuConfig.Tracks)
    fun changeConfigToTrackCollections() = changeConfig(Config.MainMenuConfig.TrackCollections)
    fun changeConfigToArtists() = changeConfig(Config.MainMenuConfig.Artists)
    fun changeConfigToFavourites() = changeConfig(Config.MainMenuConfig.Favourites)
    fun changeConfigToMP3Converter() = changeConfig(Config.MainMenuConfig.MP3Converter)
    fun changeConfigToGTM() = changeConfig(Config.MainMenuConfig.GTM)
    fun changeConfigToStatistics() = changeConfig(Config.MainMenuConfig.Statistics)
    fun changeConfigToSettings() = changeConfig(Config.MainMenuConfig.Settings)
    fun changeConfigToAboutApp() = changeConfig(Config.MainMenuConfig.AboutApp)
    fun changeConfigToCurPlaylist() = changeConfig(Config.PlaybarConfig.CurrentPlaylist)
    fun changeConfigToArtistTracks() = changeConfig(Config.ArtistTracks)
}