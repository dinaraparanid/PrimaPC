package com.paranid5.prima.domain.extensions

inline val IntArray.triple
    get() = Triple(get(0), get(1), get(2))