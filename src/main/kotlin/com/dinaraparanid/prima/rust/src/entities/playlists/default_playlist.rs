use crate::{
    entities::playlists::{playlist_trait::PlaylistTrait, playlist_type::PlaylistType},
    TrackTrait,
};

pub struct DefaultPlaylist<T: TrackTrait> {
    title: Option<String>,
    tp: PlaylistType,
    tracks: Vec<T>,
    cur_ind: usize,
}

impl<T: TrackTrait> IntoIterator for DefaultPlaylist<T> {
    type Item = T;
    type IntoIter = std::vec::IntoIter<Self::Item>;

    #[inline]
    fn into_iter(self) -> Self::IntoIter {
        self.tracks.into_iter()
    }
}

impl<Tr: TrackTrait> Extend<Tr> for DefaultPlaylist<Tr> {
    #[inline]
    fn extend<T: IntoIterator<Item = Tr>>(&mut self, iter: T) {
        self.push_all(iter)
    }
}

impl<T: TrackTrait> PlaylistTrait<T> for DefaultPlaylist<T> {
    #[inline]
    fn get_title(&self) -> Option<&String> {
        self.title.as_ref()
    }

    #[inline]
    fn get_type(&self) -> PlaylistType {
        self.tp
    }

    #[inline]
    fn get_cur_ind(&self) -> usize {
        self.cur_ind
    }

    #[inline]
    fn get_tracks(&self) -> &Vec<T> {
        &self.tracks
    }
}

impl<T: TrackTrait> DefaultPlaylist<T> {
    #[inline]
    pub fn new<I: IntoIterator<Item = T>>(
        title: Option<String>,
        tp: PlaylistType,
        tracks: I,
    ) -> DefaultPlaylist<T> {
        Self {
            title,
            tp,
            tracks: Vec::from_iter(tracks),
            cur_ind: 0,
        }
    }

    #[inline]
    pub(in crate::entities::playlists) fn set_cur_ind(&mut self, new_ind: usize) {
        self.cur_ind = new_ind
    }

    #[inline]
    pub(in crate::entities::playlists) fn get_tracks_mut(&mut self) -> &mut Vec<T> {
        &mut self.tracks
    }
}
