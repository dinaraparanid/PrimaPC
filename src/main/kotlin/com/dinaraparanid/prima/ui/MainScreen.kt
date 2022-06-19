package com.dinaraparanid.prima.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.dinaraparanid.prima.entities.Track
import com.dinaraparanid.prima.rust.RustLibs
import com.dinaraparanid.prima.ui.tracks.Tracks
import com.dinaraparanid.prima.utils.Params

@Composable
@Preview
fun MainScreen() {
    val primary = Params.primaryColor
    val secondary = Params.secondaryColor

    MaterialTheme(
        colors = Colors(
            primary = primary,
            primaryVariant = primary,
            secondary = secondary,
            secondaryVariant = secondary,
            background = secondary,
            surface = secondary,
            error = Color.Red,
            onPrimary = primary,
            onSecondary = secondary,
            onBackground = secondary,
            onSurface = secondary,
            onError = Color.Red,
            isLight = !Params.theme.isNight
        )
    ) {
        val isPlayingState = remember { mutableStateOf(false) }
        val tracksState = remember { mutableStateListOf<Track>() }
        val currentTrackState = remember { mutableStateOf(RustLibs.getCurTrack()) }
        val isPlayingCoverLoadedState = remember { mutableStateOf(false) }
        val playbackPositionState = remember { mutableStateOf(0F) } // TODO: load position

        Surface(color = secondary, modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                Scaffold(
                    topBar = { AppBar() },
                    bottomBar = {
                        PlayingBar(
                            tracksState,
                            currentTrackState,
                            isPlayingCoverLoadedState,
                            playbackPositionState,
                            isPlayingState
                        )
                    }
                ) {
                    // TODO: Load first screen

                    Tracks(
                        tracksState,
                        currentTrackState,
                        isPlayingCoverLoadedState,
                        playbackPositionState,
                        isPlayingState,
                    )
                }
            }
        }
    }
}