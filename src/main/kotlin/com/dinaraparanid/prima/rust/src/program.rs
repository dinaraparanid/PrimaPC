use crate::utils::{audio_scanner::AudioScanner, params::Params};
use std::sync::{Arc, RwLock};

#[derive(Debug)]
pub struct Program {
    pub instance: Arc<RwLock<Option<ProgramInstance>>>,
}

#[derive(Debug)]
pub struct ProgramInstance {
    pub audio_scanner: Arc<RwLock<Option<AudioScanner>>>,
    pub(crate) params: Arc<RwLock<Option<Params>>>,
}

impl Program {
    pub fn new() -> Self {
        let instance = Arc::new(RwLock::new(Some(ProgramInstance {
            audio_scanner: Arc::new(RwLock::new(None)),
            params: Arc::new(RwLock::new(None)),
        })));

        *instance
            .write()
            .unwrap()
            .as_mut()
            .unwrap()
            .params
            .write()
            .unwrap() = Params::new(instance.clone());

        *instance
            .write()
            .unwrap()
            .as_mut()
            .unwrap()
            .audio_scanner
            .write()
            .unwrap() = Some(AudioScanner::new(instance.clone()));

        Program { instance }
    }
}
