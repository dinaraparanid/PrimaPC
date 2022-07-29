package com.dinaraparanid.prima.ui.fragments.playbar_fragments.current_playlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.dinaraparanid.prima.entities.Track
import com.dinaraparanid.prima.rust.RustLibs
import com.dinaraparanid.prima.ui.utils.tracks.DraggableTrackList
import kotlinx.coroutines.*

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
    speedState: State<Float>,
    isLikedState: MutableState<Boolean>
) {
    rememberCoroutineScope().launch {
        val playlistTracksTask = async(Dispatchers.IO) { RustLibs.getCurPlaylist() }

        currentPlaylistTracksState.clear()
        currentPlaylistFilteredTracksState.clear()

        playlistTracksTask.await()?.let {
            currentPlaylistTracksState.addAll(it)
            currentPlaylistFilteredTracksState.addAll(it)
        }
    }

    DraggableTrackList(
        currentPlaylistTracksState,
        currentPlaylistFilteredTracksState,
        currentTrackState,
        isPlayingState,
        isPlayingCoverLoadedState,
        playbackPositionState,
        loopingState,
        isPlaybackTrackDraggingState,
        speedState,
        isLikedState,
        onTrackDragged = { curPlaylist ->
            coroutineScope {
                launch(Dispatchers.IO) {
                    RustLibs.updateAndStoreCurPlaylist(curPlaylist)
                }
            }
        }
    ) { _, filteredTracksState, listState -> CurrentPlaylistBar(filteredTracksState, listState) }
}