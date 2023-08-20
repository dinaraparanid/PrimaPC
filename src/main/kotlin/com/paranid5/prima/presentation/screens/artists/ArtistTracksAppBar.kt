package com.paranid5.prima.presentation.screens.artists

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.paranid5.prima.data.Artist
import com.paranid5.prima.data.Track
import com.paranid5.prima.di.KOIN_ARTIST_TRACKS
import com.paranid5.prima.di.KOIN_FILTERED_ARTIST_TRACKS
import com.paranid5.prima.di.KOIN_SELECTED_ARTIST
import com.paranid5.prima.presentation.ui.tracks.DefaultTracksAppBar
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
fun ArtistTracksAppBar(
    modifier: Modifier = Modifier,
    selectedArtistState: MutableStateFlow<Artist?> = koinInject(named(KOIN_SELECTED_ARTIST)),
    tracksState: MutableStateFlow<List<Track>> = koinInject(named(KOIN_ARTIST_TRACKS)),
    filteredTracksState: MutableStateFlow<List<Track>> = koinInject(named(KOIN_FILTERED_ARTIST_TRACKS))
) {
    val artistOrNull by selectedArtistState.collectAsState()
    val artistName = artistOrNull?.name ?: return

    DefaultTracksAppBar(
        mainLabel = artistName,
        tracksState = tracksState,
        filteredTracksState = filteredTracksState,
        modifier = modifier
    )
}