extern crate dirs2;
extern crate yaml_rust;

use std::{
    io::{Error, ErrorKind, Result},
    path::PathBuf,
};

use crate::{
    data::utils::extensions::path_buf_ext::PathBufExt,
    domain::audio_player::playback_params::LoopingState, DefaultPlaylist, DefaultTrack, TrackOrder,
    AJVM,
};

use dirs2::audio_dir;

use tokio::{
    fs::File,
    io::{AsyncReadExt, AsyncWriteExt},
};

use yaml_rust::{yaml::Hash, Yaml, YamlEmitter, YamlLoader};

const DEFAULT_VOLUME: f32 = 1_f32;
const DEFAULT_SPEED: f32 = 1_f32;

pub struct StorageUtil {
    music_search_path: Option<PathBuf>,
    track_order: TrackOrder,
    current_playlist: DefaultPlaylist<DefaultTrack>,
    current_playback_pos: u64,
    looping_state: LoopingState,
    volume: f32,
    speed: f32,
}

impl StorageUtil {
    #[inline]
    pub async fn new() -> Self {
        Self {
            music_search_path: Self::init_music_search_path().await,
            track_order: Self::init_track_order().await,
            current_playlist: DefaultPlaylist::default(),
            current_playback_pos: Self::init_current_playback_position().await,
            looping_state: Self::init_looping_state().await,
            volume: Self::init_volume().await,
            speed: Self::init_speed().await,
        }
    }

    #[inline]
    pub async fn initialize_playlist(&mut self, jvm: AJVM) {
        self.current_playlist = Self::init_current_playlist(jvm).await;
    }

    #[inline]
    async fn load_or_create_read_only_file() -> Result<File> {
        Ok(match File::open("data.yaml").await {
            Ok(file) => file,
            Err(_) => File::create("data.yaml").await?,
        })
    }

    #[inline]
    async fn get_write_only_file() -> Result<File> {
        Ok(File::create("data.yaml").await?)
    }

    #[inline]
    async fn read_all_data_from_file() -> Result<Hash> {
        let mut file = Self::load_or_create_read_only_file().await?;
        let mut data = String::new();
        file.read_to_string(&mut data).await?;

        let all_data = match YamlLoader::load_from_str(data.as_str()) {
            Ok(x) => x,
            Err(_) => return Err(Error::from(ErrorKind::InvalidData)),
        };

        Ok(match all_data.first() {
            None => Hash::new(),

            Some(all_data) => match all_data.as_hash() {
                Some(all_data) => all_data,
                None => return Err(Error::from(ErrorKind::InvalidData)),
            }
            .clone(),
        })
    }

    #[inline]
    async fn write_data_to_file(all_data: Hash) -> Result<()> {
        let mut data = String::new();

        YamlEmitter::new(&mut data)
            .dump(&Yaml::Hash(all_data))
            .unwrap();

        let mut file = Self::get_write_only_file().await?;
        Ok(file.write_all(data.as_bytes()).await?)
    }

    #[inline]
    pub async fn store_music_search_path(&mut self, music_search_path: PathBuf) -> Result<()> {
        self.music_search_path = Some(music_search_path.clone());

        let mut all_data = Self::read_all_data_from_file().await?;

        all_data.insert(
            Yaml::String("music_search_path".to_string()),
            Yaml::String(music_search_path.to_string()),
        );

        Self::write_data_to_file(all_data).await
    }

    #[inline]
    async fn init_music_search_path() -> Option<PathBuf> {
        let all_data = match Self::read_all_data_from_file().await {
            Ok(x) => x,
            Err(_) => return audio_dir(),
        };

        match all_data.get(&Yaml::String("music_search_path".to_string())) {
            None => audio_dir(),
            Some(y) => Some(PathBuf::from(y.as_str().unwrap().to_string())),
        }
    }

    #[inline]
    pub fn load_music_search_path(&self) -> Option<&PathBuf> {
        self.music_search_path.as_ref()
    }

    #[inline]
    pub async fn store_track_order(&mut self, track_order: TrackOrder) -> Result<()> {
        self.track_order = track_order;
        let mut all_data = Self::read_all_data_from_file().await?;
        all_data.insert(Yaml::String("track_order".to_string()), track_order.into());
        Self::write_data_to_file(all_data).await
    }

    #[inline]
    async fn init_track_order() -> TrackOrder {
        let all_data = match Self::read_all_data_from_file().await {
            Ok(x) => x,
            Err(_) => return TrackOrder::default(),
        };

        match all_data.get(&Yaml::String("track_order".to_string())) {
            None => TrackOrder::default(),

            Some(y) => match y.as_hash() {
                None => TrackOrder::default(),
                Some(hash) => hash.into(),
            },
        }
    }

    #[inline]
    pub fn load_track_order(&self) -> TrackOrder {
        self.track_order
    }

    #[inline]
    pub async fn store_current_playlist(
        &mut self,
        cur_playlist: DefaultPlaylist<DefaultTrack>,
    ) -> Result<()> {
        self.current_playlist = cur_playlist.clone();

        let mut all_data = Self::read_all_data_from_file().await?;

        all_data.insert(
            Yaml::String("current_playlist".to_string()),
            cur_playlist.into(),
        );

        Self::write_data_to_file(all_data).await
    }

    #[inline]
    async fn init_current_playlist(jvm: AJVM) -> DefaultPlaylist<DefaultTrack> {
        let all_data = match Self::read_all_data_from_file().await {
            Ok(x) => x,
            Err(_) => return DefaultPlaylist::default(),
        };

        match all_data.get(&Yaml::String("current_playlist".to_string())) {
            None => DefaultPlaylist::default(),

            Some(y) => match y.as_hash() {
                None => DefaultPlaylist::default(),

                Some(playlist) => DefaultPlaylist::from_yaml(jvm, playlist)
                    .await
                    .unwrap_or_default(),
            },
        }
    }

    #[inline]
    pub fn load_current_playlist(&self) -> &DefaultPlaylist<DefaultTrack> {
        &self.current_playlist
    }

    #[inline]
    pub async fn store_current_playback_position(&mut self, millis: u64) -> Result<()> {
        self.current_playback_pos = millis;

        let mut all_data = Self::read_all_data_from_file().await?;

        all_data.insert(
            Yaml::String("current_playback_position".to_string()),
            Yaml::Integer(millis as i64),
        );

        Self::write_data_to_file(all_data).await
    }

    #[inline]
    async fn init_current_playback_position() -> u64 {
        let all_data = match Self::read_all_data_from_file().await {
            Ok(x) => x,
            Err(_) => return 0,
        };

        match all_data.get(&Yaml::String("current_playback_position".to_string())) {
            None => 0,
            Some(y) => y.as_i64().unwrap_or_default() as u64,
        }
    }

    #[inline]
    pub fn load_current_playback_position(&self) -> u64 {
        self.current_playback_pos
    }

    #[inline]
    pub async fn store_looping_state(&mut self, looping_state: LoopingState) -> Result<()> {
        self.looping_state = looping_state;

        let mut all_data = Self::read_all_data_from_file().await?;

        all_data.insert(
            Yaml::String("looping_state".to_string()),
            Yaml::Integer(looping_state.into()),
        );

        Self::write_data_to_file(all_data).await
    }

    #[inline]
    async fn init_looping_state() -> LoopingState {
        let all_data = match Self::read_all_data_from_file().await {
            Ok(x) => x,
            Err(_) => return LoopingState::default(),
        };

        match all_data.get(&Yaml::String("looping_state".to_string())) {
            None => LoopingState::default(),
            Some(y) => y.as_i64().map(LoopingState::from).unwrap_or_default(),
        }
    }

    #[inline]
    pub fn load_looping_state(&self) -> LoopingState {
        self.looping_state
    }

    #[inline]
    pub async fn store_volume(&mut self, volume: f32) -> Result<()> {
        self.volume = volume;

        let mut all_data = Self::read_all_data_from_file().await?;

        all_data.insert(
            Yaml::String("volume".to_string()),
            Yaml::Real(format!("{:.2}", volume)),
        );

        Self::write_data_to_file(all_data).await
    }

    #[inline]
    async fn init_volume() -> f32 {
        let all_data = match Self::read_all_data_from_file().await {
            Ok(x) => x,
            Err(_) => return DEFAULT_VOLUME,
        };

        match all_data.get(&Yaml::String("volume".to_string())) {
            None => DEFAULT_VOLUME,

            Some(y) => y
                .as_f64()
                .map(|double| double as f32)
                .unwrap_or(DEFAULT_VOLUME),
        }
    }

    #[inline]
    pub fn load_volume(&self) -> f32 {
        self.volume
    }

    #[inline]
    pub async fn store_speed(&mut self, speed: f32) -> Result<()> {
        self.speed = speed;

        let mut all_data = Self::read_all_data_from_file().await?;

        all_data.insert(
            Yaml::String("speed".to_string()),
            Yaml::Real(format!("{:.2}", speed)),
        );

        Self::write_data_to_file(all_data).await
    }

    #[inline]
    async fn init_speed() -> f32 {
        let all_data = match Self::read_all_data_from_file().await {
            Ok(x) => x,
            Err(_) => return DEFAULT_SPEED,
        };

        match all_data.get(&Yaml::String("speed".to_string())) {
            None => DEFAULT_SPEED,

            Some(y) => y
                .as_f64()
                .map(|double| double as f32)
                .unwrap_or(DEFAULT_SPEED),
        }
    }

    #[inline]
    pub fn load_speed(&self) -> f32 {
        self.speed
    }
}
