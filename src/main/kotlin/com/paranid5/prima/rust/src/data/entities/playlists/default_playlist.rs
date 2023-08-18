use crate::{
    impl_playlist_methods, impl_playlist_traits, DefaultTrack, PlaylistType, TrackExt, AJVM,
};

use std::{cell::RefCell, rc::Rc};

pub struct DefaultPlaylist<T: TrackExt> {
    title: Option<String>,
    tp: PlaylistType,
    tracks: Vec<T>,
    cur_ind: usize,
}

impl_playlist_traits!(DefaultPlaylist);

impl DefaultPlaylist<DefaultTrack> {
    #[inline]
    pub async fn from_yaml(jvm: AJVM, playlist: &yaml_rust::yaml::Hash) -> Option<Self> {
        use yaml_rust::Yaml;

        let jni_env = Rc::new(RefCell::new(
            jvm.attach_current_thread_permanently().unwrap(),
        ));

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

        Some(Self::new(
            None,
            PlaylistType::ALBUM,
            tracks
                .iter()
                .filter_map(|y| DefaultTrack::from_path(jni_env.clone(), y.as_str()?.to_string())),
            index as usize,
        ))
    }
}

impl<T: TrackExt> DefaultPlaylist<T> {
    #[inline]
    pub fn new<I: IntoIterator<Item = T>>(
        title: Option<String>,
        tp: PlaylistType,
        tracks: I,
        cur_ind: usize,
    ) -> Self {
        Self {
            title,
            tp,
            tracks: Vec::from_iter(tracks),
            cur_ind,
        }
    }

    impl_playlist_methods!();
}
