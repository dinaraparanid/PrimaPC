package com.paranid5.prima.presentation.ui.playing_bar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.paranid5.prima.domain.StorageHandler
import org.koin.compose.koinInject

@Composable
fun PlayingBar(
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject(),
) {
    val primaryColor by storageHandler.primaryColorState.collectAsState()
    val secondaryColor by storageHandler.secondaryColorState.collectAsState()

    BottomAppBar(
        modifier = modifier.fillMaxWidth().height(150.dp),
        elevation = 10.dp,
        backgroundColor = secondaryColor
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            color = primaryColor,
            elevation = 10.dp
        ) {
            Box(Modifier.fillMaxWidth().height(100.dp)) {
                CurrentTrackData(Modifier.align(Alignment.CenterStart))
                ButtonsAndPlaybackTrack(Modifier.align(Alignment.Center))
                AudioEffectSliders(Modifier.align(Alignment.CenterEnd).padding(10.dp))
            }
        }
    }
}

@Composable
private fun ButtonsAndPlaybackTrack(modifier: Modifier = Modifier) = Surface(
    color = Color.Transparent,
    modifier = modifier.fillMaxHeight().padding(top = 20.dp)
) {
    Column(Modifier.width(800.dp)) {
        Buttons(
            Modifier
                .width(650.dp)
                .weight(1F)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(20.dp).weight(1F))

        PlaybackTrack(
            Modifier
                .fillMaxWidth()
                .weight(1.5F)
                .align(Alignment.CenterHorizontally)
        )
    }
}