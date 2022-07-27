package com.dinaraparanid.prima.ui.fragments.main_menu_fragments.favourites.artists

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dinaraparanid.prima.entities.Artist
import com.dinaraparanid.prima.ui.utils.SearchAppBar
import com.dinaraparanid.prima.utils.Params
import com.dinaraparanid.prima.utils.localization.Localization

@Composable
fun FavouriteArtistsAppBar(
    favouriteArtistsState: SnapshotStateList<Artist>,
    filteredFavouriteArtistsState: SnapshotStateList<Artist>
) {
    val isSearchingState = remember { mutableStateOf(false) }

    when {
        isSearchingState.value -> SearchAppBar(
            favouriteArtistsState,
            filteredFavouriteArtistsState,
            isSearchingState,
        ) { q ->
            val query = q.lowercase()

            filteredFavouriteArtistsState.run {
                clear()
                addAll(favouriteArtistsState.filter { query in it.name.lowercase() })
            }
        }

        else -> DefaultAppBar(isSearchingState)
    }
}

@Composable
private fun DefaultAppBar(isSearchingState: MutableState<Boolean>) = TopAppBar(
    modifier = Modifier.fillMaxWidth().height(60.dp),
    elevation = 10.dp
) {
    Surface(
        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
        color = Params.primaryColor,
        elevation = 10.dp
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.width(40.dp).fillMaxHeight())

            Text(
                text = Localization.favourites.resource,
                fontSize = 22.sp,
                color = Params.secondaryAlternativeColor,
                modifier = Modifier.align(Alignment.CenterVertically)
            )

            Spacer(modifier = Modifier.weight(1F))

            Row(modifier = Modifier.align(Alignment.CenterVertically)) {
                IconButton(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    onClick = { isSearchingState.value = true }
                ) {
                    Icon(
                        modifier = Modifier.alpha(ContentAlpha.medium).width(30.dp).height(30.dp),
                        painter = painterResource("images/search_icon.png"),
                        contentDescription = Localization.search.resource,
                        tint = Params.secondaryAlternativeColor
                    )
                }
            }
        }
    }
}