package com.dinaraparanid.prima.ui.fragments.artists

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.dinaraparanid.prima.entities.Artist
import com.dinaraparanid.prima.entities.Track
import com.dinaraparanid.prima.rust.RustLibs
import com.dinaraparanid.prima.ui.utils.AwaitDialog
import com.dinaraparanid.prima.ui.utils.tracks.DefaultTracksFragment
import com.dinaraparanid.prima.utils.localization.Localization
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