package com.dinaraparanid.prima.ui.utils.navigation

import androidx.compose.runtime.*
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.RouterState
import com.arkivanov.decompose.router.replaceCurrent
import com.arkivanov.decompose.router.router
import com.arkivanov.decompose.value.Value

@Suppress("IncorrectFormatting")
class RootScreen(componentContext: ComponentContext) : ScreenElement, ComponentContext by componentContext {
    override val routerState: Value<RouterState<*, ScreenElement.Screen>>
        get() = router.state

    private lateinit var _currentConfigState: MutableState<Config>

    val currentConfigState: State<Config>
        get() = _currentConfigState

    private val router by lazy {
        router(
            initialConfiguration = _currentConfigState.value,
            handleBackButton = true,
            childFactory = { config, _ -> getChild(config) }
        )
    }

    private inline val initialConfig: Config.MainMenuConfig
        get() = Config.MainMenuConfig.Tracks // TODO: Load initial screen

    private fun getChild(config: Config) = when (config) {
        Config.MainMenuConfig.Tracks -> ScreenElement.Screen.MainMenuScreen.Tracks
        Config.MainMenuConfig.TrackCollections -> ScreenElement.Screen.MainMenuScreen.TrackCollections
        Config.MainMenuConfig.Artists -> ScreenElement.Screen.MainMenuScreen.Artists
        Config.MainMenuConfig.Favourites -> ScreenElement.Screen.MainMenuScreen.Favourites
        Config.MainMenuConfig.MP3Converter -> ScreenElement.Screen.MainMenuScreen.MP3Converter
        Config.MainMenuConfig.GTM -> ScreenElement.Screen.MainMenuScreen.GTM
        Config.MainMenuConfig.Statistics -> ScreenElement.Screen.MainMenuScreen.Statistics
        Config.MainMenuConfig.Settings -> ScreenElement.Screen.MainMenuScreen.Settings
        Config.MainMenuConfig.AboutApp -> ScreenElement.Screen.MainMenuScreen.AboutApp
        Config.FavouritesConfig.Artists -> ScreenElement.Screen.FavouritesScreen.Artists
        Config.FavouritesConfig.TrackCollections -> ScreenElement.Screen.FavouritesScreen.TrackCollections
        Config.FavouritesConfig.Tracks -> ScreenElement.Screen.FavouritesScreen.Tracks
        Config.GTMConfig.AboutGame -> ScreenElement.Screen.GTMScreen.AboutGame
        Config.GTMConfig.Game -> ScreenElement.Screen.GTMScreen.Game
        Config.PlaybarConfig.CurrentPlaylist -> ScreenElement.Screen.PlaybarScreen.CurrentPlaylist
        Config.PlaybarConfig.Equalizer -> ScreenElement.Screen.PlaybarScreen.Equalizer
        Config.SettingsConfig.FilesLocation -> ScreenElement.Screen.SettingsScreen.FilesLocation
        Config.SettingsConfig.Fonts -> ScreenElement.Screen.SettingsScreen.Fonts
        Config.SettingsConfig.HiddenTracks -> ScreenElement.Screen.SettingsScreen.HiddenTracks
        Config.SettingsConfig.Themes -> ScreenElement.Screen.SettingsScreen.Themes
        Config.PlaybarConfig.TrimTrack -> ScreenElement.Screen.PlaybarScreen.TrimTrack
        Config.StatisticsConfig.AllTime -> ScreenElement.Screen.StatisticsScreen.AllTime
        Config.StatisticsConfig.Day -> ScreenElement.Screen.StatisticsScreen.Day
        Config.StatisticsConfig.Weak -> ScreenElement.Screen.StatisticsScreen.Weak
        Config.StatisticsConfig.Year -> ScreenElement.Screen.StatisticsScreen.Year
        Config.TrackCollectionsConfig.Albums -> ScreenElement.Screen.TrackCollectionsScreen.Albums
        Config.TrackCollectionsConfig.CustomPlaylists -> ScreenElement.Screen.TrackCollectionsScreen.CustomPlaylists
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun changeConfig(config: Config) {
        _currentConfigState.value = config
        router.replaceCurrent(_currentConfigState.value)
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

    @Composable
    fun start() {
        _currentConfigState = remember { mutableStateOf(initialConfig) }
    }
}

