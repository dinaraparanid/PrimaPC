package com.paranid5.prima.presentation.screens.main_menu_fragments.favourites.tracks

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.paranid5.prima.data.Track
import com.paranid5.prima.presentation.ui.AwaitDialog
import com.paranid5.prima.presentation.ui.tracks.DefaultTracksFragment
import com.paranid5.prima.rust.RustLibs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun FavouriteTracksFragment(
    currentTrackState: MutableState<Track?>,
    isPlayingState: MutableState<Boolean>,
    isPlayingCoverLoadedState: MutableState<Boolean>,
    playbackPositionState: MutableState<Float>,
    loopingState: MutableState<Int>,
    tracksState: MutableState<List<Track>>,
    filteredTracksState: MutableState<List<Track>>,
    isPlaybackTrackDraggingState: State<Boolean>,
    speedState: State<Float>,
    isLikedState: MutableState<Boolean>,
    modifier: Modifier = Modifier
) {
    val isLoadingState = remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val favouriteTracks = withContext(Dispatchers.IO) {
            RustLibs.getFavouriteTracks().toList()
        }

        tracksState.value = favouriteTracks
        filteredTracksState.value = favouriteTracks
        isLoadingState.value = false
    }

    AwaitDialog(isDialogShownState = isLoadingState)

    DefaultTracksFragment(
        currentTrackState = currentTrackState,
        isPlayingState = isPlayingState,
        isPlayingCoverLoadedState = isPlayingCoverLoadedState,
        playbackPositionState = playbackPositionState,
        loopingState = loopingState,
        tracksState = tracksState,
        filteredTracksState = filteredTracksState,
        isPlaybackTrackDraggingState = isPlaybackTrackDraggingState,
        speedState = speedState,
        isLikedState = isLikedState,
        modifier = modifier
    )
}