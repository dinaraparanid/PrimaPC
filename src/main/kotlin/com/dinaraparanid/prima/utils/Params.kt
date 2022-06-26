package com.dinaraparanid.prima.utils

import androidx.compose.ui.graphics.Color
import com.dinaraparanid.prima.utils.localization.Localization

object Params {
    enum class Language { EN, RU, BE, ZH }

    enum class Screens {
        TRACKS,
        PLAYLISTS,
        ARTISTS,
        FAVOURITES,
        MP3_CONVERTER,
        GTM,
        STATISTICS,
        SETTINGS,
        ABOUT_APP
    }

    enum class TracksSearchOrder { TITLE, ARTIST, ALBUM, ALL }

    @JvmField
    val language = Language.EN // TODO: Load lang

    @JvmField
    val theme = Theme.PurpleNight() // TODO: Load theme
    val primaryColor = theme.rgb
    val secondaryColor = if (theme.isNight) Color(22, 21, 25) else Color.White
    val secondaryAlternativeColor = if (theme.isNight) Color.White else Color(22, 21, 25)

    @JvmField
    val mainLabel = Localization.tracks.resource // TODO: Load main label

    @JvmField
    val currentScreen = Screens.TRACKS // TODO: Load cur screen

    @JvmField
    val tracksSearchOrder = TracksSearchOrder.ALL // TODO: Load search order
}