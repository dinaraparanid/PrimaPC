package com.dinaraparanid.prima.ui.utils.navigation

import androidx.compose.runtime.MutableState
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.RouterState
import com.arkivanov.decompose.value.Value

@Suppress("IncorrectFormatting")
class RootScreen(componentContext: ComponentContext) : AbstractScreen<ScreenElement.Screen, Config>(componentContext) {
    override val routerState: Value<RouterState<*, ScreenElement.Screen>>
        get() = router.state

    override lateinit var _currentConfigState: MutableState<Config>

    override val initialConfig: Config.MainMenuConfig = Config.MainMenuConfig.Tracks // TODO: Load initial screen

    override fun getChild(config: Config) = when (config) {
        Config.MainMenuConfig.Tracks -> ScreenElement.Screen.MainMenuScreen.Tracks
        Config.MainMenuConfig.TrackCollections -> ScreenElement.Screen.MainMenuScreen.TrackCollections
        Config.MainMenuConfig.Artists -> ScreenElement.Screen.MainMenuScreen.Artists
        Config.MainMenuConfig.Favourites -> ScreenElement.Screen.MainMenuScreen.Favourites
        Config.MainMenuConfig.MP3Converter -> ScreenElement.Screen.MainMenuScreen.MP3Converter
        Config.MainMenuConfig.GTM -> ScreenElement.Screen.MainMenuScreen.GTM
        Config.MainMenuConfig.Statistics -> ScreenElement.Screen.MainMenuScreen.Statistics
        Config.MainMenuConfig.Settings -> ScreenElement.Screen.MainMenuScreen.Settings
        Config.MainMenuConfig.AboutApp -> ScreenElement.Screen.MainMenuScreen.AboutApp
        Config.GTMConfig.AboutGame -> ScreenElement.Screen.GTMScreen.AboutGame
        Config.GTMConfig.Game -> ScreenElement.Screen.GTMScreen.Game
        Config.PlaybarConfig.CurrentPlaylist -> ScreenElement.Screen.PlaybarScreen.CurrentPlaylist
        Config.PlaybarConfig.Equalizer -> ScreenElement.Screen.PlaybarScreen.Equalizer
        Config.PlaybarConfig.TrimTrack -> ScreenElement.Screen.PlaybarScreen.TrimTrack
        Config.SettingsConfig.FilesLocation -> ScreenElement.Screen.SettingsScreen.FilesLocation
        Config.SettingsConfig.Fonts -> ScreenElement.Screen.SettingsScreen.Fonts
        Config.SettingsConfig.HiddenTracks -> ScreenElement.Screen.SettingsScreen.HiddenTracks
        Config.SettingsConfig.Themes -> ScreenElement.Screen.SettingsScreen.Themes
        Config.ArtistTracks -> ScreenElement.Screen.ArtistTracks
        else -> throw IllegalArgumentException("$config not a root screen config")
    }

    fun changeConfigToTracks()              = changeConfig(Config.MainMenuConfig.Tracks)
    fun changeConfigToTrackCollections()    = changeConfig(Config.MainMenuConfig.TrackCollections)
    fun changeConfigToArtists()             = changeConfig(Config.MainMenuConfig.Artists)
    fun changeConfigToFavourites()          = changeConfig(Config.MainMenuConfig.Favourites)
    fun changeConfigToMP3Converter()        = changeConfig(Config.MainMenuConfig.MP3Converter)
    fun changeConfigToGTM()                 = changeConfig(Config.MainMenuConfig.GTM)
    fun changeConfigToStatistics()          = changeConfig(Config.MainMenuConfig.Statistics)
    fun changeConfigToSettings()            = changeConfig(Config.MainMenuConfig.Settings)
    fun changeConfigToAboutApp()            = changeConfig(Config.MainMenuConfig.AboutApp)
    fun changeConfigToCurPlaylist()         = changeConfig(Config.PlaybarConfig.CurrentPlaylist)
    fun changeConfigToArtistTracks()        = changeConfig(Config.ArtistTracks)
}

