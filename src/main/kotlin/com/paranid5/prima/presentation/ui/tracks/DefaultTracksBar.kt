package com.paranid5.prima.presentation.ui.tracks

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paranid5.prima.data.Track
import com.paranid5.prima.domain.StorageHandler
import com.paranid5.prima.domain.scanTracks
import com.paranid5.prima.rust.RustLibs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject

@Composable
fun DefaultTracksBar(
    tracksState: MutableStateFlow<List<Track>>,
    filteredTracksState: MutableStateFlow<List<Track>>,
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val trackOrdState = remember {
        mutableStateListOf<Int>()
    }

    val isPopupMenuExpandedState = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        trackOrdState.addAll(
            withContext(Dispatchers.IO) {
                RustLibs.getTrackOrderBlocking().toList()
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxWidth().height(60.dp),
        drawerElevation = 10.dp
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
            elevation = 30.dp
        ) {
            Row(Modifier.fillMaxWidth().height(60.dp)) {
                ShuffleButton(
                    filteredTracksState = filteredTracksState,
                    listState = listState
                )

                TracksNumberLabel(
                    filteredTracksState = filteredTracksState,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )

                Spacer(Modifier.weight(1F))

                TrackOrderBar(
                    tracksState = tracksState,
                    filteredTracksState = filteredTracksState,
                    isPopupMenuExpandedState = isPopupMenuExpandedState,
                    trackOrdState = trackOrdState,
                    modifier = Modifier.padding(end = 20.dp)
                )
            }
        }
    }
}

@Composable
private fun ShuffleButton(
    filteredTracksState: MutableStateFlow<List<Track>>,
    listState: LazyListState,
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject()
) {
    val coroutineScope = rememberCoroutineScope()
    val lang by storageHandler.languageState.collectAsState()
    val primaryColor by storageHandler.primaryColorState.collectAsState()

    Button(
        onClick = {
            filteredTracksState.update { it.shuffled() }
            coroutineScope.launch { listState.scrollToItem(0) }
        },
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
        elevation = null,
        modifier = modifier.width(70.dp).height(60.dp),
    ) {
        Image(
            painter = painterResource("images/shuffle.png"),
            contentDescription = lang.trackCover,
            modifier = Modifier.fillMaxSize(),
            colorFilter = ColorFilter.tint(primaryColor),
            contentScale = ContentScale.FillWidth
        )
    }
}

@Composable
private fun TracksNumberLabel(
    filteredTracksState: MutableStateFlow<List<Track>>,
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject()
) {
    val lang by storageHandler.languageState.collectAsState()
    val primaryColor by storageHandler.primaryColorState.collectAsState()

    val tracksOnScreen by filteredTracksState.collectAsState()
    val tracksNumber by remember { derivedStateOf { tracksOnScreen.size } }

    Text(
        text = "${lang.tracks}: $tracksNumber",
        fontSize = 20.sp,
        color = primaryColor,
        modifier = modifier
    )
}

@Composable
private fun TrackOrderBar(
    tracksState: MutableStateFlow<List<Track>>,
    filteredTracksState: MutableStateFlow<List<Track>>,
    isPopupMenuExpandedState: MutableState<Boolean>,
    trackOrdState: SnapshotStateList<Int>,
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject()
) {
    val lang by storageHandler.languageState.collectAsState()
    val primaryColor by storageHandler.primaryColorState.collectAsState()

    if (trackOrdState.isEmpty())
        return

    Row(modifier.fillMaxHeight().padding(end = 20.dp)) {
        Button(
            onClick = { isPopupMenuExpandedState.value = true },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
            elevation = null,
            modifier = Modifier.align(Alignment.CenterVertically),
        ) {
            Image(
                painter = painterResource("images/arrow_down.png"),
                contentDescription = lang.trackOrder,
                modifier = Modifier.height(20.dp),
                colorFilter = ColorFilter.tint(primaryColor),
                contentScale = ContentScale.Inside
            )
        }

        TrackOrderMenu(
            isPopupMenuExpandedState = isPopupMenuExpandedState,
            trackOrdState,
            tracksState,
            filteredTracksState
        )

        Spacer(modifier = Modifier.fillMaxHeight().width(5.dp))

        Text(
            modifier = Modifier.align(Alignment.CenterVertically),
            text = when (trackOrdState.first()) {
                0 -> lang.byTitle
                1 -> lang.byArtist
                2 -> lang.byAlbum
                else -> lang.byDate
            },
            color = primaryColor,
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.fillMaxHeight().width(5.dp))

        Text(
            modifier = Modifier.align(Alignment.CenterVertically),
            text = when (trackOrdState.last()) {
                4 -> lang.ascending
                else -> lang.descending
            },
            color = primaryColor,
            fontSize = 20.sp
        )
    }
}

@Composable
private fun TrackOrderMenu(
    isPopupMenuExpandedState: MutableState<Boolean>,
    trackOrdState: SnapshotStateList<Int>,
    tracksState: MutableStateFlow<List<Track>>,
    filteredTracksState: MutableStateFlow<List<Track>>,
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject()
) {
    val lang by storageHandler.languageState.collectAsState()

    DropdownMenu(
        modifier = modifier,
        expanded = isPopupMenuExpandedState.value,
        onDismissRequest = { isPopupMenuExpandedState.value = false }
    ) {
        Row {
            Column(modifier = Modifier.weight(1F)) {
                TrackOrderMenuItem(0, lang.byTitle, trackOrdState, tracksState, filteredTracksState)
                TrackOrderMenuItem(1, lang.byArtist, trackOrdState, tracksState, filteredTracksState)
                TrackOrderMenuItem(2, lang.byAlbum, trackOrdState, tracksState, filteredTracksState)
                TrackOrderMenuItem(3, lang.byDate, trackOrdState, tracksState, filteredTracksState)
                TrackOrderMenuItem(4, lang.byNumberInAlbum, trackOrdState, tracksState, filteredTracksState)
            }

            Column(modifier = Modifier.weight(1F)) {
                TrackOrderMenuItem(5, lang.ascending, trackOrdState, tracksState, filteredTracksState)
                TrackOrderMenuItem(6, lang.descending, trackOrdState, tracksState, filteredTracksState)
            }
        }
    }
}

@Composable
private fun TrackOrderMenuItem(
    order: Int,
    title: String,
    trackOrdState: SnapshotStateList<Int>,
    tracksState: MutableStateFlow<List<Track>>,
    filteredTracksState: MutableStateFlow<List<Track>>,
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject()
) {
    val primaryColor by storageHandler.primaryColorState.collectAsState()
    val secondaryColor by storageHandler.secondaryColorState.collectAsState()
    val secondaryAlternativeColor by storageHandler.secondaryAlternativeColorState.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val isChecked by remember { derivedStateOf { order in trackOrdState } }

    DropdownMenuItem(
        modifier = modifier,
        onClick = {
            if (!isChecked) {
                when (order) {
                    in 0..4 -> trackOrdState[0] = order
                    else -> trackOrdState[1] = order
                }

                coroutineScope.launch {
                    RustLibs.setTrackOrderBlocking(trackOrdState[0], trackOrdState[1])
                    scanTracks(tracksState, filteredTracksState)
                }
            }
        }
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = {
                if (!isChecked) {
                    when (order) {
                        in 0..4 -> trackOrdState[0] = order
                        else -> trackOrdState[1] = order
                    }

                    RustLibs.setTrackOrderBlocking(trackOrdState[0], trackOrdState[1])
                    coroutineScope.launch { scanTracks(tracksState, filteredTracksState) }
                }
            },
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