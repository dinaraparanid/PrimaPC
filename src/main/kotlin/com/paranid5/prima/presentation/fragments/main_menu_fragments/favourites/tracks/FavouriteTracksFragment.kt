package com.paranid5.prima.presentation.fragments.main_menu_fragments.favourites.tracks

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.paranid5.prima.data.Track
import com.paranid5.prima.rust.RustLibs
import com.paranid5.prima.presentation.ui.AwaitDialog
import com.paranid5.prima.presentation.ui.tracks.DefaultTracksFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

@Composable
fun FavouriteTracksFragment(
    currentTrackState: MutableState<Track?>,
    isPlayingState: MutableState<Boolean>,
    isPlayingCoverLoadedState: MutableState<Boolean>,
    playbackPositionState: MutableState<Float>,
    loopingState: MutableState<Int>,
    tracksState: SnapshotStateList<Track>,
    filteredTracksState: SnapshotStateList<Track>,
    isPlaybackTrackDraggingState: State<Boolean>,
    speedState: State<Float>,
    isLikedState: MutableState<Boolean>
) {
    val isLoadingState = mutableStateOf(true)

    rememberCoroutineScope().launch {
        val favouriteTracks = async(Dispatchers.IO) { RustLibs.getFavouriteTracks() }

        tracksState.clear()
        filteredTracksState.clear()

        tracksState.addAll(favouriteTracks.await())
        filteredTracksState.addAll(tracksState)
        isLoadingState.value = false
    }

    AwaitDialog(isDialogShownState = isLoadingState)

    DefaultTracksFragment(
        currentTrackState,
        isPlayingState,
        isPlayingCoverLoadedState,
        playbackPositionState,
        loopingState,
        tracksState,
        filteredTracksState,
        isPlaybackTrackDraggingState,
        speedState,
        isLikedState
    )
}