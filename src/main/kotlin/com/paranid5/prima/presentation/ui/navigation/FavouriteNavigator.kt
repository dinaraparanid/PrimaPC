package com.paranid5.prima.presentation.ui.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.childStack

class FavouriteNavigator(
    componentContext: ComponentContext,
    initialConfig: Config.FavouritesConfig
) : AbstractNavigator<Config.FavouritesConfig>(componentContext, initialConfig) {
    override val stack = childStack(
        source = navigation,
        initialConfiguration = initialConfig,
        handleBackButton = true,
        childFactory = { config, _ ->
            getScreenFromConfig(config)
        }
    )

    override fun getScreenFromConfig(config: Config.FavouritesConfig) = when (config) {
        Config.FavouritesConfig.Tracks -> Screen.FavouritesScreen.Tracks
        Config.FavouritesConfig.Artists -> Screen.FavouritesScreen.Artists
        Config.FavouritesConfig.TrackCollections -> Screen.FavouritesScreen.TrackCollections
    }

    fun changeConfigToFavouriteTracks() = changeConfig(Config.FavouritesConfig.Tracks)
    fun changeConfigToFavouriteArtist() = changeConfig(Config.FavouritesConfig.Artists)
    fun changeConfigToFavouriteTrackCollections() = changeConfig(Config.FavouritesConfig.TrackCollections)
}