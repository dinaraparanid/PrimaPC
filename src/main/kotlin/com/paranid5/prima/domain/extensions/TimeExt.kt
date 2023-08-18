package com.paranid5.prima.domain.extensions

import com.paranid5.prima.rust.RustLibs

/**
 * Calculates time in hh:mm:ss format
 * from the given milliseconds
 * @return int[hh, mm, ss]
 */

inline val Int.trackTime
    get() = RustLibs
        .calcTrackTime(this)
        .let { (f, s, t) -> Triple(f, s, t) }