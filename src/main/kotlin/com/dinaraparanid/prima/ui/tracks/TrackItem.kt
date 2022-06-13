package com.dinaraparanid.prima.ui.tracks

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dinaraparanid.prima.entities.Track
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
@OptIn(ExperimentalFoundationApi::class)
fun LazyItemScope.TrackItem(
    track: Track,
    currentTrackState: MutableState<Track?>,
    isPlayingCoverLoadedState: MutableState<Boolean>
) {
    val coroutineScope = rememberCoroutineScope()
    val isCoverLoadedState = remember { mutableStateOf(false) }
    val coverState = remember { mutableStateOf(ImageBitmap(0, 0)) }

    val coverTask = coroutineScope.async(Dispatchers.IO) {
        AudioFileIO.read(File(track.path.correctUTF8)).tagOrCreateAndSetDefault?.firstArtwork?.binaryData
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
                currentTrackState.value = track
                isPlayingCoverLoadedState.value = false

                // TODO: Play track
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

                Column(modifier = Modifier.weight(1F).align(Alignment.CenterVertically)) {
                    Text(
                        text = track.title?.takeIf(String::isNotEmpty) ?: Localization.unknownTrack.resource,
                        fontSize = 18.sp,
                        color = Params.secondaryAlternativeColor
                    )

                    Text(
                        text = "${
                            track.artist?.takeIf(String::isNotEmpty) ?: Localization.unknownArtist.resource
                        } / ${
                            track.album?.takeIf(String::isNotEmpty) ?: Localization.unknownAlbum.resource
                        }",
                        fontSize = 14.sp,
                        color = Params.secondaryAlternativeColor
                    )
                }

                Spacer(Modifier.width(20.dp).fillMaxHeight())

                Button(
                    onClick = {
                        // TODO: Track settings
                    },
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
                }
            }
        }
    }
}