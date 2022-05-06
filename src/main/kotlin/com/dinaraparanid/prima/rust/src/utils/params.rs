extern crate dirs2;
extern crate once_cell;

use dirs2::audio_dir;
use once_cell::sync::Lazy;

use std::{
    path::PathBuf,
    sync::{Arc, RwLock},
};

#[derive(Debug)]
pub struct Params {
    pub music_search_path: PathBuf,
}

pub static mut PARAMS: Lazy<Arc<RwLock<Option<Params>>>> =
    Lazy::new(|| Arc::new(RwLock::new(Params::new())));

impl Params {
    #[inline]
    pub fn new() -> Option<Self> {
        Some(Self {
            music_search_path: audio_dir()?,
        })
    }
}
