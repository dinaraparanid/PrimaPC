extern crate futures;
extern crate jni;

pub mod entities;
pub mod jvm;
pub(crate) mod utils;

#[cfg(test)]
mod tests;

use futures::executor::block_on;
use std::sync::Arc;

use crate::{
    entities::{playlists::playlist_trait::PlaylistTrait, tracks::track_trait::TrackTrait},
    jvm::JVM,
    utils::{audio_scanner::AudioScanner, extensions::track_ext::TrackExt, params::PARAMS},
};

use crate::entities::playlists::default_playlist::DefaultPlaylist;
use crate::entities::playlists::playlist_type::PlaylistType;
use crate::entities::tracks::default_track::DefaultTrack;
use crate::utils::wrappers::jtrack::JTrack;
use jni::{
    objects::{JList, JObject, JString},
    sys::{jclass, jint, jintArray, jobject, jobjectArray, jsize, jstring},
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

    let h = millis / 3600;
    millis -= h * 3600;

    let m = millis / 60;
    millis -= m * 60;

    let s = millis;

    let arr = [h, m, s];

    env.set_int_array_region(time, 0, arr.as_slice())
        .unwrap_unchecked();
    time
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_onTrackClicked(
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
    )
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_onNextTrackClicked(
    _env: JNIEnv,
    _class: jclass,
) {
    PARAMS
        .write()
        .unwrap()
        .as_mut()
        .unwrap()
        .cur_playlist
        .skip_to_next()
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_onPreviousTrackClicked(
    _env: JNIEnv,
    _class: jclass,
) {
    PARAMS
        .write()
        .unwrap()
        .as_mut()
        .unwrap()
        .cur_playlist
        .skip_to_prev()
}
