package com.paranid5.prima.presentation.ui.tracks

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paranid5.prima.data.Track
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(ExperimentalFoundationApi::class)
@Composable
inline fun TrackList(
    tracksState: MutableStateFlow<List<Track>>,
    filteredTracksState: MutableStateFlow<List<Track>>,
    modifier: Modifier = Modifier,
    trackBar: TrackBarTemplate
) {
    val listState = rememberLazyListState()
    val tracksOnScreen by filteredTracksState.collectAsState()

    Column(modifier.fillMaxWidth().wrapContentHeight()) {
        trackBar(tracksState, filteredTracksState, listState)

        LazyColumn(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            contentPadding = PaddingValues(top = 20.dp, bottom = 180.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp),
            state = listState
        ) {
            itemsIndexed(tracksOnScreen, key = { _, track -> track }) { ind, _ ->
                TrackItem(
                    tracksOnScreen = tracksOnScreen,
                    index = ind,
                    allTracksState = tracksState,
                    modifier = Modifier.animateItemPlacement(
                        animationSpec = tween(durationMillis = 300)
                    )
                )
            }
        }
    }
}