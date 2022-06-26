package com.dinaraparanid.prima.ui.tracks.appbar

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dinaraparanid.prima.entities.Track
import com.dinaraparanid.prima.utils.Params
import com.dinaraparanid.prima.utils.localization.Localization

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
                        when (Params.tracksSearchOrder) {
                            Params.TracksSearchOrder.TITLE -> it.title?.lowercase()?.contains(query) == true

                            Params.TracksSearchOrder.ARTIST -> it.artist?.lowercase()?.contains(query) == true

                            Params.TracksSearchOrder.ALBUM -> it.album?.lowercase()?.contains(query) == true

                            Params.TracksSearchOrder.ALL -> it.title?.lowercase()?.contains(query) == true
                                    || it.artist?.lowercase()?.contains(query) == true
                                    || it.album?.lowercase()?.contains(query) == true
                        }
                    })
                }
            },
        )

        else -> DefaultAppBar(isSearchingState)
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
        modifier = Modifier.fillMaxWidth().height(60.dp),
        color = MaterialTheme.colors.primary
    ) {
        val textState = remember { mutableStateOf("") }

        TextField(
            modifier = Modifier.fillMaxSize(),
            value = textState.value,
            onValueChange = { onTextChanged(it) },
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
private fun DefaultAppBar(isSearchingState: MutableState<Boolean>) = TopAppBar(
    modifier = Modifier.fillMaxWidth().height(60.dp),
    elevation = 10.dp
) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(60.dp),
        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
        color = Params.primaryColor,
        elevation = 10.dp
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(60.dp)) {
            Spacer(modifier = Modifier.width(5.dp).fillMaxHeight())

            Button(
                onClick = {
                    // TODO: Show menu
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
                elevation = null,
                modifier = Modifier.width(50.dp).height(50.dp).align(Alignment.CenterVertically),
            ) {
                Image(
                    painter = painterResource("images/burger_button_icon.png"),
                    contentDescription = Localization.trackCover.resource,
                    modifier = Modifier.fillMaxSize(),
                    colorFilter = ColorFilter.tint(Params.secondaryAlternativeColor),
                    contentScale = ContentScale.Inside
                )
            }

            Spacer(modifier = Modifier.width(20.dp).fillMaxHeight())

            Text(
                text = Params.mainLabel,
                fontSize = 20.sp,
                color = Params.secondaryAlternativeColor,
                modifier = Modifier.align(Alignment.CenterVertically)
            )

            Spacer(modifier = Modifier.weight(1F))

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
        }
    }
}