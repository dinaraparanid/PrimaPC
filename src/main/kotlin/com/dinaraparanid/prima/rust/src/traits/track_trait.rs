extern crate chrono;

use chrono::{DateTime, Duration, Local};
use std::path::PathBuf;

pub(crate) trait TrackTrait {
    fn get_title(&self) -> &str;
    fn get_artist(&self) -> &str;
    fn get_album(&self) -> &str;
    fn get_path(&self) -> &PathBuf;
    fn get_duration(&self) -> &Duration;
    fn get_add_date(&self) -> &DateTime<Local>;
    fn get_number_in_album(&self) -> isize;
}

impl PartialEq for TrackTrait {
    #[inline]
    fn eq(&self, other: &Self) -> bool {
        self.get_path() == other.get_path()
    }
}
