extern crate chrono;
extern crate jni;

use crate::{
    entities::tracks::default_track::DefaultTrack, utils::extensions::jni_env_ext::JNIEnvExt,
};

use chrono::{DateTime, Duration};
use std::{path::PathBuf, slice::from_raw_parts};

use jni::{
    objects::{JObject, JValue, ReleaseMode},
    signature::{JavaType, Primitive},
    sys::{jbyte, jsize},
    JNIEnv,
};

pub(crate) trait JObjectExt {
    fn array_to_track(&self, jni_env: &JNIEnv, path: PathBuf) -> Option<DefaultTrack>;
}

impl JObjectExt for JObject<'_> {
    #[inline]
    fn array_to_track(&self, jni_env: &JNIEnv, path: PathBuf) -> Option<DefaultTrack> {
        if self.eq(&std::ptr::null_mut()) {
            return None;
        }

        let title = get_bytes(jni_env, *self, 0);
        let artist = get_bytes(jni_env, *self, 1);
        let album = get_bytes(jni_env, *self, 2);

        let duration = unsafe {
            JNIEnvExt::call_static_method(
                jni_env,
                "com/dinaraparanid/prima/rust/RustLibs",
                "toIntPrimitive",
                "(Ljava/lang/Integer;)I",
                JavaType::Primitive(Primitive::Int),
                &[JValue::Object(
                    jni_env
                        .get_object_array_element(self.into_inner(), 3)
                        .unwrap(),
                )],
            )
        }
        .i()
        .unwrap();

        let number_in_album = unsafe {
            JNIEnvExt::call_static_method(
                jni_env,
                "com/dinaraparanid/prima/rust/RustLibs",
                "toShortPrimitive",
                "(Ljava/lang/Short;)S",
                JavaType::Primitive(Primitive::Short),
                &[JValue::Object(
                    jni_env
                        .get_object_array_element(self.into_inner(), 3)
                        .unwrap(),
                )],
            )
        }
        .s()
        .unwrap();

        Some(DefaultTrack::new(
            title,
            artist,
            album,
            path.clone(),
            Duration::seconds(duration as i64),
            DateTime::from(std::fs::metadata(path).unwrap().created().unwrap()),
            number_in_album,
        ))
    }
}

#[inline]
fn get_bytes(jni_env: &JNIEnv, array: JObject, index: jsize) -> Option<Vec<jbyte>> {
    let arr = match jni_env.get_byte_array_elements(
        match jni_env.get_object_array_element(array.into_inner(), index) {
            Ok(x) => x,
            Err(_) => return None,
        }
        .into_inner(),
        ReleaseMode::NoCopyBack,
    ) {
        Ok(x) => x,
        Err(_) => return None,
    };

    let size = arr.size().unwrap() as usize;

    Some(unsafe { from_raw_parts(arr.as_ptr(), size).to_vec() })
}