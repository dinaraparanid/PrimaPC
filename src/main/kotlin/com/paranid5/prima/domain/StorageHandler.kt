package com.paranid5.prima.domain

import androidx.compose.ui.graphics.Color
import com.paranid5.prima.data.TrackSearchOrder
import com.paranid5.prima.presentation.StartScreens
import com.paranid5.prima.presentation.ui.theme.Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

class StorageHandler : CoroutineScope by CoroutineScope(Dispatchers.IO) {
    val languageState = MutableStateFlow(Language.English)

    val themeState = MutableStateFlow<Theme>(Theme.PurpleNight())

    val primaryColorState = themeState.map { it.rgb }
        .stateIn(this, SharingStarted.Eagerly, Theme.PurpleNight().rgb)

    val secondaryColorState = themeState.map { theme ->
        if (theme.isNight) Color(22, 21, 25) else Color.White
    }.stateIn(this, SharingStarted.Eagerly, Color.White)

    val secondaryAlternativeColorState = themeState.map { theme ->
        if (theme.isNight) Color.White else Color(22, 21, 25)
    }.stateIn(this, SharingStarted.Eagerly, Color.White)

    val startScreenState = MutableStateFlow(StartScreens.TRACKS)

    val trackSearchOrderState = MutableStateFlow(
        hashSetOf(
            TrackSearchOrder.TITLE,
            TrackSearchOrder.ARTIST,
            TrackSearchOrder.ALBUM
        )
    )

    fun storeTrackSearchOrder(order: TrackSearchOrder) {
        // TODO: Store updated version

        trackSearchOrderState.updateAndGet { orders ->
            when (order) {
                in orders -> orders.remove(order)
                else -> orders.add(order)
            }

            orders
        }
    }
}