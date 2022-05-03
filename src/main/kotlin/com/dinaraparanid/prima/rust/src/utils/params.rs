extern crate dirs2;

use dirs2::audio_dir;

use crate::program::ProgramInstance;

use std::{
    path::PathBuf,
    sync::{Arc, RwLock, Weak},
};

#[derive(Debug)]
pub struct Params {
    pub music_search_path: PathBuf,
    pub program: Weak<RwLock<Option<ProgramInstance>>>,
}

impl Params {
    #[inline]
    pub fn new(program: Arc<RwLock<Option<ProgramInstance>>>) -> Option<Self> {
        Some(Params {
            music_search_path: audio_dir()?,
            program: Arc::downgrade(&program),
        })
    }
}
