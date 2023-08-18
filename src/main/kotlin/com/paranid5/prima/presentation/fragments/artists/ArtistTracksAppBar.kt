package com.paranid5.prima.presentation.fragments.artists

import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.paranid5.prima.data.Artist
import com.paranid5.prima.data.Track
import com.paranid5.prima.presentation.ui.tracks.DefaultTracksAppBar

@Composable
fun ArtistTracksAppBar(
    artist: Artist,
    tracksState: SnapshotStateList<Track>,
    filteredTracksState: SnapshotStateList<Track>,
) = DefaultTracksAppBar(tracksState, filteredTracksState, mainLabel = artist.name)