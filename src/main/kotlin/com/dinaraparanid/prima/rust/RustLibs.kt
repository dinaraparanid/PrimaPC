package com.dinaraparanid.prima.rust

object RustLibs {
    private const val LIBRARY_PATH = "/home/paranid5/PROGRAMMING/kotlin/PrimaPC/src/com.dinaraparanid.prima.rust.main/kotlin/com/dinaraparanid/prima/rust/target/release/libprima_pc.so"

    init {
        System.load(LIBRARY_PATH)
    }

    @JvmName("hello")
    internal external fun hello(name: String): String
}
