extern crate jni;

use jni::{objects::JString, sys::jbyte, JNIEnv};

pub(crate) trait StringExt {
    #[inline]
    fn from_jbyte_vec(vec: Vec<jbyte>) -> String {
        String::from_utf8_lossy(
            vec.into_iter()
                .map(|jb| jb as u8)
                .collect::<Vec<_>>()
                .as_slice(),
        )
        .to_string()
    }

    /// Creates string from jstring
    ///
    /// # Arguments
    /// jstring - String from java
    ///
    /// # Return
    /// Rust's string from give Java's string

    #[inline]
    fn from_jstring(env: &JNIEnv, jstring: JString) -> String {
        env.get_string(jstring).unwrap().into()
    }
}

impl StringExt for String {}
