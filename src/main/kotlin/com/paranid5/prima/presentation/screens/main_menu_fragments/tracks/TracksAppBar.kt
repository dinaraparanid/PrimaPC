package com.paranid5.prima.presentation.screens.main_menu_fragments.tracks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paranid5.prima.data.Track
import com.paranid5.prima.data.TrackSearchOrder
import com.paranid5.prima.di.KOIN_ALL_TRACKS
import com.paranid5.prima.di.KOIN_FILTERED_ALL_TRACKS
import com.paranid5.prima.domain.StorageHandler
import com.paranid5.prima.domain.scanTracks
import com.paranid5.prima.presentation.ui.SearchAppBar
import com.paranid5.prima.presentation.ui.tracks.SearchByParamsMenu
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
fun TracksAppBar(
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject(),
    allTracksState: MutableStateFlow<List<Track>> = koinInject(named(KOIN_ALL_TRACKS)),
    filteredAllTracksState: MutableStateFlow<List<Track>> = koinInject(named(KOIN_FILTERED_ALL_TRACKS))
) {
    val tracks by allTracksState.collectAsState()
    val trackSearchOrder by storageHandler.trackSearchOrderState.collectAsState()
    val isSearchingState = remember { mutableStateOf(false) }

    when {
        isSearchingState.value -> SearchAppBar(
            isSearchingState = isSearchingState,
            allEntitiesState = allTracksState,
            filteredEntitiesState = filteredAllTracksState,
            modifier = modifier,
            onTextChanged = { q ->
                val query = q.lowercase()

                fun contains(ord: TrackSearchOrder, track: Track) =
                    ord in trackSearchOrder && track.title?.lowercase()?.contains(query) == true

                filteredAllTracksState.update {
                    tracks.filter { track ->
                        if (contains(TrackSearchOrder.TITLE, track)) return@filter true
                        if (contains(TrackSearchOrder.ARTIST, track)) return@filter true
                        contains(TrackSearchOrder.ALBUM, track)
                    }
                }
            },
        )

        else -> DefaultAppBar(
            isSearchingState = isSearchingState,
            modifier = modifier
        )
    }
}

@Composable
private fun DefaultAppBar(
    isSearchingState: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject(),
) {
    val primaryColor by storageHandler.primaryColorState.collectAsState()
    val secondaryColor by storageHandler.secondaryColorState.collectAsState()

    val isPopupMenuExpandedState = remember { mutableStateOf(false) }

    TopAppBar(
        modifier = modifier.fillMaxWidth().height(60.dp),
        elevation = 10.dp,
        backgroundColor = secondaryColor
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
            color = primaryColor,
            elevation = 10.dp
        ) {
            Row(Modifier.fillMaxSize()) {
                Spacer(Modifier.width(40.dp).fillMaxHeight())

                Label(Modifier.align(Alignment.CenterVertically))

                Spacer(Modifier.weight(1F))

                Row(Modifier.align(Alignment.CenterVertically)) {
                    SearchButton(
                        isSearchingState = isSearchingState,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )

                    ParamsButton(
                        isPopupMenuExpandedState = isPopupMenuExpandedState,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )

                    SearchByParamsMenu(isPopupMenuExpandedState)

                    ScannerButton(Modifier.align(Alignment.CenterVertically))
                }
            }
        }
    }
}

@Composable
private fun Label(
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject()
) {
    val lang by storageHandler.languageState.collectAsState()
    val secondaryAlternativeColor by storageHandler.secondaryAlternativeColorState.collectAsState()

    Text(
        text = lang.tracks,
        fontSize = 22.sp,
        color = secondaryAlternativeColor,
        modifier = modifier
    )
}

@Composable
private fun SearchButton(
    isSearchingState: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject()
) {
    val lang by storageHandler.languageState.collectAsState()
    val secondaryAlternativeColor by storageHandler.secondaryAlternativeColorState.collectAsState()

    IconButton(
        modifier = modifier,
        onClick = { isSearchingState.value = true }
    ) {
        Icon(
            modifier = Modifier.size(30.dp).alpha(ContentAlpha.medium),
            painter = painterResource("images/search_icon.png"),
            contentDescription = lang.search,
            tint = secondaryAlternativeColor
        )
    }
}

@Composable
private fun ParamsButton(
    isPopupMenuExpandedState: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject()
) {
    val lang by storageHandler.languageState.collectAsState()
    val secondaryAlternativeColor by storageHandler.secondaryAlternativeColorState.collectAsState()

    IconButton(
        modifier = modifier,
        onClick = { isPopupMenuExpandedState.value = true }
    ) {
        Icon(
            modifier = Modifier.size(30.dp).alpha(ContentAlpha.medium),
            painter = painterResource("images/param.png"),
            contentDescription = lang.search,
            tint = secondaryAlternativeColor
        )
    }
}

@Composable
private fun ScannerButton(
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject(),
    allTracksState: MutableStateFlow<List<Track>> = koinInject(org.koin.core.qualifier.named(com.paranid5.prima.di.KOIN_ALL_TRACKS)),
    filteredAllTracksState: MutableStateFlow<List<Track>> = koinInject(named(KOIN_FILTERED_ALL_TRACKS))
) {
    val lang by storageHandler.languageState.collectAsState()
    val secondaryAlternativeColor by storageHandler.secondaryAlternativeColorState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    IconButton(
        modifier = modifier,
        onClick = {
            coroutineScope.launch {
                scanTracks(
                    tracksState = allTracksState,
                    filteredTracksState = filteredAllTracksState
                )
            }
        }
    ) {
        Icon(
            modifier = Modifier.alpha(ContentAlpha.medium).width(30.dp).height(30.dp),
            painter = painterResource("images/scanner_icon.png"),
            contentDescription = lang.search,
            tint = secondaryAlternativeColor
        )
    }
}