package com.dinaraparanid.prima.utils.extensions

internal inline val ByteArray.correctUTF8
    get() = String(this, Charsets.UTF_8)
        .replace(regex = Regex("[\u0014�${Character.MIN_VALUE}]"), replacement = "")