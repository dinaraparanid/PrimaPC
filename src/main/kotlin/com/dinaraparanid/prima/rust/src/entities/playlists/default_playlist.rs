use crate::{
    entities::playlists::playlist_type::PlaylistType, impl_playlist_methods, impl_playlist_traits,
    utils::extensions::track_ext::TrackExt,
};

pub struct DefaultPlaylist<T: TrackExt> {
    title: Option<String>,
    tp: PlaylistType,
    tracks: Vec<T>,
    cur_ind: usize,
}

impl_playlist_traits!(DefaultPlaylist);

impl DefaultPlaylist<crate::entities::tracks::default_track::DefaultTrack> {
    #[inline]
    pub fn from_yaml(playlist: &yaml_rust::yaml::Hash) -> Option<Self> {
        use yaml_rust::Yaml;

        let jni_env = unsafe { &crate::jvm::JVM.read().unwrap().jni_env }.clone();
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

        Some(Self::new(
            None,
            PlaylistType::ALBUM,
            tracks.iter().filter_map(|y| {
                crate::entities::tracks::default_track::DefaultTrack::from_path(
                    &jni_env,
                    y.as_str()?.to_string(),
                )
            }),
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
