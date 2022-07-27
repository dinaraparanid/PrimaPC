package com.dinaraparanid.prima.rust;

import com.dinaraparanid.prima.entities.Track;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
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
    public static final native Track[] getAllTracksBlocking();

    @Nullable
    public static final native Track getCurTrackBlocking();

    public static final native int getCurTrackIndexBlocking();

    /**
     * Calculates time in hh:mm:ss format
     * @param millis millisecond to convert
     * @return int[hh, mm, ss]
     */

    @NotNull
    public static final native int[] calcTrackTime(final int millis);

    public static final native void onTrackClickedBlocking(@NotNull final List<Track> tracks, final int trackIndex);

    public static final native void onPlayButtonClickedBlocking();

    public static final native void onNextTrackClickedBlocking();

    public static final native void onPreviousTrackClickedBlocking();

    public static final native void replayCurTrackBlocking();

    public static final native long getPlaybackPosition();

    public static final native boolean isPlaying();

    public static final native void seekTo(final long millis);

    public static final native int setNextLoopingState();

    public static final native void setVolume(final float volume);

    public static final native void setSpeed(final float speed);

    public static final native float getVolume();

    public static final native float getSpeed();

    public static final native int getLoopingState();

    /**
     * Gets track order's comparator and order as [int; 2]
     * @return 0 -> comparator (number in [0..4]); 1 -> order (number in [5..6])
     */

    @NotNull
    public static final native int[] getTrackOrder();

    /**
     * Updates track ordering
     * @param comparator compare by: 0 - title, 1 - artist, 2 - album, 3 - date, 4 - № in album
     * @param order compare by: 0 - asc, 1 - desc
     */

    public static final native void setTrackOrder(final int comparator, final int order);

    public static final native void setMusicSearchPath(final String path);

    public static final native void storeMusicSearchPath();

    public static final native void storeTrackOrder();

    public static final native void storeCurPlaybackPos();

    public static final native void onLikeTrackClicked(@NotNull final Track track);

    public static final native boolean isTrackLiked(@NotNull final Track track);

    @NotNull
    public static final native Track[] getFavouriteTracks();

    @Nullable
    public static final native Track[] getCurPlaylist();

    public static final native void updateAndStoreCurPlaylist(@NotNull final List<Track> curPlaylist);

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
                    (long) file.getAudioHeader().getTrackLength() * 1000L,
                    numberInAlbum
            };
        } catch (final Exception e) {
            return null;
        }
    }
}
