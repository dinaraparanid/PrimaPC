extern crate chrono;
extern crate jni;

use crate::{data::utils::extensions::jni_env_ext::JNIEnvExt, DefaultTrack};
use chrono::{DateTime, Duration};
use std::{cell::RefCell, path::PathBuf, rc::Rc};

use jni::{
    objects::{JObjectArray, JString, JValue},
    sys::jsize,
    JNIEnv,
};

pub(crate) trait JObjectArrayExt {
    fn array_to_track(&self, jni_env: Rc<RefCell<JNIEnv>>, path: PathBuf) -> Option<DefaultTrack>;
}

impl JObjectArrayExt for JObjectArray<'_> {
    #[inline]
    fn array_to_track(&self, jni_env: Rc<RefCell<JNIEnv>>, path: PathBuf) -> Option<DefaultTrack> {
        if self.eq(&std::ptr::null_mut()) {
            return None;
        }

        let title = get_str(jni_env.clone(), self, 0);
        let artist = get_str(jni_env.clone(), self, 1);
        let album = get_str(jni_env.clone(), self, 2);

        let arg = jni_env
            .borrow_mut()
            .get_object_array_element(self, 3)
            .unwrap();

        let duration = JNIEnvExt::call_static_method(
            jni_env.clone(),
            "com/paranid5/prima/rust/RustLibs",
            "toLongPrimitive",
            "(Ljava/lang/Long;)J",
            &[JValue::Object(&arg)],
        )
        .j()
        .unwrap();

        let arg = jni_env
            .borrow_mut()
            .get_object_array_element(self, 4)
            .unwrap();

        let number_in_album = JNIEnvExt::call_static_method(
            jni_env,
            "com/paranid5/prima/rust/RustLibs",
            "toShortPrimitive",
            "(Ljava/lang/Short;)S",
            &[JValue::Object(&arg)],
        )
        .s()
        .unwrap();

        Some(DefaultTrack::new(
            title,
            artist,
            album,
            path.clone(),
            Duration::milliseconds(duration as i64),
            DateTime::from(std::fs::metadata(path).unwrap().created().unwrap()),
            number_in_album,
        ))
    }
}

#[inline]
fn get_str(jni_env: Rc<RefCell<JNIEnv>>, array: &JObjectArray, index: jsize) -> Option<String> {
    let jstring = JString::from(
        match jni_env.borrow_mut().get_object_array_element(array, index) {
            Ok(x) => x,
            Err(_) => return None,
        },
    );

    let x = Some(
        match jni_env.borrow_mut().get_string(&jstring) {
            Ok(x) => x,
            Err(_) => return None,
        }
        .into(),
    );

    x
}
