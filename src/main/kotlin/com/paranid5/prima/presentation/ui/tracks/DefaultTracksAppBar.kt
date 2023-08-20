package com.paranid5.prima.presentation.ui.tracks

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
import com.paranid5.prima.domain.StorageHandler
import com.paranid5.prima.presentation.ui.SearchAppBar
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.compose.koinInject

@Composable
fun DefaultTracksAppBar(
    mainLabel: String,
    tracksState: MutableStateFlow<List<Track>>,
    filteredTracksState: MutableStateFlow<List<Track>>,
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject()
) {
    val isSearchingState = remember { mutableStateOf(false) }
    val trackSearchOrder by storageHandler.trackSearchOrderState.collectAsState()

    when {
        isSearchingState.value -> SearchAppBar(
            isSearchingState = isSearchingState,
            allEntitiesState = tracksState,
            filteredEntitiesState = filteredTracksState,
            modifier = modifier,
            onTextChanged = { q ->
                val query = q.lowercase()

                fun contains(order: TrackSearchOrder, trackTag: String?) =
                    order in trackSearchOrder && trackTag?.lowercase()?.contains(query) == true

                filteredTracksState.value = tracksState.value.filter { track ->
                    if (contains(TrackSearchOrder.TITLE, track.title)) return@filter true
                    if (contains(TrackSearchOrder.ARTIST, track.artist)) return@filter true
                    contains(TrackSearchOrder.ALBUM, track.album)
                }
            },
        )

        else -> DefaultAppBar(
            isSearchingState = isSearchingState,
            mainLabel = mainLabel,
            modifier = modifier
        )
    }
}

@Composable
private fun DefaultAppBar(
    isSearchingState: MutableState<Boolean>,
    mainLabel: String,
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject()
) {
    val primaryColor by storageHandler.primaryColorState.collectAsState()
    val isPopupMenuExpandedState = remember { mutableStateOf(false) }

    TopAppBar(
        modifier = modifier.fillMaxWidth().height(60.dp),
        elevation = 10.dp
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
            color = primaryColor,
            elevation = 10.dp
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                Spacer(Modifier.width(40.dp).fillMaxHeight())

                Label(
                    mainLabel = mainLabel,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )

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
                }
            }
        }
    }
}

@Composable
private fun Label(
    mainLabel: String,
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject()
) {
    val secondaryAlternativeColor by storageHandler.secondaryAlternativeColorState.collectAsState()

    Text(
        text = mainLabel,
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