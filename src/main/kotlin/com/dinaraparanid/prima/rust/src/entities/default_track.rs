extern crate chrono;

use crate::traits::track_trait::TrackTrait;
use chrono::{DateTime, Duration, Local};
use std::path::PathBuf;

#[derive(Clone, Debug, PartialEq)]
pub struct DefaultTrack {
    title: Option<String>,
    artist: Option<String>,
    album: Option<String>,
    path: PathBuf,
    duration: Duration,
    add_date: DateTime<Local>,
    number_in_album: i16,
}

impl TrackTrait for DefaultTrack {
    #[inline]
    fn get_title(&self) -> Option<&str> {
        match &self.title {
            None => None,
            Some(title) => Some(title.as_str()),
        }
    }

    #[inline]
    fn get_artist(&self) -> Option<&str> {
        match &self.artist {
            None => None,
            Some(artist) => Some(artist.as_str()),
        }
    }

    #[inline]
    fn get_album(&self) -> Option<&str> {
        match &self.album {
            None => None,
            Some(album) => Some(album.as_str()),
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

impl DefaultTrack {
    #[inline]
    pub fn new(
        title: Option<String>,
        artist: Option<String>,
        album: Option<String>,
        path: PathBuf,
        duration: Duration,
        add_date: DateTime<Local>,
        number_in_album: i16,
    ) -> Self {
        DefaultTrack {
            title,
            artist,
            album,
            path,
            duration,
            add_date,
            number_in_album,
        }
    }
}
