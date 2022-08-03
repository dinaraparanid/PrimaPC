use crate::{
    databases::favourites::daos::favourite_playlist_dao::FavouritePlaylistDBEntity,
    impl_playlist_methods, impl_playlist_traits, PlaylistType, TrackExt,
};

pub struct FavouritePlaylist<T: TrackExt> {
    id: Option<i32>,
    title: Option<String>,
    tp: PlaylistType,
    tracks: Vec<T>,
    cur_ind: usize,
}

impl_playlist_traits!(FavouritePlaylist);

impl<T: TrackExt> FavouritePlaylist<T> {
    #[inline]
    pub fn new<I: IntoIterator<Item = T>>(
        id: Option<i32>,
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
    pub fn get_id(&self) -> Option<i32> {
        self.id
    }

    #[inline]
    pub(crate) fn into_db_entity(self) -> FavouritePlaylistDBEntity {
        FavouritePlaylistDBEntity::new(self.id, self.title, i32::from(self.tp))
    }

    impl_playlist_methods!();
}
