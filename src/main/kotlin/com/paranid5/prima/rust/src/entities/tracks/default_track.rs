extern crate chrono;
extern crate jni;

use jni::{
    objects::{JObject, JObjectArray, JValue},
    sys::jshort,
    JNIEnv,
};

use std::{cell::RefCell, path::PathBuf, rc::Rc};

use crate::{
    entities::{favourable::Favourable, tracks::favourite_track::FavouriteTrack},
    impl_track_traits,
    utils::{
        extensions::{jni_env_ext::JNIEnvExt, jobject_array_ext::JObjectArrayExt},
        wrappers::jtrack::JTrack,
    },
};

use chrono::{DateTime, Duration, Local};

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

impl_track_traits!(DefaultTrack);

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
    pub fn from_path(jni_env: Rc<RefCell<JNIEnv>>, path: String) -> Option<Self> {
        let path_arg = jni_env.borrow_mut().new_string(path.clone()).unwrap();

        let data = JObjectArray::from(
            JNIEnvExt::call_static_method(
                jni_env.clone(),
                "com/paranid5/prima/rust/RustLibs",
                "getDataByPath",
                "(Ljava/lang/String;)[Ljava/lang/Object;",
                &[JValue::Object(&JObject::from(path_arg))],
            )
            .l()
            .unwrap(),
        );

        data.array_to_track(jni_env, PathBuf::from(path))
    }

    #[inline]
    pub fn from_env<'a, 'b: 'a>(jni_env: Rc<RefCell<JNIEnv<'a>>>, jtrack: JObject<'b>) -> Self {
        Self::from(JTrack::from_env(jni_env, jtrack))
    }
}
