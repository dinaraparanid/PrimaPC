package com.dinaraparanid.prima

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.dinaraparanid.prima.rust.RustLibs
import com.dinaraparanid.prima.rust.src.ui.Prima

fun main() {
    RustLibs.initRust()

    application {
        Window(
            title = "Prima",
            icon = painterResource("images/app_icon.png"),
            onCloseRequest = ::exitApplication,
        ) {
            Prima()
        }
    }
}