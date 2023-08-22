package com.paranid5.prima.presentation.ui.playing_bar

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.paranid5.prima.data.Track
import com.paranid5.prima.di.*
import com.paranid5.prima.domain.*
import com.paranid5.prima.presentation.ui.navigation.composition_locals.LocalRootNavigator
import com.paranid5.prima.rust.RustLibs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
fun Buttons(modifier: Modifier = Modifier) = Row(modifier) {
    val buttonModifier = Modifier.weight(3F).align(Alignment.CenterVertically)

    CurrentPlaylistButton(buttonModifier)
    ButtonSpacer()

    LikeButton(buttonModifier)
    ButtonSpacer()

    PrevTrackButton(buttonModifier)
    ButtonSpacer()

    PlayPauseButton(buttonModifier)
    ButtonSpacer()

    NextTrackButton(buttonModifier)
    ButtonSpacer()

    LoopingButton(buttonModifier)
    ButtonSpacer()

    TrimTrackButton(Modifier.weight(3F).align(Alignment.CenterVertically))
}

context(RowScope)
@Composable
private fun ButtonSpacer() = Spacer(Modifier.height(20.dp).weight(1F))

@Composable
private fun CurrentPlaylistButton(
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject()
) {
    val navigator = LocalRootNavigator.current
    val lang by storageHandler.languageState.collectAsState()
    val secondaryAlternativeColor by storageHandler.secondaryAlternativeColorState.collectAsState()

    Button(
        colors = ButtonDefaults.buttonColors(Color.Transparent),
        elevation = null,
        contentPadding = PaddingValues(3.dp),
        modifier = modifier,
        onClick = navigator::changeConfigToCurPlaylist
    ) {
        Image(
            painter = painterResource("images/playlist.png"),
            contentDescription = lang.trackCover,
            colorFilter = ColorFilter.tint(secondaryAlternativeColor),
        )
    }
}

@Composable
private fun LikeButton(
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject(),
    selectedTrackState: MutableStateFlow<Track?> = koinInject(named(KOIN_SELECTED_TRACK)),
    isLikedState: MutableStateFlow<Boolean> = koinInject(named(KOIN_IS_LIKED))
) {
    val coroutineScope = rememberCoroutineScope()
    val currentTrack by selectedTrackState.collectAsState()
    val isLiked by isLikedState.collectAsState()

    val lang by storageHandler.languageState.collectAsState()
    val secondaryAlternativeColor by storageHandler.secondaryAlternativeColorState.collectAsState()

    Button(
        colors = ButtonDefaults.buttonColors(Color.Transparent),
        elevation = null,
        contentPadding = PaddingValues(3.dp),
        modifier = modifier,
        onClick = {
            if (currentTrack != null) {
                isLikedState.update { !it }

                coroutineScope.launch(Dispatchers.IO) {
                    RustLibs.onLikeTrackClicked(currentTrack ?: return@launch)
                }
            }
        }
    ) {
        @Composable
        fun LikeImage(resource: String) = Image(
            painter = painterResource(resource),
            contentDescription = lang.trackCover,
            colorFilter = ColorFilter.tint(secondaryAlternativeColor),
            contentScale = ContentScale.Inside
        )

        when {
            isLiked -> LikeImage("images/heart_like.png")
            else -> LikeImage("images/heart.png")
        }
    }
}

@Composable
private fun PrevTrackButton(
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject(),
    selectedTrackState: MutableStateFlow<Track?> = koinInject(named(KOIN_SELECTED_TRACK)),
    isPlayingState: MutableStateFlow<Boolean> = koinInject(named(KOIN_IS_PLAYING)),
    isPlayingCoverLoadedState: MutableStateFlow<Boolean> = koinInject(named(KOIN_IS_PLAYING_COVER_LOADED)),
    playbackPositionState: MutableStateFlow<Float> = koinInject(named(KOIN_PLAYBACK_POS)),
    loopingState: MutableStateFlow<Int> = koinInject(named(KOIN_LOOPING)),
    currentPlaylistTracksState: MutableStateFlow<List<Track>> = koinInject(named(KOIN_CURRENT_PLAYLIST_TRACKS)),
    isPlaybackTrackDraggingState: MutableStateFlow<Boolean> = koinInject(named(KOIN_IS_PLAYBACK_TRACK_DRAGGING)),
    speedState: MutableStateFlow<Float> = koinInject(named(KOIN_SPEED))

) {
    val coroutineScope = rememberCoroutineScope()
    val currentTrack by selectedTrackState.collectAsState()

    val lang by storageHandler.languageState.collectAsState()
    val secondaryAlternativeColor by storageHandler.secondaryAlternativeColorState.collectAsState()

    Button(
        colors = ButtonDefaults.buttonColors(Color.Transparent),
        elevation = null,
        contentPadding = PaddingValues(horizontal = 20.dp),
        modifier = modifier,
        onClick = {
            if (currentTrack != null) coroutineScope.launch {
                switchToPrevTrack(
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
        }
    ) {
        Image(
            painter = painterResource("images/prev_track.png"),
            contentDescription = lang.trackCover,
            colorFilter = ColorFilter.tint(secondaryAlternativeColor),
            contentScale = ContentScale.FillBounds,
        )
    }
}

@Composable
private fun PlayPauseButton(
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject(),
    selectedTrackState: MutableStateFlow<Track?> = koinInject(named(KOIN_SELECTED_TRACK)),
    isPlayingState: MutableStateFlow<Boolean> = koinInject(named(KOIN_IS_PLAYING)),
    isPlayingCoverLoadedState: MutableStateFlow<Boolean> = koinInject(named(KOIN_IS_PLAYING_COVER_LOADED)),
    playbackPositionState: MutableStateFlow<Float> = koinInject(named(KOIN_PLAYBACK_POS)),
    loopingState: MutableStateFlow<Int> = koinInject(named(KOIN_LOOPING)),
    currentPlaylistTracksState: MutableStateFlow<List<Track>> = koinInject(named(KOIN_CURRENT_PLAYLIST_TRACKS)),
    isPlaybackTrackDraggingState: MutableStateFlow<Boolean> = koinInject(named(KOIN_IS_PLAYBACK_TRACK_DRAGGING)),
    speedState: MutableStateFlow<Float> = koinInject(named(KOIN_SPEED))
) {
    val coroutineScope = rememberCoroutineScope()
    val currentTrack by selectedTrackState.collectAsState()
    val isPlaying by isPlayingState.collectAsState()

    val lang by storageHandler.languageState.collectAsState()
    val secondaryAlternativeColor by storageHandler.secondaryAlternativeColorState.collectAsState()

    Button(
        colors = ButtonDefaults.buttonColors(Color.Transparent),
        elevation = null,
        contentPadding = PaddingValues(),
        modifier = modifier,
        onClick = {
            if (currentTrack != null) {
                coroutineScope.launch(Dispatchers.IO) {
                    RustLibs.onPlayButtonClickedBlocking()
                }

                val playing = isPlayingState.updateAndGet { !it }

                when {
                    playing -> coroutineScope.launch {
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
        }
    ) {
        @Composable
        fun PlayingImage(resource: String) = Image(
            painter = painterResource(resource),
            contentDescription = lang.trackCover,
            colorFilter = ColorFilter.tint(secondaryAlternativeColor),
            contentScale = ContentScale.Inside,
        )

        when {
            isPlaying -> PlayingImage("images/pause.png")
            else -> PlayingImage("images/play.png")
        }
    }
}

@Composable
private fun NextTrackButton(
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject(),
    selectedTrackState: MutableStateFlow<Track?> = koinInject(named(KOIN_SELECTED_TRACK)),
    isPlayingState: MutableStateFlow<Boolean> = koinInject(named(KOIN_IS_PLAYING)),
    isPlayingCoverLoadedState: MutableStateFlow<Boolean> = koinInject(named(KOIN_IS_PLAYING_COVER_LOADED)),
    playbackPositionState: MutableStateFlow<Float> = koinInject(named(KOIN_PLAYBACK_POS)),
    loopingState: MutableStateFlow<Int> = koinInject(named(KOIN_LOOPING)),
    currentPlaylistTracksState: MutableStateFlow<List<Track>> = koinInject(named(KOIN_CURRENT_PLAYLIST_TRACKS)),
    isPlaybackTrackDraggingState: MutableStateFlow<Boolean> = koinInject(named(KOIN_IS_PLAYBACK_TRACK_DRAGGING)),
    speedState: MutableStateFlow<Float> = koinInject(named(KOIN_SPEED))
) {
    val coroutineScope = rememberCoroutineScope()
    val currentTrack by selectedTrackState.collectAsState()

    val lang by storageHandler.languageState.collectAsState()
    val secondaryAlternativeColor by storageHandler.secondaryAlternativeColorState.collectAsState()

    Button(
        colors = ButtonDefaults.buttonColors(Color.Transparent),
        elevation = null,
        contentPadding = PaddingValues(horizontal = 20.dp),
        modifier = modifier,
        onClick = {
            if (currentTrack != null) coroutineScope.launch {
                switchToNextTrack(
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
        }
    ) {
        Image(
            painter = painterResource("images/next_track.png"),
            contentDescription = lang.trackCover,
            colorFilter = ColorFilter.tint(secondaryAlternativeColor),
            contentScale = ContentScale.FillBounds,
        )
    }
}

@Composable
private fun LoopingButton(
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject(),
    loopingState: MutableStateFlow<Int> = koinInject(named(KOIN_LOOPING))
) {
    val coroutineScope = rememberCoroutineScope()
    val looping by loopingState.collectAsState()

    val lang by storageHandler.languageState.collectAsState()
    val secondaryAlternativeColor by storageHandler.secondaryAlternativeColorState.collectAsState()

    Button(
        colors = ButtonDefaults.buttonColors(Color.Transparent),
        elevation = null,
        contentPadding = PaddingValues(2.dp),
        modifier = modifier,
        onClick = {
            coroutineScope.launch(Dispatchers.IO) {
                loopingState.update { RustLibs.setNextLoopingStateBlocking() }
            }
        }
    ) {
        @Composable
        fun RepeatImage(resource: String) = Image(
            painter = painterResource(resource),
            contentDescription = lang.trackCover,
            colorFilter = ColorFilter.tint(secondaryAlternativeColor),
            contentScale = ContentScale.Inside,
        )

        when (looping) {
            0 -> RepeatImage("images/repeat.png")
            1 -> RepeatImage("images/repeat_1.png")
            2 -> RepeatImage("images/no_repeat.png")
        }
    }
}

@Composable
private fun TrimTrackButton(
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject(),
) {
    val lang by storageHandler.languageState.collectAsState()
    val secondaryAlternativeColor by storageHandler.secondaryAlternativeColorState.collectAsState()

    Button(
        colors = ButtonDefaults.buttonColors(Color.Transparent),
        elevation = null,
        contentPadding = PaddingValues(1.dp),
        modifier = modifier,
        onClick = {
            // TODO: Trim track
        }
    ) {
        Image(
            painter = painterResource("images/scissors_start.png"),
            contentDescription = lang.trackCover,
            colorFilter = ColorFilter.tint(secondaryAlternativeColor),
        )
    }
}