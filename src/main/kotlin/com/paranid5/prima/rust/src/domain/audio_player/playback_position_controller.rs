extern crate futures;
extern crate tokio;

use crate::StorageUtil;
use futures::future::AbortHandle;
use std::{sync::Arc, time::Duration};
use tokio::sync::RwLock;

#[derive(Debug)]
pub struct PlaybackPositionController {
    pub position: Arc<RwLock<Duration>>,
    pub task: Option<AbortHandle>,
}

impl PlaybackPositionController {
    #[inline]
    pub async fn default() -> Self {
        Self {
            position: Arc::new(RwLock::new(Duration::from_millis(
                StorageUtil::load_current_playback_position().await,
            ))),
            task: None,
        }
    }
}
