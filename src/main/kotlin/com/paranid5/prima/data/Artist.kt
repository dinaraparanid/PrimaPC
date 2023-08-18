package com.paranid5.prima.data

import androidx.compose.runtime.Immutable
import com.paranid5.prima.domain.extensions.correctUTF8String

@Immutable
class Artist(name: String) {

    @JvmField
    val name = name.correctUTF8String
}