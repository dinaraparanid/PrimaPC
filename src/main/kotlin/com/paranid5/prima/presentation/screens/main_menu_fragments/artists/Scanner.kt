package com.paranid5.prima.presentation.screens.main_menu_fragments.artists

import androidx.compose.runtime.MutableState
import com.paranid5.prima.data.Artist
import com.paranid5.prima.domain.Language
import com.paranid5.prima.rust.RustLibs
import kotlinx.coroutines.*

suspend inline fun scanArtists(
    artistsState: MutableState<List<Artist>>,
    filteredArtistsState: MutableState<List<Artist>>,
    lang: Language
) = coroutineScope {
    val artists = withContext(Dispatchers.IO) {
        RustLibs.getAllArtistsBlocking(lang).map(::Artist)
    }

    artistsState.value = artists
    filteredArtistsState.value = artists
}