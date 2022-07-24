package com.dinaraparanid.prima.ui.tracks

import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.sp
import com.dinaraparanid.prima.utils.Params
import com.dinaraparanid.prima.utils.localization.Localization
import com.dinaraparanid.prima.utils.localization.LocalizedString

@Composable
fun SearchByParamsMenu(isPopupMenuExpandedState: MutableState<Boolean>) = DropdownMenu(
    expanded = isPopupMenuExpandedState.value,
    onDismissRequest = { isPopupMenuExpandedState.value = false }
) {
    SearchByParamsMenuItem(Params.TracksSearchOrder.TITLE, Localization.byTitle)
    SearchByParamsMenuItem(Params.TracksSearchOrder.ARTIST, Localization.byArtist)
    SearchByParamsMenuItem(Params.TracksSearchOrder.ALBUM, Localization.byAlbum)
}

@Composable
private fun SearchByParamsMenuItem(order: Params.TracksSearchOrder, title: LocalizedString) {
    val isCheckedState = remember { mutableStateOf(order in Params.tracksSearchOrder) }

    DropdownMenuItem(
        onClick = {
            Params.updateTrackSearchOrder(order)
            isCheckedState.value = !isCheckedState.value
        }
    ) {
        Checkbox(
            checked = isCheckedState.value,
            onCheckedChange = {
                Params.updateTrackSearchOrder(order)
                isCheckedState.value = !isCheckedState.value
            },
            colors = CheckboxDefaults.colors(
                checkedColor = Params.primaryColor,
                checkmarkColor = Params.secondaryColor,
                uncheckedColor = Params.secondaryAlternativeColor,
                disabledColor = Params.secondaryAlternativeColor
            )
        )

        Text(text = title.resource, fontSize = 14.sp, color = Params.secondaryAlternativeColor)
    }
}