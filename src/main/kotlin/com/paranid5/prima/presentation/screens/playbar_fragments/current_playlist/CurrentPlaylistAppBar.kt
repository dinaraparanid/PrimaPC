package com.paranid5.prima.presentation.screens.playbar_fragments.current_playlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.paranid5.prima.data.Track
import com.paranid5.prima.presentation.ui.tracks.DefaultTracksAppBar

@Composable
fun CurrentPlaylistAppBar(
    tracksState: SnapshotStateList<Track>,
    filteredTracksState: SnapshotStateList<Track>,
) = DefaultTracksAppBar(tracksState, filteredTracksState, Localization.currentPlaylist.resource)