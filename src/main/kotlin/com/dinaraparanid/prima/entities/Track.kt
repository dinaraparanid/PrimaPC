package com.dinaraparanid.prima.entities

internal data class Track(
    @JvmField val title: String?,
    @JvmField val artist: String?,
    @JvmField val album: String?,
    @JvmField val duration: Long,
    @JvmField val numberInAlbum: Short
)