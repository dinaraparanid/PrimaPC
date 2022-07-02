extern crate yaml_rust;

use yaml_rust::{
    yaml::{Array, Hash},
    Yaml,
};

use crate::{
    entities::playlists::{playlist_trait::PlaylistTrait, playlist_type::PlaylistType},
    utils::extensions::track_ext::TrackExt,
    DefaultTrack, TrackTrait, JVM,
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

impl<T: TrackTrait> From<DefaultPlaylist<T>> for Yaml {
    #[inline]
    fn from(playlist: DefaultPlaylist<T>) -> Self {
        let mut hash = Hash::new();

        hash.insert(
            Yaml::String("current_index".to_string()),
            Yaml::Integer(playlist.cur_ind as i64),
        );

        hash.insert(
            Yaml::String("tracks".to_string()),
            Yaml::Array(Array::from_iter(
                playlist.into_iter().map(|t| TrackExt::into_yaml(t)),
            )),
        );

        Yaml::Hash(hash)
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
        cur_ind: usize,
    ) -> DefaultPlaylist<T> {
        Self {
            title,
            tp,
            tracks: Vec::from_iter(tracks),
            cur_ind,
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

impl DefaultPlaylist<DefaultTrack> {
    #[inline]
    pub fn from_yaml(playlist: &Hash) -> Option<Self> {
        let jni_env = unsafe { &JVM.read().unwrap().jni_env }.clone();
        let jvm = jni_env.unwrap().get_java_vm().unwrap();
        let jni_env = jvm.attach_current_thread_permanently().unwrap();

        let index = playlist
            .get(&Yaml::String("current_index".to_string()))
            .unwrap()
            .as_i64()
            .unwrap();

        let tracks = playlist
            .get(&Yaml::String("tracks".to_string()))
            .unwrap()
            .as_vec()
            .unwrap();

        Some(DefaultPlaylist::new(
            None,
            PlaylistType::ALBUM,
            tracks
                .iter()
                .filter_map(|y| DefaultTrack::from_path(&jni_env, y.as_str()?.to_string())),
            index as usize,
        ))
    }
}
