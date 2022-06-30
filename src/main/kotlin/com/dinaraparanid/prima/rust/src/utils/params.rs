extern crate dirs2;
extern crate once_cell;

use once_cell::sync::Lazy;

use crate::{
    entities::{playlists::default_playlist::DefaultPlaylist, tracks::default_track::DefaultTrack},
    utils::{storage_util::StorageUtil, track_order::TrackOrder},
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
            music_search_path: StorageUtil::load_music_search_path()?,
            track_order: StorageUtil::load_track_order(),
            cur_playlist: StorageUtil::load_current_playlist(),
        })
    }
}
