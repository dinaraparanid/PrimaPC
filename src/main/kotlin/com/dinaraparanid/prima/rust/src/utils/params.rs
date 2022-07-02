extern crate once_cell;

use once_cell::sync::Lazy;

use crate::{
    entities::{playlists::default_playlist::DefaultPlaylist, tracks::default_track::DefaultTrack},
    utils::{storage_util::StorageUtil, track_order::TrackOrder},
};

use std::{
    cell::RefCell,
    path::PathBuf,
    sync::{Arc, RwLock},
};

#[derive(Debug)]
pub struct Params {
    pub music_search_path: PathBuf,
    pub track_order: TrackOrder,
    cur_playlist: RefCell<*mut DefaultPlaylist<DefaultTrack>>,
}

pub static mut PARAMS: Lazy<Arc<RwLock<Option<Params>>>> =
    Lazy::new(|| Arc::new(RwLock::new(Params::new())));

impl Drop for Params {
    #[inline]
    fn drop(&mut self) {
        unsafe { std::ptr::drop_in_place(*self.cur_playlist.borrow_mut()) }
    }
}

impl Params {
    #[inline]
    pub fn new() -> Option<Self> {
        Some(Self {
            music_search_path: StorageUtil::load_music_search_path()?,
            track_order: StorageUtil::load_track_order(),
            cur_playlist: RefCell::new(std::ptr::null_mut()),
        })
    }

    #[inline]
    fn init_cur_playlist(&self) {
        if self.cur_playlist.borrow().is_null() {
            let playlist = Box::new(StorageUtil::load_current_playlist());
            let playlist = Box::leak(playlist);
            self.cur_playlist.replace(playlist);
        }
    }

    #[inline]
    pub fn get_cur_playlist(&self) -> &DefaultPlaylist<DefaultTrack> {
        self.init_cur_playlist();

        unsafe {
            (*self.cur_playlist.borrow() as *const DefaultPlaylist<DefaultTrack>)
                .as_ref()
                .unwrap_unchecked()
        }
    }

    #[inline]
    pub fn get_cur_playlist_mut(&self) -> &mut DefaultPlaylist<DefaultTrack> {
        self.init_cur_playlist();
        unsafe { self.cur_playlist.borrow_mut().as_mut().unwrap_unchecked() }
    }
}
