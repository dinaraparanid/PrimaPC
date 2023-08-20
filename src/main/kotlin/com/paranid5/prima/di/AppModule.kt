package com.paranid5.prima.di

import com.paranid5.prima.data.Artist
import com.paranid5.prima.data.Track
import com.paranid5.prima.domain.StorageHandler
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

const val KOIN_PLAYBACK_POS = "playback_pos"
const val KOIN_IS_LIKED = "is_liked"

const val KOIN_IS_PLAYING = "is_playing"
const val KOIN_IS_PLAYING_COVER_LOADED = "is_playing_cover_loaded"
const val KOIN_IS_PLAYBACK_TRACK_DRAGGING = "is_playback_track_dragging"
const val KOIN_LOOPING = "looping"
const val KOIN_SPEED = "speed"
const val KOIN_VOLUME = "volume"

const val KOIN_ALL_TRACKS = "all_tracks"
const val KOIN_FILTERED_ALL_TRACKS = "filtered_all_tracks"
const val KOIN_SELECTED_TRACK = "selected_track"

const val KOIN_ALL_ARTISTS = "all_artists"
const val KOIN_FILTERED_ALL_ARTISTS = "filtered_all_artists"
const val KOIN_SELECTED_ARTIST = "selected_track"

const val KOIN_ARTIST_TRACKS = "artist_tracks"
const val KOIN_FILTERED_ARTIST_TRACKS = "filtered_artist_tracks"

const val KOIN_CURRENT_PLAYLIST_TRACKS = "current_playlist_tracks"
const val KOIN_CURRENT_PLAYLIST_FILTERED_TRACKS = "current_playlist_filtered_tracks"

const val KOIN_FAVOURITE_TRACKS = "favourite_tracks"
const val KOIN_FILTERED_FAVOURITE_TRACKS = "filtered_favourite_tracks"

const val KOIN_FAVOURITE_ARTISTS = "favourite_artists"
const val KOIN_FILTERED_FAVOURITE_ARTISTS = "filtered_favourite_artists"

private val globalsModule = module {
    singleOf(::StorageHandler)
}

private val playingTrackModule = module {
    single(named(KOIN_PLAYBACK_POS)) { MutableStateFlow(0F) }
    single(named(KOIN_IS_LIKED)) { MutableStateFlow(false) }
}

private val playbackModule = module {
    single(named(KOIN_IS_PLAYING)) { MutableStateFlow(false) }
    single(named(KOIN_IS_PLAYING_COVER_LOADED)) { MutableStateFlow(false) }
    single(named(KOIN_IS_PLAYBACK_TRACK_DRAGGING)) { MutableStateFlow(false) }
    single(named(KOIN_LOOPING)) { MutableStateFlow(0) }
    single(named(KOIN_SPEED)) { MutableStateFlow(0F) }
    single(named(KOIN_VOLUME)) { MutableStateFlow(0F) }
}

private val tracksModule = module {
    single(named(KOIN_ALL_TRACKS)) { MutableStateFlow(listOf<Track>()) }
    single(named(KOIN_FILTERED_ALL_TRACKS)) { MutableStateFlow(listOf<Track>()) }
    single(named(KOIN_SELECTED_TRACK)) { MutableStateFlow<Track?>(null) }
}

private val artistsModule = module {
    single(named(KOIN_ALL_ARTISTS)) { MutableStateFlow(listOf<Artist>()) }
    single(named(KOIN_FILTERED_ALL_ARTISTS)) { MutableStateFlow(listOf<Artist>()) }
    single(named(KOIN_SELECTED_ARTIST)) { MutableStateFlow<Artist?>(null) }
}

private val artistTracksModule = module {
    single(named(KOIN_ARTIST_TRACKS)) { MutableStateFlow(listOf<Track>()) }
    single(named(KOIN_FILTERED_ARTIST_TRACKS)) { MutableStateFlow(listOf<Track>()) }
}

private val currentPlaylistModule = module {
    single(named(KOIN_CURRENT_PLAYLIST_TRACKS)) { MutableStateFlow(listOf<Track>()) }
    single(named(KOIN_CURRENT_PLAYLIST_FILTERED_TRACKS)) { MutableStateFlow(listOf<Track>()) }
}

private val favouriteTracksModule = module {
    single(named(KOIN_FAVOURITE_TRACKS)) { MutableStateFlow(listOf<Track>()) }
    single(named(KOIN_FILTERED_FAVOURITE_TRACKS)) { MutableStateFlow(listOf<Track>()) }
}

private val favouriteArtistsModule = module {
    single(named(KOIN_FAVOURITE_ARTISTS)) { MutableStateFlow(listOf<Artist>()) }
    single(named(KOIN_FILTERED_FAVOURITE_ARTISTS)) { MutableStateFlow(listOf<Artist>()) }
}

private val favouritesModule = module {
    includes(favouriteTracksModule, favouriteArtistsModule)
}

private val uiStatesModule = module {
    includes(
        playingTrackModule,
        playbackModule,
        tracksModule,
        artistsModule,
        artistTracksModule,
        currentPlaylistModule,
        favouritesModule
    )
}

val appModule = module {
    includes(globalsModule, uiStatesModule)
}