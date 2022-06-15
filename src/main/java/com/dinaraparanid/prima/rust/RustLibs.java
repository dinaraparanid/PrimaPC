package com.dinaraparanid.prima.rust;

import com.dinaraparanid.prima.entities.Track;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

public enum RustLibs {;
    private static final String LIBRARY_PATH = "/home/paranid5/PROGRAMMING/kotlin/PrimaPC/src/main/kotlin/com/dinaraparanid/prima/rust/target/release/libprima_pc.so";

    static {
        System.load(LIBRARY_PATH);
    }

    public static final native void initRust();

    @NotNull
    public static final native String hello(@NotNull final String name);

    @NotNull
    public static final native Track[] getAllTracksAsync();

    @Nullable
    public static final native Track getCurTrack();

    /**
     * Calculates time in hh:mm:ss format
     * @param millis millisecond to convert
     * @return int[hh, mm, ss]
     */

    @NotNull
    public static final native int[] calcTrackTime(final int millis);

    public static final native void onTrackClicked(@NotNull final List<Track> tracks, final int trackIndex);

    public static final native void onNextTrackClicked();

    public static final native void onPreviousTrackClicked();

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
                    tag.getFirst(FieldKey.TITLE).getBytes(StandardCharsets.UTF_16),
                    tag.getFirst(FieldKey.ARTIST).getBytes(StandardCharsets.UTF_16),
                    tag.getFirst(FieldKey.ALBUM).getBytes(StandardCharsets.UTF_16),
                    file.getAudioHeader().getTrackLength(),
                    numberInAlbum
            };
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
