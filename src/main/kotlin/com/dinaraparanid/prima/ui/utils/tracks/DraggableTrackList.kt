package com.dinaraparanid.prima.ui.utils.tracks

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.dinaraparanid.prima.entities.Track
import com.dinaraparanid.prima.utils.extensions.move
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@Composable
inline fun DraggableTrackList(
    tracksState: SnapshotStateList<Track>,
    filteredTracksState: SnapshotStateList<Track>,
    currentTrackState: MutableState<Track?>,
    isPlayingState: MutableState<Boolean>,
    isPlayingCoverLoadedState: MutableState<Boolean>,
    playbackPositionState: MutableState<Float>,
    loopingState: MutableState<Int>,
    isPlaybackTrackDraggingState: State<Boolean>,
    speedState: State<Float>,
    isLikedState: MutableState<Boolean>,
    crossinline onTrackDragged: suspend (List<Track>) -> Unit,
    trackBar: @Composable (SnapshotStateList<Track>, SnapshotStateList<Track>, LazyListState) -> Unit,
) {
    val listState = rememberLazyListState()
    val positionState = remember { mutableStateOf<Float?>(null) }
    val draggedItemIndexState = remember { mutableStateOf<Int?>(null) }
    val isDraggingState = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
        trackBar(tracksState, filteredTracksState, listState)

        val indexWithOffset = derivedStateOf {
            draggedItemIndexState
                .value
                ?.let {
                    listState
                        .layoutInfo
                        .visibleItemsInfo
                        .getOrNull(it - listState.firstVisibleItemIndex)
                }
                ?.let { it.index to ((positionState.value ?: 0F) - it.offset - it.size / 2F) }
        }

        coroutineScope.launch {
            snapshotFlow { listState.layoutInfo }
                .combine(snapshotFlow { positionState.value }.distinctUntilChanged()) { state, pos ->
                    pos?.let { draggedCenter ->
                        state.visibleItemsInfo.minByOrNull {
                            (draggedCenter - (it.offset + it.size / 2F)).absoluteValue
                        }
                    }?.index
                }
                .collect { near ->
                    if (isDraggingState.value) draggedItemIndexState.value = when {
                        near == null -> null
                        draggedItemIndexState.value == null -> near
                        else -> near.also { filteredTracksState.move(draggedItemIndexState.value!!, it) }
                    }
                }
        }

        LazyColumn(
            contentPadding = PaddingValues(top = 20.dp, bottom = 180.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp),
            state = listState,
            modifier = Modifier.fillMaxWidth().wrapContentHeight().pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDrag = { change, offset ->
                        change.consumeAllChanges()
                        positionState.value = positionState.value?.plus(offset.y)
                    },
                    onDragStart = { offset ->
                        isDraggingState.value = true

                        listState
                            .layoutInfo
                            .visibleItemsInfo
                            .firstOrNull { offset.y.toInt() in it.offset..it.offset + it.size }
                            ?.also { positionState.value = it.offset + it.size / 2F }
                    },
                    onDragEnd = {
                        isDraggingState.value = false
                        draggedItemIndexState.value = null
                        coroutineScope.launch { onTrackDragged(filteredTracksState) }
                    },
                    onDragCancel = {
                        isDraggingState.value = false
                        draggedItemIndexState.value = null
                        coroutineScope.launch { onTrackDragged(filteredTracksState) }
                    }
                )
            }
        ) {
            itemsIndexed(filteredTracksState) { ind, _ ->
                val offset = remember {
                    derivedStateOf { indexWithOffset.value?.takeIf { it.first == ind }?.second }
                }

                Box(
                    Modifier
                        .zIndex(offset.value?.let { 1F } ?: 0F)
                        .graphicsLayer { translationY = offset.value ?: 0F }
                ) {
                    TrackItem(
                        filteredTracksState,
                        ind,
                        currentTrackState,
                        isPlayingState,
                        isPlayingCoverLoadedState,
                        playbackPositionState,
                        loopingState,
                        tracksState,
                        isPlaybackTrackDraggingState,
                        speedState,
                        isLikedState
                    )
                }
            }
        }
    }
}