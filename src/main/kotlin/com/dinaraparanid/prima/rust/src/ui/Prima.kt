package com.dinaraparanid.prima.rust.src.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dinaraparanid.prima.rust.src.ui.tracks.Tracks
import com.dinaraparanid.prima.utils.Params

@Composable
@Preview
fun Prima() {
    val theme = Params.theme
    val primary = Params.primaryColor
    val secondary = Params.secondaryColor

    MaterialTheme(
        colors = Colors(
            primary = primary,
            primaryVariant = primary,
            secondary = secondary,
            secondaryVariant = secondary,
            background = secondary,
            surface = secondary,
            error = Color.Red,
            onPrimary = primary,
            onSecondary = secondary,
            onBackground = secondary,
            onSurface = secondary,
            onError = Color.Red,
            isLight = !theme.isNight
        )
    ) {
        Surface(color = secondary, modifier = Modifier.fillMaxSize()) { Tracks() }
    }
}