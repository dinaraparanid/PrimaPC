package com.paranid5.prima.presentation.ui.artists

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paranid5.prima.data.Artist
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArtistsList(
    selectedArtistState: MutableStateFlow<Artist?>,
    artistsState: MutableStateFlow<List<Artist>>,
    modifier: Modifier = Modifier
) {
    val artists by artistsState.collectAsState()

    LazyColumn(
        modifier = modifier.fillMaxWidth().wrapContentHeight(),
        contentPadding = PaddingValues(top = 20.dp, bottom = 180.dp, start = 20.dp, end = 20.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp),
    ) {
        itemsIndexed(artists, key = { _, artist -> artist }) { ind, _ ->
            ArtistItem(
                selectedArtistState = selectedArtistState,
                artistsState = artistsState,
                index = ind,
                modifier = Modifier.animateItemPlacement(animationSpec = tween(durationMillis = 300))
            )
        }
    }
}