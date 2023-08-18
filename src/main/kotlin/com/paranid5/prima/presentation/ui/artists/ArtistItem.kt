package com.paranid5.prima.presentation.ui.artists

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paranid5.prima.data.Artist
import com.paranid5.prima.rust.RustLibs
import com.paranid5.prima.presentation.ui.navigation.RootScreen
import com.paranid5.prima.domain.localization.LocalizedString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LazyItemScope.ArtistItem(
    rootScreen: RootScreen,
    curArtistState: MutableState<Artist?>,
    artistsState: SnapshotStateList<Artist>,
    ind: Int
) {
    val artist = artistsState[ind]

    Card(
        backgroundColor = Params.primaryColor,
        elevation = 15.dp,
        modifier = Modifier
            .fillMaxWidth()
            .animateItemPlacement(animationSpec = tween(durationMillis = 300)),
    ) {
        Button(
            onClick = {
                curArtistState.value = artist
                rootScreen.changeConfigToArtistTracks()
            },
            modifier = Modifier.fillMaxSize().padding(3.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Params.secondaryColor),
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    text = RustLibs.artistImageBind(artist.name),
                    color = Params.primaryColor,
                    fontSize = 24.sp
                )

                Spacer(Modifier.width(20.dp).fillMaxHeight())

                Text(
                    modifier = Modifier.align(Alignment.CenterVertically).weight(1F),
                    text = artist.name,
                    color = Params.secondaryAlternativeColor,
                    fontSize = 16.sp
                )

                Spacer(Modifier.width(20.dp).fillMaxHeight())

                val isPopupMenuExpandedState = remember { mutableStateOf(false) }

                Button(
                    onClick = { isPopupMenuExpandedState.value = true },
                    modifier = Modifier
                        .width(50.dp)
                        .fillMaxHeight()
                        .align(Alignment.CenterVertically),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
                    elevation = null
                ) {
                    Image(
                        painter = painterResource("images/three_dots.png"),
                        contentDescription = Localization.trackCover.resource,
                        modifier = Modifier.fillMaxSize(),
                        colorFilter = ColorFilter.tint(Params.primaryColor),
                        contentScale = ContentScale.Inside
                    )

                    ArtistSettingsMenu(artist, isPopupMenuExpandedState)
                }
            }
        }
    }
}

@Composable
private fun ArtistSettingsMenu(artist: Artist, isPopupMenuExpandedState: MutableState<Boolean>) = DropdownMenu(
    expanded = isPopupMenuExpandedState.value,
    onDismissRequest = { isPopupMenuExpandedState.value = false }
) {
    val coroutineScope = rememberCoroutineScope()

    ArtistSettingsMenuItem(title = Localization.addToFavourites) {
        coroutineScope.launch(Dispatchers.IO) { RustLibs.onLikeArtistClicked(artist.name) }
    }

    ArtistSettingsMenuItem(title = Localization.hideArtist) {
        // TODO: hide artist
    }
}

@Composable
private fun ArtistSettingsMenuItem(title: LocalizedString, onClick: () -> Unit) = DropdownMenuItem(onClick) {
    Text(text = title.resource, fontSize = 14.sp, color = Params.secondaryAlternativeColor)
}