extern crate jni;
extern crate yaml_rust;

use crate::{utils::extensions::path_buf_ext::PathBufExt, TrackTrait};
use std::{cell::RefCell, rc::Rc};
use yaml_rust::Yaml;

use jni::{
    objects::{JObject, JValue},
    sys::{jlong, jshort},
    JNIEnv,
};

pub trait TrackExt: TrackTrait {
    fn to_java_track<'a>(&self, env: Rc<RefCell<JNIEnv<'a>>>) -> JObject<'a>;
    fn to_yaml(&self) -> Yaml;
}

impl<T: TrackTrait> TrackExt for T {
    #[inline]
    fn to_java_track<'a>(&self, env: Rc<RefCell<JNIEnv<'a>>>) -> JObject<'a> {
        let mut path_buf = Vec::new();

        #[cfg(unix)]
        {
            use std::os::unix::ffi::OsStrExt;
            path_buf.extend(self.get_path().as_os_str().as_bytes());
            path_buf.push(0);
        }

        #[cfg(windows)]
        {
            use std::os::windows::ffi::OsStrExt;
            buf.extend(
                self.get_path()
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

        let obj = JObject::from(
            env.borrow_mut()
                .new_string(self.get_title().unwrap())
                .unwrap(),
        );

        let title = JValue::Object(&obj);

        let obj = JObject::from(
            env.borrow_mut()
                .new_string(self.get_artist().unwrap())
                .unwrap(),
        );

        let artist = JValue::Object(&obj);

        let obj = JObject::from(
            env.borrow_mut()
                .new_string(self.get_album().unwrap())
                .unwrap(),
        );

        let album = JValue::Object(&obj);

        let obj = JObject::from(
            env.borrow_mut()
                .byte_array_from_slice(path_buf.as_slice())
                .unwrap(),
        );

        let path = JValue::Object(&obj);

        let duration = JValue::Long(self.get_duration().num_milliseconds() as jlong);
        let num_in_album = JValue::Short(self.get_number_in_album() as jshort);

        env.borrow_mut()
            .new_object(
                "com/paranid5/prima/entities/Track",
                "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[BJS)V",
                &[title, artist, album, path, duration, num_in_album],
            )
            .unwrap()
    }

    #[inline]
    fn to_yaml(&self) -> Yaml {
        Yaml::String(self.get_path().to_string())
    }
}
