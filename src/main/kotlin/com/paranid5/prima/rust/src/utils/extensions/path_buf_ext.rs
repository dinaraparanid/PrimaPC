use std::path::PathBuf;

pub(crate) trait PathBufExt {
    fn to_string(&self) -> String;
}

impl PathBufExt for PathBuf {
    #[inline]
    fn to_string(&self) -> String {
        self.to_string_lossy().to_string()
    }
}
