package com.dinaraparanid.prima.ui.fragments.playbar_fragments.current_playlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.dinaraparanid.prima.entities.Track
import com.dinaraparanid.prima.ui.utils.tracks.DefaultTracksAppBar
import com.dinaraparanid.prima.utils.localization.Localization

@Composable
fun CurrentPlaylistAppBar(
    tracksState: SnapshotStateList<Track>,
    filteredTracksState: SnapshotStateList<Track>,
) = DefaultTracksAppBar(tracksState, filteredTracksState, Localization.currentPlaylist.resource)