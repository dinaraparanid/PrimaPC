package com.paranid5.prima.presentation.ui.tracks

import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.sp
import com.paranid5.prima.domain.localization.LocalizedString

@Composable
fun SearchByParamsMenu(isPopupMenuExpandedState: MutableState<Boolean>) = DropdownMenu(
    expanded = isPopupMenuExpandedState.value,
    onDismissRequest = { isPopupMenuExpandedState.value = false }
) {
    SearchByParamsMenuItem(Params.TrackSearchOrder.TITLE, Localization.byTitle)
    SearchByParamsMenuItem(Params.TrackSearchOrder.ARTIST, Localization.byArtist)
    SearchByParamsMenuItem(Params.TrackSearchOrder.ALBUM, Localization.byAlbum)
}

@Composable
private fun SearchByParamsMenuItem(order: Params.TrackSearchOrder, title: LocalizedString) {
    val isCheckedState = remember { mutableStateOf(order in Params.trackSearchOrders) }

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