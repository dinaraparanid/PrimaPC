package com.dinaraparanid.prima.ui.fragments.playbar_fragments.current_playlist

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.dinaraparanid.prima.entities.Track
import com.dinaraparanid.prima.rust.RustLibs
import com.dinaraparanid.prima.ui.utils.AwaitDialog
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
    val isLoadingState = mutableStateOf(true)

    rememberCoroutineScope().launch {
        val playlistTracksTask = async(Dispatchers.IO) { RustLibs.getCurPlaylist() }

        currentPlaylistTracksState.clear()
        currentPlaylistFilteredTracksState.clear()

        playlistTracksTask.await()?.let {
            currentPlaylistTracksState.addAll(it)
            currentPlaylistFilteredTracksState.addAll(it)
        }

        isLoadingState.value = false
    }

    AwaitDialog(isDialogShownState = isLoadingState)

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