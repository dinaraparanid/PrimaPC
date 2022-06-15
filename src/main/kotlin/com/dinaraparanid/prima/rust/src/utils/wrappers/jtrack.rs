extern crate chrono;
extern crate jni;
extern crate os_str_bytes;

use chrono::{DateTime, Duration, Local};
use os_str_bytes::{OsStrBytes, OsStringBytes};

use crate::{
    entities::tracks::default_track::DefaultTrack, utils::constants::NULL_CHARACTER,
    utils::extensions::jni_env_ext::JNIEnvExt, TrackTrait,
};

use std::{
    ffi::{OsStr, OsString},
    path::PathBuf,
    slice::from_raw_parts,
    time::SystemTime,
};

use jni::{
    objects::{JObject, JString, JValue, ReleaseMode},
    signature::{JavaType, Primitive},
    sys::{jbyte, jlong, jshort},
    JNIEnv,
};

#[derive(Clone, Debug)]
pub struct JTrack {
    title: Option<Vec<jbyte>>,
    artist: Option<Vec<jbyte>>,
    album: Option<Vec<jbyte>>,
    path: PathBuf,
    duration: Duration,
    add_date: DateTime<Local>,
    number_in_album: i16,
}

impl JTrack {
    #[inline]
    pub fn new(
        title: Option<Vec<jbyte>>,
        artist: Option<Vec<jbyte>>,
        album: Option<Vec<jbyte>>,
        path: PathBuf,
        duration: Duration,
        add_date: DateTime<Local>,
        number_in_album: jshort,
    ) -> Self {
        Self {
            title,
            artist,
            album,
            path,
            duration,
            add_date,
            number_in_album,
        }
    }

    #[inline]
    pub fn from_env(jni_env: &JNIEnv, jobject: JObject) -> Self {
        let path = PathBuf::from(OsString::from(
            OsStr::from_raw_bytes(
                get_byte_vec_field_of_jtrack(jni_env, &jobject, "path")
                    .unwrap()
                    .into_iter()
                    .filter(|jb| *jb != *NULL_CHARACTER as i8)
                    .map(|jb| jb as u8)
                    .collect::<Vec<_>>()
                    .as_slice(),
            )
            .unwrap(),
        ));

        Self {
            title: get_string_field_of_jtrack(jni_env, &jobject, "title"),
            artist: get_string_field_of_jtrack(jni_env, &jobject, "artist"),
            album: get_string_field_of_jtrack(jni_env, &jobject, "album"),
            duration: get_duration_field_of_jtrack(jni_env, &jobject),
            number_in_album: get_number_in_album_of_jtrack(jni_env, &jobject),
            add_date: DateTime::from(
                std::fs::metadata(&path)
                    .unwrap()
                    .created()
                    .unwrap_or(SystemTime::now()),
            ),
            path,
        }
    }

    #[inline]
    pub fn to_jobject<'a, 'b>(&'a self, jni_env: &'b JNIEnv) -> JObject<'b> {
        jni_env
            .new_object(
                "com/dinaraparanid/prima/entities/Track",
                "([B[B[B[BJS)V",
                &[
                    JValue::Object(JObject::from(
                        jni_env
                            .byte_array_from_slice(
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
                        jni_env
                            .byte_array_from_slice(
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
                        jni_env
                            .byte_array_from_slice(
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
                        jni_env
                            .byte_array_from_slice(
                                OsString::into_raw_vec(self.path.clone().into_os_string())
                                    .as_slice(),
                            )
                            .unwrap(),
                    )),
                    JValue::Long(self.get_duration().num_seconds() as jlong),
                    JValue::Short(self.get_number_in_album() as jshort),
                ],
            )
            .unwrap()
    }

    #[inline]
    pub fn to_default_track(self) -> DefaultTrack {
        DefaultTrack::new(
            self.title,
            self.artist,
            self.album,
            self.path,
            self.duration,
            self.add_date,
            self.number_in_album,
        )
    }
}

impl TrackTrait for JTrack {
    #[inline]
    fn get_title(&self) -> Option<&Vec<jbyte>> {
        self.title.as_ref()
    }

    #[inline]
    fn get_artist(&self) -> Option<&Vec<jbyte>> {
        self.artist.as_ref()
    }

    #[inline]
    fn get_album(&self) -> Option<&Vec<jbyte>> {
        self.album.as_ref()
    }

    #[inline]
    fn get_path(&self) -> &PathBuf {
        &self.path
    }

    #[inline]
    fn get_duration(&self) -> &Duration {
        &self.duration
    }

    #[inline]
    fn get_add_date(&self) -> &DateTime<Local> {
        &self.add_date
    }

    #[inline]
    fn get_number_in_album(&self) -> i16 {
        self.number_in_album
    }
}

impl PartialEq for JTrack {
    #[inline]
    fn eq(&self, other: &Self) -> bool {
        self.path.eq(other.get_path())
    }
}

impl From<DefaultTrack> for JTrack {
    #[inline]
    fn from(track: DefaultTrack) -> Self {
        track.to_jtrack()
    }
}

#[inline]
fn get_byte_vec_field_of_jtrack(
    jni_env: &JNIEnv,
    jtrack: &JObject,
    field: &str,
) -> Option<Vec<jbyte>> {
    let jbytes = unsafe {
        match JNIEnvExt::get_field(
            jni_env,
            jtrack.into_inner(),
            field,
            "[B",
            JavaType::Array(Box::new(JavaType::Primitive(Primitive::Byte))),
        )
        .l()
        {
            Ok(x) => x,
            Err(_) => return None,
        }
    };

    if jbytes.is_null() {
        return None;
    }

    let jbytes = match jni_env.get_byte_array_elements(jbytes.into_inner(), ReleaseMode::CopyBack) {
        Ok(x) => x,
        Err(_) => return None,
    };

    Some(unsafe {
        from_raw_parts(
            jbytes.as_ptr() as *const jbyte,
            jbytes.size().unwrap() as usize,
        )
        .to_vec()
    })
}

#[inline]
fn get_string_field_of_jtrack(
    jni_env: &JNIEnv,
    jtrack: &JObject,
    field: &str,
) -> Option<Vec<jbyte>> {
    let jstring = unsafe {
        match JNIEnvExt::get_field(
            jni_env,
            jtrack.into_inner(),
            field,
            "Ljava/lang/String;",
            JavaType::Object(String::from("java/lang/String")),
        )
        .l()
        {
            Ok(x) => x,
            Err(_) => return None,
        }
    };

    if jstring.is_null() {
        return None;
    }

    let string: String = jni_env.get_string(JString::from(jstring)).unwrap().into();
    Some(unsafe { from_raw_parts(string.as_ptr() as *const jbyte, string.len()).to_vec() })
}

#[inline]
fn get_duration_field_of_jtrack(jni_env: &JNIEnv, jtrack: &JObject) -> Duration {
    Duration::seconds(
        unsafe {
            JNIEnvExt::get_field(
                jni_env,
                jtrack.into_inner(),
                "duration",
                "J",
                JavaType::Primitive(Primitive::Long),
            )
        }
        .j()
        .unwrap(),
    )
}

#[inline]
fn get_number_in_album_of_jtrack(jni_env: &JNIEnv, jtrack: &JObject) -> jshort {
    unsafe {
        JNIEnvExt::get_field(
            jni_env,
            jtrack.into_inner(),
            "numberInAlbum",
            "S",
            JavaType::Primitive(Primitive::Short),
        )
    }
    .s()
    .unwrap()
}
