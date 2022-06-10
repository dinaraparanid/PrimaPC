extern crate futures;
extern crate jni;

pub mod entities;
pub mod jvm;
mod traits;
pub(crate) mod utils;

#[cfg(test)]
mod tests;

use crate::{jvm::JVM, traits::track_trait::TrackTrait, utils::audio_scanner::AudioScanner};
use futures::executor::block_on;
use std::sync::Arc;

use jni::{
    objects::{JObject, JString, JValue},
    sys::{jclass, jlong, jobjectArray, jshort, jsize, jstring},
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
            let mut path_buf = Vec::new();

            #[cfg(unix)]
            {
                use std::os::unix::ffi::OsStrExt;
                path_buf.extend(track.get_path().as_os_str().as_bytes());
                path_buf.push(0);
            }

            #[cfg(windows)]
            {
                use std::os::windows::ffi::OsStrExt;
                buf.extend(
                    track
                        .get_path()
                        .as_os_str()
                        .encode_wide()
                        .chain(Some(0))
                        .map(|b| {
                            let b = b.to_ne_bytes();
                            b.get(0).map(|s| *s).into_iter().chain(b.get(1).map(|s| *s))
                        })
                        .flatten(),
                );
            }

            let java_track = env
                .new_object(
                    java_track_class,
                    "([B[B[B[BJS)V",
                    &[
                        JValue::Object(JObject::from(
                            env.byte_array_from_slice(
                                &track
                                    .get_title()
                                    .unwrap()
                                    .iter()
                                    .map(|&jb| jb as u8)
                                    .collect::<Vec<_>>()
                                    .as_slice(),
                            )
                            .unwrap(),
                        )),
                        JValue::Object(JObject::from(
                            env.byte_array_from_slice(
                                &track
                                    .get_artist()
                                    .unwrap()
                                    .iter()
                                    .map(|&jb| jb as u8)
                                    .collect::<Vec<_>>()
                                    .as_slice(),
                            )
                            .unwrap(),
                        )),
                        JValue::Object(JObject::from(
                            env.byte_array_from_slice(
                                &track
                                    .get_album()
                                    .unwrap()
                                    .iter()
                                    .map(|&jb| jb as u8)
                                    .collect::<Vec<_>>()
                                    .as_slice(),
                            )
                            .unwrap(),
                        )),
                        JValue::Object(JObject::from(
                            env.byte_array_from_slice(path_buf.as_slice()).unwrap(),
                        )),
                        JValue::Long(track.get_duration().num_seconds() as jlong),
                        JValue::Short(track.get_number_in_album() as jshort),
                    ],
                )
                .unwrap();

            env.set_object_array_element(java_tracks, ind as jsize, java_track)
                .unwrap();
        });

    java_tracks
}
