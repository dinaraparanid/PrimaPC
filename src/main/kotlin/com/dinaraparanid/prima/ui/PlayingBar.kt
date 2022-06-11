package com.dinaraparanid.prima.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.dinaraparanid.prima.rust.RustLibs
import com.dinaraparanid.prima.utils.Params
import com.dinaraparanid.prima.utils.extensions.correctUTF8
import com.dinaraparanid.prima.utils.localization.Localization
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jaudiotagger.audio.AudioFileIO
import java.io.File

@Composable
fun PlayingBar() = BottomAppBar(
    modifier = Modifier.fillMaxWidth().height(150.dp),
    elevation = 10.dp
) {
    val track = RustLibs.getCurTrack()
    val coroutineScope = rememberCoroutineScope()
    val isCoverLoaded = remember { mutableStateOf(false) }
    val cover = remember { mutableStateOf(ImageBitmap(0, 0)) }

    val coverTask = track?.let { track ->
        coroutineScope.async(Dispatchers.IO) {
            AudioFileIO.read(File(track.path.correctUTF8)).tagOrCreateAndSetDefault?.firstArtwork?.binaryData
        }
    }

    coroutineScope.launch {
        coverTask?.await()?.toList()?.let {
            cover.value = withContext(Dispatchers.IO) {
                org.jetbrains.skia.Image.makeFromEncoded(it.toByteArray()).toComposeImageBitmap()
            }

            isCoverLoaded.value = true
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        color = Params.primaryColor,
        elevation = 10.dp
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(100.dp)) {
            Spacer(modifier = Modifier.width(20.dp))

            Card(
                backgroundColor = Color.Black,
                contentColor = Color.Black,
                shape = CircleShape,
                elevation = 15.dp,
                modifier = Modifier
                    .height(80.dp)
                    .width(80.dp)
                    .clip(CircleShape)
                    .align(Alignment.CenterVertically)
            ) {
                if (isCoverLoaded.value) Image(
                    bitmap = cover.value,
                    contentDescription = Localization.trackCover.resource,
                    filterQuality = FilterQuality.High,
                    modifier = Modifier
                        .padding(20.dp)
                        .clip(CircleShape)
                        .align(Alignment.CenterVertically),
                )
                else Image(
                    painter = painterResource("images/default_cover.png"),
                    contentDescription = Localization.trackCover.resource,
                    modifier = Modifier
                        .padding(20.dp)
                        .clip(CircleShape)
                        .align(Alignment.CenterVertically),
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                Text(
                    text = track?.run {
                        title?.takeIf(String::isNotEmpty) ?: Localization.unknownTrack.resource
                    } ?: "-----",
                    fontSize = 22.sp,
                    color = Params.secondaryAlternativeColor
                )

                Text(
                    text = track?.run {
                        "${
                            track.artist?.takeIf(String::isNotEmpty) ?: Localization.unknownArtist.resource
                        } / ${
                            track.album?.takeIf(String::isNotEmpty) ?: Localization.unknownAlbum.resource
                        }"
                    } ?: "-----",
                    fontSize = 16.sp,
                    color = Params.secondaryAlternativeColor
                )
            }

            Surface(modifier = Modifier.weight(1F).padding(top = 10.dp).fillMaxSize(), color = Color.Transparent) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(modifier = Modifier.width(500.dp).weight(1F).align(Alignment.CenterHorizontally)) {
                        Button(
                            colors = ButtonDefaults.buttonColors(Color.Transparent),
                            elevation = null,
                            contentPadding = PaddingValues(3.dp),
                            modifier = Modifier.weight(3F).align(Alignment.CenterVertically),
                            onClick = {
                                // TODO: Like track
                            }
                        ) {
                            Image(
                                painter = painterResource("images/heart.png"),
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
                                // TODO: Like track
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
                                // TODO: Like track
                            }
                        ) {
                            Image(
                                painter = painterResource("images/play.png"),
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
                                // TODO: Like track
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
                                // TODO: Like track
                            }
                        ) {
                            Image(
                                painter = painterResource("images/repeat.png"),
                                contentDescription = Localization.trackCover.resource,
                                colorFilter = ColorFilter.tint(Params.secondaryAlternativeColor),
                                contentScale = ContentScale.Inside,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp).weight(1F))

                    Column(modifier = Modifier.width(750.dp).weight(1.5F).align(Alignment.CenterHorizontally)) {
                        val sliderPosition = remember { mutableStateOf(0F) } // TODO: load position

                        Slider(
                            value = sliderPosition.value,
                            colors = SliderDefaults.colors(
                                thumbColor = Params.secondaryAlternativeColor,
                                activeTrackColor = Params.secondaryAlternativeColor,
                                inactiveTrackColor = Params.secondaryColor
                            ),
                            modifier = Modifier.fillMaxWidth().height(20.dp),
                            onValueChange = { sliderPosition.value = it },
                            onValueChangeFinished = {
                                // TODO: playback seek to position
                            }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(text = "00:00", color = Params.secondaryAlternativeColor, fontSize = 14.sp)
                            Spacer(modifier = Modifier.weight(1F))
                            Text(text = "00:00", color = Params.secondaryAlternativeColor, fontSize = 14.sp)
                        }
                    }
                }
            }

            Row(modifier = Modifier.align(Alignment.CenterVertically).padding(10.dp)) {
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
        }
    }
}