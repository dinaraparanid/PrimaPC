package com.paranid5.prima.domain

import com.paranid5.prima.data.Artist
import com.paranid5.prima.data.Track
import com.paranid5.prima.rust.RustLibs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

suspend inline fun scanTracks(
    tracksState: MutableStateFlow<List<Track>>,
    filteredTracksState: MutableStateFlow<List<Track>>
) = coroutineScope {
    val tracks = withContext(Dispatchers.IO) {
        RustLibs.getAllTracksBlocking().toList()
    }

    tracksState.update { tracks }
    filteredTracksState.update { tracks }
}

suspend inline fun scanArtists(
    artistsState: MutableStateFlow<List<Artist>>,
    filteredArtistsState: MutableStateFlow<List<Artist>>,
    lang: Language
) = coroutineScope {
    val artists = withContext(Dispatchers.IO) {
        RustLibs.getAllArtistsBlocking(lang).map(::Artist)
    }

    artistsState.update { artists }
    filteredArtistsState.update { artists }
}