package com.dinaraparanid.prima.ui.tracks

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
import com.dinaraparanid.prima.entities.Track
import com.dinaraparanid.prima.rust.RustLibs
import com.dinaraparanid.prima.utils.Params
import com.dinaraparanid.prima.utils.localization.Localization
import com.dinaraparanid.prima.utils.localization.LocalizedString
import kotlinx.coroutines.launch

@Composable
fun TracksBar(
    tracksState: SnapshotStateList<Track>,
    filteredTracksState: SnapshotStateList<Track>,
    listState: LazyListState
) {
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxWidth().height(60.dp),
        drawerElevation = 10.dp
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
            elevation = 30.dp
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                elevation = 10.dp
            ) {
                Row(modifier = Modifier.fillMaxWidth().height(60.dp)) {
                    Button(
                        onClick = {
                            filteredTracksState.shuffle()
                            coroutineScope.launch { listState.scrollToItem(0) }
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
                        elevation = null,
                        modifier = Modifier.width(70.dp).height(60.dp),
                    ) {
                        Image(
                            painter = painterResource("images/shuffle.png"),
                            contentDescription = Localization.trackCover.resource,
                            modifier = Modifier.fillMaxSize(),
                            colorFilter = ColorFilter.tint(Params.primaryColor),
                            contentScale = ContentScale.FillWidth
                        )
                    }

                    Text(
                        text = "${Localization.tracks.resource}: ${filteredTracksState.size}",
                        fontSize = 20.sp,
                        color = Params.primaryColor,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )

                    Spacer(modifier = Modifier.weight(1F))

                    Row(modifier = Modifier.fillMaxHeight().padding(end = 20.dp)) {
                        val trackOdrState = remember {
                            mutableStateListOf(*RustLibs.getTrackOrder().toTypedArray())
                        }

                        val isPopupMenuExpandedState = remember { mutableStateOf(false) }

                        Button(
                            onClick = { isPopupMenuExpandedState.value = true },
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
                            elevation = null,
                            modifier = Modifier.align(Alignment.CenterVertically),
                        ) {
                            Image(
                                painter = painterResource("images/arrow_down.png"),
                                contentDescription = Localization.trackOrder.resource,
                                modifier = Modifier.height(20.dp),
                                colorFilter = ColorFilter.tint(Params.primaryColor),
                                contentScale = ContentScale.Inside
                            )
                        }

                        TrackOrderMenu(isPopupMenuExpandedState, trackOdrState, tracksState, filteredTracksState)

                        Spacer(modifier = Modifier.fillMaxHeight().width(5.dp))

                        Text(
                            modifier = Modifier.align(Alignment.CenterVertically),
                            text = when (trackOdrState.first()) {
                                0 -> Localization.byTitle.resource
                                1 -> Localization.byArtist.resource
                                2 -> Localization.byAlbum.resource
                                else -> Localization.byDate.resource
                            },
                            color = Params.primaryColor,
                            fontSize = 20.sp
                        )

                        Spacer(modifier = Modifier.fillMaxHeight().width(5.dp))

                        Text(
                            modifier = Modifier.align(Alignment.CenterVertically),
                            text = when (trackOdrState.last()) {
                                4 -> Localization.ascending.resource
                                else -> Localization.descending.resource
                            },
                            color = Params.primaryColor,
                            fontSize = 20.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackOrderMenu(
    isPopupMenuExpandedState: MutableState<Boolean>,
    trackOrdState: SnapshotStateList<Int>,
    tracksState: SnapshotStateList<Track>,
    filteredTracksState: SnapshotStateList<Track>
) =
    DropdownMenu(
        expanded = isPopupMenuExpandedState.value,
        onDismissRequest = { isPopupMenuExpandedState.value = false }
    ) {
        Row {
            Column(modifier = Modifier.weight(1F)) {
                TrackOrderMenuItem(0, Localization.byTitle, trackOrdState, tracksState, filteredTracksState)
                TrackOrderMenuItem(1, Localization.byArtist, trackOrdState, tracksState, filteredTracksState)
                TrackOrderMenuItem(2, Localization.byAlbum, trackOrdState, tracksState, filteredTracksState)
                TrackOrderMenuItem(3, Localization.byDate, trackOrdState, tracksState, filteredTracksState)
                TrackOrderMenuItem(4, Localization.byNumberInAlbum, trackOrdState, tracksState, filteredTracksState)
            }

            Column(modifier = Modifier.weight(1F)) {
                TrackOrderMenuItem(5, Localization.ascending, trackOrdState, tracksState, filteredTracksState)
                TrackOrderMenuItem(6, Localization.descending, trackOrdState, tracksState, filteredTracksState)
            }
        }
    }

@Composable
private fun TrackOrderMenuItem(
    order: Int,
    title: LocalizedString,
    trackOrdState: SnapshotStateList<Int>,
    tracksState: SnapshotStateList<Track>,
    filteredTracksState: SnapshotStateList<Track>
) {
    val isChecked = order in trackOrdState
    val coroutineScope = rememberCoroutineScope()

    DropdownMenuItem(
        onClick = {
            if (!isChecked) {
                when (order) {
                    in 0..4 -> trackOrdState[0] = order
                    else -> trackOrdState[1] = order
                }

                RustLibs.setTrackOrder(trackOrdState[0], trackOrdState[1])
                coroutineScope.launch { scanTracks(tracksState, filteredTracksState) }
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

                    RustLibs.setTrackOrder(trackOrdState[0], trackOrdState[1])
                    coroutineScope.launch { scanTracks(tracksState, filteredTracksState) }
                }
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