package com.dinaraparanid.prima.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
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
import com.dinaraparanid.prima.utils.Params
import com.dinaraparanid.prima.utils.calcTrackTime
import com.dinaraparanid.prima.utils.extensions.correctUTF8
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
    currentTrackState: MutableState<Track?>,
    isPlayingCoverLoadedState: MutableState<Boolean>,
    playbackPositionState: MutableState<Float>,
    isPlayingState: MutableState<Boolean>
) =
    BottomAppBar(
        modifier = Modifier.fillMaxWidth().height(150.dp),
        elevation = 10.dp
    ) {
        val coroutineScope = rememberCoroutineScope()
        val coverState = remember { mutableStateOf(ImageBitmap(0, 0)) }

        val coverTask = currentTrackState.value?.let { track ->
            coroutineScope.async(Dispatchers.IO) {
                AudioFileIO.read(File(track.path.correctUTF8)).tagOrCreateAndSetDefault?.firstArtwork?.binaryData
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
                ButtonsAndTrack(currentTrackState, isPlayingCoverLoadedState, playbackPositionState, isPlayingState)
                Volume()
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
    currentTrackState: MutableState<Track?>,
    isPlayingCoverLoadedState: MutableState<Boolean>,
    playbackPositionState: MutableState<Float>,
    isPlayingState: MutableState<Boolean>
) {
    val isLikedState = remember { mutableStateOf(false) } // TODO: Load like status
    val loopingState = remember { mutableStateOf(0) }          // TODO: Load looping status

    Row(modifier = Modifier.width(500.dp).weight(1F).align(Alignment.CenterHorizontally)) {
        Button(
            colors = ButtonDefaults.buttonColors(Color.Transparent),
            elevation = null,
            contentPadding = PaddingValues(3.dp),
            modifier = Modifier.weight(3F).align(Alignment.CenterVertically),
            onClick = {
                isLikedState.value = !isLikedState.value
                // TODO: Like track
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
                RustLibs.onPreviousTrackClicked()
                currentTrackState.value = RustLibs.getCurTrack()
                isPlayingState.value = true
                isPlayingCoverLoadedState.value = false
                playbackPositionState.value = 0F
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
                isPlayingState.value = !isPlayingState.value
                // TODO: Play / pause track
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
                RustLibs.onNextTrackClicked()
                currentTrackState.value = RustLibs.getCurTrack()
                isPlayingState.value = true
                isPlayingCoverLoadedState.value = false
                playbackPositionState.value = 0F
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
            onClick = {
                loopingState.value = (loopingState.value + 1) % 3
                // TODO: Change looping status
            }
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
    }
}

@Composable
private fun ColumnScope.Track(currentTrackState: State<Track?>, playbackPositionState: MutableState<Float>) =
    Column(modifier = Modifier.fillMaxWidth().weight(1.5F).align(Alignment.CenterHorizontally)) {
        val currentTimeState = remember { mutableStateOf(0) } // TODO: load current time

        Slider(
            value = playbackPositionState.value,
            valueRange = (0F..(currentTrackState.value?.duration?.toFloat() ?: 1F)),
            colors = SliderDefaults.colors(
                thumbColor = Params.secondaryAlternativeColor,
                activeTrackColor = Params.secondaryAlternativeColor,
                inactiveTrackColor = Params.secondaryColor
            ),
            modifier = Modifier.fillMaxWidth().height(20.dp),
            onValueChange = {
                playbackPositionState.value = it
                currentTimeState.value = playbackPositionState.value.toInt()
            },
            onValueChangeFinished = {
                // TODO: Seek playback to position
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = calcTrackTime(currentTimeState.value).timeString,
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
    currentTrackState: MutableState<Track?>,
    isPlayingCoverLoadedState: MutableState<Boolean>,
    playbackPositionState: MutableState<Float>,
    isPlayingState: MutableState<Boolean>
) = Surface(
    color = Color.Transparent,
    modifier = Modifier
        .fillMaxHeight()
        .padding(top = 20.dp)
        .align(Alignment.Center),
) {
    Column(modifier = Modifier.width(750.dp)) {
        Buttons(currentTrackState, isPlayingCoverLoadedState, playbackPositionState, isPlayingState)
        Spacer(modifier = Modifier.height(20.dp).weight(1F))
        Track(currentTrackState, playbackPositionState)
    }
}

@Composable
private fun BoxScope.Volume() = Row(modifier = Modifier.align(Alignment.CenterEnd).padding(10.dp)) {
    Image(
        painter = painterResource("images/volume_icon.png"),
        contentDescription = Localization.trackCover.resource,
        modifier = Modifier.width(40.dp).height(40.dp).align(Alignment.CenterVertically),
        colorFilter = ColorFilter.tint(Params.secondaryAlternativeColor),
        contentScale = ContentScale.Inside
    )

    val sliderPosition = remember { mutableStateOf(0F) } // TODO: load volume

    Slider(
        value = sliderPosition.value,
        colors = SliderDefaults.colors(
            thumbColor = Params.secondaryAlternativeColor,
            activeTrackColor = Params.secondaryAlternativeColor,
            inactiveTrackColor = Params.secondaryColor
        ),
        modifier = Modifier.width(150.dp),
        onValueChange = { sliderPosition.value = it },
        onValueChangeFinished = {
            // TODO: playback seek to position
        }
    )
}