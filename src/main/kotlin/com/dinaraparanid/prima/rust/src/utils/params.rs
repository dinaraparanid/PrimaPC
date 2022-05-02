extern crate dirs2;

use dirs2::audio_dir;

use std::{
    path::PathBuf,
    sync::{Arc, Mutex},
};

#[derive(Debug)]
pub(crate) struct ParamsInstance {
    pub(crate) music_search_path: PathBuf,
}

impl ParamsInstance {
    #[inline]
    fn new() -> Option<Self> {
        Some(ParamsInstance {
            music_search_path: audio_dir()?,
        })
    }
}

#[derive(Debug)]
pub(crate) struct Params {
    instance: Arc<Mutex<Option<ParamsInstance>>>,
}

impl Params {
    #[inline]
    fn new() -> Self {
        Params {
            instance: Arc::new(Mutex::new(ParamsInstance::new())),
        }
    }

    #[inline]
    pub(crate) async fn get_instance(&self) -> Option<ParamsInstance> {
        self.instance.lock().unwrap()?
    }
}

pub(crate) static mut PARAMS: Params = Params::new();
