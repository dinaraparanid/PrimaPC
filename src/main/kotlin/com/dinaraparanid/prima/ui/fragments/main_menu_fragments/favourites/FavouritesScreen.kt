package com.dinaraparanid.prima.ui.fragments.main_menu_fragments.favourites

import androidx.compose.runtime.MutableState
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.RouterState
import com.arkivanov.decompose.value.Value
import com.dinaraparanid.prima.ui.utils.navigation.AbstractScreen
import com.dinaraparanid.prima.ui.utils.navigation.Config
import com.dinaraparanid.prima.ui.utils.navigation.ScreenElement

@Suppress("IncorrectFormatting")
class FavouritesScreen(componentContext: ComponentContext) :
    AbstractScreen<ScreenElement.Screen.FavouritesScreen, Config.FavouritesConfig>(componentContext) {
    override val routerState: Value<RouterState<*, ScreenElement.Screen.FavouritesScreen>>
        get() = router.state

    override lateinit var _currentConfigState: MutableState<Config.FavouritesConfig>

    override val initialConfig = Config.FavouritesConfig.Tracks

    override fun getChild(config: Config.FavouritesConfig) = when (config) {
        Config.FavouritesConfig.Artists -> ScreenElement.Screen.FavouritesScreen.Artists
        Config.FavouritesConfig.TrackCollections -> ScreenElement.Screen.FavouritesScreen.TrackCollections
        Config.FavouritesConfig.Tracks -> ScreenElement.Screen.FavouritesScreen.Tracks
    }

    fun changeConfigToTracks()              = changeConfig(Config.FavouritesConfig.Tracks)
    fun changeConfigToArtist()              = changeConfig(Config.FavouritesConfig.Artists)
    fun changeConfigToTrackCollections()    = changeConfig(Config.FavouritesConfig.TrackCollections)
}