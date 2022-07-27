extern crate chrono;
extern crate jni;

use jni::{
    objects::{JObject, JValue},
    signature::JavaType,
    sys::jshort,
    JNIEnv,
};

use crate::{
    entities::{
        favourable::Favourable,
        tracks::{favourite_track::FavouriteTrack, track_trait::TrackTrait},
    },
    utils::{
        extensions::{jni_env_ext::JNIEnvExt, jobject_ext::JObjectExt},
        wrappers::jtrack::JTrack,
    },
};

use chrono::{DateTime, Duration, Local};
use std::path::PathBuf;

#[derive(Clone, Debug)]
pub struct DefaultTrack {
    title: Option<String>,
    artist: Option<String>,
    album: Option<String>,
    path: PathBuf,
    duration: Duration,
    add_date: DateTime<Local>,
    number_in_album: jshort,
}

impl TrackTrait for DefaultTrack {
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

impl TrackTrait for &DefaultTrack {
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

impl Favourable<FavouriteTrack> for DefaultTrack {
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
        favourable.into_default()
    }
}

impl PartialEq for DefaultTrack {
    #[inline]
    fn eq(&self, other: &Self) -> bool {
        self.path.eq(other.get_path())
    }
}

impl From<JTrack> for DefaultTrack {
    #[inline]
    fn from(jtrack: JTrack) -> Self {
        jtrack.to_default_track()
    }
}

impl DefaultTrack {
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
    pub fn into_jtrack(self) -> JTrack {
        JTrack::new(
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
    pub fn from_path(jni_env: &JNIEnv, path: String) -> Option<Self> {
        JObjectExt::array_to_track(
            &unsafe {
                JNIEnvExt::call_static_method(
                    jni_env,
                    "com/dinaraparanid/prima/rust/RustLibs",
                    "getDataByPath",
                    "(Ljava/lang/String;)[Ljava/lang/Object;",
                    JavaType::Array(Box::new(JavaType::Object(String::from("java/lang/Object")))),
                    &[JValue::Object(JObject::from(
                        jni_env.new_string(path.clone()).unwrap(),
                    ))],
                )
            }
            .l()
            .unwrap(),
            jni_env,
            PathBuf::from(path),
        )
    }

    #[inline]
    pub fn from_env(jni_env: &JNIEnv, jtrack: JObject) -> Self {
        Self::from(JTrack::from_env(jni_env, jtrack))
    }
}
