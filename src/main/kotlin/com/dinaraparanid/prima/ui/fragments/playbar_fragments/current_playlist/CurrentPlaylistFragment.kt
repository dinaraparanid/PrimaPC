package com.dinaraparanid.prima.ui.fragments.playbar_fragments.current_playlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.dinaraparanid.prima.entities.Track
import com.dinaraparanid.prima.rust.RustLibs
import com.dinaraparanid.prima.ui.tracks.TrackList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun CurrentPlaylistFragment(
    currentPlaylistTracksState: SnapshotStateList<Track>,
    currentPlaylistFilteredTracksState: SnapshotStateList<Track>,
    currentTrackState: MutableState<Track?>,
    isPlayingState: MutableState<Boolean>,
    isPlayingCoverLoadedState: MutableState<Boolean>,
    playbackPositionState: MutableState<Float>,
    loopingState: MutableState<Int>,
    isPlaybackTrackDraggingState: State<Boolean>,
    speedState: State<Float>
) {
    rememberCoroutineScope().launch {
        withContext(Dispatchers.IO) {
            RustLibs.getCurPlaylist()?.toList()
        }?.let {
            currentPlaylistTracksState.run {
                clear()
                addAll(it)
            }

            currentPlaylistFilteredTracksState.run {
                clear()
                addAll(it)
            }
        }
    }

    TrackList(
        currentPlaylistTracksState,
        currentPlaylistFilteredTracksState,
        currentTrackState,
        isPlayingState,
        isPlayingCoverLoadedState,
        playbackPositionState,
        loopingState,
        isPlaybackTrackDraggingState,
        speedState,
    ) { _, filteredTracksState, listState -> CurrentPlaylistBar(filteredTracksState, listState) }
}