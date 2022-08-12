extern crate once_cell;
extern crate tokio;

use crate::{
    entities::{playlists::default_playlist::DefaultPlaylist, tracks::default_track::DefaultTrack},
    utils::{storage_util::StorageUtil, track_order::TrackOrder},
};

use once_cell::sync::Lazy;
use std::{path::PathBuf, sync::Arc};
use tokio::sync::{RwLockReadGuard, RwLockWriteGuard};
use tokio::{runtime::Runtime, sync::RwLock};

#[derive(Debug)]
pub struct Params {
    pub tokio_runtime: Arc<Runtime>,
    pub music_search_path: PathBuf,
    pub track_order: TrackOrder,
    cur_playlist: Arc<RwLock<Option<DefaultPlaylist<DefaultTrack>>>>,
}

pub static mut PARAMS: Lazy<Arc<RwLock<Option<Params>>>> =
    Lazy::new(|| Arc::new(RwLock::new(Params::new())));

impl Params {
    #[inline]
    pub fn new() -> Option<Self> {
        Some(Self {
            music_search_path: StorageUtil::load_music_search_path()?,
            track_order: StorageUtil::load_track_order(),
            cur_playlist: Arc::new(RwLock::new(None)),
            tokio_runtime: Arc::new(
                tokio::runtime::Builder::new_multi_thread()
                    .enable_all()
                    .build()
                    .unwrap(),
            ),
        })
    }

    #[inline]
    async fn init_cur_playlist_if_needed(&self) {
        if self.cur_playlist.read().await.is_none() {
            self.cur_playlist
                .write()
                .await
                .replace(StorageUtil::load_current_playlist().await);
        }
    }

    #[inline]
    pub async fn get_cur_playlist(
        &self,
    ) -> RwLockReadGuard<'_, Option<DefaultPlaylist<DefaultTrack>>> {
        self.init_cur_playlist_if_needed().await;
        self.cur_playlist.read().await
    }

    #[inline]
    pub async fn get_cur_playlist_mut(
        &self,
    ) -> RwLockWriteGuard<'_, Option<DefaultPlaylist<DefaultTrack>>> {
        self.init_cur_playlist_if_needed().await;
        self.cur_playlist.write().await
    }
}
