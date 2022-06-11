#[derive(Copy, Clone, Debug)]
pub enum PlaylistType {
    ALBUM,
    CUSTOM,
    GTM,
}

impl Default for PlaylistType {
    #[inline]
    fn default() -> Self {
        Self::ALBUM
    }
}
