extern crate chrono;
extern crate jni;

use chrono::{DateTime, Duration, Local};
use jni::sys::jshort;
use std::{fmt::Debug, path::PathBuf};

pub trait TrackTrait: PartialEq + Debug + Clone {
    fn get_title(&self) -> Option<&String>;
    fn get_artist(&self) -> Option<&String>;
    fn get_album(&self) -> Option<&String>;
    fn get_path(&self) -> &PathBuf;
    fn get_duration(&self) -> &Duration;
    fn get_add_date(&self) -> &DateTime<Local>;
    fn get_number_in_album(&self) -> jshort;
}
