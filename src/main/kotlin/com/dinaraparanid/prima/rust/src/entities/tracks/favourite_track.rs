extern crate chrono;
extern crate jni;

use crate::{
    databases::{
        db_entity::DBEntity, favourites::daos::favourite_track_dao::FavouriteTrackDBEntity,
    },
    entities::favourable::Favourable,
    impl_track_traits,
    utils::{extensions::path_buf_ext::PathBufExt, wrappers::jtrack::JTrack},
    DefaultTrack,
};

use chrono::{DateTime, Duration, Local};
use jni::sys::jshort;
use std::path::PathBuf;

#[derive(Clone, Debug)]
pub struct FavouriteTrack {
    title: Option<String>,
    artist: Option<String>,
    album: Option<String>,
    path: PathBuf,
    duration: Duration,
    add_date: DateTime<Local>,
    number_in_album: jshort,
}

impl_track_traits!(FavouriteTrack);

impl DBEntity for FavouriteTrack {
    type PrimaryKey = PathBuf;

    #[inline]
    fn get_key(&self) -> &PathBuf {
        &self.path
    }
}

impl DBEntity for &FavouriteTrack {
    type PrimaryKey = PathBuf;

    #[inline]
    fn get_key(&self) -> &PathBuf {
        &self.path
    }
}

impl From<DefaultTrack> for FavouriteTrack {
    #[inline]
    fn from(default_track: DefaultTrack) -> Self {
        default_track.into_favourable()
    }
}

impl From<JTrack> for FavouriteTrack {
    #[inline]
    fn from(jtrack: JTrack) -> Self {
        jtrack.into_favourable()
    }
}

impl FavouriteTrack {
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
    pub fn into_default(self) -> DefaultTrack {
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

    #[inline]
    pub(crate) fn into_db_entity(self) -> FavouriteTrackDBEntity {
        FavouriteTrackDBEntity {
            title: self.title,
            artist: self.artist,
            album: self.album,
            path: self.path.to_string(),
            duration: self.duration.num_milliseconds(),
            add_date: self.add_date.timestamp_millis(),
            number_in_album: self.number_in_album as i32,
        }
    }
}
