package com.dinaraparanid.prima.utils

import androidx.compose.ui.graphics.Color

object Params {
    enum class Language { EN, RU, BE, ZH }

    val language = Language.EN // TODO: Load lang

    val theme = Theme.PurpleNight() // TODO: Load theme
    val primaryColor = theme.rgb
    val secondaryColor = if (theme.isNight) Color(22, 21, 25) else Color.White
    val fontColor = if (theme.isNight) Color.White else Color(22, 21, 25)
}