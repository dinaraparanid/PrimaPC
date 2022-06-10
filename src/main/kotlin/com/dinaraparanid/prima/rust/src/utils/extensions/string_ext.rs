extern crate jni;

use jni::sys::jbyte;

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
}

impl StringExt for String {}
