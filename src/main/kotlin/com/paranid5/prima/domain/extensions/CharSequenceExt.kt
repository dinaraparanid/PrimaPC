package com.paranid5.prima.domain.extensions

inline val CharSequence.correctUTF8String
    get() = replace(Char(0).toString().toRegex(), "")