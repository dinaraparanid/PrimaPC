package com.paranid5.prima.presentation.ui.navigation.composition_locals

import androidx.compose.runtime.staticCompositionLocalOf
import com.paranid5.prima.presentation.ui.navigation.RootNavigator

val LocalRootNavigator = staticCompositionLocalOf<RootNavigator> {
    throw IllegalStateException("Root Navigator is not initialized")
}