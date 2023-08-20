package com.paranid5.prima.presentation.screens.playbar_screens.current_playlist

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paranid5.prima.data.Track
import com.paranid5.prima.di.KOIN_CURRENT_PLAYLIST_FILTERED_TRACKS
import com.paranid5.prima.domain.StorageHandler
import com.paranid5.prima.rust.RustLibs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
fun CurrentPlaylistBar(
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxWidth().height(60.dp),
        drawerElevation = 10.dp
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
            elevation = 30.dp
        ) {
            Row(Modifier.fillMaxWidth().height(60.dp)) {
                ShuffleButton(listState)

                TracksNumberLabel(
                    Modifier
                        .align(Alignment.CenterVertically)
                        .padding(start = 20.dp)
                )
            }
        }
    }
}

@Composable
private fun ShuffleButton(
    listState: LazyListState,
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject(),
    filteredTracksState: MutableStateFlow<List<Track>> = koinInject(named(KOIN_CURRENT_PLAYLIST_FILTERED_TRACKS))
) {
    val lang by storageHandler.languageState.collectAsState()
    val primaryColor by storageHandler.primaryColorState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Button(
        onClick = {
            val tracks = filteredTracksState.updateAndGet { it.shuffled() }
            coroutineScope.launch { listState.scrollToItem(0) }

            coroutineScope.launch(Dispatchers.IO) {
                RustLibs.updateAndStoreCurPlaylistBlocking(tracks)
            }
        },
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
        elevation = null,
        modifier = modifier.width(70.dp).height(60.dp),
    ) {
        Image(
            painter = painterResource("images/shuffle.png"),
            contentDescription = lang.trackCover,
            modifier = Modifier.fillMaxSize(),
            colorFilter = ColorFilter.tint(primaryColor),
            contentScale = ContentScale.FillWidth
        )
    }
}

@Composable
private fun TracksNumberLabel(
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject(),
    filteredTracksState: MutableStateFlow<List<Track>> = koinInject(named(KOIN_CURRENT_PLAYLIST_FILTERED_TRACKS))
) {
    val lang by storageHandler.languageState.collectAsState()
    val primaryColor by storageHandler.primaryColorState.collectAsState()

    val tracksOnScreen by filteredTracksState.collectAsState()
    val tracksNumber by remember { derivedStateOf { tracksOnScreen.size } }

    Text(
        text = "${lang.tracks}: $tracksNumber",
        fontSize = 20.sp,
        color = primaryColor,
        modifier = modifier
    )
}