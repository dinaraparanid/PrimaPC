package com.dinaraparanid.prima.ui.fragments.main_menu_fragments.favourites.tracks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.dinaraparanid.prima.entities.Track
import com.dinaraparanid.prima.rust.RustLibs
import com.dinaraparanid.prima.ui.utils.tracks.DefaultTracksFragment
import com.dinaraparanid.prima.utils.extensions.correctUTF8String
import kotlinx.coroutines.Dispatchers
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
    rememberCoroutineScope().launch(Dispatchers.IO) {
        val favouriteTracks = RustLibs.getFavouriteTracks()

        tracksState.run {
            clear()
            addAll(favouriteTracks)
        }

        filteredTracksState.run {
            clear()
            addAll(favouriteTracks)
        }
    }

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