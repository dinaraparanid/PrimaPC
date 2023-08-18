package com.paranid5.prima.domain.extensions

fun Float.precision(len: Int) = toString().run { if (length > len) take(len) else this + "0".repeat(len - length) }