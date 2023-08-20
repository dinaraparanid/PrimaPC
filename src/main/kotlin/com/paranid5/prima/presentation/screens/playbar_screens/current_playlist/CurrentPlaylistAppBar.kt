package com.paranid5.prima.presentation.screens.playbar_screens.current_playlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.paranid5.prima.data.Track
import com.paranid5.prima.di.KOIN_CURRENT_PLAYLIST_FILTERED_TRACKS
import com.paranid5.prima.di.KOIN_CURRENT_PLAYLIST_TRACKS
import com.paranid5.prima.domain.StorageHandler
import com.paranid5.prima.presentation.ui.tracks.DefaultTracksAppBar
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
fun CurrentPlaylistAppBar(
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject(),
    tracksState: MutableStateFlow<List<Track>> = koinInject(named(KOIN_CURRENT_PLAYLIST_TRACKS)),
    filteredTracksState: MutableStateFlow<List<Track>> = koinInject(named(KOIN_CURRENT_PLAYLIST_FILTERED_TRACKS)),
) {
    val lang by storageHandler.languageState.collectAsState()

    DefaultTracksAppBar(
        mainLabel = lang.currentPlaylist,
        tracksState = tracksState,
        filteredTracksState = filteredTracksState,
        modifier = modifier
    )
}