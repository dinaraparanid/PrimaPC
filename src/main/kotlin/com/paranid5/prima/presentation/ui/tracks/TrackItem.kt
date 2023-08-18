package com.paranid5.prima.presentation.ui.tracks

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paranid5.prima.data.Track
import com.paranid5.prima.rust.RustLibs
import com.paranid5.prima.presentation.cancelPlaybackControlTasks
import com.paranid5.prima.presentation.startPlaybackControlTasks
import com.paranid5.prima.domain.extensions.correctUTF8String
import com.paranid5.prima.domain.localization.LocalizedString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jaudiotagger.audio.AudioFileIO
import java.io.File

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun LazyItemScope.TrackItem(
    tracksOnScreen: List<Track>,
    index: Int,
    currentTrackState: MutableState<Track?>,
    isPlayingState: MutableState<Boolean>,
    isPlayingCoverLoadedState: MutableState<Boolean>,
    playbackPositionState: MutableState<Float>,
    loopingState: MutableState<Int>,
    allTracksState: SnapshotStateList<Track>,
    isPlaybackTrackDraggingState: State<Boolean>,
    speedState: State<Float>,
    isLikedState: MutableState<Boolean>
) {
    val track = tracksOnScreen[index]
    val coroutineScope = rememberCoroutineScope()
    val isCoverLoadedState = remember { mutableStateOf(false) }
    val coverState = remember { mutableStateOf(ImageBitmap(0, 0)) }

    val coverTask = coroutineScope.async(Dispatchers.IO) {
        try {
            AudioFileIO.read(File(track.path.correctUTF8String)).tagOrCreateAndSetDefault?.firstArtwork?.binaryData
        } catch (e: Exception) {
            null
        }
    }

    coroutineScope.launch {
        coverTask.await()?.toList()?.let {
            coverState.value = withContext(Dispatchers.IO) {
                org.jetbrains.skia.Image.makeFromEncoded(it.toByteArray()).toComposeImageBitmap()
            }

            isCoverLoadedState.value = true
        }
    }

    Card(
        backgroundColor = Params.primaryColor,
        elevation = 15.dp,
        modifier = Modifier
            .fillMaxWidth()
            .animateItemPlacement(animationSpec = tween(durationMillis = 300)),
    ) {
        Button(
            onClick = {
                if (currentTrackState.value != track) {
                    currentTrackState.value = track
                    isPlayingState.value = true
                    playbackPositionState.value = 0F
                    isPlayingCoverLoadedState.value = false
                } else {
                    isPlayingState.value = !isPlayingState.value
                }

                coroutineScope.launch(Dispatchers.IO) {
                    RustLibs.onTrackClickedBlocking(tracksOnScreen, index)

                    when {
                        isPlayingState.value -> coroutineScope.launch {
                            startPlaybackControlTasks(
                                currentTrackState,
                                isPlayingState,
                                isPlayingCoverLoadedState,
                                playbackPositionState,
                                loopingState,
                                allTracksState,
                                isPlaybackTrackDraggingState,
                                speedState
                            )
                        }

                        else -> cancelPlaybackControlTasks()
                    }
                }
            },
            modifier = Modifier.fillMaxSize().padding(3.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Params.secondaryColor),
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                if (isCoverLoadedState.value) Image(
                    bitmap = coverState.value,
                    contentDescription = Localization.trackCover.resource,
                    filterQuality = FilterQuality.High,
                    modifier = Modifier
                        .height(50.dp)
                        .width(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .align(Alignment.CenterVertically),
                )
                else Image(
                    painter = painterResource("images/default_cover.png"),
                    contentDescription = Localization.trackCover.resource,
                    modifier = Modifier
                        .height(50.dp)
                        .width(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .align(Alignment.CenterVertically),
                )

                Spacer(Modifier.width(20.dp).fillMaxHeight())

                val textColor = when (track) {
                    currentTrackState.value -> Params.primaryColor
                    else -> Params.secondaryAlternativeColor
                }

                Column(modifier = Modifier.weight(1F).align(Alignment.CenterVertically)) {
                    Text(
                        text = track.title?.takeIf(String::isNotEmpty) ?: Localization.unknownTrack.resource,
                        fontSize = 18.sp,
                        color = textColor
                    )

                    Text(
                        text = "${
                            track.artist?.takeIf(String::isNotEmpty) ?: Localization.unknownArtist.resource
                        } / ${
                            track.album?.takeIf(String::isNotEmpty) ?: Localization.unknownAlbum.resource
                        }",
                        fontSize = 14.sp,
                        color = textColor
                    )
                }

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

                    TrackSettingsMenu(
                        track,
                        currentTrackState,
                        isLikedState,
                        isPopupMenuExpandedState
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackSettingsMenu(
    track: Track,
    curTrackState: State<Track?>,
    isLikedState: MutableState<Boolean>,
    isPopupMenuExpandedState: MutableState<Boolean>
) = DropdownMenu(
    expanded = isPopupMenuExpandedState.value,
    onDismissRequest = { isPopupMenuExpandedState.value = false }
) {
    val coroutineScope = rememberCoroutineScope()

    TrackSettingsMenuItem(title = Localization.changeTrackInfo) {
        // TODO: Change track's info
    }

    TrackSettingsMenuItem(title = Localization.addToQueue) {
        // TODO: Add track to queue
    }

    TrackSettingsMenuItem(title = Localization.addToFavourites) {
        coroutineScope.launch(Dispatchers.IO) { RustLibs.onLikeTrackClicked(track) }
        if (track == curTrackState.value) isLikedState.value = !isLikedState.value
    }

    TrackSettingsMenuItem(title = Localization.removeTrack) {
        // TODO: Remove track
    }

    TrackSettingsMenuItem(title = Localization.lyrics) {
        // TODO: Show lyrics
    }

    TrackSettingsMenuItem(title = Localization.trackInformation) {
        // TODO: Show track's information
    }

    TrackSettingsMenuItem(title = Localization.trimTrack) {
        // TODO: trim track
    }

    TrackSettingsMenuItem(title = Localization.hideTrack) {
        // TODO: hide track
    }
}

@Composable
private fun TrackSettingsMenuItem(title: LocalizedString, onClick: () -> Unit) = DropdownMenuItem(onClick) {
    Text(text = title.resource, fontSize = 14.sp, color = Params.secondaryAlternativeColor)
}