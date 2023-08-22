package com.paranid5.prima.presentation.ui.tracks

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paranid5.prima.data.Track
import com.paranid5.prima.di.*
import com.paranid5.prima.domain.StorageHandler
import com.paranid5.prima.domain.cancelPlaybackControlTasks
import com.paranid5.prima.domain.extensions.correctUTF8String
import com.paranid5.prima.domain.startPlaybackControlTasks
import com.paranid5.prima.rust.RustLibs
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.jaudiotagger.audio.AudioFileIO
import org.koin.compose.koinInject
import org.koin.core.qualifier.named
import java.io.File

@Composable
fun TrackItem(
    tracksOnScreen: List<Track>,
    index: Int,
    allTracksState: MutableStateFlow<List<Track>>,
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject(),
    selectedTrackState: MutableStateFlow<Track?> = koinInject(named(KOIN_SELECTED_TRACK)),
    isPlayingState: MutableStateFlow<Boolean> = koinInject(named(KOIN_IS_PLAYING)),
    isPlayingCoverLoadedState: MutableStateFlow<Boolean> = koinInject(named(KOIN_IS_PLAYING_COVER_LOADED)),
    playbackPositionState: MutableStateFlow<Float> = koinInject(named(KOIN_PLAYBACK_POS)),
    loopingState: MutableStateFlow<Int> = koinInject(named(KOIN_LOOPING)),
    isPlaybackTrackDraggingState: MutableStateFlow<Boolean> = koinInject(named(KOIN_IS_PLAYBACK_TRACK_DRAGGING)),
    speedState: MutableStateFlow<Float> = koinInject(named(KOIN_SPEED))
) {
    val primaryColor by storageHandler.primaryColorState.collectAsState()
    val secondaryColor by storageHandler.secondaryColorState.collectAsState()
    val secondaryAlternativeColor by storageHandler.secondaryAlternativeColorState.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val isCoverLoadedState = remember { mutableStateOf(false) }
    val coverState = remember { mutableStateOf(ImageBitmap(0, 0)) }
    val track by remember { derivedStateOf { tracksOnScreen[index] } }
    val isPopupMenuExpandedState = remember { mutableStateOf(false) }

    var textColor by remember {
        mutableStateOf(
            when (track) {
                selectedTrackState.value -> primaryColor
                else -> secondaryAlternativeColor
            }
        )
    }

    LaunchedEffect(Unit) {
        val cover = withContext(Dispatchers.IO) {
            val coverBytes = AudioFileIO
                .read(File(track.path.correctUTF8String))
                .tagOrCreateAndSetDefault
                ?.firstArtwork
                ?.binaryData
                ?: return@withContext null

            org.jetbrains.skia.Image.makeFromEncoded(coverBytes).toComposeImageBitmap()
        }

        cover?.let {
            coverState.value = it
            isCoverLoadedState.value = true
        }
    }

    LaunchedEffect(selectedTrackState.value) {
        textColor = when (track) {
            selectedTrackState.value -> primaryColor
            else -> secondaryAlternativeColor
        }
    }

    Card(
        backgroundColor = primaryColor,
        elevation = 15.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Button(
            onClick = {
                onTrackClicked(
                    clickedTrack = track,
                    tracksOnScreen = tracksOnScreen,
                    index = index,
                    selectedTrackState = selectedTrackState,
                    isPlayingState = isPlayingState,
                    isPlayingCoverLoadedState = isPlayingCoverLoadedState,
                    playbackPositionState = playbackPositionState,
                    loopingState = loopingState,
                    currentPlaylistTracksState = allTracksState,
                    isPlaybackTrackDraggingState = isPlaybackTrackDraggingState,
                    speedState = speedState,
                    coroutineScope
                )
            },
            modifier = Modifier.fillMaxSize().padding(3.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = secondaryColor),
        ) {
            Row(Modifier.fillMaxWidth()) {
                TrackCover(
                    isCoverLoaded = isCoverLoadedState.value,
                    cover = coverState.value,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .align(Alignment.CenterVertically),
                )

                Spacer(Modifier.width(20.dp).fillMaxHeight())

                TrackInfoLabels(
                    track = track,
                    textColor = textColor,
                    modifier = Modifier
                        .weight(1F)
                        .align(Alignment.CenterVertically)
                )

                Spacer(Modifier.width(20.dp).fillMaxHeight())

                TrackSettingsButton(
                    track = track,
                    isPopupMenuExpandedState = isPopupMenuExpandedState,
                    modifier = Modifier
                        .width(50.dp)
                        .fillMaxHeight()
                        .align(Alignment.CenterVertically)
                )
            }
        }
    }
}

@Composable
private fun TrackCover(
    isCoverLoaded: Boolean,
    cover: ImageBitmap,
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject()
) {
    val lang by storageHandler.languageState.collectAsState()

    when {
        isCoverLoaded -> Image(
            bitmap = cover,
            contentDescription = lang.trackCover,
            filterQuality = FilterQuality.High,
            modifier = modifier
        )

        else -> Image(
            painter = painterResource("images/default_cover.png"),
            contentDescription = lang.trackCover,
            modifier = modifier
        )
    }
}

@Composable
private fun TrackInfoLabels(
    track: Track,
    textColor: Color,
    modifier: Modifier = Modifier
) = Column(modifier) {
    TrackTitleLabel(trackTitle = track.title, textColor = textColor)

    TrackArtistAlbumLabel(
        trackArtist = track.artist,
        trackAlbum = track.album,
        textColor = textColor
    )
}

@Composable
private fun TrackTitleLabel(
    trackTitle: String?,
    textColor: Color,
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject()
) {
    val lang by storageHandler.languageState.collectAsState()

    Text(
        text = trackTitle?.takeIf(String::isNotEmpty) ?: lang.unknownTrack,
        fontSize = 18.sp,
        color = textColor,
        modifier = modifier
    )
}

@Composable
private fun TrackArtistAlbumLabel(
    trackArtist: String?,
    trackAlbum: String?,
    textColor: Color,
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject()
) {
    val lang by storageHandler.languageState.collectAsState()

    Text(
        text = "${
            trackArtist?.takeIf(String::isNotEmpty) ?: lang.unknownArtist
        } / ${
            trackAlbum?.takeIf(String::isNotEmpty) ?: lang.unknownAlbum
        }",
        fontSize = 14.sp,
        color = textColor,
        modifier = modifier
    )
}

@Composable
private fun TrackSettingsButton(
    track: Track,
    isPopupMenuExpandedState: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject(),
) {
    val lang by storageHandler.languageState.collectAsState()
    val primaryColor by storageHandler.primaryColorState.collectAsState()

    Button(
        onClick = { isPopupMenuExpandedState.value = true },
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
        elevation = null,
        modifier = modifier,
    ) {
        Image(
            painter = painterResource("images/three_dots.png"),
            contentDescription = lang.trackCover,
            modifier = Modifier.fillMaxSize(),
            colorFilter = ColorFilter.tint(primaryColor),
            contentScale = ContentScale.Inside
        )

        TrackSettingsMenu(
            track = track,
            isPopupMenuExpandedState = isPopupMenuExpandedState
        )
    }
}

@Composable
private fun TrackSettingsMenu(
    track: Track,
    isPopupMenuExpandedState: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject(),
    selectedTrackState: MutableStateFlow<Track?> = koinInject(named(KOIN_SELECTED_TRACK)),
    isLikedState: MutableStateFlow<Boolean> = koinInject(named(KOIN_IS_LIKED)),
) {
    val lang by storageHandler.languageState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    DropdownMenu(
        expanded = isPopupMenuExpandedState.value,
        onDismissRequest = { isPopupMenuExpandedState.value = false },
        modifier = modifier
    ) {
        TrackSettingsMenuItem(title = lang.changeTrackInfo) {
            // TODO: Change track's info
        }

        TrackSettingsMenuItem(title = lang.addToQueue) {
            // TODO: Add track to queue
        }

        TrackSettingsMenuItem(title = lang.addToFavourites) {
            coroutineScope.launch(Dispatchers.IO) { RustLibs.onLikeTrackClicked(track) }
            if (track == selectedTrackState.value) isLikedState.value = !isLikedState.value
        }

        TrackSettingsMenuItem(title = lang.removeTrack) {
            // TODO: Remove track
        }

        TrackSettingsMenuItem(title = lang.lyrics) {
            // TODO: Show lyrics
        }

        TrackSettingsMenuItem(title = lang.trackInfo) {
            // TODO: Show track's information
        }

        TrackSettingsMenuItem(title = lang.trimTrack) {
            // TODO: trim track
        }

        TrackSettingsMenuItem(title = lang.hideTrack) {
            // TODO: hide track
        }
    }
}

@Composable
private fun TrackSettingsMenuItem(
    title: String,
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject(),
    onClick: () -> Unit
) {
    val secondaryAlternativeColor by storageHandler.secondaryAlternativeColorState.collectAsState()

    DropdownMenuItem(modifier = modifier, onClick = onClick) {
        Text(text = title, fontSize = 14.sp, color = secondaryAlternativeColor)
    }
}

private fun onTrackClicked(
    clickedTrack: Track,
    tracksOnScreen: List<Track>,
    index: Int,
    selectedTrackState: MutableStateFlow<Track?>,
    isPlayingState: MutableStateFlow<Boolean>,
    isPlayingCoverLoadedState: MutableStateFlow<Boolean>,
    playbackPositionState: MutableStateFlow<Float>,
    loopingState: MutableStateFlow<Int>,
    currentPlaylistTracksState: MutableStateFlow<List<Track>>,
    isPlaybackTrackDraggingState: MutableStateFlow<Boolean>,
    speedState: MutableStateFlow<Float>,
    coroutineScope: CoroutineScope
) {
    when (selectedTrackState.value) {
        clickedTrack -> isPlayingState.update { !it }

        else -> {
            selectedTrackState.update { clickedTrack }
            isPlayingState.update { true }
            playbackPositionState.update { 0F }
            isPlayingCoverLoadedState.update { false }
        }
    }

    coroutineScope.launch(Dispatchers.IO) {
        RustLibs.onTrackClickedBlocking(tracksOnScreen, index)
    }

    when {
        isPlayingState.value -> coroutineScope.launch {
            startPlaybackControlTasks(
                selectedTrackState = selectedTrackState,
                isPlayingState = isPlayingState,
                isPlayingCoverLoadedState = isPlayingCoverLoadedState,
                playbackPositionState = playbackPositionState,
                loopingState = loopingState,
                currentPlaylistTracksState = currentPlaylistTracksState,
                isPlaybackTrackDraggingState = isPlaybackTrackDraggingState,
                speedState = speedState
            )
        }

        else -> cancelPlaybackControlTasks()
    }
}