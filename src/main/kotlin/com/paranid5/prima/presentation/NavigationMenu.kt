package com.paranid5.prima.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.NavigationRail
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paranid5.prima.domain.StorageHandler
import com.paranid5.prima.presentation.ui.navigation.composition_locals.LocalRootNavigator
import org.koin.compose.koinInject

@Composable
fun NavigationMenu(
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject()
) {
    val navigator = LocalRootNavigator.current
    val lang by storageHandler.languageState.collectAsState()

    NavigationRail(modifier = modifier.fillMaxHeight().width(IntrinsicSize.Max)) {
        NavigationMenuItem(
            title = lang.tracks,
            iconPath = "images/tracks_icon.png",
            switchToElement = navigator::changeConfigToTracks
        )

        NavigationMenuItem(
            title = lang.trackCollections,
            iconPath = "images/playlist.png",
            switchToElement = navigator::changeConfigToTrackCollections
        )

        NavigationMenuItem(
            title = lang.artists,
            iconPath = "images/human.png",
            switchToElement = navigator::changeConfigToArtists
        )

        NavigationMenuItem(
            title = lang.favourites,
            iconPath = "images/heart.png",
            switchToElement = navigator::changeConfigToFavourites
        )

        NavigationMenuItem(
            title = lang.mp3Converter,
            iconPath = "images/mp3_icon.png",
            switchToElement = navigator::changeConfigToMP3Converter
        )

        NavigationMenuItem(
            title = lang.gtm,
            iconPath = "images/guess_the_melody.png",
            switchToElement = navigator::changeConfigToGTM
        )

        NavigationMenuItem(
            title = lang.statistics,
            iconPath = "images/statistics_icon.png",
            switchToElement = navigator::changeConfigToStatistics
        )

        NavigationMenuItem(
            title = lang.settings,
            iconPath = "images/settings.png",
            switchToElement = navigator::changeConfigToSettings
        )

        NavigationMenuItem(
            title = lang.aboutApp,
            iconPath = "images/about_app.png",
            switchToElement = navigator::changeConfigToAboutApp
        )
    }
}

@Composable
private fun ColumnScope.NavigationMenuItem(
    title: String,
    iconPath: String,
    switchToElement: () -> Unit,
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject()
) {
    val primaryColor by storageHandler.primaryColorState.collectAsState()
    val secondaryAlternativeColor by storageHandler.secondaryAlternativeColorState.collectAsState()

    Button(
        modifier = modifier.weight(1F).fillMaxWidth(),
        elevation = null,
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
        contentPadding = PaddingValues(5.dp),
        onClick = switchToElement
    ) {
        Column(modifier = Modifier.align(Alignment.CenterVertically)) {
            Image(
                painter = painterResource(iconPath),
                contentDescription = title,
                modifier = Modifier.width(50.dp).weight(2F).align(Alignment.CenterHorizontally),
                colorFilter = ColorFilter.tint(primaryColor),
                contentScale = ContentScale.Inside
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = title,
                color = secondaryAlternativeColor,
                fontSize = 14.sp,
                maxLines = 1,
                modifier = Modifier.weight(1F).align(Alignment.CenterHorizontally),
            )
        }
    }
}