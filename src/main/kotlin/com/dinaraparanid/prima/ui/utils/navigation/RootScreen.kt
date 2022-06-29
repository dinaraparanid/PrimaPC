package com.dinaraparanid.prima.ui.utils.navigation

import androidx.compose.runtime.*
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.RouterState
import com.arkivanov.decompose.router.replaceCurrent
import com.arkivanov.decompose.router.router
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize

@Suppress("IncorrectFormatting")
class RootScreen(componentContext: ComponentContext) : ScreenElement, ComponentContext by componentContext {
    override val routerState: Value<RouterState<*, ScreenElement.Screen.MainMenuScreen>>
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

    sealed interface Config : Parcelable {
        @Parcelize
        object Tracks : Config

        @Parcelize
        object TrackCollections: Config

        @Parcelize
        object Artists: Config

        @Parcelize
        object Favourites: Config

        @Parcelize
        object MP3Converter: Config

        @Parcelize
        object GTM: Config

        @Parcelize
        object Statistics: Config

        @Parcelize
        object Settings: Config

        @Parcelize
        object AboutApp: Config
    }

    private inline val initialConfig: Config
        get() = Config.Tracks // TODO: Load initial screen

    private fun getChild(config: Config) = when (config) {
        Config.Tracks -> ScreenElement.Screen.MainMenuScreen.Tracks
        Config.TrackCollections -> ScreenElement.Screen.MainMenuScreen.TrackCollections
        Config.Artists -> ScreenElement.Screen.MainMenuScreen.Artists
        Config.Favourites -> ScreenElement.Screen.MainMenuScreen.Favourites
        Config.MP3Converter -> ScreenElement.Screen.MainMenuScreen.MP3Converter
        Config.GTM -> ScreenElement.Screen.MainMenuScreen.GTM
        Config.Statistics -> ScreenElement.Screen.MainMenuScreen.Statistics
        Config.Settings -> ScreenElement.Screen.MainMenuScreen.Settings
        Config.AboutApp -> ScreenElement.Screen.MainMenuScreen.AboutApp
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun changeConfig(config: Config) {
        _currentConfigState.value = config
        router.replaceCurrent(_currentConfigState.value)
    }

    fun changeConfigToTracks()              = changeConfig(Config.Tracks)
    fun changeConfigToTrackCollections()    = changeConfig(Config.TrackCollections)
    fun changeConfigToArtists()             = changeConfig(Config.Artists)
    fun changeConfigToFavourites()          = changeConfig(Config.Favourites)
    fun changeConfigToMP3Converter()        = changeConfig(Config.MP3Converter)
    fun changeConfigToGTM()                 = changeConfig(Config.GTM)
    fun changeConfigToStatistics()          = changeConfig(Config.Statistics)
    fun changeConfigToSettings()            = changeConfig(Config.Settings)
    fun changeConfigToAboutApp()            = changeConfig(Config.AboutApp)

    @Composable
    fun start() {
        _currentConfigState = remember { mutableStateOf(initialConfig) }
    }
}

