package com.paranid5.prima.presentation.ui.navigation.composition_locals

import androidx.compose.runtime.staticCompositionLocalOf
import com.paranid5.prima.presentation.ui.navigation.FavouriteNavigator

val LocalFavouriteNavigator = staticCompositionLocalOf<FavouriteNavigator> {
    throw IllegalArgumentException("Favourite Navigator is not initialized")
}