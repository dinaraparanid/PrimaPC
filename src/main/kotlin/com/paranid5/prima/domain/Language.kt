package com.paranid5.prima.domain

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import java.io.File

sealed interface Language {
    private companion object {
        private const val UNKNOWN_TRACK = "unknown-track"
        private const val UNKNOWN_ARTIST = "unknown-artist"
        private const val UNKNOWN_ALBUM = "unknown-album"
        private const val TRACK_COVER = "track-cover"
        private const val TRACKS = "tracks"
        private const val SEARCH = "search"
        private const val CANCEL = "cancel"
        private const val BY_TITLE = "by-title"
        private const val BY_ARTIST = "by-artist"
        private const val BY_ALBUM = "by-album"
        private const val BY_DATE = "by-date"
        private const val BY_NUMBER_IN_ALBUM = "by-number-in-album"
        private const val ARTISTS = "artists"
        private const val TRACK_COLLECTIONS = "track-collections"
        private const val FAVOURITES = "favourites"
        private const val MP3_CONVERTER = "mp3-converter"
        private const val GTM = "gtm"
        private const val STATISTICS = "statistics"
        private const val SETTINGS = "settings"
        private const val ABOUT_APP = "about-app"
        private const val TODO = "todo"
        private const val ASCENDING = "ascending"
        private const val DESCENDING = "descending"
        private const val TRACK_ORDER = "track-order"
        private const val CHANGE_TRACK_INFO = "change-track-info"
        private const val ADD_TO_QUEUE = "add-to-queue"
        private const val ADD_TO_FAVOURITES = "add-to-favourites"
        private const val REMOVE_TRACK = "remove-track"
        private const val LYRICS = "lyrics"
        private const val TRACK_INFO = "track-information"
        private const val TRIM_TRACK = "trim-track"
        private const val HIDE_TRACK = "hide-track"
        private const val CURRENT_PLAYLIST = "current-playlist"
        private const val HIDE_ARTIST = "hide-artist"
        private const val LOADING = "loading"
    }

    data object English : Language {
        override val data by lazy {
            Yaml.default.decodeFromStream<Map<String, String>>(
                File("/src/main/resources/lang/ru.yaml").inputStream()
            )
        }
    }

    data object Russian : Language {
        override val data by lazy {
            Yaml.default.decodeFromStream<Map<String, String>>(
                File("/src/main/resources/lang/ru.yaml").inputStream()
            )
        }
    }

    val data: Map<String, String>

    private inline val String.fromData
        get() = data[this]!!

    val unknownTrack get() = UNKNOWN_TRACK.fromData
    val unknownArtist get() = UNKNOWN_ARTIST.fromData
    val unknownAlbum get() = UNKNOWN_ALBUM.fromData
    val trackCover get() = TRACK_COVER.fromData
    val tracks get() = TRACKS.fromData
    val search get() = SEARCH.fromData
    val cancel get() = CANCEL.fromData
    val byTitle get() = BY_TITLE.fromData
    val byArtist get() = BY_ARTIST.fromData
    val byAlbum get() = BY_ALBUM.fromData
    val byDate get() = BY_DATE.fromData
    val byNumberInAlbum get() = BY_NUMBER_IN_ALBUM.fromData
    val artists get() = ARTISTS.fromData
    val trackCollections get() = TRACK_COLLECTIONS.fromData
    val favourites get() = FAVOURITES.fromData
    val mp3Converter get() = MP3_CONVERTER.fromData
    val gtm get() = GTM.fromData
    val statistics get() = STATISTICS.fromData
    val settings get() = SETTINGS.fromData
    val aboutApp get() = ABOUT_APP.fromData
    val todo get() = TODO.fromData
    val ascending get() = ASCENDING.fromData
    val descending get() = DESCENDING.fromData
    val trackOrder get() = TRACK_ORDER.fromData
    val changeTrackInfo get() = CHANGE_TRACK_INFO.fromData
    val addToQueue get() = ADD_TO_QUEUE.fromData
    val addToFavourites get() = ADD_TO_FAVOURITES.fromData
    val removeTrack get() = REMOVE_TRACK.fromData
    val lyrics get() = LYRICS.fromData
    val trackInfo get() = TRACK_INFO.fromData
    val trimTrack get() = TRIM_TRACK.fromData
    val hideTrack get() = HIDE_TRACK.fromData
    val currentPlaylist get() = CURRENT_PLAYLIST.fromData
    val hideArtist get() = HIDE_ARTIST.fromData
    val loading get() = LOADING.fromData
}