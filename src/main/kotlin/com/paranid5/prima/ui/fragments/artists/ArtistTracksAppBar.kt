package com.dinaraparanid.prima.ui.fragments.artists

import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.dinaraparanid.prima.entities.Artist
import com.dinaraparanid.prima.entities.Track
import com.dinaraparanid.prima.ui.utils.tracks.DefaultTracksAppBar

@Composable
fun ArtistTracksAppBar(
    artist: Artist,
    tracksState: SnapshotStateList<Track>,
    filteredTracksState: SnapshotStateList<Track>,
) = DefaultTracksAppBar(tracksState, filteredTracksState, mainLabel = artist.name)