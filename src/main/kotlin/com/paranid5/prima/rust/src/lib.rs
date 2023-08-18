extern crate futures;
extern crate jni;

#[macro_use]
extern crate diesel;

pub mod audio_player;
pub mod audio_scanner;
pub mod databases;
pub mod entities;
pub mod utils;

#[cfg(test)]
mod tests;

use diesel::prelude::*;
use futures::executor::block_on;

use std::{cell::RefCell, collections::HashSet, path::PathBuf, rc::Rc, sync::Arc, time::Duration};

use crate::{
    audio_player::{audio_player::*, playback_params::PlaybackParams},
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
    utils::{
        extensions::{
            jlist_ext::JListExt, playlist_ext::PlaylistExt, string_ext::StringExt,
            track_ext::TrackExt, vec_ext::ExactSizeIteratorExt,
        },
        storage_util::StorageUtil,
        track_order::{Comparator, Ord, TrackOrder},
        types::{TokioRuntime, AJVM},
    },
};

use jni::{
    objects::{JClass, JList, JObject, JString},
    sys::*,
    JNIEnv,
};

use once_cell::sync::Lazy;
use tokio::sync::RwLock;

static TOKIO_RUNTIME: Lazy<TokioRuntime> = Lazy::new(|| {
    Arc::new(
        tokio::runtime::Builder::new_multi_thread()
            .enable_all()
            .build()
            .unwrap(),
    )
});

static mut AUDIO_PLAYER: Lazy<Option<ARWLPlayer>> = Lazy::new(|| None);

#[inline]
fn atomic_audio_player() -> ARWLPlayer {
    unsafe { AUDIO_PLAYER.clone().unwrap() }
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_initRust(
    _env: JNIEnv,
    _class: JClass,
) {
    block_on(async move {
        let mut player = Arc::new(RwLock::new(
            AudioPlayer::new(PlaybackParams::default().await).await,
        ));

        unsafe {
            AUDIO_PLAYER.as_mut().replace(&mut player);
        }
    });

    let mut db_connection = establish_connection().unwrap();

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
    .execute(&mut db_connection)
    .unwrap_or_default();

    diesel::sql_query(
        "CREATE TABLE IF NOT EXISTS favourite_artists (name TEXT PRIMARY KEY NOT NULL)",
    )
    .execute(&mut db_connection)
    .unwrap_or_default();

    diesel::sql_query(
        r#"CREATE TABLE IF NOT EXISTS favourite_playlists (
  id INTEGER PRIMARY KEY,
  title TEXT,
  tp INTEGER NOT NULL
)"#,
    )
    .execute(&mut db_connection)
    .unwrap_or_default();
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_hello(
    mut env: JNIEnv,
    _class: jclass,
    name: JString,
) -> jstring {
    let name = unsafe { String::from_jstring_unchecked(&mut env, &name) };

    env.new_string(format!("Hello, {}!", name))
        .unwrap()
        .into_raw()
}

#[inline]
fn ajvm(env: &JNIEnv) -> AJVM {
    Arc::new(env.get_java_vm().unwrap())
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_getAllTracksBlocking(
    env: JNIEnv,
    _class: JClass,
) -> jobjectArray {
    let env = Rc::new(RefCell::new(env));
    let jvm = { ajvm(&*env.borrow()) };

    block_on(async move {
        AudioScanner::get_all_tracks(jvm, TOKIO_RUNTIME.clone())
            .await
            .lock()
            .await
            .iter()
            .into_jobject_array(env)
            .into_raw()
    })
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_getCurTrackBlocking(
    env: JNIEnv,
    _class: JClass,
) -> jobject {
    let env = Rc::new(RefCell::new(env));
    let jvm = { ajvm(&*env.borrow()) };

    block_on(async move {
        let playlist = StorageUtil::load_current_playlist(jvm).await;
        let cur_track = playlist.get_cur_track();

        match cur_track {
            None => std::ptr::null_mut(),
            Some(track) => track.to_java_track(env).into_raw(),
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
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_calcTrackTime(
    env: JNIEnv,
    _class: JClass,
    mut millis: jint,
) -> jintArray {
    let time = env.new_int_array(3).unwrap();

    let h = millis / 3600000;
    millis -= h * 3600000;

    let m = millis / 60000;
    millis -= m * 60000;

    let s = millis / 1000;

    let arr = [h, m, s];

    env.set_int_array_region(&time, 0, arr.as_slice()).unwrap();
    time.into_raw()
}

#[inline]
async fn get_path_and_duration_of_cur_track(jvm: AJVM) -> (PathBuf, Duration) {
    let playlist = StorageUtil::load_current_playlist(jvm).await;
    let cur_track = playlist.get_cur_track().unwrap();

    (
        cur_track.get_path().clone(),
        cur_track.get_duration().to_std().unwrap(),
    )
}

#[inline]
async fn get_duration_of_cur_track(jvm: AJVM) -> Duration {
    StorageUtil::load_current_playlist(jvm)
        .await
        .get_cur_track()
        .unwrap()
        .get_duration()
        .to_std()
        .unwrap()
}

#[inline]
async fn has_cur_track(jvm: AJVM) -> bool {
    StorageUtil::load_current_playlist(jvm)
        .await
        .get_cur_track()
        .is_some()
}

#[inline]
async fn set_cur_playlist(playlist: DefaultPlaylist<DefaultTrack>) {
    StorageUtil::store_current_playlist(playlist)
        .await
        .unwrap_or_default()
}

#[inline]
async fn is_prev_track_equals_cur_track(jvm: AJVM, cur_track: &DefaultTrack) -> bool {
    let playlist = StorageUtil::load_current_playlist(jvm).await;
    let prev_track = playlist.get_cur_track();
    *prev_track.unwrap() == *cur_track
}

#[inline]
async fn play_pause_cur_track(jvm: AJVM, playlist: Option<DefaultPlaylist<DefaultTrack>>) {
    let cur_track = playlist.as_ref().map(|p| p.get_cur_track()).flatten();

    let audio_player = atomic_audio_player();
    let is_playing = atomic_audio_player().read().await.is_playing();

    if is_playing {
        AudioPlayer::stop(audio_player.clone(), TOKIO_RUNTIME.clone()).await;

        if playlist.is_none() {
            pause().await;
            return;
        }

        if is_prev_track_equals_cur_track(jvm.clone(), cur_track.unwrap()).await {
            set_cur_playlist(playlist.unwrap()).await;
            pause().await;
        } else {
            store_and_play_playlist(jvm, playlist).await;
        }

        return;
    }

    if !has_cur_track(jvm.clone()).await {
        store_and_play_playlist(jvm, playlist).await;
        return;
    }

    if playlist.is_none() {
        resume(jvm).await;
        return;
    }

    if is_prev_track_equals_cur_track(jvm.clone(), cur_track.unwrap()).await {
        set_cur_playlist(playlist.unwrap()).await;
        resume(jvm).await
    } else {
        store_and_play_playlist(jvm, playlist).await
    }
}

#[inline]
async fn pause() {
    AudioPlayer::pause(atomic_audio_player(), TOKIO_RUNTIME.clone()).await;
}

#[inline]
async fn resume(jvm: AJVM) {
    let (_, track_duration) = get_path_and_duration_of_cur_track(jvm.clone()).await;

    AudioPlayer::resume(
        atomic_audio_player(),
        TOKIO_RUNTIME.clone(),
        jvm,
        track_duration,
    )
    .await
}

#[inline]
async fn store_and_play_playlist(jvm: AJVM, playlist: Option<DefaultPlaylist<DefaultTrack>>) {
    set_cur_playlist(playlist.unwrap()).await;

    let (path, track_duration) = get_path_and_duration_of_cur_track(jvm.clone()).await;

    AudioPlayer::play(
        atomic_audio_player(),
        TOKIO_RUNTIME.clone(),
        jvm,
        path,
        track_duration,
    )
    .await
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_onTrackClickedBlocking(
    env: JNIEnv,
    _class: JClass,
    tracks: JObject,
    track_index: jint,
) {
    let env = Rc::new(RefCell::new(env));
    let jvm = { ajvm(&*env.borrow()) };

    let playlist = DefaultPlaylist::new(
        None,
        PlaylistType::default(),
        JList::from_env(&mut *env.borrow_mut(), &tracks)
            .unwrap()
            .map(env.clone(), |jtrack, env| {
                DefaultTrack::from_env(env, jtrack)
            }),
        track_index as usize,
    );

    block_on(async move {
        StorageUtil::store_current_playlist(playlist.clone())
            .await
            .unwrap_or_default();

        play_pause_cur_track(jvm, Some(playlist)).await
    })
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_onPlayButtonClickedBlocking(
    env: JNIEnv,
    _class: JClass,
) {
    let jvm = ajvm(&env);

    block_on(async move {
        if has_cur_track(jvm.clone()).await {
            play_pause_cur_track(jvm, None).await
        }
    })
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_onNextTrackClickedBlocking(
    env: JNIEnv,
    _class: JClass,
) {
    let jvm = ajvm(&env);

    block_on(async move {
        let mut playlist = StorageUtil::load_current_playlist(jvm.clone()).await;
        let cur_track = playlist.get_cur_track();

        if cur_track.is_some() {
            playlist.skip_to_next();

            StorageUtil::store_current_playlist(playlist.clone())
                .await
                .unwrap_or_default();

            let cur_track = playlist.get_cur_track().unwrap();

            let (path, duration) = (
                cur_track.get_path().clone(),
                cur_track.get_duration().to_std().unwrap(),
            );

            AudioPlayer::play(
                atomic_audio_player(),
                TOKIO_RUNTIME.clone(),
                jvm,
                path,
                duration,
            )
            .await
        }
    })
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_onPreviousTrackClickedBlocking(
    env: JNIEnv,
    _class: JClass,
) {
    let jvm = ajvm(&env);

    block_on(async move {
        let mut playlist = StorageUtil::load_current_playlist(jvm.clone()).await;
        let cur_track = playlist.get_cur_track();

        if cur_track.is_some() {
            playlist.skip_to_prev();

            StorageUtil::store_current_playlist(playlist.clone())
                .await
                .unwrap_or_default();

            let cur_track = playlist.get_cur_track().unwrap();

            let (path, duration) = (
                cur_track.get_path().clone(),
                cur_track.get_duration().to_std().unwrap(),
            );

            AudioPlayer::play(
                atomic_audio_player(),
                TOKIO_RUNTIME.clone(),
                jvm,
                path,
                duration,
            )
            .await
        }
    })
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_getCurTrackIndexBlocking(
    env: JNIEnv,
    _class: JClass,
) -> jsize {
    let jvm = ajvm(&env);
    block_on(async move { StorageUtil::load_current_playlist(jvm).await.get_cur_ind() as jsize })
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_getPlaybackPositionBlocking(
    _env: JNIEnv,
    _class: JClass,
) -> jlong {
    block_on(async { StorageUtil::load_current_playback_position().await as jlong })
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_seekToBlocking(
    env: JNIEnv,
    _class: JClass,
    millis: jlong,
) {
    let jvm = ajvm(&env);

    block_on(async move {
        let playlist = StorageUtil::load_current_playlist(jvm.clone()).await;
        let cur_track = playlist.get_cur_track();

        if let Some(cur_track) = cur_track {
            AudioPlayer::seek_to(
                atomic_audio_player(),
                TOKIO_RUNTIME.clone(),
                jvm,
                Duration::from_millis(millis as u64),
                cur_track.get_duration().to_std().unwrap(),
            )
            .await
        }
    })
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_isPlaying(
    _env: JNIEnv,
    _class: JClass,
) -> jboolean {
    jboolean::from(block_on(async {
        atomic_audio_player().read().await.is_playing()
    }))
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_replayCurTrackBlocking(
    env: JNIEnv,
    _class: JClass,
) {
    let jvm = ajvm(&env);

    block_on(async move {
        let (path, duration) = get_path_and_duration_of_cur_track(jvm.clone()).await;

        AudioPlayer::play(
            atomic_audio_player(),
            TOKIO_RUNTIME.clone(),
            jvm,
            path,
            duration,
        )
        .await
    })
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_setNextLoopingStateBlocking(
    _env: JNIEnv,
    _class: JClass,
) -> jint {
    block_on(async {
        let audio_player = atomic_audio_player();
        AudioPlayer::set_next_looping_state(audio_player.clone()).await;
        let state = audio_player.read().await.get_looping_state();

        StorageUtil::store_looping_state(state)
            .await
            .unwrap_or_default();

        state.into()
    })
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_setVolumeBlocking(
    _env: JNIEnv,
    _class: JClass,
    volume: jfloat,
) {
    block_on(async {
        StorageUtil::store_volume(volume).await.unwrap_or_default();
        AudioPlayer::set_volume(atomic_audio_player(), volume as f32).await
    })
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_setSpeed(
    _env: JNIEnv,
    _class: JClass,
    speed: jfloat,
) {
    block_on(async {
        StorageUtil::store_speed(speed).await.unwrap_or_default();
        AudioPlayer::set_speed(atomic_audio_player(), speed as f32).await
    })
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_getVolume(
    _env: JNIEnv,
    _class: JClass,
) -> jfloat {
    block_on(async { atomic_audio_player().write().await.get_volume() as jfloat })
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_getSpeed(
    _env: JNIEnv,
    _class: JClass,
) -> jfloat {
    block_on(async { atomic_audio_player().write().await.get_speed() as jfloat })
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_getLoopingState(
    _env: JNIEnv,
    _class: JClass,
) -> jint {
    block_on(async {
        atomic_audio_player()
            .write()
            .await
            .get_looping_state()
            .into()
    })
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_getTrackOrder(
    env: JNIEnv,
    _class: JClass,
) -> jintArray {
    let ord = block_on(async {
        let order = StorageUtil::load_track_order().await;
        (order.comparator, order.order)
    });

    let arr = env.new_int_array(2).unwrap();
    let order: jint = ord.1.into();

    env.set_int_array_region(&arr, 0, &[ord.0.into(), order + 5])
        .unwrap();

    arr.into_raw()
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_setTrackOrderBlocking(
    _env: JNIEnv,
    _class: JClass,
    comparator: jint,
    order: jint,
) {
    let order = TrackOrder::new(Comparator::from(comparator), Ord::from(order - 5));

    block_on(async move {
        StorageUtil::store_track_order(order)
            .await
            .unwrap_or_default()
    });
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_setMusicSearchPathBlocking(
    mut env: JNIEnv,
    _class: JClass,
    path: JString,
) {
    let path = unsafe { String::from_jstring_unchecked(&mut env, &path) };

    block_on(async {
        StorageUtil::store_music_search_path(PathBuf::from(path))
            .await
            .unwrap_or_default()
    })
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_storeCurPlaybackPosBlocking(
    _env: JNIEnv,
    _class: JClass,
) {
    block_on(async {
        AudioPlayer::save_cur_playback_pos_async(
            atomic_audio_player().clone(),
            TOKIO_RUNTIME.clone(),
        )
        .await
        .await
        .unwrap_or_default()
    })
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_onLikeTrackClicked(
    env: JNIEnv,
    _class: JClass,
    track: JObject,
) {
    let env = Rc::new(RefCell::new(env));
    let track = DefaultTrack::from_env(env, track).into_favourable();
    let mut connection = establish_connection().unwrap();

    if FavouriteTrackDao::get_by_key(track.get_key().clone(), &mut connection).is_some() {
        FavouriteTrackDao::remove(vec![track], &mut connection)
    } else {
        FavouriteTrackDao::insert(vec![track], &mut connection)
    }
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_isTrackLiked(
    env: JNIEnv,
    _class: JClass,
    track: JObject,
) -> jboolean {
    let env = Rc::new(RefCell::new(env));
    let track = DefaultTrack::from_env(env, track);
    let mut connection = establish_connection().unwrap();

    jboolean::from(
        FavouriteTrackDao::get_by_key(track.get_path().clone(), &mut connection).is_some(),
    )
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_getCurPlaylistBlocking(
    env: JNIEnv,
    _class: JClass,
) -> jobjectArray {
    let env = Rc::new(RefCell::new(env));
    let jvm = { ajvm(&*env.borrow()) };

    block_on(async move {
        StorageUtil::load_current_playlist(jvm)
            .await
            .into_jobject_array(env)
            .into_raw()
    })
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_updateAndStoreCurPlaylistBlocking(
    env: JNIEnv,
    _class: JClass,
    cur_playlist: JObject,
) {
    let env = Rc::new(RefCell::new(env));
    let jvm = { ajvm(&*env.borrow_mut()) };

    let new_playlist = JList::from_env(&mut *env.borrow_mut(), &cur_playlist)
        .unwrap()
        .map(env.clone(), |jtrack, env| {
            DefaultTrack::from_env(env, jtrack)
        });

    block_on(async move {
        let playlist = StorageUtil::load_current_playlist(jvm).await;
        let cur_track = playlist.get_cur_track().unwrap();

        let new_cur_ind = new_playlist
            .iter()
            .position(|track| *track == *cur_track)
            .unwrap();

        StorageUtil::store_current_playlist(DefaultPlaylist::new(
            None,
            PlaylistType::default(),
            new_playlist,
            new_cur_ind,
        ))
        .await
        .unwrap_or_default();
    });
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_getFavouriteTracks(
    env: JNIEnv,
    _class: JClass,
) -> jobjectArray {
    let mut connection = establish_connection().unwrap();
    let tracks: Vec<FavouriteTrack> = FavouriteTrackDao::get_all(&mut connection);

    tracks
        .into_iter()
        .into_jobject_array(Rc::new(RefCell::new(env)))
        .into_raw()
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
    mut env: JNIEnv,
    _class: JClass,
    name: JString,
) -> jstring {
    let jstring = unsafe { String::from_jstring_unchecked(&mut env, &name) };

    env.new_string(String::from_iter(
        jstring
            .trim()
            .split_whitespace()
            .into_iter()
            .filter(|&x| x != "&" && x != "feat." && x != "/" && x != "ft.")
            .take(2)
            .map(|s| s.chars().next().unwrap().to_uppercase().next().unwrap()),
    ))
    .unwrap()
    .into_raw()
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_onLikeArtistClicked(
    mut env: JNIEnv,
    _class: JClass,
    artist: JString,
) {
    let artist = FavouriteArtist::new(unsafe { String::from_jstring_unchecked(&mut env, &artist) });
    let mut connection = establish_connection().unwrap();

    if FavouriteArtistDao::get_by_key(artist.get_key().clone(), &mut connection).is_some() {
        FavouriteArtistDao::remove(vec![artist], &mut connection)
    } else {
        FavouriteArtistDao::insert(vec![artist], &mut connection)
    }
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_isArtistLiked(
    mut env: JNIEnv,
    _class: JClass,
    artist: JString,
) -> jboolean {
    let artist = FavouriteArtist::new(unsafe { String::from_jstring_unchecked(&mut env, &artist) });
    let mut connection = establish_connection().unwrap();
    jboolean::from(
        FavouriteArtistDao::get_by_key(artist.get_key().clone(), &mut connection).is_some(),
    )
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_getFavouriteArtists(
    mut env: JNIEnv,
    _class: JClass,
) -> jobjectArray {
    let mut connection = establish_connection().unwrap();
    let artists = FavouriteArtistDao::get_all(&mut connection);

    let arr = env
        .new_object_array(artists.len() as jsize, "java/lang/String", &JObject::null())
        .unwrap();

    artists.into_iter().enumerate().for_each(|(ind, artist)| {
        env.set_object_array_element(
            &arr,
            ind as jsize,
            env.new_string(artist.into_string()).unwrap(),
        )
        .unwrap();
    });

    arr.into_raw()
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_getAllArtistsBlocking(
    mut env: JNIEnv,
    _class: JClass,
    placeholder: JString,
) -> jobjectArray {
    let jvm = Arc::new(env.get_java_vm().unwrap());

    block_on(async move {
        let tracks = AudioScanner::get_all_tracks(jvm, TOKIO_RUNTIME.clone()).await;
        let tracks = tracks.lock().await;

        let mut artists = tracks
            .iter()
            .filter_map(|track| track.get_artist())
            .collect::<HashSet<_>>();

        let is_empty_array_list = artists.remove(&"".to_string());

        let mut artists = artists.into_iter().collect::<Vec<_>>();
        artists.sort_unstable();

        let mut artists = artists
            .into_iter()
            .map(|string| env.new_string(string.clone()).unwrap())
            .collect::<Vec<_>>();

        if is_empty_array_list {
            artists.push(placeholder)
        }

        let arr = env
            .new_object_array(artists.len() as jsize, "java/lang/String", &JObject::null())
            .unwrap();

        artists.into_iter().enumerate().for_each(|(ind, artist)| {
            env.set_object_array_element(&arr, ind as jsize, artist)
                .unwrap()
        });

        arr.into_raw()
    })
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_getArtistTracksBlocking(
    mut env: JNIEnv,
    _class: JClass,
    artist: JString,
) -> jobjectArray {
    let jvm = Arc::new(env.get_java_vm().unwrap());
    let artist = unsafe { String::from_jstring_unchecked(&mut env, &artist) };

    block_on(async move {
        AudioScanner::get_all_tracks(jvm, TOKIO_RUNTIME.clone())
            .await
            .lock()
            .await
            .iter()
            .filter(|track| track.get_artist().is_some())
            .filter(|track| artist == *track.get_artist().unwrap())
            .collect::<Vec<_>>()
            .into_iter()
            .into_jobject_array(Rc::new(RefCell::new(env)))
            .into_raw()
    })
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_onLikePlaylistClicked(
    mut env: JNIEnv,
    _class: JClass,
    id: JObject,
    title: JString,
    tp: jint,
) {
    let title = String::from_jstring(&mut env, &title);
    let tp = tp as i32;
    let mut connection = establish_connection().unwrap();

    if id.is_null() {
        FavouritePlaylistDao::insert(
            vec![FavouritePlaylistDBEntity::new(0, title, tp)],
            &mut connection,
        )
    } else {
        let id = env
            .call_method(id, "intValue", "()I", &[])
            .unwrap()
            .i()
            .unwrap();

        if FavouritePlaylistDao::get_by_key(id, &mut connection).is_some() {
            FavouritePlaylistDao::remove(
                vec![FavouritePlaylistDBEntity::new(id, title, tp)],
                &mut connection,
            )
        } else {
            FavouritePlaylistDao::insert(
                vec![FavouritePlaylistDBEntity::new(id, title, tp)],
                &mut connection,
            )
        }
    }
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_isPlaylistLiked(
    mut env: JNIEnv,
    _class: JClass,
    id: JObject,
) -> jboolean {
    jboolean::from(if id.is_null() {
        false
    } else {
        let id = env
            .call_method(id, "intValue", "()I", &[])
            .unwrap()
            .i()
            .unwrap();

        FavouritePlaylistDao::get_by_key(id, &mut establish_connection().unwrap()).is_some()
    })
}
