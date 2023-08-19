package com.paranid5.prima.presentation.ui.artists

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
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
import com.paranid5.prima.data.Artist
import com.paranid5.prima.domain.StorageHandler
import com.paranid5.prima.presentation.ui.navigation.composition_locals.LocalRootNavigator
import com.paranid5.prima.rust.RustLibs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun ArtistItem(
    selectedArtistState: MutableState<Artist?>,
    artistsState: MutableState<List<Artist>>,
    ind: Int,
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject()
) {
    val navigator = LocalRootNavigator.current

    val primaryColor by storageHandler.primaryColorState.collectAsState()
    val secondaryColor by storageHandler.secondaryColorState.collectAsState()

    val artist by remember { derivedStateOf { artistsState.value[ind] } }
    val isPopupMenuExpandedState = remember { mutableStateOf(false) }

    Card(
        backgroundColor = primaryColor,
        elevation = 15.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Button(
            onClick = {
                selectedArtistState.value = artist
                navigator.changeConfigToArtistTracks()
            },
            modifier = Modifier.fillMaxSize().padding(3.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = secondaryColor),
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                ArtistImageLabel(
                    artistName = artist.name,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )

                Spacer(Modifier.width(20.dp).fillMaxHeight())

                ArtistNameLabel(
                    artistName = artist.name,
                    modifier = Modifier.align(Alignment.CenterVertically).weight(1F)
                )

                Spacer(Modifier.width(20.dp).fillMaxHeight())

                ArtistSettingsButton(
                    artistName = artist.name,
                    isPopupMenuExpandedState = isPopupMenuExpandedState,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }
    }
}

@Composable
private fun ArtistImageLabel(
    artistName: String,
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject()
) {
    val primaryColor by storageHandler.primaryColorState.collectAsState()

    Text(
        modifier = modifier,
        text = RustLibs.artistImageBind(artistName),
        color = primaryColor,
        fontSize = 24.sp
    )
}

@Composable
private fun ArtistNameLabel(
    artistName: String,
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject()
) {
    val secondaryAlternativeColor by storageHandler.secondaryAlternativeColorState.collectAsState()

    Text(
        modifier = modifier,
        text = artistName,
        color = secondaryAlternativeColor,
        fontSize = 16.sp
    )
}

@Composable
private fun ArtistSettingsButton(
    artistName: String,
    isPopupMenuExpandedState: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject()
) {
    val lang by storageHandler.languageState.collectAsState()
    val primaryColor by storageHandler.primaryColorState.collectAsState()

    Button(
        onClick = { isPopupMenuExpandedState.value = true },
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
        elevation = null,
        modifier = modifier.width(50.dp).fillMaxHeight()
    ) {
        Image(
            painter = painterResource("images/three_dots.png"),
            contentDescription = lang.trackCover,
            modifier = Modifier.fillMaxSize(),
            colorFilter = ColorFilter.tint(primaryColor),
            contentScale = ContentScale.Inside
        )

        ArtistSettingsMenu(
            artistName = artistName,
            isPopupMenuExpandedState = isPopupMenuExpandedState
        )
    }
}

@Composable
private fun ArtistSettingsMenu(
    artistName: String,
    isPopupMenuExpandedState: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject()
) {
    val lang by storageHandler.languageState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    DropdownMenu(
        modifier = modifier,
        expanded = isPopupMenuExpandedState.value,
        onDismissRequest = { isPopupMenuExpandedState.value = false }
    ) {
        ArtistSettingsMenuItem(title = lang.addToFavourites) {
            coroutineScope.launch(Dispatchers.IO) { RustLibs.onLikeArtistClicked(artistName) }
        }

        ArtistSettingsMenuItem(title = lang.hideArtist) {
            // TODO: hide artist
        }
    }
}

@Composable
private fun ArtistSettingsMenuItem(
    title: String,
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject(),
    onClick: () -> Unit
) {
    val secondaryAlternativeColor by storageHandler.secondaryAlternativeColorState.collectAsState()

    DropdownMenuItem(
        modifier = modifier,
        onClick = onClick
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            color = secondaryAlternativeColor
        )
    }
}