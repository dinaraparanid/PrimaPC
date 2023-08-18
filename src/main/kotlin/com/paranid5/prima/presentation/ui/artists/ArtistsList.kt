package com.paranid5.prima.presentation.ui.artists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paranid5.prima.data.Artist
import com.paranid5.prima.presentation.ui.navigation.RootScreen

@Composable
fun ArtistsList(
    rootScreen: RootScreen,
    curArtistState: MutableState<Artist?>,
    filteredArtistsState: SnapshotStateList<Artist>
) = LazyColumn(
    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
    contentPadding = PaddingValues(top = 20.dp, bottom = 180.dp, start = 20.dp, end = 20.dp),
    verticalArrangement = Arrangement.spacedBy(15.dp),
) {
    itemsIndexed(filteredArtistsState, key = { _, artist -> artist }) { ind, _ ->
        ArtistItem(rootScreen, curArtistState, filteredArtistsState, ind)
    }
}