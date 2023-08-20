package com.paranid5.prima.presentation.screens.main_menu_fragments.artists

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
import com.paranid5.prima.data.Artist
import com.paranid5.prima.di.KOIN_ALL_ARTISTS
import com.paranid5.prima.di.KOIN_FILTERED_ALL_ARTISTS
import com.paranid5.prima.domain.StorageHandler
import com.paranid5.prima.domain.scanArtists
import com.paranid5.prima.presentation.ui.SearchAppBar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
fun ArtistsAppBar(
    modifier: Modifier = Modifier,
    allArtistsState: MutableStateFlow<List<Artist>> = koinInject(named(KOIN_ALL_ARTISTS)),
    filteredAllArtistsState: MutableStateFlow<List<Artist>> = koinInject(named(KOIN_FILTERED_ALL_ARTISTS))
) {
    val artists by allArtistsState.collectAsState()
    val isSearchingState = remember { mutableStateOf(false) }

    when {
        isSearchingState.value -> SearchAppBar(
            isSearchingState = isSearchingState,
            allEntitiesState = allArtistsState,
            filteredEntitiesState = filteredAllArtistsState,
            modifier = modifier
        ) { q ->
            val query = q.lowercase()

            filteredAllArtistsState.update {
                artists.filter { artist -> query in artist.name.lowercase() }
            }
        }

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
            Row(Modifier.fillMaxSize()) {
                Spacer(Modifier.width(40.dp).fillMaxHeight())

                Label(Modifier.align(Alignment.CenterVertically))

                Spacer(Modifier.weight(1F))

                Row(Modifier.align(Alignment.CenterVertically)) {
                    SearchButton(
                        isSearchingState = isSearchingState,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )

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
        text = lang.artists,
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
private fun ScannerButton(
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject(),
    allArtistsState: MutableStateFlow<List<Artist>> = koinInject(named(KOIN_ALL_ARTISTS)),
    filteredAllArtistsState: MutableStateFlow<List<Artist>> = koinInject(named(KOIN_FILTERED_ALL_ARTISTS))
) {
    val lang by storageHandler.languageState.collectAsState()
    val secondaryAlternativeColor by storageHandler.secondaryAlternativeColorState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    IconButton(
        modifier = modifier,
        onClick = {
            coroutineScope.launch {
                scanArtists(
                    artistsState = allArtistsState,
                    filteredArtistsState = filteredAllArtistsState,
                    lang = lang
                )
            }
        }
    ) {
        Icon(
            modifier = Modifier.size(30.dp).alpha(ContentAlpha.medium),
            painter = painterResource("images/scanner_icon.png"),
            contentDescription = lang.search,
            tint = secondaryAlternativeColor
        )
    }
}