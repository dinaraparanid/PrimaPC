package com.paranid5.prima.domain.extensions

inline val ByteArray.correctUTF8String
    get() = String(this, Charsets.UTF_8)
        .replace(regex = Regex("[\u0014�${Character.MIN_VALUE}]"), replacement = "")