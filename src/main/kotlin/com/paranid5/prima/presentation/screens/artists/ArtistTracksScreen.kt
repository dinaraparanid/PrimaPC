package com.paranid5.prima.presentation.screens.artists

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.paranid5.prima.data.Artist
import com.paranid5.prima.data.Track
import com.paranid5.prima.di.*
import com.paranid5.prima.presentation.ui.AwaitDialog
import com.paranid5.prima.presentation.ui.tracks.DefaultTracksFragment
import com.paranid5.prima.rust.RustLibs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
fun ArtistTracksScreen(
    modifier: Modifier = Modifier,
    selectedArtistState: MutableStateFlow<Artist?> = koinInject(named(KOIN_SELECTED_ARTIST)),
    artistTracksState: MutableStateFlow<List<Track>> = koinInject(named(KOIN_ARTIST_TRACKS)),
    filteredArtistTracksState: MutableStateFlow<List<Track>> = koinInject(named(KOIN_FILTERED_ARTIST_TRACKS))
) {
    val isLoadingState = remember { mutableStateOf(true) }
    val artistOrNull by selectedArtistState.collectAsState()
    val artist = artistOrNull ?: return

    LaunchedEffect(Unit) {
        val tracks = withContext(Dispatchers.IO) {
            RustLibs.getArtistTracksBlocking(artist.name).toList()
        }

        artistTracksState.update { tracks }
        filteredArtistTracksState.update { tracks }
        isLoadingState.value = false
    }

    AwaitDialog(isDialogShownState = isLoadingState)

    DefaultTracksFragment(
        tracksState = artistTracksState,
        filteredTracksState = filteredArtistTracksState,
        modifier = modifier
    )
}