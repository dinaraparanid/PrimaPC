package com.dinaraparanid.prima.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dinaraparanid.prima.entities.Track
import com.dinaraparanid.prima.rust.RustLibs
import com.dinaraparanid.prima.ui.utils.navigation.RootScreen
import com.dinaraparanid.prima.utils.Params
import com.dinaraparanid.prima.utils.calcTrackTime
import com.dinaraparanid.prima.utils.extensions.correctUTF8String
import com.dinaraparanid.prima.utils.extensions.take
import com.dinaraparanid.prima.utils.extensions.timeString
import com.dinaraparanid.prima.utils.localization.Localization
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jaudiotagger.audio.AudioFileIO
import java.io.File

@Composable
fun PlayingBar(
    rootScreen: RootScreen,
    tracksState: SnapshotStateList<Track>,
    currentTrackState: MutableState<Track?>,
    isPlayingCoverLoadedState: MutableState<Boolean>,
    playbackPositionState: MutableState<Float>,
    isPlayingState: MutableState<Boolean>,
    isPlaybackTrackDraggingState: MutableState<Boolean>,
    loopingState: MutableState<Int>,
    speedState: MutableState<Float>,
    volumeState: MutableState<Float>,
    isLikedState: MutableState<Boolean>
) = BottomAppBar(
    modifier = Modifier.fillMaxWidth().height(150.dp),
    elevation = 10.dp
) {
    val coroutineScope = rememberCoroutineScope()
    val coverState = remember { mutableStateOf(ImageBitmap(0, 0)) }

    val coverTask = currentTrackState.value?.let { track ->
        coroutineScope.async(Dispatchers.IO) {
            AudioFileIO.read(File(track.path.correctUTF8String)).tagOrCreateAndSetDefault?.firstArtwork?.binaryData
        }
    }

    coroutineScope.launch {
        coverTask?.await()?.toList()?.let {
            coverState.value = withContext(Dispatchers.IO) {
                org.jetbrains.skia.Image.makeFromEncoded(it.toByteArray()).toComposeImageBitmap()
            }

            isPlayingCoverLoadedState.value = true
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        color = Params.primaryColor,
        elevation = 10.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(100.dp)) {
            CurrentTrackData(currentTrackState, coverState, isPlayingCoverLoadedState)

            ButtonsAndTrack(
                rootScreen,
                tracksState,
                currentTrackState,
                isPlayingCoverLoadedState,
                playbackPositionState,
                isPlayingState,
                isPlaybackTrackDraggingState,
                loopingState,
                speedState,
                isLikedState
            )

            Sliders(volumeState, speedState)
        }
    }
}

@Composable
private fun BoxScope.CurrentTrackData(
    currentTrackState: State<Track?>,
    coverState: State<ImageBitmap>,
    isPlayingCoverLoadedState: State<Boolean>
) = Row(modifier = Modifier.fillMaxHeight().align(Alignment.CenterStart)) {
    Spacer(modifier = Modifier.width(20.dp))

    Card(
        backgroundColor = Color.Black,
        contentColor = Color.Black,
        shape = CircleShape,
        elevation = 15.dp,
        modifier = Modifier
            .height(100.dp)
            .width(100.dp)
            .clip(CircleShape)
            .align(Alignment.CenterVertically)
    ) {
        if (isPlayingCoverLoadedState.value) Image(
            bitmap = coverState.value,
            contentDescription = Localization.trackCover.resource,
            filterQuality = FilterQuality.High,
            modifier = Modifier
                .padding(24.dp)
                .clip(CircleShape)
                .align(Alignment.CenterVertically),
        )
        else Image(
            painter = painterResource("images/default_cover.png"),
            contentDescription = Localization.trackCover.resource,
            modifier = Modifier
                .padding(24.dp)
                .clip(CircleShape)
                .align(Alignment.CenterVertically),
        )
    }

    Spacer(modifier = Modifier.width(20.dp))

    Column(modifier = Modifier.width(300.dp).align(Alignment.CenterVertically)) {
        Text(
            text = currentTrackState.value?.run {
                title?.takeIf(String::isNotEmpty) ?: Localization.unknownTrack.resource
            } ?: "-----",
            fontSize = 22.sp,
            color = Params.secondaryAlternativeColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = currentTrackState.value?.run {
                "${
                    artist?.takeIf(String::isNotEmpty) ?: Localization.unknownArtist.resource
                } / ${
                    album?.takeIf(String::isNotEmpty) ?: Localization.unknownAlbum.resource
                }"
            } ?: "-----",
            fontSize = 16.sp,
            color = Params.secondaryAlternativeColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ColumnScope.Buttons(
    rootScreen: RootScreen,
    tracksState: SnapshotStateList<Track>,
    currentTrackState: MutableState<Track?>,
    isPlayingCoverLoadedState: MutableState<Boolean>,
    playbackPositionState: MutableState<Float>,
    isPlayingState: MutableState<Boolean>,
    isPlaybackTrackDraggingState: State<Boolean>,
    loopingState: MutableState<Int>,
    speedState: State<Float>,
    isLikedState: MutableState<Boolean>
) {
    val coroutineScope = rememberCoroutineScope()

    Row(modifier = Modifier.width(650.dp).weight(1F).align(Alignment.CenterHorizontally)) {
        Button(
            colors = ButtonDefaults.buttonColors(Color.Transparent),
            elevation = null,
            contentPadding = PaddingValues(3.dp),
            modifier = Modifier.weight(3F).align(Alignment.CenterVertically),
            onClick = rootScreen::changeConfigToCurPlaylist
        ) {
            Image(
                painter = painterResource("images/playlist.png"),
                contentDescription = Localization.trackCover.resource,
                colorFilter = ColorFilter.tint(Params.secondaryAlternativeColor),
            )
        }

        Spacer(modifier = Modifier.height(20.dp).weight(1F))

        Button(
            colors = ButtonDefaults.buttonColors(Color.Transparent),
            elevation = null,
            contentPadding = PaddingValues(3.dp),
            modifier = Modifier.weight(3F).align(Alignment.CenterVertically),
            onClick = {
                if (currentTrackState.value != null)
                    isLikedState.value = !isLikedState.value

                val curTrack = currentTrackState.value!!
                coroutineScope.launch(Dispatchers.IO) { RustLibs.onLikeTrackClicked(curTrack) }
            }
        ) {
            Image(
                painter = painterResource(
                    when {
                        isLikedState.value -> "images/heart_like.png"
                        else -> "images/heart.png"
                    }
                ),
                contentDescription = Localization.trackCover.resource,
                colorFilter = ColorFilter.tint(Params.secondaryAlternativeColor),
                contentScale = ContentScale.Inside,
            )
        }

        Spacer(modifier = Modifier.height(20.dp).weight(1F))

        Button(
            colors = ButtonDefaults.buttonColors(Color.Transparent),
            elevation = null,
            contentPadding = PaddingValues(horizontal = 20.dp),
            modifier = Modifier.weight(3F).align(Alignment.CenterVertically),
            onClick = {
                if (currentTrackState.value != null) coroutineScope.launch(Dispatchers.IO) {
                    RustLibs.onPreviousTrackClickedBlocking()
                    currentTrackState.value = RustLibs.getCurTrackBlocking()
                    isPlayingState.value = true
                    isPlayingCoverLoadedState.value = false
                    playbackPositionState.value = 0F

                    startPlaybackControlTasks(
                        currentTrackState,
                        isPlayingState,
                        isPlayingCoverLoadedState,
                        playbackPositionState,
                        loopingState,
                        tracksState,
                        isPlaybackTrackDraggingState,
                        speedState
                    )
                }
            }
        ) {
            Image(
                painter = painterResource("images/prev_track.png"),
                contentDescription = Localization.trackCover.resource,
                colorFilter = ColorFilter.tint(Params.secondaryAlternativeColor),
                contentScale = ContentScale.FillBounds,
            )
        }

        Spacer(modifier = Modifier.height(20.dp).weight(1F))

        Button(
            colors = ButtonDefaults.buttonColors(Color.Transparent),
            elevation = null,
            contentPadding = PaddingValues(),
            modifier = Modifier.weight(3F).align(Alignment.CenterVertically),
            onClick = {
                if (currentTrackState.value != null) coroutineScope.launch(Dispatchers.IO) {
                    isPlayingState.value = !isPlayingState.value
                    RustLibs.onPlayButtonClickedBlocking()

                    when {
                        isPlayingState.value -> startPlaybackControlTasks(
                            currentTrackState,
                            isPlayingState,
                            isPlayingCoverLoadedState,
                            playbackPositionState,
                            loopingState,
                            tracksState,
                            isPlaybackTrackDraggingState,
                            speedState
                        )

                        else -> cancelPlaybackControlTasks()
                    }
                }
            }
        ) {
            Image(
                painter = painterResource(
                    when {
                        isPlayingState.value -> "images/pause.png"
                        else -> "images/play.png"
                    }
                ),
                contentDescription = Localization.trackCover.resource,
                colorFilter = ColorFilter.tint(Params.secondaryAlternativeColor),
                contentScale = ContentScale.Inside,
            )
        }

        Spacer(modifier = Modifier.height(20.dp).weight(1F))

        Button(
            colors = ButtonDefaults.buttonColors(Color.Transparent),
            elevation = null,
            contentPadding = PaddingValues(horizontal = 20.dp),
            modifier = Modifier.weight(3F).align(Alignment.CenterVertically),
            onClick = {
                if (currentTrackState.value != null)
                    coroutineScope.launch(Dispatchers.IO) {
                        switchToNextTrack(
                            currentTrackState,
                            isPlayingState,
                            isPlayingCoverLoadedState,
                            playbackPositionState,
                            loopingState,
                            tracksState,
                            isPlaybackTrackDraggingState,
                            speedState
                        )
                    }
            }
        ) {
            Image(
                painter = painterResource("images/next_track.png"),
                contentDescription = Localization.trackCover.resource,
                colorFilter = ColorFilter.tint(Params.secondaryAlternativeColor),
                contentScale = ContentScale.FillBounds,
            )
        }

        Spacer(modifier = Modifier.height(20.dp).weight(1F))

        Button(
            colors = ButtonDefaults.buttonColors(Color.Transparent),
            elevation = null,
            contentPadding = PaddingValues(2.dp),
            modifier = Modifier.weight(3F).align(Alignment.CenterVertically),
            onClick = { loopingState.value = RustLibs.setNextLoopingState() }
        ) {
            Image(
                painter = painterResource(
                    when (loopingState.value) {
                        0 -> "images/repeat.png"
                        1 -> "images/repeat_1.png"
                        else -> "images/no_repeat.png"
                    }
                ),
                contentDescription = Localization.trackCover.resource,
                colorFilter = ColorFilter.tint(Params.secondaryAlternativeColor),
                contentScale = ContentScale.Inside,
            )
        }

        Spacer(modifier = Modifier.height(20.dp).weight(1F))

        Button(
            colors = ButtonDefaults.buttonColors(Color.Transparent),
            elevation = null,
            contentPadding = PaddingValues(1.dp),
            modifier = Modifier.weight(3F).align(Alignment.CenterVertically),
            onClick = {
                // TODO: Trim track
            }
        ) {
            Image(
                painter = painterResource("images/scissors_start.png"),
                contentDescription = Localization.trackCover.resource,
                colorFilter = ColorFilter.tint(Params.secondaryAlternativeColor),
            )
        }
    }
}

@Composable
private fun ColumnScope.Track(
    currentTrackState: MutableState<Track?>,
    isPlayingState: MutableState<Boolean>,
    isPlayingCoverLoadedState: MutableState<Boolean>,
    playbackPositionState: MutableState<Float>,
    loopingState: MutableState<Int>,
    tracksState: SnapshotStateList<Track>,
    isPlaybackTrackDraggingState: MutableState<Boolean>,
    speedState: State<Float>
) = Column(modifier = Modifier.fillMaxWidth().weight(1.5F).align(Alignment.CenterHorizontally)) {
    val coroutineScope = rememberCoroutineScope()

    Slider(
        valueRange = (0F..(currentTrackState.value?.duration?.toFloat()?.coerceAtLeast(1F) ?: 1F)),
        value = playbackPositionState.value,
        colors = SliderDefaults.colors(
            thumbColor = Params.secondaryAlternativeColor,
            activeTrackColor = Params.secondaryAlternativeColor,
            inactiveTrackColor = Params.secondaryColor
        ),
        modifier = Modifier.fillMaxWidth().height(20.dp),
        onValueChange = {
            isPlaybackTrackDraggingState.value = true
            playbackPositionState.value = it
            cancelPlaybackControlTasks()
        },
        onValueChangeFinished = {
            isPlayingState.value = true
            isPlaybackTrackDraggingState.value = false
            RustLibs.seekTo(playbackPositionState.value.toLong())

            coroutineScope.launch {
                startPlaybackControlTasks(
                    currentTrackState,
                    isPlayingState,
                    isPlayingCoverLoadedState,
                    playbackPositionState,
                    loopingState,
                    tracksState,
                    isPlaybackTrackDraggingState,
                    speedState
                )
            }
        }
    )

    Spacer(modifier = Modifier.height(10.dp))

    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = calcTrackTime(playbackPositionState.value.toInt()).timeString,
            color = Params.secondaryAlternativeColor,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.weight(1F))

        Text(
            text = calcTrackTime(currentTrackState.value?.duration?.toInt() ?: 0).timeString,
            color = Params.secondaryAlternativeColor,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun BoxScope.ButtonsAndTrack(
    rootScreen: RootScreen,
    tracksState: SnapshotStateList<Track>,
    currentTrackState: MutableState<Track?>,
    isPlayingCoverLoadedState: MutableState<Boolean>,
    playbackPositionState: MutableState<Float>,
    isPlayingState: MutableState<Boolean>,
    isPlaybackTrackDraggingState: MutableState<Boolean>,
    loopingState: MutableState<Int>,
    speedState: State<Float>,
    isLikedState: MutableState<Boolean>
) = Surface(
    color = Color.Transparent,
    modifier = Modifier
        .fillMaxHeight()
        .padding(top = 20.dp)
        .align(Alignment.Center),
) {
    Column(modifier = Modifier.width(800.dp)) {
        Buttons(
            rootScreen,
            tracksState,
            currentTrackState,
            isPlayingCoverLoadedState,
            playbackPositionState,
            isPlayingState,
            isPlaybackTrackDraggingState,
            loopingState,
            speedState,
            isLikedState
        )

        Spacer(modifier = Modifier.height(20.dp).weight(1F))

        Track(
            currentTrackState,
            isPlayingState,
            isPlayingCoverLoadedState,
            playbackPositionState,
            loopingState,
            tracksState,
            isPlaybackTrackDraggingState,
            speedState
        )
    }
}

@Composable
private fun BoxScope.Sliders(volumeState: MutableState<Float>, speedState: MutableState<Float>) =
    Column(modifier = Modifier.align(Alignment.CenterEnd).padding(10.dp)) {
        Volume(volumeState)
        Speed(speedState)
    }

@Composable
private fun Volume(volumeState: MutableState<Float>) = Row(modifier = Modifier.padding(10.dp)) {
    Image(
        painter = painterResource("images/volume_icon.png"),
        contentDescription = Localization.trackCover.resource,
        modifier = Modifier.width(40.dp).height(40.dp).align(Alignment.CenterVertically),
        colorFilter = ColorFilter.tint(Params.secondaryAlternativeColor),
        contentScale = ContentScale.Inside
    )

    Slider(
        value = volumeState.value,
        valueRange = (0F..2F),
        colors = SliderDefaults.colors(
            thumbColor = Params.secondaryAlternativeColor,
            activeTrackColor = Params.secondaryAlternativeColor,
            inactiveTrackColor = Params.secondaryColor
        ),
        modifier = Modifier.width(150.dp),
        onValueChange = { volumeState.value = it },
        onValueChangeFinished = { RustLibs.setVolume(volumeState.value) }
    )

    Spacer(Modifier.width(5.dp))

    Text(
        text = volumeState.value.take(4),
        fontSize = 14.sp,
        color = Params.secondaryAlternativeColor,
        modifier = Modifier.align(Alignment.CenterVertically)
    )
}

@Composable
private fun Speed(speedState: MutableState<Float>) = Row(modifier = Modifier.padding(10.dp)) {
    Image(
        painter = painterResource("images/speed.png"),
        contentDescription = Localization.trackCover.resource,
        modifier = Modifier.width(40.dp).height(40.dp).align(Alignment.CenterVertically),
        colorFilter = ColorFilter.tint(Params.secondaryAlternativeColor),
        contentScale = ContentScale.Inside
    )

    Slider(
        value = speedState.value,
        valueRange = (0.5F..2F),
        colors = SliderDefaults.colors(
            thumbColor = Params.secondaryAlternativeColor,
            activeTrackColor = Params.secondaryAlternativeColor,
            inactiveTrackColor = Params.secondaryColor
        ),
        modifier = Modifier.width(150.dp),
        onValueChange = {
            speedState.value = it
        },
        onValueChangeFinished = { RustLibs.setSpeed(speedState.value) }
    )

    Spacer(Modifier.width(5.dp))

    Text(
        text = speedState.value.take(4),
        fontSize = 14.sp,
        color = Params.secondaryAlternativeColor,
        modifier = Modifier.align(Alignment.CenterVertically)
    )
}