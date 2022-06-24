package com.dinaraparanid.prima.utils.extensions

fun Float.take(len: Int) = toString().run { if (length > len) take(len) else this + "0".repeat(len - length) }