package com.dinaraparanid.prima.entities

import com.dinaraparanid.prima.utils.extensions.correctUTF8
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes

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

    @JvmField
    val addDate = Files
        .readAttributes(File(path.correctUTF8).toPath(), BasicFileAttributes::class.java)
        .creationTime()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!path.contentEquals((other as Track).path)) return false
        return true
    }

    override fun hashCode() = path.hashCode()

    override fun toString() =
        "Track(path=${path.contentToString()}, duration=$duration, addDate=$addDate, numberInAlbum=$numberInAlbum, title=$title, artist=$artist, album=$album)"
}