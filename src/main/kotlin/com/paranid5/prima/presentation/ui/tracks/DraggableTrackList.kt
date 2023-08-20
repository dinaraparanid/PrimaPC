package com.paranid5.prima.presentation.ui.tracks

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.paranid5.prima.data.Track
import com.paranid5.prima.domain.extensions.move
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal inline fun DraggableTrackList(
    tracksState: MutableStateFlow<List<Track>>,
    filteredTracksState: MutableStateFlow<List<Track>>,
    crossinline onTrackDragged: suspend (List<Track>) -> Unit,
    modifier: Modifier = Modifier,
    trackBar: TrackBarTemplate,
) {
    val filteredTracks by filteredTracksState.collectAsState()

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val positionState = remember { mutableStateOf<Float?>(null) }
    val draggedItemIndexState = remember { mutableStateOf<Int?>(null) }
    val isDraggingState = remember { mutableStateOf(false) }

    val indexWithOffset by remember {
        derivedStateOf {
            draggedItemIndexState
                .value
                ?.let {
                    listState
                        .layoutInfo
                        .visibleItemsInfo
                        .getOrNull(it - listState.firstVisibleItemIndex)
                }
                ?.let {
                    val offset = ((positionState.value ?: 0F) - it.offset - it.size / 2F)
                    it.index to offset
                }
        }
    }

    LaunchDraggingHandling(
        filteredTracksState = filteredTracksState,
        listState = listState,
        positionState = positionState,
        isDraggingState = isDraggingState,
        draggedItemIndexState = draggedItemIndexState
    )

    Column(modifier.fillMaxWidth().wrapContentHeight()) {
        trackBar(tracksState, filteredTracksState, listState)

        LazyColumn(
            contentPadding = PaddingValues(top = 20.dp, bottom = 180.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp),
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .handleTracksMovement(
                    filteredTracks = filteredTracks,
                    listState = listState,
                    positionState = positionState,
                    isDraggingState = isDraggingState,
                    draggedItemIndexState = draggedItemIndexState,
                    coroutineScope = coroutineScope,
                    onTrackDragged = onTrackDragged
                )
        ) {
            itemsIndexed(filteredTracks) { ind, _ ->
                val offset by remember {
                    derivedStateOf { indexWithOffset?.takeIf { it.first == ind }?.second }
                }

                Box(
                    Modifier
                        .zIndex(offset?.let { 1F } ?: 0F)
                        .graphicsLayer { translationY = offset ?: 0F }
                ) {
                    TrackItem(
                        tracksOnScreen = filteredTracks,
                        index = ind,
                        allTracksState = tracksState,
                        modifier = Modifier.animateItemPlacement(animationSpec = tween(durationMillis = 300))
                    )
                }
            }
        }
    }
}

@Composable
private fun LaunchDraggingHandling(
    filteredTracksState: MutableStateFlow<List<Track>>,
    listState: LazyListState,
    positionState: MutableState<Float?>,
    isDraggingState: MutableState<Boolean>,
    draggedItemIndexState: MutableState<Int?>
) {
    val isDragging by isDraggingState

    val listFlow = snapshotFlow { listState.layoutInfo }
    val positionFlow = snapshotFlow { positionState.value }.distinctUntilChanged()

    LaunchedEffect(Unit) {
        listFlow
            .combine(positionFlow) { listState, pos ->
                pos
                    ?.let { draggedCenter ->
                        listState.visibleItemsInfo.minByOrNull {
                            (draggedCenter - (it.offset + it.size / 2F)).absoluteValue
                        }
                    }
                    ?.index
            }
            .collect { near ->
                if (isDragging) draggedItemIndexState.value = when {
                    near == null -> null

                    draggedItemIndexState.value == null -> near

                    else -> near.also { toInd ->
                        filteredTracksState.update {
                            it.toMutableList().apply {
                                move(fromIdx = draggedItemIndexState.value!!, toIdx = toInd)
                            }
                        }
                    }
                }
            }
    }
}

private inline fun Modifier.handleTracksMovement(
    filteredTracks: List<Track>,
    listState: LazyListState,
    positionState: MutableState<Float?>,
    isDraggingState: MutableState<Boolean>,
    draggedItemIndexState: MutableState<Int?>,
    coroutineScope: CoroutineScope,
    crossinline onTrackDragged: suspend (List<Track>) -> Unit,
) = pointerInput(Unit) {
    detectDragGesturesAfterLongPress(
        onDrag = { change, offset ->
            change.consume()
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
            coroutineScope.launch { onTrackDragged(filteredTracks) }
        },
        onDragCancel = {
            isDraggingState.value = false
            draggedItemIndexState.value = null
            coroutineScope.launch { onTrackDragged(filteredTracks) }
        }
    )
}