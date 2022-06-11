use crate::{
    entities::playlists::{default_playlist::DefaultPlaylist, playlist_type::PlaylistType},
    utils::extensions::path_buf_ext::PathBufExt,
    TrackTrait,
};

use std::fmt::{Debug, Formatter};

mod private {
    use crate::TrackTrait;
    use std::fmt::Debug;

    pub trait PrivatePlaylistTrait<T: TrackTrait>:
        FromIterator<T> + IntoIterator + Extend<T> + Clone + Debug + Default
    {
        fn set_cur_index(&mut self, new_index: usize);
        fn get_tracks_mut(&mut self) -> &mut Vec<T>;
    }
}

pub trait PlaylistTrait<T: TrackTrait>: private::PrivatePlaylistTrait<T> {
    fn get_title(&self) -> Option<&String>;
    fn get_type(&self) -> PlaylistType;
    fn get_cur_ind(&self) -> usize;
    fn get_tracks(&self) -> &Vec<T>;

    #[inline]
    fn len(&self) -> usize {
        self.get_tracks().len()
    }

    #[inline]
    fn get_cur_track(&self) -> Option<&T> {
        self.get_tracks().get(self.get_cur_ind())
    }

    #[inline]
    fn skip_to_next(&mut self) {
        let new_index = self.get_cur_ind() + 1;

        let new_index = if new_index == self.len() {
            0
        } else {
            new_index
        };

        self.set_cur_index(new_index)
    }

    #[inline]
    fn skip_to_prev(&mut self) {
        let cur_ind = self.get_cur_ind();
        let new_index = match cur_ind {
            0 => self.len(),
            _ => cur_ind,
        } - 1;

        self.set_cur_index(new_index)
    }

    /// Adds track if it's not in the playlist
    /// or changes it's position

    #[inline]
    fn push(&mut self, track: T) {
        if let Some(index) = { self.get_tracks().iter().position(|t| track.eq(t)) } {
            self.get_tracks_mut().remove(index);
        }

        self.get_tracks_mut().push(track)
    }

    /// Adds track from given collection
    /// if it's not in the playlist
    /// or changes it's position

    #[inline]
    fn push_all<I: IntoIterator<Item = T>>(&mut self, tracks: I) {
        tracks.into_iter().for_each(|t| self.push(t))
    }

    /// Removes last track
    /// which is matching pattern.
    /// Also changes current index.
    ///
    /// # Returns
    /// true if the element has been successfully removed;
    /// false if it was not presented in the collection.

    #[inline]
    fn remove(&mut self, track: &T) -> bool {
        match { self.get_tracks().iter().position(|t| t == track) } {
            None => false,
            Some(ind) => {
                let cur_ind = { self.get_cur_ind() };

                let new_ind = if {
                    self.get_cur_track()
                        .map(|t| t.get_path().to_string())
                        .unwrap_or(String::new())
                } == track.get_path().to_string()
                {
                    if cur_ind == self.len() {
                        0
                    } else {
                        cur_ind
                    }
                } else {
                    if ind < cur_ind {
                        cur_ind - 1
                    } else {
                        cur_ind
                    }
                };

                self.set_cur_index(new_ind);
                self.get_tracks_mut().retain(|t| t == track);
                true
            }
        }
    }

    /// Removes last track
    /// which is matching patterns from given collection.
    /// Also changes current index.
    ///
    /// # Returns
    /// true if any of elements have been successfully removed;
    /// false if all of tracks were not presented in the collection.

    #[inline]
    fn remove_all<I: IntoIterator<Item = T>>(&mut self, tracks: I) -> bool {
        tracks.into_iter().fold(false, |is_changed, t| {
            let is_removed = self.remove(&t);
            match is_changed {
                true => true,
                false => is_removed,
            }
        })
    }

    /// Replaces old track in a playlist with new one
    ///
    /// # Parameters
    /// **oldTrack** track which will be replaced
    ///
    /// **newTrack** track to override old one
    ///
    /// # Returns
    /// true if track's changed
    /// false if it isn't founded

    #[inline]
    fn replace(&mut self, old_track: &T, new_track: T) -> bool {
        match { self.get_tracks().iter().position(|t| t == old_track) } {
            None => false,
            Some(ind) => unsafe {
                *self.get_tracks_mut().get_unchecked_mut(ind) = new_track;
                true
            },
        }
    }
}

impl<T: TrackTrait> Clone for DefaultPlaylist<T> {
    #[inline]
    fn clone(&self) -> Self {
        DefaultPlaylist::new(
            self.get_title().map(|title| title.clone()),
            self.get_type(),
            self.get_tracks().clone(),
        )
    }
}

impl<T: TrackTrait> Debug for DefaultPlaylist<T> {
    #[inline]
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        f.debug_struct("DefaultPlaylist")
            .field("title", &self.get_title())
            .field("type", &self.get_type())
            .field("tracks", &self.get_tracks())
            .field("current index", &self.get_cur_ind())
            .finish()
    }
}

impl<Tr: TrackTrait> FromIterator<Tr> for DefaultPlaylist<Tr> {
    #[inline]
    fn from_iter<T: IntoIterator<Item = Tr>>(iter: T) -> Self {
        DefaultPlaylist::new(None, PlaylistType::ALBUM, Vec::from_iter(iter))
    }
}

impl<T: TrackTrait> Default for DefaultPlaylist<T> {
    #[inline]
    fn default() -> Self {
        DefaultPlaylist::new(None, PlaylistType::ALBUM, vec![])
    }
}

impl<T: TrackTrait> private::PrivatePlaylistTrait<T> for DefaultPlaylist<T> {
    #[inline]
    fn set_cur_index(&mut self, new_index: usize) {
        self.set_cur_ind(new_index)
    }

    #[inline]
    fn get_tracks_mut(&mut self) -> &mut Vec<T> {
        self.get_tracks_mut()
    }
}
