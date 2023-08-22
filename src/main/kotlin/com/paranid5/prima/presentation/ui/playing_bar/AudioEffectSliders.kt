package com.paranid5.prima.presentation.ui.playing_bar

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paranid5.prima.di.KOIN_SPEED
import com.paranid5.prima.di.KOIN_VOLUME
import com.paranid5.prima.domain.StorageHandler
import com.paranid5.prima.domain.extensions.precision
import com.paranid5.prima.rust.RustLibs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
fun AudioEffectSliders(modifier: Modifier = Modifier) = Column(modifier) {
    Volume(Modifier.padding(10.dp))
    Speed(Modifier.padding(10.dp))
}

@Composable
private fun Volume(
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject(),
    volumeState: MutableStateFlow<Float> = koinInject(named(KOIN_VOLUME))
) {
    val lang by storageHandler.languageState.collectAsState()
    val secondaryColor by storageHandler.secondaryColorState.collectAsState()
    val secondaryAlternativeColor by storageHandler.secondaryAlternativeColorState.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val volume by volumeState.collectAsState()

    Row(modifier) {
        Image(
            painter = painterResource("images/volume_icon.png"),
            contentDescription = lang.trackCover,
            modifier = Modifier.size(40.dp).align(Alignment.CenterVertically),
            colorFilter = ColorFilter.tint(secondaryAlternativeColor),
            contentScale = ContentScale.Inside
        )

        Slider(
            value = volume,
            valueRange = (0F..2F),
            colors = SliderDefaults.colors(
                thumbColor = secondaryAlternativeColor,
                activeTrackColor = secondaryAlternativeColor,
                inactiveTrackColor = secondaryColor
            ),
            modifier = Modifier.width(150.dp),
            onValueChange = { volumeState.value = it },
            onValueChangeFinished = {
                coroutineScope.launch(Dispatchers.IO) {
                    RustLibs.setVolumeBlocking(volumeState.value)
                }
            }
        )

        Spacer(Modifier.width(5.dp))

        Text(
            text = volume.precision(4),
            fontSize = 14.sp,
            color = secondaryAlternativeColor,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}

@Composable
private fun Speed(
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject(),
    speedState: MutableStateFlow<Float> = koinInject(named(KOIN_SPEED))
) {
    val lang by storageHandler.languageState.collectAsState()
    val secondaryColor by storageHandler.secondaryColorState.collectAsState()
    val secondaryAlternativeColor by storageHandler.secondaryAlternativeColorState.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val speed by speedState.collectAsState()

    Row(modifier) {
        Image(
            painter = painterResource("images/speed.png"),
            contentDescription = lang.trackCover,
            modifier = Modifier.size(40.dp).align(Alignment.CenterVertically),
            colorFilter = ColorFilter.tint(secondaryAlternativeColor),
            contentScale = ContentScale.Inside
        )

        Slider(
            value = speed,
            valueRange = (0.5F..2F),
            colors = SliderDefaults.colors(
                thumbColor = secondaryAlternativeColor,
                activeTrackColor = secondaryAlternativeColor,
                inactiveTrackColor = secondaryColor
            ),
            modifier = Modifier.width(150.dp),
            onValueChange = {
                speedState.value = it
            },
            onValueChangeFinished = {
                coroutineScope.launch(Dispatchers.IO) {
                    RustLibs.setSpeedBlocking(speedState.value)
                }
            }
        )

        Spacer(Modifier.width(5.dp))

        Text(
            text = speed.precision(4),
            fontSize = 14.sp,
            color = secondaryAlternativeColor,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}