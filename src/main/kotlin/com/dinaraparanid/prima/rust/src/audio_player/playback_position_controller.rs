extern crate futures;

use crate::StorageUtil;
use futures::future::AbortHandle;

use std::{
    sync::{Arc, RwLock},
    time::Duration,
};

#[derive(Debug)]
pub struct PlaybackPositionController {
    pub position: Arc<RwLock<Duration>>,
    pub task: Option<AbortHandle>,
}

impl Default for PlaybackPositionController {
    #[inline]
    fn default() -> Self {
        Self {
            position: Arc::new(RwLock::new(Duration::from_millis(
                StorageUtil::load_current_playback_position(),
            ))),
            task: None,
        }
    }
}
