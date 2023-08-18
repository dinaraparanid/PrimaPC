package com.paranid5.prima.rust;

import com.paranid5.prima.data.Track;
import com.paranid5.prima.domain.localization.Localization;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

public enum RustLibs {;
    private static final String LIBRARY_PATH = "/home/paranid5/PROGRAMMING/kotlin/PrimaPC/src/main/kotlin/com/paranid5/prima/rust/target/release/libprima_pc.so";

    static {
        System.load(LIBRARY_PATH);
    }

    public static native void initRust();

    @NotNull
    public static native String hello(@NotNull String name);

    @NotNull
    public static native Track[] getAllTracksBlocking();

    @Nullable
    public static native Track getCurTrackBlocking();

    public static native int getCurTrackIndexBlocking();

    /**
     * Calculates time in hh:mm:ss format
     * @param millis millisecond to convert
     * @return int[hh, mm, ss]
     */

    @NotNull
    public static native int[] calcTrackTime(int millis);

    public static native void onTrackClickedBlocking(@NotNull List<Track> tracks, int trackIndex);

    public static native void onPlayButtonClickedBlocking();

    public static native void onNextTrackClickedBlocking();

    public static native void onPreviousTrackClickedBlocking();

    public static native void replayCurTrackBlocking();

    public static native long getPlaybackPositionBlocking();

    public static native boolean isPlaying();

    public static native void seekToBlocking(long millis);

    public static native int setNextLoopingStateBlocking();

    public static native void setVolumeBlocking(float volume);

    public static native void setSpeedBlocking(float speed);

    public static native float getVolume();

    public static native float getSpeed();

    public static native int getLoopingState();

    /**
     * Gets track order's comparator and order as [int; 2]
     * @return 0 -> comparator (number in [0..4]); 1 -> order (number in [5..6])
     */

    @NotNull
    public static native int[] getTrackOrder();

    /**
     * Updates track ordering
     * @param comparator compare by: 0 - title, 1 - artist, 2 - album, 3 - date, 4 - № in album
     * @param order compare by: 0 - asc, 1 - desc
     */

    public static native void setTrackOrderBlocking(int comparator, int order);

    public static native void setMusicSearchPathBlocking(@NotNull String path);

    public static native void storeCurPlaybackPosBlocking();

    public static native void onLikeTrackClicked(@NotNull Track track);

    public static native boolean isTrackLiked(@NotNull Track track);

    @NotNull
    public static native Track[] getFavouriteTracks();

    @Nullable
    public static native Track[] getCurPlaylistBlocking();

    public static native void updateAndStoreCurPlaylistBlocking(@NotNull List<Track> curPlaylist);

    /**
     * Converts artist name to the next pattern:
     * name family ... -> NF (upper case).
     * If artist don't have second word in his name, it will return only first letter
     *
     * @param name artist's name as byte array
     * @return name patter
     */

    @NotNull
    public static native String artistImageBind(@NotNull String name);

    public static native void onLikeArtistClicked(@NotNull String artist);

    public static native boolean isArtistLiked(@NotNull String artist);

    @NotNull
    public static native String[] getFavouriteArtists();

    @NotNull
    private static native String[] getAllArtistsBlocking(@NotNull String placeholder);

    @NotNull
    public static String[] getAllArtistsBlocking() {
        return getAllArtistsBlocking(Localization.unknownArtist.getResource());
    }

    @NotNull
    public static native Track[] getArtistTracksBlocking(@NotNull String artist);

    public static int toIntPrimitive(@NotNull final Integer i) {
        return i;
    }

    public static long toLongPrimitive(@NotNull final Long l) {
        return l;
    }

    public static short toShortPrimitive(@NotNull final Short s) {
        return s;
    }

    @Nullable
    public static Object[] getDataByPath(@NotNull final String path) {
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
