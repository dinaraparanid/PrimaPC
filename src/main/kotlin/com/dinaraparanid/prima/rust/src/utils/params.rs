extern crate dirs2;
extern crate once_cell;

use dirs2::audio_dir;
use once_cell::sync::Lazy;

use crate::{
    entities::{playlists::default_playlist::DefaultPlaylist, tracks::default_track::DefaultTrack},
    utils::track_order::TrackOrder,
};

use std::{
    path::PathBuf,
    sync::{Arc, RwLock},
};

#[derive(Debug)]
pub struct Params {
    pub music_search_path: PathBuf,
    pub track_order: TrackOrder,
    pub cur_playlist: DefaultPlaylist<DefaultTrack>,
}

pub static mut PARAMS: Lazy<Arc<RwLock<Option<Params>>>> =
    Lazy::new(|| Arc::new(RwLock::new(Params::new())));

impl Params {
    #[inline]
    pub fn new() -> Option<Self> {
        Some(Self {
            music_search_path: audio_dir()?,          // TODO: Load search path
            track_order: TrackOrder::default(),       // TODO: Load track order
            cur_playlist: DefaultPlaylist::default(), // TODO: Load cur playlist
        })
    }
}
