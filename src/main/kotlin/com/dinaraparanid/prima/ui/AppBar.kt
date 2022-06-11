package com.dinaraparanid.prima.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dinaraparanid.prima.utils.Params
import com.dinaraparanid.prima.utils.localization.Localization

@Composable
fun AppBar() = TopAppBar(
    modifier = Modifier.fillMaxWidth().height(60.dp),
    elevation = 10.dp
) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(60.dp),
        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
        color = Params.primaryColor,
        elevation = 10.dp
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(60.dp)) {
            Spacer(modifier = Modifier.width(5.dp).fillMaxHeight())

            Button(
                onClick = {
                    // TODO: Show menu
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
                elevation = null,
                modifier = Modifier.width(50.dp).height(50.dp).align(Alignment.CenterVertically),
            ) {
                Image(
                    painter = painterResource("images/burger_button_icon.png"),
                    contentDescription = Localization.trackCover.resource,
                    modifier = Modifier.fillMaxSize(),
                    colorFilter = ColorFilter.tint(Params.secondaryAlternativeColor),
                    contentScale = ContentScale.Inside
                )
            }

            Spacer(modifier = Modifier.width(20.dp).fillMaxHeight())

            Text(
                text = Params.mainLabel.resource,
                fontSize = 20.sp,
                color = Params.secondaryAlternativeColor,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }
}