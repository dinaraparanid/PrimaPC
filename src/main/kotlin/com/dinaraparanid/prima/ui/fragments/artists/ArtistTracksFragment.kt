package com.dinaraparanid.prima.ui.fragments.artists

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.dinaraparanid.prima.entities.Artist
import com.dinaraparanid.prima.entities.Track
import com.dinaraparanid.prima.rust.RustLibs
import com.dinaraparanid.prima.ui.utils.tracks.DefaultTracksFragment
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
    rememberCoroutineScope().launch {
        val tracksByArtistTask = async(Dispatchers.IO) { RustLibs.getArtistTracksBlocking(artist.name) }

        tracksState.clear()
        filteredTracksState.clear()

        tracksState.addAll(tracksByArtistTask.await())
        filteredTracksState.addAll(tracksState)
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