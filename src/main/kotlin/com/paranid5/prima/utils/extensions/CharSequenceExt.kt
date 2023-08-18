package com.dinaraparanid.prima.utils.extensions

inline val CharSequence.correctUTF8String
    get() = replace(Char(0).toString().toRegex(), "")