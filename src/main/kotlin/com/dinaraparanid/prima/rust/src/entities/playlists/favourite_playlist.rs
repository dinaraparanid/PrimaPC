use crate::{impl_playlist_methods, impl_playlist_traits, PlaylistType, TrackExt};

pub struct FavouritePlaylist<T: TrackExt> {
    id: i32,
    title: Option<String>,
    tp: PlaylistType,
    tracks: Vec<T>,
    cur_ind: usize,
}

impl_playlist_traits!(FavouritePlaylist);

impl<T: TrackExt> FavouritePlaylist<T> {
    #[inline]
    pub fn new<I: IntoIterator<Item = T>>(
        id: i32,
        title: Option<String>,
        tp: PlaylistType,
        tracks: I,
        cur_ind: usize,
    ) -> Self {
        Self {
            id,
            title,
            tp,
            tracks: Vec::from_iter(tracks),
            cur_ind,
        }
    }

    #[inline]
    pub fn get_id(&self) -> i32 {
        self.id
    }

    impl_playlist_methods!();
}
