package com.dinaraparanid.prima

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.dinaraparanid.prima.rust.RustLibs
import org.jaudiotagger.audio.AudioFileIO

@Composable
@Preview
fun App() {
    var text by remember { mutableStateOf("Press Button!") }

    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    text = RustLibs.hello("Rust")
                }) {
                Text(text)
            }
        }
    }
}

fun main() {
    AudioFileIO

    application {
        Window(onCloseRequest = ::exitApplication) {
            App()
        }
    }
}