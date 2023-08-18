package com.dinaraparanid.prima.utils

import com.dinaraparanid.prima.rust.RustLibs

/**
 * Calculates time in hh:mm:ss format
 * @param millis millisecond to convert
 * @return int[hh, mm, ss]
 */

internal fun calcTrackTime(millis: Int) =
    RustLibs.calcTrackTime(millis).let { (f, s, t) -> Triple(f, s, t) }