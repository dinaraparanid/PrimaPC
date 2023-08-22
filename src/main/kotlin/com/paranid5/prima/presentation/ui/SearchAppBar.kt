package com.paranid5.prima.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.paranid5.prima.domain.StorageHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.koin.compose.koinInject

@Composable
fun <T> SearchAppBar(
    isSearchingState: MutableState<Boolean>,
    allEntitiesState: MutableStateFlow<List<T>>,
    filteredEntitiesState: MutableStateFlow<List<T>>,
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject(),
    onTextChanged: suspend (String) -> Unit,
) {
    val textState = remember { mutableStateOf("") }
    val secondaryColor by storageHandler.secondaryColorState.collectAsState()
    val secondaryAlternativeColor by storageHandler.secondaryAlternativeColorState.collectAsState()

    LaunchedEffect(textState.value) {
        onTextChanged(textState.value)
    }

    TopAppBar(
        modifier = modifier.fillMaxWidth().height(60.dp),
        elevation = 10.dp,
        backgroundColor = secondaryColor
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
            color = MaterialTheme.colors.primary,
            elevation = 10.dp
        ) {
            TextField(
                modifier = Modifier.fillMaxSize(),
                singleLine = true,
                value = textState.value,
                onValueChange = { textState.value = it },
                placeholder = { SearchPlaceholder() },
                leadingIcon = { SearchIcon() },
                trailingIcon = {
                    CancelSearchIcon(
                        textState = textState,
                        allEntitiesState = allEntitiesState,
                        filteredEntitiesState = filteredEntitiesState,
                        isSearchingState = isSearchingState,
                    )
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                colors = TextFieldDefaults.textFieldColors(
                    textColor = secondaryAlternativeColor,
                    backgroundColor = Color.Transparent,
                    cursorColor = Color.White.copy(alpha = ContentAlpha.medium)
                )
            )
        }
    }
}

@Composable
private fun SearchPlaceholder(
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject()
) {
    val secondaryAlternativeColor by storageHandler.secondaryAlternativeColorState.collectAsState()

    Text(
        text = "...",
        color = secondaryAlternativeColor.copy(alpha = ContentAlpha.medium),
        modifier = modifier
    )
}

@Composable
private fun SearchIcon(
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject()
) {
    val lang by storageHandler.languageState.collectAsState()
    val secondaryAlternativeColor by storageHandler.secondaryAlternativeColorState.collectAsState()

    Icon(
        modifier = modifier.alpha(ContentAlpha.medium).width(30.dp).height(30.dp),
        painter = painterResource("images/search_icon.png"),
        contentDescription = lang.search,
        tint = secondaryAlternativeColor
    )
}

@Composable
private fun <T> CancelSearchIcon(
    textState: MutableState<String>,
    allEntitiesState: MutableStateFlow<List<T>>,
    filteredEntitiesState: MutableStateFlow<List<T>>,
    isSearchingState: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    storageHandler: StorageHandler = koinInject(),
) {
    val lang by storageHandler.languageState.collectAsState()

    IconButton(
        modifier = modifier,
        onClick = {
            when {
                textState.value.isNotEmpty() -> textState.value = ""

                else -> {
                    filteredEntitiesState.update { allEntitiesState.value }
                    isSearchingState.value = false
                }
            }
        }
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = lang.cancel,
            tint = Color.White
        )
    }
}