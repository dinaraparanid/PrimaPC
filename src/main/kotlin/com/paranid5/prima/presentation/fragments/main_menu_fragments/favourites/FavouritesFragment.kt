package com.paranid5.prima.presentation.fragments.main_menu_fragments.favourites

import androidx.compose.foundation.layout.*
import androidx.compose.material.BottomNavigation
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.Children
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.fade
import com.paranid5.prima.data.Artist
import com.paranid5.prima.data.Track
import com.paranid5.prima.presentation.fragments.main_menu_fragments.favourites.artists.FavouriteArtistsFragment
import com.paranid5.prima.presentation.fragments.main_menu_fragments.favourites.tracks.FavouriteTracksFragment
import com.paranid5.prima.presentation.ui.navigation.RootScreen
import com.paranid5.prima.presentation.ui.navigation.ScreenElement

@Composable
fun FavouritesFragment(
    rootScreen: RootScreen,
    favouritesScreen: FavouritesScreen,
    currentTrackState: MutableState<Track?>,
    isPlayingState: MutableState<Boolean>,
    isPlayingCoverLoadedState: MutableState<Boolean>,
    playbackPositionState: MutableState<Float>,
    loopingState: MutableState<Int>,
    tracksState: SnapshotStateList<Track>,
    filteredTracksState: SnapshotStateList<Track>,
    artistsState: SnapshotStateList<Artist>,
    filteredArtistsState: SnapshotStateList<Artist>,
    isPlaybackTrackDraggingState: State<Boolean>,
    speedState: State<Float>,
    isLikedState: MutableState<Boolean>,
    curArtistState: MutableState<Artist?>
) {
    Column(Modifier.fillMaxSize()) {
        BottomNavigation(
            modifier = Modifier.wrapContentHeight().fillMaxWidth(),
            backgroundColor = Params.secondaryColor,
            contentColor = Color.Transparent,
            elevation = 10.dp
        ) {
            Button(
                elevation = null,
                colors = ButtonDefaults.buttonColors(Color.Transparent),
                modifier = Modifier.fillMaxHeight().weight(1F),
                onClick = favouritesScreen::changeConfigToTracks
            ) {
                Text(text = Localization.tracks.resource, color = Params.primaryColor, fontSize = 14.sp)
            }

            Button(
                elevation = null,
                colors = ButtonDefaults.buttonColors(Color.Transparent),
                modifier = Modifier.fillMaxHeight().weight(1F),
                onClick = favouritesScreen::changeConfigToArtist
            ) {
                Text(text = Localization.artists.resource, color = Params.primaryColor, fontSize = 14.sp)
            }

            Button(
                elevation = null,
                colors = ButtonDefaults.buttonColors(Color.Transparent),
                modifier = Modifier.fillMaxHeight().weight(1F),
                onClick = favouritesScreen::changeConfigToTrackCollections
            ) {
                Text(text = Localization.trackCollections.resource, color = Params.primaryColor, fontSize = 14.sp)
            }
        }

        Children(
            routerState = favouritesScreen.routerState,
            animation = childAnimation(fade())
        ) {
            when (it.instance) {
                ScreenElement.Screen.FavouritesScreen.Tracks -> FavouriteTracksFragment(
                    currentTrackState,
                    isPlayingState,
                    isPlayingCoverLoadedState,
                    playbackPositionState,
                    loopingState,
                    tracksState,
                    filteredTracksState,
                    isPlaybackTrackDraggingState,
                    speedState,
                    isLikedState
                )

                ScreenElement.Screen.FavouritesScreen.Artists -> FavouriteArtistsFragment(
                    rootScreen,
                    curArtistState,
                    artistsState,
                    filteredArtistsState
                )

                ScreenElement.Screen.FavouritesScreen.TrackCollections -> Unit
            }
        }
    }
}