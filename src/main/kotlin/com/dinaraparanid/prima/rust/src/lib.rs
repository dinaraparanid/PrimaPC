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

use diesel::RunQueryDsl;
use futures::executor::block_on;
use std::{path::PathBuf, sync::Arc, time::Duration};

use crate::{
    audio_player::audio_player::AUDIO_PLAYER,
    audio_scanner::AudioScanner,
    databases::{
        entity_dao::EntityDao,
        favourites::{daos::favourite_track_dao::FavouriteTrackDao, db::establish_connection},
    },
    entities::{
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
            playlist_ext::PlaylistExt, track_ext::TrackExt, vec_ext::ExactSizeIteratorExt,
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
    unsafe {
        let jvm = &mut JVM.write().unwrap();
        jvm.jni_env = Arc::new(Some(env));
    }

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
    let name: String = env.get_string(name).unwrap().into();

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
    block_on(AudioScanner::get_all_tracks())
        .lock()
        .unwrap()
        .iter()
        .into_jobject_array(&env)
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_getCurTrackBlocking(
    env: JNIEnv,
    _class: jclass,
) -> jobject {
    match unsafe {
        PARAMS
            .read()
            .unwrap()
            .as_ref()
            .unwrap()
            .get_cur_playlist()
            .get_cur_track()
    } {
        None => std::ptr::null_mut(),
        Some(track) => track.to_java_track(&env).into_inner(),
    }
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
fn get_path_and_duration_of_cur_track() -> (PathBuf, Duration) {
    let track = unsafe { &PARAMS.read() };
    let track = track
        .as_ref()
        .unwrap()
        .as_ref()
        .unwrap()
        .get_cur_playlist()
        .get_cur_track()
        .unwrap();

    (
        track.get_path().clone(),
        track.get_duration().to_std().unwrap(),
    )
}

#[inline]
fn get_duration_of_cur_track() -> Duration {
    let track = unsafe { &PARAMS.read() };
    let track = track
        .as_ref()
        .unwrap()
        .as_ref()
        .unwrap()
        .get_cur_playlist()
        .get_cur_track()
        .unwrap();

    track.get_duration().to_std().unwrap()
}

#[inline]
fn has_cur_track() -> bool {
    let params = unsafe { &PARAMS.read() };
    params
        .as_ref()
        .unwrap()
        .as_ref()
        .unwrap()
        .get_cur_playlist()
        .get_cur_track()
        .is_some()
}

#[inline]
fn set_cur_playlist(playlist: DefaultPlaylist<DefaultTrack>) {
    unsafe {
        *PARAMS
            .write()
            .unwrap()
            .as_mut()
            .unwrap()
            .get_cur_playlist_mut() = playlist;
    }
}

#[inline]
unsafe fn play_pause_cur_track(playlist: Option<DefaultPlaylist<DefaultTrack>>) {
    let cur_track = playlist.as_ref().map(|p| p.get_cur_track()).flatten();
    let is_playing = AUDIO_PLAYER.read().unwrap().is_playing();

    if is_playing {
        AUDIO_PLAYER.write().unwrap().stop();

        if playlist.is_none() {
            AUDIO_PLAYER.write().unwrap().pause();
            return;
        }

        if {
            let params = PARAMS.read().unwrap();
            let prev_track = params.as_ref().unwrap().get_cur_playlist().get_cur_track();
            prev_track.unwrap() == cur_track.unwrap()
        } {
            set_cur_playlist(playlist.unwrap());
            AUDIO_PLAYER.write().unwrap().pause()
        } else {
            set_cur_playlist(playlist.unwrap());
            let (path, track_duration) = get_path_and_duration_of_cur_track();
            block_on(AUDIO_PLAYER.write().unwrap().play(path, track_duration))
        }
        return;
    }

    if {
        PARAMS
            .read()
            .unwrap()
            .as_ref()
            .unwrap()
            .get_cur_playlist()
            .get_cur_track()
            .is_none()
    } {
        set_cur_playlist(playlist.unwrap());
        let (path, track_duration) = get_path_and_duration_of_cur_track();
        block_on(AUDIO_PLAYER.write().unwrap().play(path, track_duration));
        return;
    }

    if playlist.is_none() {
        let (_, track_duration) = get_path_and_duration_of_cur_track();
        AUDIO_PLAYER.write().unwrap().resume(track_duration);
        return;
    }

    if {
        let params = PARAMS.read().unwrap();
        let prev_track = params.as_ref().unwrap().get_cur_playlist().get_cur_track();
        prev_track.unwrap() == cur_track.unwrap()
    } {
        set_cur_playlist(playlist.unwrap());
        let (_, track_duration) = get_path_and_duration_of_cur_track();
        AUDIO_PLAYER.write().unwrap().resume(track_duration)
    } else {
        set_cur_playlist(playlist.unwrap());
        let (path, track_duration) = get_path_and_duration_of_cur_track();
        block_on(AUDIO_PLAYER.write().unwrap().play(path, track_duration))
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
    play_pause_cur_track(Some(playlist))
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_onPlayButtonClickedBlocking(
    _env: JNIEnv,
    _class: jclass,
) {
    if has_cur_track() {
        unsafe { play_pause_cur_track(None) }
    }
}

#[inline]
fn store_cur_playlist() {
    unsafe {
        StorageUtil::store_current_playlist(
            PARAMS
                .read()
                .unwrap()
                .as_ref()
                .unwrap()
                .get_cur_playlist()
                .clone(),
        )
        .unwrap_or_default();
    }
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_onNextTrackClickedBlocking(
    _env: JNIEnv,
    _class: jclass,
) {
    if has_cur_track() {
        {
            PARAMS
                .write()
                .unwrap()
                .as_mut()
                .unwrap()
                .get_cur_playlist_mut()
                .skip_to_next();
        }

        store_cur_playlist();

        let (path, duration) = get_path_and_duration_of_cur_track();
        block_on(AUDIO_PLAYER.write().unwrap().play(path, duration))
    }
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_onPreviousTrackClickedBlocking(
    _env: JNIEnv,
    _class: jclass,
) {
    if has_cur_track() {
        {
            PARAMS
                .write()
                .unwrap()
                .as_mut()
                .unwrap()
                .get_cur_playlist_mut()
                .skip_to_prev();
        }

        store_cur_playlist();

        let (path, duration) = get_path_and_duration_of_cur_track();
        block_on(AUDIO_PLAYER.write().unwrap().play(path, duration))
    }
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_getCurTrackIndexBlocking(
    _env: JNIEnv,
    _class: jclass,
) -> jsize {
    unsafe {
        PARAMS
            .read()
            .unwrap()
            .as_ref()
            .unwrap()
            .get_cur_playlist_mut()
            .get_cur_ind() as jsize
    }
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_getPlaybackPosition(
    _env: JNIEnv,
    _class: jclass,
) -> jlong {
    unsafe {
        AUDIO_PLAYER
            .read()
            .unwrap()
            .get_cur_playback_pos()
            .as_millis() as jlong
    }
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_seekTo(
    _env: JNIEnv,
    _class: jclass,
    millis: jlong,
) {
    let duration = get_duration_of_cur_track();

    unsafe {
        if has_cur_track() {
            AUDIO_PLAYER
                .write()
                .unwrap()
                .seek_to(Duration::from_millis(millis as u64), duration)
        }
    }
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_isPlaying(
    _env: JNIEnv,
    _class: jclass,
) -> bool {
    unsafe { AUDIO_PLAYER.read().unwrap().is_playing() }
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_replayCurTrackBlocking(
    _env: JNIEnv,
    _class: jclass,
) {
    let (path, duration) = get_path_and_duration_of_cur_track();
    unsafe { block_on(AUDIO_PLAYER.write().unwrap().play(path, duration)) }
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_setNextLoopingState(
    _env: JNIEnv,
    _class: jclass,
) -> jint {
    unsafe {
        AUDIO_PLAYER.write().unwrap().set_next_looping_state();
        let state = AUDIO_PLAYER.read().unwrap().get_looping_state();
        StorageUtil::store_looping_state(state).unwrap_or_default();
        state.into()
    }
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_setVolume(
    _env: JNIEnv,
    _class: jclass,
    volume: jfloat,
) {
    StorageUtil::store_volume(volume).unwrap_or_default();
    unsafe { AUDIO_PLAYER.write().unwrap().set_volume(volume as f32) }
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_setSpeed(
    _env: JNIEnv,
    _class: jclass,
    speed: jfloat,
) {
    StorageUtil::store_speed(speed).unwrap_or_default();
    unsafe { AUDIO_PLAYER.write().unwrap().set_speed(speed as f32) }
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_getVolume(
    _env: JNIEnv,
    _class: jclass,
) -> jfloat {
    unsafe { AUDIO_PLAYER.write().unwrap().get_volume() as jfloat }
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_getSpeed(
    _env: JNIEnv,
    _class: jclass,
) -> jfloat {
    unsafe { AUDIO_PLAYER.write().unwrap().get_speed() as jfloat }
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_getLoopingState(
    _env: JNIEnv,
    _class: jclass,
) -> jint {
    unsafe { AUDIO_PLAYER.write().unwrap().get_looping_state().into() }
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_getTrackOrder(
    env: JNIEnv,
    _class: jclass,
) -> jintArray {
    let ord = unsafe {
        let order = PARAMS.read().unwrap().as_ref().unwrap().track_order;
        (order.comparator, order.order)
    };

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
        PARAMS.write().unwrap().as_mut().unwrap().track_order = order;
        StorageUtil::store_track_order(order).unwrap_or_default();
    }
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_setMusicSearchPath(
    env: JNIEnv,
    _class: jclass,
    path: JString,
) {
    let path: String = env.get_string(path).unwrap().into();
    unsafe { PARAMS.write().unwrap().as_mut().unwrap().music_search_path = PathBuf::from(path) }
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_storeMusicSearchPath(
    _env: JNIEnv,
    _class: jclass,
) {
    StorageUtil::store_music_search_path(unsafe {
        PARAMS
            .read()
            .unwrap()
            .as_ref()
            .unwrap()
            .music_search_path
            .clone()
    })
    .unwrap_or_default()
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_storeTrackOrder(
    _env: JNIEnv,
    _class: jclass,
) {
    StorageUtil::store_track_order(unsafe { PARAMS.read().unwrap().as_ref().unwrap().track_order })
        .unwrap_or_default()
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_storeCurPlaybackPos(
    _env: JNIEnv,
    _class: jclass,
) {
    unsafe { AUDIO_PLAYER.read().unwrap().save_cur_playback_pos_async() }
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

    if FavouriteTrackDao::get_by_key(track.get_path().clone(), &connection).is_some() {
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
    unsafe {
        PARAMS
            .read()
            .unwrap()
            .as_ref()
            .unwrap()
            .get_cur_playlist()
            .clone()
            .into_jobject_array(&env)
    }
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
        .position(|track| unsafe {
            *track
                == *PARAMS
                    .read()
                    .unwrap()
                    .as_ref()
                    .unwrap()
                    .get_cur_playlist()
                    .get_cur_track()
                    .unwrap()
        })
        .unwrap();

    unsafe {
        *PARAMS
            .write()
            .unwrap()
            .as_mut()
            .unwrap()
            .get_cur_playlist_mut() =
            DefaultPlaylist::new(None, PlaylistType::default(), new_playlist, new_cur_ind);
    }

    StorageUtil::store_current_playlist(unsafe {
        PARAMS
            .read()
            .unwrap()
            .as_ref()
            .unwrap()
            .get_cur_playlist()
            .clone()
    })
    .unwrap_or_default()
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
