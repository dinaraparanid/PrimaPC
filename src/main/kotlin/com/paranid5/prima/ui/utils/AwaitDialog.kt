package com.dinaraparanid.prima.ui.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import com.dinaraparanid.prima.utils.Params
import com.dinaraparanid.prima.utils.localization.Localization
import com.dinaraparanid.prima.utils.localization.LocalizedString

@Composable
fun AwaitDialog(isDialogShownState: MutableState<Boolean>, message: LocalizedString = Localization.loading) = Window(
    title = message.resource,
    visible = isDialogShownState.value,
    resizable = false,
    focusable = true,
    alwaysOnTop = true,
    state = WindowState(width = 300.dp, height = 200.dp, position = WindowPosition(Alignment.Center)),
    onCloseRequest = { isDialogShownState.value = false },
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .width(300.dp)
            .height(200.dp)
            .background(Params.secondaryColor)
    ) {
        Column {
            CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(15.dp))
            Text(text = message.resource, color = Params.secondaryAlternativeColor, fontSize = 16.sp)
        }
    }
}