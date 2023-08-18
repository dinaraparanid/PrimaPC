use jni::JavaVM;
use std::sync::Arc;

use tokio::{
    runtime::Runtime,
    sync::{Mutex, RwLock},
};

pub type AMutex<T> = Arc<Mutex<T>>;

pub type ARWLock<T> = Arc<RwLock<T>>;

pub type TokioRuntime = Arc<Runtime>;

pub type AJVM = Arc<JavaVM>;
