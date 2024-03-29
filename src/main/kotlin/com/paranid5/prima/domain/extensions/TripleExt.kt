package com.paranid5.prima.domain.extensions

inline val Triple<Int, Int, Int>.timeString
    get() = "${first.let { if (it < 10) "0$it" else it }}:" +
            "${second.let { if (it < 10) "0$it" else it }}:" +
            "${third.let { if (it < 10) "0$it" else it }}"