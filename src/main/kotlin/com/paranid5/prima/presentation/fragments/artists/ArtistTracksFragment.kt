package com.paranid5.prima.presentation.fragments.artists

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.paranid5.prima.data.Artist
import com.paranid5.prima.data.Track
import com.paranid5.prima.presentation.ui.AwaitDialog
import com.paranid5.prima.presentation.ui.tracks.DefaultTracksFragment
import com.paranid5.prima.rust.RustLibs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

@Composable
fun ArtistTracksFragment(
    artist: Artist,
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
        val tracksByArtistTask = async(Dispatchers.IO) { RustLibs.getArtistTracksBlocking(artist.name) }

        tracksState.clear()
        filteredTracksState.clear()

        tracksState.addAll(tracksByArtistTask.await())
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