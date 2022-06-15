extern crate chrono;
extern crate jni;

use crate::{entities::tracks::track_trait::TrackTrait, utils::wrappers::jtrack::JTrack};
use chrono::{DateTime, Duration, Local};
use jni::sys::{jbyte, jshort};
use std::path::PathBuf;

#[derive(Clone, Debug)]
pub struct DefaultTrack {
    title: Option<Vec<jbyte>>,
    artist: Option<Vec<jbyte>>,
    album: Option<Vec<jbyte>>,
    path: PathBuf,
    duration: Duration,
    add_date: DateTime<Local>,
    number_in_album: jshort,
}

impl TrackTrait for DefaultTrack {
    #[inline]
    fn get_title(&self) -> Option<&Vec<jbyte>> {
        match &self.title {
            None => None,
            Some(title) => Some(title),
        }
    }

    #[inline]
    fn get_artist(&self) -> Option<&Vec<jbyte>> {
        match &self.artist {
            None => None,
            Some(artist) => Some(artist),
        }
    }

    #[inline]
    fn get_album(&self) -> Option<&Vec<jbyte>> {
        match &self.album {
            None => None,
            Some(album) => Some(album),
        }
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
    pub fn to_jtrack(self) -> JTrack {
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
}
