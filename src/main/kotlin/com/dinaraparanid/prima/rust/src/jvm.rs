extern crate jni;
extern crate once_cell;

use jni::{sys::jclass, JNIEnv};
use once_cell::sync::Lazy;
use std::sync::{Arc, RwLock};

pub struct JVM {
    pub jni_env: Arc<Option<JNIEnv<'static>>>,
    pub rust_libs_class: Arc<Option<jclass>>,
}

pub static mut JVM: Lazy<Arc<RwLock<JVM>>> =
    Lazy::new(|| Arc::new(RwLock::new(JVM::new(None, None))));

impl JVM {
    #[inline]
    fn new(jni_env: Option<JNIEnv<'static>>, rust_libs_class: Option<jclass>) -> Self {
        Self {
            jni_env: Arc::new(jni_env),
            rust_libs_class: Arc::new(rust_libs_class),
        }
    }
}
