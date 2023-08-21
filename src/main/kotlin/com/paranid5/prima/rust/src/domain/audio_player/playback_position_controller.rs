extern crate futures;
extern crate tokio;

use crate::ARWLStorage;
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
    pub async fn default(storage_util: ARWLStorage) -> Self {
        Self {
            position: Arc::new(RwLock::new(Duration::from_millis(
                storage_util.read().await.load_current_playback_position(),
            ))),
            task: None,
        }
    }
}
