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

    /// Creates string from jstring without any null checks
    ///
    /// # Arguments
    /// jstring - String from java
    ///
    /// # Safety
    /// jstring should not be null
    ///
    /// # Return
    /// Rust's string from given Java's string

    #[inline]
    unsafe fn from_jstring_unchecked(env: &JNIEnv, jstring: JString) -> String {
        env.get_string(jstring).unwrap().into()
    }

    /// Creates string from jstring
    ///
    /// # Arguments
    /// jstring - String from java
    ///
    /// # Return
    /// Rust's string from given Java's string or None if jstring was null

    #[inline]
    fn from_jstring(env: &JNIEnv, jstring: JString) -> Option<String> {
        if jstring.is_null() {
            None
        } else {
            unsafe { Some(Self::from_jstring_unchecked(env, jstring)) }
        }
    }
}

impl StringExt for String {}
