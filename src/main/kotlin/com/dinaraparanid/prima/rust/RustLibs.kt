package com.dinaraparanid.prima.rust

import com.dinaraparanid.prima.entities.Track
import org.jaudiotagger.audio.AudioFileIO
import java.io.File

object RustLibs {
    private const val LIBRARY_PATH = "/home/paranid5/PROGRAMMING/kotlin/PrimaPC/src/com.dinaraparanid.prima.rust.main/kotlin/com/dinaraparanid/prima/rust/target/release/libprima_pc.so"

    init {
        System.load(LIBRARY_PATH)
    }

    @JvmName("initRust")
    internal external fun initRust()

    @JvmName("hello")
    internal external fun hello(name: String): String

    @JvmName("getAllTracks")
    internal external fun getAllTracks(): Array<Track>

    @JvmName("getTrackDuration")
    internal fun getTrackDuration(fileName: String) =
        AudioFileIO.read(File(fileName)).audioHeader.trackLength
}
