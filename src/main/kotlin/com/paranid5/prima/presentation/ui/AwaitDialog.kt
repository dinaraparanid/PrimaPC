package com.paranid5.prima.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import com.paranid5.prima.domain.StorageHandler
import org.koin.compose.koinInject

@Composable
fun AwaitDialog(
    isDialogShownState: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject(),
) {
    val lang by storageHandler.languageState.collectAsState()
    val secondaryColor by storageHandler.secondaryColorState.collectAsState()
    val secondaryAlternativeColor by storageHandler.secondaryAlternativeColorState.collectAsState()

    val message = lang.loading

    Window(
        title = message,
        visible = isDialogShownState.value,
        resizable = false,
        focusable = true,
        alwaysOnTop = true,
        state = WindowState(
            width = 300.dp,
            height = 200.dp,
            position = WindowPosition(Alignment.Center)
        ),
        onCloseRequest = { isDialogShownState.value = false },
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .width(300.dp)
                .height(200.dp)
                .background(secondaryColor)
        ) {
            Column {
                CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))

                Spacer(Modifier.height(15.dp))

                Text(
                    text = message,
                    color = secondaryAlternativeColor,
                    fontSize = 16.sp
                )
            }
        }
    }
}