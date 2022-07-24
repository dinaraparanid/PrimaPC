package com.dinaraparanid.prima.ui.utils.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.jetbrains.Children
import com.arkivanov.decompose.extensions.compose.jetbrains.animation.child.childAnimation
import com.arkivanov.decompose.extensions.compose.jetbrains.animation.child.fade
import com.dinaraparanid.prima.entities.Track
import com.dinaraparanid.prima.ui.tracks.Tracks

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun RootView(
    rootScreen: RootScreen,
    currentTrackState: MutableState<Track?>,
    isPlayingState: MutableState<Boolean>,
    isPlayingCoverLoadedState: MutableState<Boolean>,
    playbackPositionState: MutableState<Float>,
    loopingState: MutableState<Int>,
    tracksState: SnapshotStateList<Track>,
    filteredTracksState: SnapshotStateList<Track>,
    isPlaybackTrackDraggingState: State<Boolean>,
    speedState: State<Float>
) = Children(
    routerState = rootScreen.routerState,
    animation = childAnimation(fade())
) {
    when (it.instance) {
        ScreenElement.Screen.MainMenuScreen.Tracks -> Tracks(
            currentTrackState,
            isPlayingState,
            isPlayingCoverLoadedState,
            playbackPositionState,
            loopingState,
            tracksState,
            filteredTracksState,
            isPlaybackTrackDraggingState,
            speedState
        )

        // TODO: Other screens
        ScreenElement.Screen.MainMenuScreen.TrackCollections -> Unit
        ScreenElement.Screen.MainMenuScreen.Artists -> Unit
        ScreenElement.Screen.MainMenuScreen.Favourites -> Unit
        ScreenElement.Screen.MainMenuScreen.AboutApp -> Unit
        ScreenElement.Screen.MainMenuScreen.MP3Converter -> Unit
        ScreenElement.Screen.MainMenuScreen.GTM -> Unit
        ScreenElement.Screen.MainMenuScreen.Statistics -> Unit
        ScreenElement.Screen.MainMenuScreen.Settings -> Unit
        ScreenElement.Screen.FavouritesScreen.Artists -> Unit
        ScreenElement.Screen.FavouritesScreen.TrackCollections -> Unit
        ScreenElement.Screen.FavouritesScreen.Tracks -> Unit
        ScreenElement.Screen.GTMScreen.AboutGame -> Unit
        ScreenElement.Screen.GTMScreen.Game -> Unit
        ScreenElement.Screen.PlaybarScreen.CurrentPlaylist -> Unit
        ScreenElement.Screen.PlaybarScreen.Equalizer -> Unit
        ScreenElement.Screen.SettingsScreen.FilesLocation -> Unit
        ScreenElement.Screen.SettingsScreen.Fonts -> Unit
        ScreenElement.Screen.SettingsScreen.HiddenTracks -> Unit
        ScreenElement.Screen.SettingsScreen.Themes -> Unit
        ScreenElement.Screen.PlaybarScreen.TrimTrack -> Unit
        ScreenElement.Screen.StatisticsScreen.AllTime -> Unit
        ScreenElement.Screen.StatisticsScreen.Day -> Unit
        ScreenElement.Screen.StatisticsScreen.Weak -> Unit
        ScreenElement.Screen.StatisticsScreen.Year -> Unit
        ScreenElement.Screen.TrackCollectionsScreen.Albums -> Unit
        ScreenElement.Screen.TrackCollectionsScreen.CustomPlaylists -> Unit
    }
}