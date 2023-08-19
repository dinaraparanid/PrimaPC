package com.paranid5.prima.presentation.screens.artists

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import com.paranid5.prima.data.Track
import com.paranid5.prima.presentation.ui.tracks.DefaultTracksAppBar

@Composable
fun ArtistTracksAppBar(
    artistName: String,
    tracksState: MutableState<List<Track>>,
    filteredTracksState: MutableState<List<Track>>,
    modifier: Modifier = Modifier
) = DefaultTracksAppBar(
    mainLabel = artistName,
    tracksState = tracksState,
    filteredTracksState = filteredTracksState,
    modifier = modifier
)