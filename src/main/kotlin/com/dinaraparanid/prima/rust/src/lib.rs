extern crate futures;
extern crate jni;

pub mod audio_player;
pub mod audio_scanner;
pub mod entities;
pub mod jvm;
pub mod utils;

#[cfg(test)]
mod tests;

use futures::executor::block_on;
use std::{path::PathBuf, sync::Arc, time::Duration};

use crate::{
    audio_player::audio_player::AUDIO_PLAYER,
    audio_scanner::AudioScanner,
    entities::{
        playlists::{
            default_playlist::DefaultPlaylist, playlist_trait::PlaylistTrait,
            playlist_type::PlaylistType,
        },
        tracks::{default_track::DefaultTrack, track_trait::TrackTrait},
    },
    jvm::JVM,
    utils::{
        extensions::track_ext::TrackExt,
        params::PARAMS,
        track_order::{Comparator, Ord, TrackOrder},
        wrappers::jtrack::JTrack,
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
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_getAllTracksAsync(
    env: JNIEnv,
    _class: jclass,
) -> jobjectArray {
    let rust_tracks = block_on(AudioScanner::get_all_tracks());
    let rust_tracks = &*rust_tracks.lock().unwrap();

    let java_track_class = env
        .find_class("com/dinaraparanid/prima/entities/Track")
        .unwrap();

    let java_tracks = env
        .new_object_array(
            rust_tracks.len() as jsize,
            java_track_class,
            JObject::null(),
        )
        .unwrap();

    rust_tracks
        .into_iter()
        .enumerate()
        .for_each(|(ind, track)| {
            env.set_object_array_element(java_tracks, ind as jsize, track.to_java_track(&env))
                .unwrap();
        });

    java_tracks
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_getCurTrack(
    env: JNIEnv,
    _class: jclass,
) -> jobject {
    match unsafe {
        PARAMS
            .read()
            .unwrap()
            .as_ref()
            .unwrap()
            .cur_playlist
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
        .cur_playlist
        .get_cur_track()
        .unwrap();

    (
        track.get_path().clone(),
        track.get_duration().to_std().unwrap(),
    )
}

#[inline]
fn has_cur_track() -> bool {
    let params = unsafe { &PARAMS.read() };
    params
        .as_ref()
        .unwrap()
        .as_ref()
        .unwrap()
        .cur_playlist
        .get_cur_track()
        .is_some()
}

#[inline]
fn play_pause_cur_track() {
    let (path, duration) = get_path_and_duration_of_cur_track();
    let is_playing = unsafe { AUDIO_PLAYER.read().unwrap().is_playing() };

    if is_playing {
        unsafe {
            AUDIO_PLAYER.write().unwrap().stop();
        }

        if unsafe {
            AUDIO_PLAYER
                .read()
                .unwrap()
                .get_cur_path()
                .unwrap()
                .eq(&path)
        } {
            unsafe { AUDIO_PLAYER.write().unwrap().pause() }
        } else {
            block_on(unsafe { AUDIO_PLAYER.write().unwrap().play(path, duration) })
        }
    } else {
        if unsafe { AUDIO_PLAYER.read().unwrap().get_cur_path().is_none() } {
            block_on(unsafe { AUDIO_PLAYER.write().unwrap().play(path, duration) })
        } else {
            if {
                unsafe {
                    AUDIO_PLAYER
                        .read()
                        .unwrap()
                        .get_cur_path()
                        .unwrap()
                        .eq(&path)
                }
            } {
                unsafe { AUDIO_PLAYER.write().unwrap().resume() }
            } else {
                block_on(unsafe { AUDIO_PLAYER.write().unwrap().play(path, duration) })
            }
        }
    }
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_onTrackClickedAsync(
    env: JNIEnv,
    _class: jclass,
    tracks: JObject,
    track_index: jint,
) {
    PARAMS.write().unwrap().as_mut().unwrap().cur_playlist = DefaultPlaylist::new(
        None,
        PlaylistType::default(),
        JList::from_env(&env, tracks)
            .unwrap()
            .iter()
            .unwrap()
            .map(|jtrack| DefaultTrack::from(JTrack::from_env(&env, jtrack))),
        track_index as usize,
    );

    play_pause_cur_track()
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_onPlayButtonClickedAsync(
    _env: JNIEnv,
    _class: jclass,
) {
    if has_cur_track() {
        play_pause_cur_track()
    }
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_onNextTrackClickedAsync(
    _env: JNIEnv,
    _class: jclass,
) {
    if has_cur_track() {
        PARAMS
            .write()
            .unwrap()
            .as_mut()
            .unwrap()
            .cur_playlist
            .skip_to_next();

        let (path, duration) = get_path_and_duration_of_cur_track();
        block_on(AUDIO_PLAYER.write().unwrap().play(path, duration))
    }
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_onPreviousTrackClickedAsync(
    _env: JNIEnv,
    _class: jclass,
) {
    if has_cur_track() {
        PARAMS
            .write()
            .unwrap()
            .as_mut()
            .unwrap()
            .cur_playlist
            .skip_to_prev();

        let (path, duration) = get_path_and_duration_of_cur_track();
        block_on(AUDIO_PLAYER.write().unwrap().play(path, duration))
    }
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_getCurTrackIndex(
    _env: JNIEnv,
    _class: jclass,
) -> jsize {
    unsafe {
        PARAMS
            .read()
            .unwrap()
            .as_ref()
            .unwrap()
            .cur_playlist
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
    unsafe {
        if has_cur_track() {
            AUDIO_PLAYER
                .write()
                .unwrap()
                .seek_to(Duration::from_millis(millis as u64))
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
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_replayCurrentTrackAsync(
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
        AUDIO_PLAYER.read().unwrap().get_looping_state().into()
    }
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_setVolume(
    _env: JNIEnv,
    _class: jclass,
    volume: jfloat,
) {
    unsafe { AUDIO_PLAYER.write().unwrap().set_volume(volume as f32) }
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_setSpeed(
    _env: JNIEnv,
    _class: jclass,
    speed: jfloat,
) {
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
        let order = PARAMS.write().unwrap().as_mut().unwrap().track_order;
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
        PARAMS.write().unwrap().as_mut().unwrap().track_order =
            TrackOrder::new(Comparator::from(comparator), Ord::from(order - 5))
    }
}
