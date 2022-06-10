extern crate jni;
extern crate once_cell;

use jni::JNIEnv;
use once_cell::sync::Lazy;
use std::sync::{Arc, RwLock};

pub struct JVM {
    pub jni_env: Arc<Option<JNIEnv<'static>>>,
}

pub static mut JVM: Lazy<Arc<RwLock<JVM>>> = Lazy::new(|| Arc::new(RwLock::new(JVM::new(None))));

impl JVM {
    #[inline]
    fn new(jni_env: Option<JNIEnv<'static>>) -> Self {
        Self {
            jni_env: Arc::new(jni_env),
        }
    }
}
