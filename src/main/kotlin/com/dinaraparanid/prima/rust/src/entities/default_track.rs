extern crate chrono;

use crate::traits::track_trait::TrackTrait;
use chrono::{DateTime, Duration, Local};
use std::path::PathBuf;

#[derive(Clone, Debug)]
pub(crate) struct DefaultTrack {
    title: String,
    artist: String,
    album: String,
    path: PathBuf,
    duration: Duration,
    add_date: DateTime<Local>,
    number_in_album: isize,
}

impl TrackTrait for DefaultTrack {
    #[inline]
    fn get_title(&self) -> &str {
        self.title.as_str()
    }

    #[inline]
    fn get_artist(&self) -> &str {
        self.artist.as_str()
    }

    #[inline]
    fn get_album(&self) -> &str {
        self.album.as_str()
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
    fn get_number_in_album(&self) -> isize {
        self.number_in_album
    }
}

impl DefaultTrack {
    #[inline]
    pub(crate) fn new(
        title: String,
        artist: String,
        album: String,
        path: PathBuf,
        duration: Duration,
        add_date: DateTime<Local>,
        number_in_album: isize,
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
