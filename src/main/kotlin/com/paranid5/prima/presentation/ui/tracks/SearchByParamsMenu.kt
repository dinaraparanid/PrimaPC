package com.paranid5.prima.presentation.ui.tracks

import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.paranid5.prima.data.TrackSearchOrder
import com.paranid5.prima.domain.StorageHandler
import org.koin.compose.koinInject

@Composable
fun SearchByParamsMenu(
    isPopupMenuExpandedState: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject()
) {
    val lang by storageHandler.languageState.collectAsState()

    DropdownMenu(
        modifier = modifier,
        expanded = isPopupMenuExpandedState.value,
        onDismissRequest = { isPopupMenuExpandedState.value = false }
    ) {
        SearchByParamsMenuItem(TrackSearchOrder.TITLE, lang.byTitle)
        SearchByParamsMenuItem(TrackSearchOrder.ARTIST, lang.byArtist)
        SearchByParamsMenuItem(TrackSearchOrder.ALBUM, lang.byAlbum)
    }
}

@Composable
private fun SearchByParamsMenuItem(
    order: TrackSearchOrder,
    title: String,
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject()
) {
    val primaryColor by storageHandler.primaryColorState.collectAsState()
    val secondaryColor by storageHandler.secondaryColorState.collectAsState()
    val secondaryAlternativeColor by storageHandler.secondaryAlternativeColorState.collectAsState()

    val trackSearchOrder by storageHandler.trackSearchOrderState.collectAsState()
    val isChecked by remember { derivedStateOf { order in trackSearchOrder } }

    DropdownMenuItem(
        modifier = modifier,
        onClick = { storageHandler.storeTrackSearchOrder(order) }
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = { storageHandler.storeTrackSearchOrder(order) },
            colors = CheckboxDefaults.colors(
                checkedColor = primaryColor,
                checkmarkColor = secondaryColor,
                uncheckedColor = secondaryAlternativeColor,
                disabledColor = secondaryAlternativeColor
            )
        )

        Text(text = title, fontSize = 14.sp, color = secondaryAlternativeColor)
    }
}