package com.dinaraparanid.prima.ui

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.dinaraparanid.prima.rust.RustLibs
import java.awt.GraphicsEnvironment

fun App() {
    RustLibs.initRust()

    val (width, height) = GraphicsEnvironment
        .getLocalGraphicsEnvironment()
        .screenDevices
        .first()
        .displayMode
        .run { width to height }

    application {
        Window(
            title = "Prima",
            icon = painterResource("images/app_icon.png"),
            state = WindowState(width = width.dp, height = height.dp),
            onCloseRequest = {
                RustLibs.storeCurPlaybackPos()
                exitApplication()
            },
        ) {
            MainScreen(window)
        }
    }
}