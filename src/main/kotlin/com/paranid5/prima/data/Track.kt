package com.paranid5.prima.data

import androidx.compose.runtime.Immutable
import com.paranid5.prima.domain.extensions.correctUTF8String
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes

@Immutable
class Track(
    title: String?,
    artist: String?,
    album: String?,
    @JvmField val path: ByteArray,
    @JvmField val duration: Long,
    @JvmField val numberInAlbum: Short
) {
    @JvmField
    val title = title?.correctUTF8String

    @JvmField
    val artist = artist?.correctUTF8String

    @JvmField
    val album = album?.correctUTF8String

    @JvmField
    val addDate = Files
        .readAttributes(File(path.correctUTF8String).toPath(), BasicFileAttributes::class.java)
        .creationTime()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!path.contentEquals((other as Track).path)) return false
        return true
    }

    override fun hashCode() = path.hashCode()

    override fun toString() =
        "Track(path=$path, duration=$duration, addDate=$addDate, numberInAlbum=$numberInAlbum, title=$title, artist=$artist, album=$album)"
}