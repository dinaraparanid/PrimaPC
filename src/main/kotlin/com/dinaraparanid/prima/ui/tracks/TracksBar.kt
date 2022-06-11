package com.dinaraparanid.prima.ui.tracks

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dinaraparanid.prima.entities.Track
import com.dinaraparanid.prima.utils.Params
import com.dinaraparanid.prima.utils.localization.Localization
import kotlinx.coroutines.launch

@Composable
fun TracksBar(tracks: SnapshotStateList<Track>, listState: LazyListState) {
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxWidth().height(60.dp),
        drawerElevation = 10.dp
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
            elevation = 30.dp
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                elevation = 10.dp
            ) {
                Row(modifier = Modifier.fillMaxWidth().height(60.dp)) {
                    Button(
                        onClick = {
                            tracks.shuffle()
                            coroutineScope.launch { listState.scrollToItem(0) }
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
                        elevation = null,
                        modifier = Modifier.width(70.dp).height(60.dp),
                    ) {
                        Image(
                            painter = painterResource("images/shuffle.png"),
                            contentDescription = Localization.trackCover.resource,
                            modifier = Modifier.fillMaxSize(),
                            colorFilter = ColorFilter.tint(Params.primaryColor),
                            contentScale = ContentScale.FillWidth
                        )
                    }

                    Text(
                        text = "${Localization.tracks.resource}: ${tracks.size}",
                        fontSize = 20.sp,
                        color = Params.primaryColor,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }
        }
    }
}