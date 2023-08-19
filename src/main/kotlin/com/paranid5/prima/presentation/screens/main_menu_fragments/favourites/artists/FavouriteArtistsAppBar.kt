package com.paranid5.prima.presentation.screens.main_menu_fragments.favourites.artists

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paranid5.prima.data.Artist
import com.paranid5.prima.domain.StorageHandler
import com.paranid5.prima.presentation.ui.SearchAppBar
import org.koin.compose.koinInject

@Composable
fun FavouriteArtistsAppBar(
    favouriteArtistsState: MutableState<List<Artist>>,
    filteredFavouriteArtistsState: MutableState<List<Artist>>,
    modifier: Modifier = Modifier
) {
    val isSearchingState = remember { mutableStateOf(false) }

    when {
        isSearchingState.value -> SearchAppBar(
            isSearchingState = isSearchingState,
            allEntitiesState = favouriteArtistsState,
            filteredEntitiesState = filteredFavouriteArtistsState,
            modifier = modifier
        ) { q ->
            val query = q.lowercase()

            filteredFavouriteArtistsState.value = favouriteArtistsState.value.filter { artist ->
                query in artist.name.lowercase()
            }
        }

        else -> DefaultAppBar(
            isSearchingState = isSearchingState,
            modifier = modifier
        )
    }
}

@Composable
private fun DefaultAppBar(
    isSearchingState: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject()
) {
    val primaryColor by storageHandler.primaryColorState.collectAsState()

    TopAppBar(
        modifier = modifier.fillMaxWidth().height(60.dp),
        elevation = 10.dp
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
            color = primaryColor,
            elevation = 10.dp
        ) {
            Row(Modifier.fillMaxSize()) {
                Spacer(Modifier.width(40.dp).fillMaxHeight())

                Label(Modifier.align(Alignment.CenterVertically))

                Spacer(Modifier.weight(1F))

                SearchButton(
                    isSearchingState = isSearchingState,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }
    }
}

@Composable
private fun Label(
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject()
) {
    val lang by storageHandler.languageState.collectAsState()
    val secondaryAlternativeColor by storageHandler.secondaryAlternativeColorState.collectAsState()

    Text(
        text = lang.favourites,
        fontSize = 22.sp,
        color = secondaryAlternativeColor,
        modifier = modifier
    )
}

@Composable
private fun SearchButton(
    isSearchingState: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject()
) {
    val lang by storageHandler.languageState.collectAsState()
    val secondaryAlternativeColor by storageHandler.secondaryAlternativeColorState.collectAsState()

    IconButton(
        modifier = modifier,
        onClick = { isSearchingState.value = true }
    ) {
        Icon(
            modifier = Modifier.size(30.dp).alpha(ContentAlpha.medium),
            painter = painterResource("images/search_icon.png"),
            contentDescription = lang.search,
            tint = secondaryAlternativeColor
        )
    }
}