extern crate jni;
extern crate yaml_rust;

use crate::{utils::extensions::path_buf_ext::PathBufExt, TrackTrait};
use yaml_rust::Yaml;

use jni::{
    objects::{JObject, JValue},
    sys::{jlong, jshort},
    JNIEnv,
};

pub trait TrackExt: TrackTrait {
    fn to_java_track<'a>(&self, env: &'a JNIEnv<'a>) -> JObject<'a>;
    fn into_yaml(self) -> Yaml;
}

impl<T: TrackTrait> TrackExt for T {
    #[inline]
    fn to_java_track<'a>(&self, env: &'a JNIEnv<'a>) -> JObject<'a> {
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

        env.new_object(
            "com/dinaraparanid/prima/entities/Track",
            "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[BJS)V",
            &[
                JValue::Object(JObject::from(
                    env.new_string(self.get_title().unwrap()).unwrap(),
                )),
                JValue::Object(JObject::from(
                    env.new_string(self.get_artist().unwrap()).unwrap(),
                )),
                JValue::Object(JObject::from(
                    env.new_string(self.get_album().unwrap()).unwrap(),
                )),
                JValue::Object(JObject::from(
                    env.byte_array_from_slice(path_buf.as_slice()).unwrap(),
                )),
                JValue::Long(self.get_duration().num_milliseconds() as jlong),
                JValue::Short(self.get_number_in_album() as jshort),
            ],
        )
        .unwrap()
    }

    #[inline]
    fn into_yaml(self) -> Yaml {
        Yaml::String(self.get_path().to_string())
    }
}
