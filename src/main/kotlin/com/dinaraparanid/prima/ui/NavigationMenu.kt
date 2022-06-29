package com.dinaraparanid.prima.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.NavigationRail
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dinaraparanid.prima.ui.utils.navigation.RootScreen
import com.dinaraparanid.prima.utils.Params
import com.dinaraparanid.prima.utils.localization.Localization
import com.dinaraparanid.prima.utils.localization.LocalizedString

@Composable
fun NavigationMenu(rootScreen: RootScreen) =
    NavigationRail(modifier = Modifier.fillMaxHeight().width(IntrinsicSize.Max).padding(bottom = 160.dp)) {
        NavigationMenuItem(
            title = Localization.tracks,
            iconPath = "images/tracks_icon.png",
            switchToElement = rootScreen::changeConfigToTracks
        )

        NavigationMenuItem(
            title = Localization.trackCollections,
            iconPath = "images/playlist.png",
            switchToElement = rootScreen::changeConfigToTrackCollections
        )

        NavigationMenuItem(
            title = Localization.artists,
            iconPath = "images/human.png",
            switchToElement = rootScreen::changeConfigToArtists
        )

        NavigationMenuItem(
            title = Localization.favourites,
            iconPath = "images/heart.png",
            switchToElement = rootScreen::changeConfigToFavourites
        )

        NavigationMenuItem(
            title = Localization.mp3Converter,
            iconPath = "images/mp3_icon.png",
            switchToElement = rootScreen::changeConfigToMP3Converter
        )

        NavigationMenuItem(
            title = Localization.gtm,
            iconPath = "images/guess_the_melody.png",
            switchToElement = rootScreen::changeConfigToGTM
        )

        NavigationMenuItem(
            title = Localization.statistics,
            iconPath = "images/statistics_icon.png",
            switchToElement = rootScreen::changeConfigToStatistics
        )

        NavigationMenuItem(
            title = Localization.settings,
            iconPath = "images/settings.png",
            switchToElement = rootScreen::changeConfigToSettings
        )

        NavigationMenuItem(
            title = Localization.aboutApp,
            iconPath = "images/about_app.png",
            switchToElement = rootScreen::changeConfigToAboutApp
        )
    }

@Composable
private fun ColumnScope.NavigationMenuItem(
    title: LocalizedString,
    iconPath: String,
    switchToElement: () -> Unit
) = Button(
    modifier = Modifier.weight(1F).fillMaxWidth(),
    elevation = null,
    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
    contentPadding = PaddingValues(5.dp),
    onClick = switchToElement
) {
    Column(modifier = Modifier.align(Alignment.CenterVertically)) {
        val title = title.resource

        Image(
            painter = painterResource(iconPath),
            contentDescription = title,
            modifier = Modifier.width(50.dp).weight(2F).align(Alignment.CenterHorizontally),
            colorFilter = ColorFilter.tint(Params.primaryColor),
            contentScale = ContentScale.Inside
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = title,
            color = Params.secondaryAlternativeColor,
            fontSize = 14.sp,
            maxLines = 1,
            modifier = Modifier.weight(1F).align(Alignment.CenterHorizontally),
        )
    }
}