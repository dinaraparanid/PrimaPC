package com.dinaraparanid.prima.entities

import com.dinaraparanid.prima.utils.extensions.correctUTF8

class Track(
    title: ByteArray?,
    artist: ByteArray?,
    album: ByteArray?,
    @JvmField val path: ByteArray,
    @JvmField val duration: Long,
    @JvmField val numberInAlbum: Short
) {
    @JvmField
    val title = title?.correctUTF8

    @JvmField
    val artist = artist?.correctUTF8

    @JvmField
    val album = album?.correctUTF8

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (path != (other as Track).path) return false
        return true
    }

    override fun hashCode() = path.hashCode()

    override fun toString() =
        "Track(duration=$duration, numberInAlbum=$numberInAlbum, title=$title, artist=$artist, album=$album, path=$path)"
}