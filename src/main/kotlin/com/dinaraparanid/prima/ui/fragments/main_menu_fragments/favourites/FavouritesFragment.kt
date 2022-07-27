package com.dinaraparanid.prima.ui.fragments.main_menu_fragments.favourites

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.jetbrains.Children
import com.arkivanov.decompose.extensions.compose.jetbrains.animation.child.childAnimation
import com.arkivanov.decompose.extensions.compose.jetbrains.animation.child.fade
import com.dinaraparanid.prima.entities.Track
import com.dinaraparanid.prima.ui.fragments.main_menu_fragments.favourites.tracks.FavouritesTracksFragment
import com.dinaraparanid.prima.ui.utils.navigation.ScreenElement

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun FavouritesFragment(
    favouritesScreen: FavouritesScreen,
    currentTrackState: MutableState<Track?>,
    isPlayingState: MutableState<Boolean>,
    isPlayingCoverLoadedState: MutableState<Boolean>,
    playbackPositionState: MutableState<Float>,
    loopingState: MutableState<Int>,
    tracksState: SnapshotStateList<Track>,
    filteredTracksState: SnapshotStateList<Track>,
    isPlaybackTrackDraggingState: State<Boolean>,
    speedState: State<Float>,
) = Children(
    routerState = favouritesScreen.routerState,
    animation = childAnimation(fade())
) {
    when (it.instance) {
        ScreenElement.Screen.FavouritesScreen.Tracks -> FavouritesTracksFragment(
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

        ScreenElement.Screen.FavouritesScreen.Artists -> TODO()
        ScreenElement.Screen.FavouritesScreen.TrackCollections -> TODO()
    }
}