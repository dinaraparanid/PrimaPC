extern crate futures;
extern crate jni;

pub mod entities;
pub mod jvm;
mod traits;
mod utils;

#[cfg(test)]
mod tests;

use crate::{jvm::JVM, traits::track_trait::TrackTrait, utils::audio_scanner::AUDIO_SCANNER};
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
    class: jclass,
) {
    unsafe {
        let jvm = &mut JVM.write().unwrap();
        jvm.jni_env = Arc::new(Some(env));
        jvm.rust_libs_class = Arc::new(Some(class));
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
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_getAllTracks(
    env: JNIEnv,
    _class: jclass,
) -> jobjectArray {
    let rust_tracks = block_on(unsafe { &AUDIO_SCANNER }.read().unwrap().get_all_tracks()).clone();
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
            let java_track = env
                .new_object(
                    java_track_class,
                    "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;JS)V",
                    &[
                        JValue::Object(JObject::from(
                            env.new_string(track.get_title().unwrap()).unwrap(),
                        )),
                        JValue::Object(JObject::from(
                            env.new_string(track.get_artist().unwrap()).unwrap(),
                        )),
                        JValue::Object(JObject::from(
                            env.new_string(track.get_album().unwrap()).unwrap(),
                        )),
                        JValue::Long(track.get_duration().num_seconds() as jlong),
                        JValue::Short(track.get_number_in_album() as jshort),
                    ],
                )
                .unwrap();

            env.set_object_array_element(java_tracks, ind as jsize, java_track);
        });

    java_tracks
}
