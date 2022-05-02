use crate::{entities::default_track::DefaultTrack, utils::params::PARAMS};

use std::{
    fs,
    path::Path,
    sync::{Arc, Mutex},
    time::SystemTime,
};

use audiotags::{Album, Tag};
use chrono::{DateTime, Duration};

#[derive(Default, Debug)]
pub(crate) struct AudioScannerInstance {}

impl AudioScannerInstance {
    #[inline]
    const fn new() -> Self {
        AudioScannerInstance::default()
    }
}

#[derive(Debug)]
pub(crate) struct AudioScanner {
    instance: Arc<Mutex<Option<AudioScannerInstance>>>,
}

impl Default for AudioScanner {
    #[inline]
    fn default() -> Self {
        AudioScanner {
            instance: Arc::new(Mutex::new(Some(AudioScannerInstance::new()))),
        }
    }
}

impl AudioScanner {
    #[inline]
    fn new() -> Self {
        AudioScanner::default()
    }

    #[inline]
    pub(crate) async fn get_instance(&self) -> Option<AudioScannerInstance> {
        self.instance.lock().unwrap()?
    }

    pub(crate) async fn get_all_tracks(&self) -> Vec<DefaultTrack> {
        let mut tracks = vec![];
        self.search_all_tracks(
            unsafe { PARAMS.get_instance() }
                .await
                .unwrap()
                .music_search_path
                .as_path(),
            &mut tracks,
        );
        tracks
    }

    #[inline]
    async fn scan_file(&self, file: &Path) -> Option<DefaultTrack> {
        let tag = Tag::default().read_from_path(file)?;

        Some(DefaultTrack::new(
            tag.title().unwrap_or("No title").to_string(),
            tag.artist().unwrap_or("Unknown artist").to_string(),
            tag.album()
                .unwrap_or(Album::with_title("Unknown album"))
                .title
                .to_string(),
            file.into_path_buf(),
            Duration::milliseconds(0),
            DateTime::from(
                fs::metadata(file)
                    .map(|md| md.created().unwrap_or(SystemTime::now()))
                    .unwrap_or(SystemTime::now()),
            ),
            tag.track_number().map(|x| x as isize).unwrap_or(-1),
        ))
    }

    async fn search_all_tracks(
        &self,
        dir: &Path,
        tracks: &mut Vec<DefaultTrack>,
    ) -> std::io::Result<()> {
        let dir = fs::read_dir(dir)?;
        let mut tasks = Vec::with_capacity(1000);

        for entry in dir {
            let path = entry?.path();

            tasks.push(async move {
                if path.is_dir() {
                    self.search_all_tracks(path.as_path(), tracks)
                        .await
                        .unwrap();
                } else {
                    self.scan_file(path.as_path()).await
                }
            });
        }

        futures::future::join_all(tasks).await;
        Ok(())
    }
}

pub(crate) static mut AUDIO_SCANNER: AudioScanner = AudioScanner::new();
