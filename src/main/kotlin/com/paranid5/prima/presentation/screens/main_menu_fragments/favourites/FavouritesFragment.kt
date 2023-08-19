package com.paranid5.prima.presentation.screens.main_menu_fragments.favourites

import androidx.compose.foundation.layout.*
import androidx.compose.material.BottomNavigation
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.Children
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.stackAnimation
import com.paranid5.prima.data.Artist
import com.paranid5.prima.data.Track
import com.paranid5.prima.domain.StorageHandler
import com.paranid5.prima.presentation.screens.main_menu_fragments.favourites.artists.FavouriteArtistsScreen
import com.paranid5.prima.presentation.screens.main_menu_fragments.favourites.tracks.FavouriteTracksFragment
import com.paranid5.prima.presentation.ui.navigation.FavouriteNavigator
import com.paranid5.prima.presentation.ui.navigation.Screen
import com.paranid5.prima.presentation.ui.navigation.composition_locals.LocalFavouriteNavigator
import org.koin.compose.koinInject

@Composable
fun FavouritesFragment(
    currentTrackState: MutableState<Track?>,
    isPlayingState: MutableState<Boolean>,
    isPlayingCoverLoadedState: MutableState<Boolean>,
    playbackPositionState: MutableState<Float>,
    loopingState: MutableState<Int>,
    tracksState: MutableState<List<Track>>,
    filteredTracksState: MutableState<List<Track>>,
    artistsState: MutableState<List<Artist>>,
    filteredArtistsState: MutableState<List<Artist>>,
    isPlaybackTrackDraggingState: State<Boolean>,
    speedState: State<Float>,
    isLikedState: MutableState<Boolean>,
    selectedArtistState: MutableState<Artist?>,
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject()
) {
    val navigator = LocalFavouriteNavigator.current
    val lang by storageHandler.languageState.collectAsState()

    val primaryColor by storageHandler.primaryColorState.collectAsState()
    val secondaryColor by storageHandler.secondaryColorState.collectAsState()

    Column(modifier.fillMaxSize()) {
        BottomNavigation(
            modifier = Modifier.wrapContentHeight().fillMaxWidth(),
            backgroundColor = secondaryColor,
            contentColor = Color.Transparent,
            elevation = 10.dp
        ) {
            TracksNavigationButton(Modifier.weight(1F))
            ArtistsNavigationButton(Modifier.weight(1F))
            TrackCollectionsNavigationButton(Modifier.weight(1F))
        }

        Children(
            stack = navigator.stack,
            modifier = modifier,
            animation = stackAnimation(fade())
        ) {
            when (it.instance) {
                Screen.FavouritesScreen.Tracks -> FavouriteTracksFragment(
                    currentTrackState = currentTrackState,
                    isPlayingState = isPlayingState,
                    isPlayingCoverLoadedState = isPlayingCoverLoadedState,
                    playbackPositionState = playbackPositionState,
                    loopingState = loopingState,
                    tracksState = tracksState,
                    filteredTracksState = filteredTracksState,
                    isPlaybackTrackDraggingState = isPlaybackTrackDraggingState,
                    speedState = speedState,
                    isLikedState = isLikedState
                )

                Screen.FavouritesScreen.Artists -> FavouriteArtistsScreen(
                    selectedArtistState = selectedArtistState,
                    artistsState = artistsState,
                    filteredArtistsState = filteredArtistsState
                )

                Screen.FavouritesScreen.TrackCollections -> Unit // TODO: Favourite collections
            }
        }
    }
}

@Composable
private fun TracksNavigationButton(
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject()
) {
    val lang by storageHandler.languageState.collectAsState()

    NavigationButton(
        title = lang.tracks,
        modifier = modifier,
        onClick = FavouriteNavigator::changeConfigToFavouriteTracks
    )
}

@Composable
private fun ArtistsNavigationButton(
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject()
) {
    val lang by storageHandler.languageState.collectAsState()

    NavigationButton(
        title = lang.artists,
        modifier = modifier,
        onClick = FavouriteNavigator::changeConfigToFavouriteArtist
    )
}

@Composable
private fun TrackCollectionsNavigationButton(
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject()
) {
    val lang by storageHandler.languageState.collectAsState()

    NavigationButton(
        title = lang.trackCollections,
        modifier = modifier,
        onClick = FavouriteNavigator::changeConfigToFavouriteTrackCollections
    )
}

@Composable
private fun NavigationButton(
    title: String,
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject(),
    onClick: FavouriteNavigator.() -> Unit
) {
    val navigator = LocalFavouriteNavigator.current
    val primaryColor by storageHandler.primaryColorState.collectAsState()

    Button(
        elevation = null,
        colors = ButtonDefaults.buttonColors(Color.Transparent),
        modifier = modifier.fillMaxHeight(),
        onClick = { onClick(navigator) }
    ) {
        Text(
            text = title,
            color = primaryColor,
            fontSize = 14.sp
        )
    }
}