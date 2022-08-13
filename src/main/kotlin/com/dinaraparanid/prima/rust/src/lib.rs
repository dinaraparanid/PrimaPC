extern crate futures;
extern crate jni;

#[macro_use]
extern crate diesel;

#[macro_use]
extern crate diesel_migrations;

pub mod audio_player;
pub mod audio_scanner;
pub mod databases;
pub mod entities;
pub mod jvm;
pub mod utils;

#[cfg(test)]
mod tests;

use diesel::prelude::*;
use futures::executor::block_on;
use std::{collections::HashSet, path::PathBuf, sync::Arc, time::Duration};

use crate::{
    audio_player::audio_player::{AudioPlayer, AUDIO_PLAYER},
    audio_scanner::AudioScanner,
    databases::{
        db_entity::DBEntity,
        entity_dao::EntityDao,
        favourites::{
            daos::{
                favourite_artist_dao::FavouriteArtistDao,
                favourite_playlist_dao::{FavouritePlaylistDBEntity, FavouritePlaylistDao},
                favourite_track_dao::FavouriteTrackDao,
            },
            db::establish_connection,
        },
    },
    entities::{
        artists::favourite_artist::FavouriteArtist,
        favourable::Favourable,
        playlists::{
            default_playlist::DefaultPlaylist, playlist_trait::PlaylistTrait,
            playlist_type::PlaylistType,
        },
        tracks::{
            default_track::DefaultTrack, favourite_track::FavouriteTrack, track_trait::TrackTrait,
        },
    },
    jvm::JVM,
    utils::{
        extensions::{
            playlist_ext::PlaylistExt, string_ext::StringExt, track_ext::TrackExt,
            vec_ext::ExactSizeIteratorExt,
        },
        params::PARAMS,
        storage_util::StorageUtil,
        track_order::{Comparator, Ord, TrackOrder},
    },
};

use jni::{
    objects::{JList, JObject, JString},
    sys::*,
    JNIEnv,
};

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_initRust(
    env: JNIEnv<'static>,
    _class: jclass,
) {
    block_on(async {
        unsafe {
            let jvm = &mut JVM.write().await;
            jvm.jni_env = Arc::new(Some(env));
        }
    });

    let db_connection = establish_connection().unwrap();

    diesel::sql_query(
        r#"CREATE TABLE IF NOT EXISTS favourite_tracks (
  title TEXT,
  artist TEXT,
  album TEXT,
  path TEXT PRIMARY KEY NOT NULL,
  duration BIGINT NOT NULL,
  add_date BIGINT NOT NULL,
  number_in_album INTEGER NOT NULL
);"#,
    )
    .execute(&db_connection)
    .unwrap_or_default();

    diesel::sql_query(
        "CREATE TABLE IF NOT EXISTS favourite_artists (name TEXT PRIMARY KEY NOT NULL)",
    )
    .execute(&db_connection)
    .unwrap_or_default();

    diesel::sql_query(
        r#"CREATE TABLE IF NOT EXISTS favourite_playlists (
  id INTEGER PRIMARY KEY,
  title TEXT,
  tp INTEGER NOT NULL
)"#,
    )
    .execute(&db_connection)
    .unwrap_or_default();
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_hello(
    env: JNIEnv,
    _class: jclass,
    name: JString,
) -> jstring {
    let name = unsafe { String::from_jstring_unchecked(&env, name) };

    env.new_string(format!("Hello, {}!", name))
        .unwrap()
        .into_inner()
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_getAllTracksBlocking(
    env: JNIEnv,
    _class: jclass,
) -> jobjectArray {
    block_on(async {
        AudioScanner::get_all_tracks()
            .await
            .lock()
            .await
            .iter()
            .into_jobject_array(&env)
    })
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_getCurTrackBlocking(
    env: JNIEnv,
    _class: jclass,
) -> jobject {
    block_on(async {
        unsafe {
            let params = PARAMS.read().await;
            let params = params.as_ref();
            let x = match params
                .unwrap()
                .get_cur_playlist()
                .await
                .as_ref()
                .unwrap()
                .get_cur_track()
            {
                None => std::ptr::null_mut(),
                Some(track) => track.to_java_track(&env).into_inner(),
            };
            x
        }
    })
}

/// Calculates time in hh:mm:ss format
///
/// # Safety
/// Extern JNI junction
///
/// # Arguments
/// *millis* - millisecond to convert
///
/// # Return
/// jintArray[hh, mm, ss]
#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_calcTrackTime(
    env: JNIEnv,
    _class: jclass,
    mut millis: jint,
) -> jintArray {
    let time = env.new_int_array(3).unwrap_unchecked();

    let h = millis / 3600000;
    millis -= h * 3600000;

    let m = millis / 60000;
    millis -= m * 60000;

    let s = millis / 1000;

    let arr = [h, m, s];

    env.set_int_array_region(time, 0, arr.as_slice())
        .unwrap_unchecked();
    time
}

#[inline]
async fn get_path_and_duration_of_cur_track() -> (PathBuf, Duration) {
    let track = unsafe { &PARAMS.read().await };
    let track = track.as_ref().unwrap().get_cur_playlist().await;
    let track = track.as_ref().unwrap().get_cur_track().unwrap();

    (
        track.get_path().clone(),
        track.get_duration().to_std().unwrap(),
    )
}

#[inline]
async fn get_duration_of_cur_track() -> Duration {
    unsafe {
        get_cur_playlist_async!()
            .get_cur_track()
            .unwrap()
            .get_duration()
            .to_std()
            .unwrap()
    }
}

#[inline]
async fn has_cur_track() -> bool {
    unsafe { get_cur_playlist_async!().get_cur_track().is_some() }
}

#[inline]
async fn set_cur_playlist(playlist: DefaultPlaylist<DefaultTrack>) {
    unsafe {
        *PARAMS
            .write()
            .await
            .as_mut()
            .unwrap()
            .get_cur_playlist_mut()
            .await = Some(playlist);
    }
}

#[inline]
async fn is_prev_track_equals_cur_track(cur_track: &DefaultTrack) -> bool {
    unsafe {
        let params = PARAMS.read().await;
        let params = params.as_ref().unwrap().get_cur_playlist().await;
        let prev_track = params.as_ref().unwrap().get_cur_track();
        *prev_track.unwrap() == *cur_track
    }
}

#[inline]
async unsafe fn play_pause_cur_track(playlist: Option<DefaultPlaylist<DefaultTrack>>) {
    let cur_track = playlist.as_ref().map(|p| p.get_cur_track()).flatten();
    let is_playing = AUDIO_PLAYER.read().await.is_playing();

    if is_playing {
        AUDIO_PLAYER.write().await.stop().await;

        if playlist.is_none() {
            AUDIO_PLAYER.write().await.pause().await;
            return;
        }

        if is_prev_track_equals_cur_track(cur_track.unwrap()).await {
            set_cur_playlist(playlist.unwrap()).await;
            AUDIO_PLAYER.write().await.pause().await
        } else {
            set_cur_playlist(playlist.unwrap()).await;
            let (path, track_duration) = get_path_and_duration_of_cur_track().await;
            AudioPlayer::play(path, track_duration).await
        }
        return;
    }

    if get_cur_playlist_async!().get_cur_track().is_none() {
        set_cur_playlist(playlist.unwrap()).await;
        let (path, track_duration) = get_path_and_duration_of_cur_track().await;
        AudioPlayer::play(path, track_duration).await;
        return;
    }

    if playlist.is_none() {
        let (_, track_duration) = get_path_and_duration_of_cur_track().await;
        AudioPlayer::resume(AUDIO_PLAYER.clone(), track_duration).await;
        return;
    }

    if is_prev_track_equals_cur_track(cur_track.unwrap()).await {
        set_cur_playlist(playlist.unwrap()).await;
        let (_, track_duration) = get_path_and_duration_of_cur_track().await;
        AudioPlayer::resume(AUDIO_PLAYER.clone(), track_duration).await;
    } else {
        set_cur_playlist(playlist.unwrap()).await;
        let (path, track_duration) = get_path_and_duration_of_cur_track().await;
        AudioPlayer::play(path, track_duration).await
    }
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_onTrackClickedBlocking(
    env: JNIEnv,
    _class: jclass,
    tracks: JObject,
    track_index: jint,
) {
    let playlist = DefaultPlaylist::new(
        None,
        PlaylistType::default(),
        JList::from_env(&env, tracks)
            .unwrap()
            .iter()
            .unwrap()
            .map(|jtrack| DefaultTrack::from_env(&env, jtrack)),
        track_index as usize,
    );

    StorageUtil::store_current_playlist(playlist.clone()).unwrap_or_default();
    block_on(play_pause_cur_track(Some(playlist)))
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_onPlayButtonClickedBlocking(
    _env: JNIEnv,
    _class: jclass,
) {
    block_on(async {
        if has_cur_track().await {
            unsafe { play_pause_cur_track(None).await }
        }
    })
}

#[inline]
async fn store_cur_playlist() {
    unsafe {
        StorageUtil::store_current_playlist(get_cur_playlist_async!().clone()).unwrap_or_default();
    }
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_onNextTrackClickedBlocking(
    _env: JNIEnv,
    _class: jclass,
) {
    block_on(async {
        if has_cur_track().await {
            {
                get_cur_playlist_mut_async!().skip_to_next();
            }

            store_cur_playlist().await;

            let (path, duration) = get_path_and_duration_of_cur_track().await;
            AudioPlayer::play(path, duration).await
        }
    })
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_onPreviousTrackClickedBlocking(
    _env: JNIEnv,
    _class: jclass,
) {
    block_on(async {
        if has_cur_track().await {
            {
                get_cur_playlist_mut_async!().skip_to_prev();
            }

            store_cur_playlist().await;

            let (path, duration) = get_path_and_duration_of_cur_track().await;
            AudioPlayer::play(path, duration).await
        }
    })
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_getCurTrackIndexBlocking(
    _env: JNIEnv,
    _class: jclass,
) -> jsize {
    block_on(async { unsafe { get_cur_playlist_async!().get_cur_ind() as jsize } })
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_getPlaybackPosition(
    _env: JNIEnv,
    _class: jclass,
) -> jlong {
    block_on(async {
        unsafe {
            AUDIO_PLAYER
                .read()
                .await
                .get_cur_playback_pos()
                .await
                .as_millis() as jlong
        }
    })
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_seekTo(
    _env: JNIEnv,
    _class: jclass,
    millis: jlong,
) {
    block_on(async {
        let duration = get_duration_of_cur_track().await;

        unsafe {
            if has_cur_track().await {
                AudioPlayer::seek_to(
                    AUDIO_PLAYER.clone(),
                    Duration::from_millis(millis as u64),
                    duration,
                )
                .await
            }
        }
    })
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_isPlaying(
    _env: JNIEnv,
    _class: jclass,
) -> jboolean {
    jboolean::from(block_on(async {
        unsafe { AUDIO_PLAYER.read().await.is_playing() }
    }))
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_replayCurTrackBlocking(
    _env: JNIEnv,
    _class: jclass,
) {
    block_on(async {
        let (path, duration) = get_path_and_duration_of_cur_track().await;
        AudioPlayer::play(path, duration).await
    })
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_setNextLoopingState(
    _env: JNIEnv,
    _class: jclass,
) -> jint {
    block_on(async {
        unsafe {
            AudioPlayer::set_next_looping_state(AUDIO_PLAYER.clone()).await;
            let state = AUDIO_PLAYER.read().await.get_looping_state();
            StorageUtil::store_looping_state(state).unwrap_or_default();
            state.into()
        }
    })
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_setVolume(
    _env: JNIEnv,
    _class: jclass,
    volume: jfloat,
) {
    StorageUtil::store_volume(volume).unwrap_or_default();
    block_on(async {
        unsafe { AudioPlayer::set_volume(AUDIO_PLAYER.clone(), volume as f32).await }
    })
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_setSpeed(
    _env: JNIEnv,
    _class: jclass,
    speed: jfloat,
) {
    StorageUtil::store_speed(speed).unwrap_or_default();
    block_on(async { unsafe { AudioPlayer::set_speed(AUDIO_PLAYER.clone(), speed as f32).await } })
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_getVolume(
    _env: JNIEnv,
    _class: jclass,
) -> jfloat {
    block_on(async { unsafe { AUDIO_PLAYER.write().await.get_volume() as jfloat } })
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_getSpeed(
    _env: JNIEnv,
    _class: jclass,
) -> jfloat {
    block_on(async { unsafe { AUDIO_PLAYER.write().await.get_speed() as jfloat } })
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_getLoopingState(
    _env: JNIEnv,
    _class: jclass,
) -> jint {
    block_on(async { unsafe { AUDIO_PLAYER.write().await.get_looping_state().into() } })
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_getTrackOrder(
    env: JNIEnv,
    _class: jclass,
) -> jintArray {
    let ord = block_on(async {
        unsafe {
            let order = PARAMS.read().await.as_ref().unwrap().track_order;
            (order.comparator, order.order)
        }
    });

    let arr = env.new_int_array(2).unwrap();
    let order: jint = ord.1.into();
    env.set_int_array_region(arr, 0, &[ord.0.into(), 5 + order])
        .unwrap();
    arr
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_setTrackOrder(
    _env: JNIEnv,
    _class: jclass,
    comparator: jint,
    order: jint,
) {
    unsafe {
        let order = TrackOrder::new(Comparator::from(comparator), Ord::from(order - 5));

        block_on(async move {
            PARAMS.write().await.as_mut().unwrap().track_order = order;
            StorageUtil::store_track_order(order).unwrap_or_default()
        });
    }
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_setMusicSearchPath(
    env: JNIEnv,
    _class: jclass,
    path: JString,
) {
    let path = unsafe { String::from_jstring_unchecked(&env, path) };

    block_on(async {
        unsafe { PARAMS.write().await.as_mut().unwrap().music_search_path = PathBuf::from(path) }
    })
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_storeMusicSearchPath(
    _env: JNIEnv,
    _class: jclass,
) {
    StorageUtil::store_music_search_path(block_on(async {
        unsafe {
            PARAMS
                .read()
                .await
                .as_ref()
                .unwrap()
                .music_search_path
                .clone()
        }
    }))
    .unwrap_or_default()
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_storeTrackOrder(
    _env: JNIEnv,
    _class: jclass,
) {
    StorageUtil::store_track_order(block_on(async {
        unsafe { PARAMS.read().await.as_ref().unwrap().track_order }
    }))
    .unwrap_or_default()
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_storeCurPlaybackPos(
    _env: JNIEnv,
    _class: jclass,
) {
    block_on(async {
        unsafe {
            AUDIO_PLAYER
                .read()
                .await
                .save_cur_playback_pos_async()
                .await
                .await
                .unwrap_or_default()
        }
    })
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_onLikeTrackClicked(
    env: JNIEnv,
    _class: jclass,
    track: JObject,
) {
    let track = DefaultTrack::from_env(&env, track).into_favourable();
    let connection = establish_connection().unwrap();

    if FavouriteTrackDao::get_by_key(track.get_key().clone(), &connection).is_some() {
        FavouriteTrackDao::remove(vec![track], &connection)
    } else {
        FavouriteTrackDao::insert(vec![track], &connection)
    }
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_isTrackLiked(
    env: JNIEnv,
    _class: jclass,
    track: JObject,
) -> jboolean {
    let track = DefaultTrack::from_env(&env, track);
    let connection = establish_connection().unwrap();
    jboolean::from(FavouriteTrackDao::get_by_key(track.get_path().clone(), &connection).is_some())
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_getCurPlaylist(
    env: JNIEnv,
    _class: jclass,
) -> jobjectArray {
    block_on(async { unsafe { get_cur_playlist_async!().clone().into_jobject_array(&env) } })
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_updateAndStoreCurPlaylist(
    env: JNIEnv,
    _class: jclass,
    cur_playlist: JObject,
) {
    let new_playlist = JList::from_env(&env, cur_playlist)
        .unwrap()
        .iter()
        .unwrap()
        .map(|jtrack| DefaultTrack::from_env(&env, jtrack))
        .collect::<Vec<_>>();

    let new_cur_ind = new_playlist
        .iter()
        .position(|track| {
            block_on(async {
                unsafe { *track == *get_cur_playlist_async!().get_cur_track().unwrap() }
            })
        })
        .unwrap();

    block_on(async move {
        unsafe {
            *PARAMS
                .write()
                .await
                .as_mut()
                .unwrap()
                .get_cur_playlist_mut()
                .await = Some(DefaultPlaylist::new(
                None,
                PlaylistType::default(),
                new_playlist,
                new_cur_ind,
            ));
        };

        StorageUtil::store_current_playlist(block_on(async {
            unsafe { get_cur_playlist_async!().clone() }
        }))
        .unwrap_or_default()
    });
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_getFavouriteTracks(
    env: JNIEnv,
    _class: jclass,
) -> jobjectArray {
    let connection = establish_connection().unwrap();
    let tracks: Vec<FavouriteTrack> = FavouriteTrackDao::get_all(&connection);
    tracks.into_iter().into_jobject_array(&env)
}

/// Converts artist name to the next pattern:
/// Name Family ... -> NF (upper case)
/// If artist don't have second word in his name, it will return only first letter
///
/// # Safety
/// Extern JNI function
///
/// # Arguments
/// name - full name of artist
///
/// # Return
/// Converted artist's name

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_artistImageBind(
    env: JNIEnv,
    _class: jclass,
    name: JString,
) -> jstring {
    env.new_string(String::from_iter(
        unsafe { String::from_jstring_unchecked(&env, name) }
            .trim()
            .split_whitespace()
            .into_iter()
            .filter(|&x| x != "&" && x != "feat." && x != "/" && x != "ft.")
            .take(2)
            .map(|s| s.chars().next().unwrap().to_uppercase().next().unwrap()),
    ))
    .unwrap()
    .into_inner()
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_onLikeArtistClicked(
    env: JNIEnv,
    _class: jclass,
    artist: JString,
) {
    let artist = FavouriteArtist::new(unsafe { String::from_jstring_unchecked(&env, artist) });
    let connection = establish_connection().unwrap();

    if FavouriteArtistDao::get_by_key(artist.get_key().clone(), &connection).is_some() {
        FavouriteArtistDao::remove(vec![artist], &connection)
    } else {
        FavouriteArtistDao::insert(vec![artist], &connection)
    }
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_isArtistLiked(
    env: JNIEnv,
    _class: jclass,
    artist: JString,
) -> jboolean {
    let artist = FavouriteArtist::new(unsafe { String::from_jstring_unchecked(&env, artist) });
    let connection = establish_connection().unwrap();
    jboolean::from(FavouriteArtistDao::get_by_key(artist.get_key().clone(), &connection).is_some())
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_getFavouriteArtists(
    env: JNIEnv,
    _class: jclass,
) -> jobjectArray {
    let connection = establish_connection().unwrap();
    let artists = FavouriteArtistDao::get_all(&connection);

    let arr = env
        .new_object_array(artists.len() as jsize, "java/lang/String", JObject::null())
        .unwrap();

    artists.into_iter().enumerate().for_each(|(ind, artist)| {
        env.set_object_array_element(
            arr,
            ind as jsize,
            env.new_string(artist.into_string()).unwrap(),
        )
        .unwrap();
    });

    arr
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_getAllArtistsBlocking(
    env: JNIEnv,
    _class: jclass,
    placeholder: JString,
) -> jobjectArray {
    let tracks = block_on(AudioScanner::get_all_tracks());
    let tracks = tracks.lock();
    let tracks = block_on(async move { tracks.await });

    let mut artists = tracks
        .iter()
        .filter_map(|track| track.get_artist())
        .collect::<HashSet<_>>();

    let isEmptyArtist = artists.remove(&"".to_string());

    let mut artists = artists.into_iter().collect::<Vec<_>>();
    artists.sort_unstable();

    let mut artists = artists
        .into_iter()
        .map(|string| unsafe { env.new_string(string.clone()).unwrap_unchecked() })
        .collect::<Vec<_>>();

    if isEmptyArtist {
        artists.push(placeholder)
    }

    let arr = unsafe {
        env.new_object_array(artists.len() as jsize, "java/lang/String", JObject::null())
            .unwrap_unchecked()
    };

    artists
        .into_iter()
        .enumerate()
        .for_each(|(ind, artist)| unsafe {
            env.set_object_array_element(arr, ind as jsize, artist)
                .unwrap_unchecked()
        });

    arr
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_getArtistTracksBlocking(
    env: JNIEnv,
    _class: jclass,
    artist: JString,
) -> jobjectArray {
    let artist = unsafe { String::from_jstring_unchecked(&env, artist) };

    block_on(async move {
        AudioScanner::get_all_tracks()
            .await
            .lock()
            .await
            .iter()
            .filter(|track| track.get_artist().is_some())
            .filter(|track| unsafe { artist == *track.get_artist().unwrap_unchecked() })
            .collect::<Vec<_>>()
            .into_iter()
            .into_jobject_array(&env)
    })
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_onLikePlaylistClicked(
    env: JNIEnv,
    _class: jclass,
    id: JObject,
    title: JString,
    tp: jint,
) {
    let title = String::from_jstring(&env, title);
    let tp = tp as i32;
    let connection = establish_connection().unwrap();

    if id.is_null() {
        FavouritePlaylistDao::insert(
            vec![FavouritePlaylistDBEntity::new(None, title, tp)],
            &connection,
        )
    } else {
        let id = Some(
            env.call_method(id, "intValue", "()I", &[])
                .unwrap()
                .i()
                .unwrap(),
        );

        if FavouritePlaylistDao::get_by_key(id.clone(), &connection).is_some() {
            FavouritePlaylistDao::remove(
                vec![FavouritePlaylistDBEntity::new(id, title, tp)],
                &connection,
            )
        } else {
            FavouritePlaylistDao::insert(
                vec![FavouritePlaylistDBEntity::new(id, title, tp)],
                &connection,
            )
        }
    }
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_isPlaylistLiked(
    env: JNIEnv,
    _class: jclass,
    id: JObject,
) -> jboolean {
    jboolean::from(if id.is_null() {
        false
    } else {
        let id = Some(
            env.call_method(id, "intValue", "()I", &[])
                .unwrap()
                .i()
                .unwrap(),
        );

        FavouritePlaylistDao::get_by_key(id.clone(), &establish_connection().unwrap()).is_some()
    })
}
