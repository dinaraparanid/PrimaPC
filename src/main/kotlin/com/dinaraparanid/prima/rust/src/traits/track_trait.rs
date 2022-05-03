extern crate chrono;

use chrono::{DateTime, Duration, Local};
use std::path::PathBuf;

pub trait TrackTrait {
    fn get_title(&self) -> Option<&str>;
    fn get_artist(&self) -> Option<&str>;
    fn get_album(&self) -> Option<&str>;
    fn get_path(&self) -> &PathBuf;
    fn get_duration(&self) -> &Duration;
    fn get_add_date(&self) -> &DateTime<Local>;
    fn get_number_in_album(&self) -> isize;
}

impl PartialEq for dyn TrackTrait {
    #[inline]
    fn eq(&self, other: &Self) -> bool {
        self.get_path() == other.get_path()
    }
}
