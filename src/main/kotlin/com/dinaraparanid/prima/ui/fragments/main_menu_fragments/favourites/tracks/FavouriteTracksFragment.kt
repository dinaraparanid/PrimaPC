package com.dinaraparanid.prima.ui.fragments.main_menu_fragments.favourites.tracks

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.dinaraparanid.prima.entities.Track
import com.dinaraparanid.prima.rust.RustLibs
import com.dinaraparanid.prima.ui.utils.AwaitDialog
import com.dinaraparanid.prima.ui.utils.tracks.DefaultTracksFragment
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