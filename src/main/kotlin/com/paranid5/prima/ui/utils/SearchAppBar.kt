package com.dinaraparanid.prima.ui.utils

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.dinaraparanid.prima.utils.Params
import com.dinaraparanid.prima.utils.localization.Localization

@Composable
internal fun <T> SearchAppBar(
    allEntitiesState: SnapshotStateList<T>,
    filteredEntitiesState: SnapshotStateList<T>,
    isSearchingState: MutableState<Boolean>,
    onTextChanged: (String) -> Unit,
) = TopAppBar(
    modifier = Modifier.fillMaxWidth().height(60.dp),
    elevation = 10.dp
) {
    Surface(
        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
        color = MaterialTheme.colors.primary,
        elevation = 10.dp
    ) {
        val textState = remember { mutableStateOf("") }

        TextField(
            modifier = Modifier.fillMaxSize(),
            value = textState.value,
            onValueChange = {
                textState.value = it
                onTextChanged(it)
            },
            placeholder = {
                Text(text = "...", color = Params.secondaryAlternativeColor.copy(alpha = ContentAlpha.medium))
            },
            singleLine = true,
            leadingIcon = {
                Icon(
                    modifier = Modifier.alpha(ContentAlpha.medium).width(30.dp).height(30.dp),
                    painter = painterResource("images/search_icon.png"),
                    contentDescription = Localization.search.resource,
                    tint = Params.secondaryAlternativeColor
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        when {
                            textState.value.isNotEmpty() -> {
                                textState.value = ""
                                onTextChanged("")
                            }

                            else -> {
                                filteredEntitiesState.run {
                                    clear()
                                    addAll(allEntitiesState)
                                }

                                isSearchingState.value = false
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = Localization.cancel.resource,
                        tint = Color.White
                    )
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onTextChanged(textState.value) }),
            colors = TextFieldDefaults.textFieldColors(
                textColor = Params.secondaryAlternativeColor,
                backgroundColor = Color.Transparent,
                cursorColor = Color.White.copy(alpha = ContentAlpha.medium)
            )
        )
    }
}