extern crate jni;

use crate::TrackTrait;

use jni::{
    objects::{JObject, JValue},
    sys::{jlong, jshort},
    JNIEnv,
};

pub trait TrackExt: TrackTrait {
    fn to_java_track<'a>(&self, env: &'a JNIEnv<'a>) -> JObject<'a>;
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
            "([B[B[B[BJS)V",
            &[
                JValue::Object(JObject::from(
                    env.byte_array_from_slice(
                        self.get_title()
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
                        self.get_artist()
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
                        self.get_album()
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
                JValue::Long(self.get_duration().num_seconds() as jlong),
                JValue::Short(self.get_number_in_album() as jshort),
            ],
        )
        .unwrap()
    }
}
