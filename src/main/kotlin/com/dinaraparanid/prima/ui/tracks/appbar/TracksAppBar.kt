package com.dinaraparanid.prima.ui.tracks.appbar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dinaraparanid.prima.entities.Track
import com.dinaraparanid.prima.ui.tracks.scanTracks
import com.dinaraparanid.prima.utils.Params
import com.dinaraparanid.prima.utils.localization.Localization
import com.dinaraparanid.prima.utils.localization.LocalizedString
import kotlinx.coroutines.launch

@Composable
fun TracksAppBar(tracksState: SnapshotStateList<Track>, filteredTracksState: SnapshotStateList<Track>) {
    val isSearchingState = remember { mutableStateOf(false) }

    when {
        isSearchingState.value -> SearchAppBar(
            tracksState,
            filteredTracksState,
            isSearchingState,
            onTextChanged = { q ->
                val query = q.lowercase()

                filteredTracksState.run {
                    clear()
                    addAll(tracksState.filter {
                        val ord = Params.tracksSearchOrder

                        if (Params.TracksSearchOrder.TITLE in ord && it.title?.lowercase()?.contains(query) == true)
                            return@filter true

                        if (Params.TracksSearchOrder.ARTIST in ord && it.artist?.lowercase()?.contains(query) == true)
                            return@filter true

                        Params.TracksSearchOrder.ALBUM in ord && it.album?.lowercase()?.contains(query) == true
                    })
                }
            },
        )

        else -> DefaultAppBar(isSearchingState, tracksState, filteredTracksState)
    }
}

@Composable
private fun SearchAppBar(
    tracksState: SnapshotStateList<Track>,
    filteredTracksState: SnapshotStateList<Track>,
    isSearchingState: MutableState<Boolean>,
    onTextChanged: (String) -> Unit,
) = TopAppBar(
    modifier = Modifier.fillMaxWidth().height(60.dp),
    elevation = 10.dp
) {
    Surface(
        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
        color = MaterialTheme.colors.primary,
        elevation = 10.dp
    ) {
        val textState = remember { mutableStateOf("") }

        TextField(
            modifier = Modifier.fillMaxSize(),
            value = textState.value,
            onValueChange = {
                textState.value = it
                onTextChanged(it)
            },
            placeholder = {
                Text(text = "...", color = Params.secondaryAlternativeColor.copy(alpha = ContentAlpha.medium))
            },
            singleLine = true,
            leadingIcon = {
                Icon(
                    modifier = Modifier.alpha(ContentAlpha.medium).width(30.dp).height(30.dp),
                    painter = painterResource("images/search_icon.png"),
                    contentDescription = Localization.search.resource,
                    tint = Params.secondaryAlternativeColor
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        when {
                            textState.value.isNotEmpty() -> {
                                textState.value = ""
                                onTextChanged("")
                            }

                            else -> {
                                filteredTracksState.run {
                                    clear()
                                    addAll(tracksState)
                                }

                                isSearchingState.value = false
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = Localization.cancel.resource,
                        tint = Color.White
                    )
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onTextChanged(textState.value) }),
            colors = TextFieldDefaults.textFieldColors(
                textColor = Params.secondaryAlternativeColor,
                backgroundColor = Color.Transparent,
                cursorColor = Color.White.copy(alpha = ContentAlpha.medium)
            )
        )
    }
}

@Composable
private fun DefaultAppBar(
    isSearchingState: MutableState<Boolean>,
    tracksState: SnapshotStateList<Track>,
    filteredTracksState: SnapshotStateList<Track>
) = TopAppBar(
    modifier = Modifier.fillMaxWidth().height(60.dp),
    elevation = 10.dp
) {
    Surface(
        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
        color = Params.primaryColor,
        elevation = 10.dp
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.width(40.dp).fillMaxHeight())

            Text(
                text = Params.mainLabel,
                fontSize = 22.sp,
                color = Params.secondaryAlternativeColor,
                modifier = Modifier.align(Alignment.CenterVertically)
            )

            Spacer(modifier = Modifier.weight(1F))

            Row(modifier = Modifier.align(Alignment.CenterVertically)) {
                val coroutineScope = rememberCoroutineScope()

                IconButton(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    onClick = { isSearchingState.value = true }
                ) {
                    Icon(
                        modifier = Modifier.alpha(ContentAlpha.medium).width(30.dp).height(30.dp),
                        painter = painterResource("images/search_icon.png"),
                        contentDescription = Localization.search.resource,
                        tint = Params.secondaryAlternativeColor
                    )
                }

                val isPopupMenuExpandedState = remember { mutableStateOf(false) }

                IconButton(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    onClick = { isPopupMenuExpandedState.value = true }
                ) {
                    Icon(
                        modifier = Modifier.alpha(ContentAlpha.medium).width(30.dp).height(30.dp),
                        painter = painterResource("images/param.png"),
                        contentDescription = Localization.search.resource,
                        tint = Params.secondaryAlternativeColor
                    )
                }

                SearchByParamsMenu(isPopupMenuExpandedState)

                IconButton(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    onClick = { coroutineScope.launch { scanTracks(tracksState, filteredTracksState) } }
                ) {
                    Icon(
                        modifier = Modifier.alpha(ContentAlpha.medium).width(30.dp).height(30.dp),
                        painter = painterResource("images/scanner_icon.png"),
                        contentDescription = Localization.search.resource,
                        tint = Params.secondaryAlternativeColor
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchByParamsMenu(isPopupMenuExpandedState: MutableState<Boolean>) = DropdownMenu(
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