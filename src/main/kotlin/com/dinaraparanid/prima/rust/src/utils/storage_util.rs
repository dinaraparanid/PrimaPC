extern crate dirs2;
extern crate yaml_rust;

use std::{
    fs::File,
    io::{Error, ErrorKind, Read, Result, Write},
    path::PathBuf,
};

use crate::{
    audio_player::playback_params::LoopingState, utils::extensions::path_buf_ext::PathBufExt,
    DefaultPlaylist, DefaultTrack, TrackOrder,
};

use dirs2::audio_dir;
use yaml_rust::{yaml::Hash, Yaml, YamlEmitter, YamlLoader};

pub struct StorageUtil;

impl StorageUtil {
    #[inline]
    fn load_or_create_read_only_file() -> Result<File> {
        Ok(match File::open("data.yaml") {
            Ok(file) => file,
            Err(_) => File::create("data.yaml")?,
        })
    }

    #[inline]
    fn get_write_only_file() -> Result<File> {
        Ok(File::create("data.yaml")?)
    }

    #[inline]
    fn read_all_data_from_file() -> Result<Hash> {
        let mut file = Self::load_or_create_read_only_file()?;
        let mut data = String::new();
        file.read_to_string(&mut data)?;

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
    pub fn write_data_to_file(all_data: Hash) -> Result<()> {
        let mut data = String::new();
        YamlEmitter::new(&mut data)
            .dump(&Yaml::Hash(all_data))
            .unwrap();

        let mut file = Self::get_write_only_file()?;
        Ok(file.write_all(data.as_bytes())?)
    }

    #[inline]
    pub fn store_music_search_path(music_search_path: PathBuf) -> Result<()> {
        let mut all_data = Self::read_all_data_from_file()?;

        all_data.insert(
            Yaml::String("music_search_path".to_string()),
            Yaml::String(music_search_path.to_string()),
        );

        Self::write_data_to_file(all_data)
    }

    #[inline]
    fn set_default_music_search_path() -> Option<PathBuf> {
        let default_music_search_path = audio_dir()?;
        Self::store_music_search_path(default_music_search_path.clone()).ok()?;
        return Some(default_music_search_path);
    }

    #[inline]
    pub fn load_music_search_path() -> Option<PathBuf> {
        let all_data = match Self::read_all_data_from_file() {
            Ok(x) => x,
            Err(_) => return Self::set_default_music_search_path(),
        };

        Some(
            match all_data.get(&Yaml::String("music_search_path".to_string())) {
                None => return Self::set_default_music_search_path(),
                Some(y) => PathBuf::from(y.as_str().unwrap().to_string()),
            },
        )
    }

    #[inline]
    pub fn store_track_order(track_order: TrackOrder) -> Result<()> {
        let mut all_data = Self::read_all_data_from_file()?;
        all_data.insert(Yaml::String("track_order".to_string()), track_order.into());
        Self::write_data_to_file(all_data)
    }

    #[inline]
    fn set_default_track_order() -> Option<TrackOrder> {
        let default_track_order = TrackOrder::default();
        Self::store_track_order(default_track_order).ok()?;
        return Some(default_track_order);
    }

    #[inline]
    pub fn load_track_order() -> TrackOrder {
        let all_data = match Self::read_all_data_from_file() {
            Ok(x) => x,
            Err(_) => return Self::set_default_track_order().unwrap_or(TrackOrder::default()),
        };

        match all_data.get(&Yaml::String("track_order".to_string())) {
            None => Self::set_default_track_order().unwrap_or(TrackOrder::default()),

            Some(y) => match y.as_hash() {
                None => Self::set_default_track_order().unwrap_or(TrackOrder::default()),
                Some(hash) => hash.into(),
            },
        }
    }

    #[inline]
    pub fn store_current_playlist(cur_playlist: DefaultPlaylist<DefaultTrack>) -> Result<()> {
        let mut all_data = Self::read_all_data_from_file()?;

        all_data.insert(
            Yaml::String("current_playlist".to_string()),
            cur_playlist.into(),
        );

        Self::write_data_to_file(all_data)
    }

    #[inline]
    fn set_default_current_playlist() -> Option<DefaultPlaylist<DefaultTrack>> {
        let default_current_playlist = DefaultPlaylist::default();
        Self::store_current_playlist(default_current_playlist.clone()).ok()?;
        return Some(default_current_playlist);
    }

    #[inline]
    pub fn load_current_playlist() -> DefaultPlaylist<DefaultTrack> {
        let all_data = match Self::read_all_data_from_file() {
            Ok(x) => x,
            Err(_) => {
                return Self::set_default_current_playlist().unwrap_or(DefaultPlaylist::default())
            }
        };

        match all_data.get(&Yaml::String("current_playlist".to_string())) {
            None => Self::set_default_current_playlist().unwrap_or(DefaultPlaylist::default()),

            Some(y) => match y.as_hash() {
                None => Self::set_default_current_playlist().unwrap_or(DefaultPlaylist::default()),
                Some(playlist) => {
                    DefaultPlaylist::from_yaml(playlist).unwrap_or(DefaultPlaylist::default())
                }
            },
        }
    }

    #[inline]
    pub fn store_current_playback_position(millis: u64) -> Result<()> {
        let mut all_data = Self::read_all_data_from_file()?;

        all_data.insert(
            Yaml::String("current_playback_position".to_string()),
            Yaml::Integer(millis as i64),
        );

        Self::write_data_to_file(all_data)
    }

    #[inline]
    fn set_default_current_playback_position() -> Option<u64> {
        let default_current_playback_position = 0;
        Self::store_current_playback_position(default_current_playback_position).ok()?;
        return Some(default_current_playback_position);
    }

    #[inline]
    pub fn load_current_playback_position() -> u64 {
        let all_data = match Self::read_all_data_from_file() {
            Ok(x) => x,
            Err(_) => {
                println!("POS 1");
                return Self::set_default_current_playback_position().unwrap_or_default();
            }
        };

        match all_data.get(&Yaml::String("current_playback_position".to_string())) {
            None => {
                println!("POS 2");
                Self::set_default_current_playback_position().unwrap_or_default()
            }

            Some(y) => y.as_i64().unwrap_or_else(|| {
                println!("POS 3");
                Self::set_default_current_playback_position().unwrap_or_default() as i64
            }) as u64,
        }
    }

    #[inline]
    pub fn store_looping_state(looping_state: LoopingState) -> Result<()> {
        let mut all_data = Self::read_all_data_from_file()?;

        all_data.insert(
            Yaml::String("looping_state".to_string()),
            Yaml::Integer(looping_state.into()),
        );

        Self::write_data_to_file(all_data)
    }

    #[inline]
    fn set_default_looping_state() -> Option<LoopingState> {
        let default_looping_state = LoopingState::default();
        Self::store_looping_state(default_looping_state).ok()?;
        return Some(default_looping_state);
    }

    #[inline]
    pub fn load_looping_state() -> LoopingState {
        let all_data = match Self::read_all_data_from_file() {
            Ok(x) => x,
            Err(_) => {
                println!("LOOPING 1");
                return Self::set_default_looping_state().unwrap_or_default();
            }
        };

        match all_data.get(&Yaml::String("looping_state".to_string())) {
            None => {
                println!("LOOPING 2");
                Self::set_default_looping_state().unwrap_or_default()
            }

            Some(y) => y.as_i64().map(LoopingState::from).unwrap_or_else(|| {
                println!("LOOPING 3");
                Self::set_default_looping_state().unwrap_or_default()
            }),
        }
    }

    #[inline]
    pub fn store_volume(volume: f32) -> Result<()> {
        let mut all_data = Self::read_all_data_from_file()?;

        all_data.insert(
            Yaml::String("volume".to_string()),
            Yaml::Real(format!("{:.2}", volume)),
        );

        Self::write_data_to_file(all_data)
    }

    #[inline]
    fn set_default_volume() -> Option<f32> {
        let default_volume = 1_f32;
        Self::store_volume(default_volume).ok()?;
        return Some(default_volume);
    }

    #[inline]
    pub fn load_volume() -> f32 {
        let all_data = match Self::read_all_data_from_file() {
            Ok(x) => x,
            Err(_) => {
                println!("VOLUME 1");
                return Self::set_default_volume().unwrap_or(1_f32);
            }
        };

        match all_data.get(&Yaml::String("volume".to_string())) {
            None => {
                println!("VOLUME 2");
                Self::set_default_volume().unwrap_or(1_f32)
            }

            Some(y) => y.as_f64().map(|double| double as f32).unwrap_or_else(|| {
                println!("VOLUME 3");
                Self::set_default_volume().unwrap_or(1_f32)
            }),
        }
    }

    #[inline]
    pub fn store_speed(speed: f32) -> Result<()> {
        let mut all_data = Self::read_all_data_from_file()?;

        all_data.insert(
            Yaml::String("speed".to_string()),
            Yaml::Real(format!("{:.2}", speed)),
        );

        Self::write_data_to_file(all_data)
    }

    #[inline]
    fn set_default_speed() -> Option<f32> {
        let default_speed = 1_f32;
        Self::store_speed(default_speed).ok()?;
        return Some(default_speed);
    }

    #[inline]
    pub fn load_speed() -> f32 {
        let all_data = match Self::read_all_data_from_file() {
            Ok(x) => x,
            Err(_) => {
                println!("SPEED 1");
                return Self::set_default_speed().unwrap_or(1_f32);
            }
        };

        match all_data.get(&Yaml::String("speed".to_string())) {
            None => {
                println!("SPEED 2");
                Self::set_default_speed().unwrap_or(1_f32)
            }

            Some(y) => y.as_f64().map(|double| double as f32).unwrap_or_else(|| {
                println!("SPEED 3");
                Self::set_default_speed().unwrap_or(1_f32)
            }),
        }
    }
}
