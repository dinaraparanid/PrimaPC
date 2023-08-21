package com.paranid5.prima.presentation.ui.playing_bar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paranid5.prima.data.Track
import com.paranid5.prima.di.KOIN_IS_PLAYING_COVER_LOADED
import com.paranid5.prima.di.KOIN_SELECTED_TRACK
import com.paranid5.prima.domain.StorageHandler
import com.paranid5.prima.domain.extensions.correctUTF8String
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import org.jaudiotagger.audio.AudioFileIO
import org.koin.compose.koinInject
import org.koin.core.qualifier.named
import java.io.File

@Composable
fun CurrentTrackData(modifier: Modifier = Modifier) =
    Row(modifier.fillMaxHeight()) {
        Spacer(Modifier.width(20.dp))

        TrackCover(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .align(Alignment.CenterVertically),
            imageModifier = Modifier
                .padding(24.dp)
                .clip(CircleShape)
        )

        Spacer(Modifier.width(20.dp))

        Column(Modifier.width(300.dp).align(Alignment.CenterVertically)) {
            TrackTitleLabel()
            TrackArtistAlbumLabel()
        }
    }

@Composable
private fun TrackCover(
    modifier: Modifier = Modifier,
    imageModifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject(),
    selectedTrackState: MutableStateFlow<Track?> = koinInject(named(KOIN_SELECTED_TRACK)),
    isPlayingCoverLoadedState: MutableStateFlow<Boolean> = koinInject(named(KOIN_IS_PLAYING_COVER_LOADED))
) {
    val lang by storageHandler.languageState.collectAsState()
    val isPlayingCoverLoaded by isPlayingCoverLoadedState.collectAsState()

    val currentTrack by selectedTrackState.collectAsState()
    var cover by remember { mutableStateOf(ImageBitmap(0, 0)) }

    LaunchedEffect(currentTrack) {
        val cov = withContext(Dispatchers.IO) {
            val data = currentTrack
                ?.path
                ?.correctUTF8String
                ?.let(::File)
                ?.let(AudioFileIO::read)
                ?.tagOrCreateAndSetDefault
                ?.firstArtwork
                ?.binaryData
                ?: return@withContext null

            org.jetbrains.skia.Image.makeFromEncoded(data).toComposeImageBitmap()
        }

        cover = cov ?: return@LaunchedEffect
        isPlayingCoverLoadedState.update { true }
    }

    Card(
        backgroundColor = Color.Black,
        contentColor = Color.Black,
        shape = CircleShape,
        elevation = 15.dp,
        modifier = modifier
    ) {
        Box(Modifier.fillMaxSize()) {
            when {
                isPlayingCoverLoaded -> Image(
                    bitmap = cover,
                    contentDescription = lang.trackCover,
                    contentScale = ContentScale.Crop,
                    filterQuality = FilterQuality.High,
                    modifier = imageModifier
                        .size(100.dp)
                        .align(Alignment.Center)
                )

                else -> Image(
                    painter = painterResource("images/default_cover.png"),
                    contentDescription = lang.trackCover,
                    contentScale = ContentScale.Crop,
                    modifier = imageModifier
                        .matchParentSize()
                        .align(Alignment.Center)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TrackTitleLabel(
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject(),
    selectedTrackState: MutableStateFlow<Track?> = koinInject(named(KOIN_SELECTED_TRACK))
) {
    val lang by storageHandler.languageState.collectAsState()
    val secondaryAlternativeColor by storageHandler.secondaryAlternativeColorState.collectAsState()
    val currentTrack by selectedTrackState.collectAsState()

    Text(
        text = currentTrack?.run {
            title?.takeIf(String::isNotEmpty) ?: lang.unknownTrack
        } ?: "-----",
        fontSize = 22.sp,
        color = secondaryAlternativeColor,
        maxLines = 1,
        modifier = modifier.basicMarquee()
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TrackArtistAlbumLabel(
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject(),
    selectedTrackState: MutableStateFlow<Track?> = koinInject(named(KOIN_SELECTED_TRACK))
) {
    val lang by storageHandler.languageState.collectAsState()
    val secondaryAlternativeColor by storageHandler.secondaryAlternativeColorState.collectAsState()
    val currentTrack by selectedTrackState.collectAsState()

    Text(
        text = currentTrack?.run {
            "${
                artist?.takeIf(String::isNotEmpty) ?: lang.unknownArtist
            } / ${
                album?.takeIf(String::isNotEmpty) ?: lang.unknownAlbum
            }"
        } ?: "-----",
        fontSize = 16.sp,
        color = secondaryAlternativeColor,
        maxLines = 1,
        modifier = modifier.basicMarquee()
    )
}