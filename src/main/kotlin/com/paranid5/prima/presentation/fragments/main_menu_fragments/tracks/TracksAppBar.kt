package com.paranid5.prima.presentation.fragments.main_menu_fragments.tracks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paranid5.prima.data.Track
import com.paranid5.prima.presentation.ui.SearchAppBar
import com.paranid5.prima.presentation.ui.tracks.SearchByParamsMenu
import kotlinx.coroutines.launch

@Composable
fun TracksAppBar(allTracksState: SnapshotStateList<Track>, filteredAllTracksState: SnapshotStateList<Track>) {
    val isSearchingState = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    when {
        isSearchingState.value -> SearchAppBar(
            allTracksState,
            filteredAllTracksState,
            isSearchingState,
            onTextChanged = { q ->
                val query = q.lowercase()

                filteredAllTracksState.run {
                    clear()
                    addAll(allTracksState.filter {
                        val ord = Params.trackSearchOrders

                        if (Params.TrackSearchOrder.TITLE in ord && it.title?.lowercase()?.contains(query) == true)
                            return@filter true

                        if (Params.TrackSearchOrder.ARTIST in ord && it.artist?.lowercase()?.contains(query) == true)
                            return@filter true

                        Params.TrackSearchOrder.ALBUM in ord && it.album?.lowercase()?.contains(query) == true
                    })
                }
            },
        )

        else -> DefaultAppBar(isSearchingState, allTracksState, filteredAllTracksState)
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
                text = Localization.tracks.resource,
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