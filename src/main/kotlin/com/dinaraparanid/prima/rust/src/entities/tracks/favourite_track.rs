extern crate chrono;
extern crate jni;

use crate::{
    databases::favourites::daos::favourite_track_dao::FavouriteTrackDBEntity,
    entities::{favourable::Favourable, tracks::track_trait::TrackTrait},
    utils::{extensions::path_buf_ext::PathBufExt, wrappers::jtrack::JTrack},
    DefaultTrack,
};

use crate::databases::db_entity::DBEntity;
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

impl TrackTrait for FavouriteTrack {
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

impl TrackTrait for &FavouriteTrack {
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

impl DBEntity for FavouriteTrack {
    type PrimaryKey = PathBuf;

    #[inline]
    fn get_key(&self) -> &PathBuf {
        &self.path
    }
}

impl PartialEq for FavouriteTrack {
    #[inline]
    fn eq(&self, other: &Self) -> bool {
        self.path.eq(other.get_path())
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
