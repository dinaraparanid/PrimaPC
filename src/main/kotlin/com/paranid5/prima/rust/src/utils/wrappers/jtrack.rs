extern crate chrono;
extern crate jni;
extern crate os_str_bytes;

use chrono::{DateTime, Duration, Local};
use os_str_bytes::{OsStrBytes, OsStringBytes};

use crate::{
    entities::{
        favourable::Favourable,
        tracks::{default_track::DefaultTrack, favourite_track::FavouriteTrack},
    },
    utils::{constants::NULL_CHARACTER, extensions::jni_env_ext::JNIEnvExt},
    TrackTrait,
};

use std::{
    cell::RefCell,
    ffi::{OsStr, OsString},
    path::PathBuf,
    rc::Rc,
    time::SystemTime,
};

use jni::{
    objects::{JByteArray, JObject, JString, JValue},
    sys::{jbyte, jlong, jshort},
    JNIEnv,
};

#[derive(Clone, Debug)]
pub struct JTrack {
    title: Option<String>,
    artist: Option<String>,
    album: Option<String>,
    path: PathBuf,
    duration: Duration,
    add_date: DateTime<Local>,
    number_in_album: jshort,
}

impl JTrack {
    #[inline]
    pub fn new(
        title: Option<String>,
        artist: Option<String>,
        album: Option<String>,
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
    pub fn from_env<'b, 'a: 'b>(jni_env: Rc<RefCell<JNIEnv<'a>>>, jobject: JObject<'b>) -> Self {
        let path = PathBuf::from(
            OsStr::assert_from_raw_bytes(
                get_byte_vec_field_of_jtrack(jni_env.clone(), &jobject, "path")
                    .unwrap()
                    .into_iter()
                    .filter(|jb| *jb != *NULL_CHARACTER as i8)
                    .map(|jb| jb as u8)
                    .collect::<Vec<_>>()
                    .as_slice(),
            )
            .to_os_string(),
        );

        Self {
            title: get_string_field_of_jtrack(jni_env.clone(), &jobject, "title"),
            artist: get_string_field_of_jtrack(jni_env.clone(), &jobject, "artist"),
            album: get_string_field_of_jtrack(jni_env.clone(), &jobject, "album"),
            duration: get_duration_field_of_jtrack(jni_env.clone(), &jobject),
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
    pub fn to_jobject<'b, 'a: 'b>(&self, jni_env: Rc<RefCell<JNIEnv<'a>>>) -> JObject<'b> {
        let obj = JObject::from(
            jni_env
                .borrow()
                .new_string(self.get_title().unwrap())
                .unwrap(),
        );

        let title = JValue::Object(&obj);

        let obj = JObject::from(
            jni_env
                .borrow()
                .new_string(self.get_artist().unwrap())
                .unwrap(),
        );

        let artist = JValue::Object(&obj);

        let obj = JObject::from(
            jni_env
                .borrow()
                .new_string(self.get_album().unwrap())
                .unwrap(),
        );

        let album = JValue::Object(&obj);

        let obj = JObject::from(
            jni_env
                .borrow()
                .byte_array_from_slice(
                    OsString::into_raw_vec(self.path.clone().into_os_string()).as_slice(),
                )
                .unwrap(),
        );

        let path = JValue::Object(&obj);

        let duration = JValue::Long(self.get_duration().num_milliseconds() as jlong);
        let num_in_album = JValue::Short(self.get_number_in_album() as jshort);

        jni_env
            .borrow_mut()
            .new_object(
                "com/paranid5/prima/entities/Track",
                "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[BJS)V",
                &[title, artist, album, path, duration, num_in_album],
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
    fn get_title(&self) -> Option<&String> {
        self.title.as_ref()
    }

    #[inline]
    fn get_artist(&self) -> Option<&String> {
        self.artist.as_ref()
    }

    #[inline]
    fn get_album(&self) -> Option<&String> {
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

impl Favourable<FavouriteTrack> for JTrack {
    #[inline]
    fn to_favourable(&self) -> FavouriteTrack {
        FavouriteTrack::new(
            self.title.clone(),
            self.artist.clone(),
            self.album.clone(),
            self.path.clone(),
            self.duration,
            self.add_date.clone(),
            self.number_in_album,
        )
    }

    #[inline]
    fn into_favourable(self) -> FavouriteTrack {
        FavouriteTrack::new(
            self.title,
            self.artist,
            self.album,
            self.path,
            self.duration,
            self.add_date,
            self.number_in_album,
        )
    }

    #[inline]
    fn into_self(favourable: FavouriteTrack) -> Self {
        favourable.into_jtrack()
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
        track.into_jtrack()
    }
}

#[inline]
fn get_byte_vec_field_of_jtrack<'b, 'a: 'b>(
    jni_env: Rc<RefCell<JNIEnv<'a>>>,
    jtrack: &'b JObject<'b>,
    field: &str,
) -> Option<Vec<jbyte>> {
    let jbytes = JNIEnvExt::get_field(jni_env.clone(), jtrack, field, "[B").l();

    let jbytes = match jbytes {
        Ok(x) => x,
        Err(_) => return None,
    };

    if jbytes.is_null() {
        return None;
    }

    let jbytes = JByteArray::from(jbytes);
    let len = jni_env.borrow().get_array_length(&jbytes).ok()? as usize;
    let mut buf: Vec<jbyte> = vec![0; len];

    jni_env
        .borrow()
        .get_byte_array_region(&jbytes, 0, buf.as_mut_slice())
        .ok()?;

    Some(buf)
}

#[inline]
fn get_string_field_of_jtrack<'b, 'a: 'b>(
    jni_env: Rc<RefCell<JNIEnv<'a>>>,
    jtrack: &'b JObject<'b>,
    field: &str,
) -> Option<String> {
    let jstring = JNIEnvExt::get_field(jni_env.clone(), jtrack, field, "Ljava/lang/String;").l();

    let jstring = match jstring {
        Ok(x) => x,
        Err(_) => return None,
    };

    if jstring.is_null() {
        return None;
    }

    Some(
        jni_env
            .borrow_mut()
            .get_string(&JString::from(jstring))
            .unwrap()
            .into(),
    )
}

#[inline]
fn get_duration_field_of_jtrack<'b, 'a: 'b>(
    jni_env: Rc<RefCell<JNIEnv<'a>>>,
    jtrack: &'b JObject<'b>,
) -> Duration {
    Duration::milliseconds(
        JNIEnvExt::get_field(jni_env, jtrack, "duration", "J")
            .j()
            .unwrap(),
    )
}

#[inline]
fn get_number_in_album_of_jtrack<'b, 'a: 'b>(
    jni_env: Rc<RefCell<JNIEnv<'a>>>,
    jtrack: &'b JObject<'b>,
) -> jshort {
    JNIEnvExt::get_field(jni_env, jtrack, "numberInAlbum", "S")
        .s()
        .unwrap()
}
