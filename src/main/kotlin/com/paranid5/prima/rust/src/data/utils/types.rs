use jni::JavaVM;
use std::sync::Arc;

use crate::{AudioPlayer, StorageUtil};

use tokio::{
    runtime::Runtime,
    sync::{Mutex, RwLock},
};

pub type AMutex<T> = Arc<Mutex<T>>;

pub type ARWLock<T> = Arc<RwLock<T>>;

pub type TokioRuntime = Arc<Runtime>;

pub type AJVM = Arc<JavaVM>;

pub type ARWLPlayer = ARWLock<AudioPlayer>;

pub type ARWLStorage = ARWLock<StorageUtil>;
