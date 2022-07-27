package com.dinaraparanid.prima.entities

import com.dinaraparanid.prima.utils.extensions.correctUTF8String

class Artist(name: String) {

    @JvmField
    val name = name.correctUTF8String
}