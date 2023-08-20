package com.paranid5.prima.presentation.screens.playbar_screens.current_playlist

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.paranid5.prima.data.Track
import com.paranid5.prima.di.KOIN_CURRENT_PLAYLIST_FILTERED_TRACKS
import com.paranid5.prima.di.KOIN_CURRENT_PLAYLIST_TRACKS
import com.paranid5.prima.presentation.ui.AwaitDialog
import com.paranid5.prima.presentation.ui.tracks.DraggableTrackList
import com.paranid5.prima.rust.RustLibs
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
fun CurrentPlaylistScreen(
    modifier: Modifier = Modifier,
    tracksState: MutableStateFlow<List<Track>> = koinInject(named(KOIN_CURRENT_PLAYLIST_TRACKS)),
    filteredTracksState: MutableStateFlow<List<Track>> = koinInject(named(KOIN_CURRENT_PLAYLIST_FILTERED_TRACKS)),
) {
    val isLoadingState = mutableStateOf(true)

    LaunchedEffect(Unit) {
        val playlistTracks = withContext(Dispatchers.IO) {
            RustLibs.getCurPlaylistBlocking().toList()
        }

        tracksState.update { playlistTracks }
        filteredTracksState.update { playlistTracks }
        isLoadingState.value = false
    }

    AwaitDialog(isDialogShownState = isLoadingState)

    DraggableTrackList(
        tracksState = tracksState,
        filteredTracksState = filteredTracksState,
        modifier = modifier,
        onTrackDragged = { curPlaylist ->
            withContext(Dispatchers.IO) {
                RustLibs.updateAndStoreCurPlaylistBlocking(curPlaylist)
            }
        }
    ) { _, _, listState ->
        CurrentPlaylistBar(listState)
    }
}