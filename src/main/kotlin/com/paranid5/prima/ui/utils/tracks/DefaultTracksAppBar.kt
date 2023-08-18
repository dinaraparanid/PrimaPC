package com.dinaraparanid.prima.ui.utils.tracks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dinaraparanid.prima.entities.Track
import com.dinaraparanid.prima.ui.utils.SearchAppBar
import com.dinaraparanid.prima.utils.Params
import com.dinaraparanid.prima.utils.localization.Localization

@Composable
fun DefaultTracksAppBar(
    tracksState: SnapshotStateList<Track>,
    filteredTracksState: SnapshotStateList<Track>,
    mainLabel: String
) {
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

        else -> DefaultAppBar(isSearchingState, mainLabel)
    }
}

@Composable
private fun DefaultAppBar(isSearchingState: MutableState<Boolean>, mainLabel: String) = TopAppBar(
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
                text = mainLabel,
                fontSize = 22.sp,
                color = Params.secondaryAlternativeColor,
                modifier = Modifier.align(Alignment.CenterVertically)
            )

            Spacer(modifier = Modifier.weight(1F))

            Row(modifier = Modifier.align(Alignment.CenterVertically)) {
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
            }
        }
    }
}