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

impl From<i32> for PlaylistType {
    #[inline]
    fn from(ind: i32) -> Self {
        match ind {
            0 => PlaylistType::ALBUM,
            1 => PlaylistType::CUSTOM,
            2 => PlaylistType::GTM,
            _ => panic!("Unknown index of PlaylistType"),
        }
    }
}

impl From<PlaylistType> for i32 {
    #[inline]
    fn from(tp: PlaylistType) -> Self {
        match tp {
            PlaylistType::ALBUM => 0,
            PlaylistType::CUSTOM => 1,
            PlaylistType::GTM => 2,
        }
    }
}
