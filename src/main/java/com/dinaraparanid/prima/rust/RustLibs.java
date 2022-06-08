package com.dinaraparanid.prima.rust;

import com.dinaraparanid.prima.entities.Track;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public enum RustLibs {;
    private static final String LIBRARY_PATH = "/home/paranid5/PROGRAMMING/kotlin/PrimaPC/src/main/kotlin/com/dinaraparanid/prima/rust/target/release/libprima_pc.so";

    static {
        System.load(LIBRARY_PATH);
    }

    public static final native void initRust();

    public static final native @NotNull String hello(@NotNull final String name);

    public static final native @NotNull Track[] getAllTracks();

    public static final int toIntPrimitive(@NotNull final Integer i) {
        return i;
    }
    public static final long toLongPrimitive(@NotNull final Long l) {
        return l;
    }
    public static final short toShortPrimitive(@NotNull final Short s) {
        return s;
    }

    public static final @Nullable Object[] getDataByPath(@NotNull final String path) {
        try {
            final var file = AudioFileIO.read(new File((path)));
            final var tag = file.getTagOrCreateAndSetDefault();
            short numberInAlbum;

            try {
                numberInAlbum = Short.parseShort(tag.getFirst(FieldKey.TRACK));
            } catch (final NumberFormatException err) {
                numberInAlbum = 0;
            }

            return new Object[]{
                    tag.getFirst(FieldKey.TITLE),
                    tag.getFirst(FieldKey.ARTIST),
                    tag.getFirst(FieldKey.ALBUM),
                    file.getAudioHeader().getTrackLength(),
                    numberInAlbum
            };
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
