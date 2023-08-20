package com.paranid5.prima.presentation.ui.tracks

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import com.paranid5.prima.data.Track
import kotlinx.coroutines.flow.MutableStateFlow

typealias TrackBarTemplate = @Composable (
    MutableStateFlow<List<Track>>,
    MutableStateFlow<List<Track>>,
    LazyListState
) -> Unit