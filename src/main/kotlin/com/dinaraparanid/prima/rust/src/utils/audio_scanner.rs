extern crate async_recursion;
extern crate audiotags;
extern crate chrono;
extern crate futures;

use crate::{entities::default_track::DefaultTrack, program::ProgramInstance};

use std::{
    fs,
    path::Path,
    sync::Weak,
    sync::{Arc, Mutex, RwLock},
    time::SystemTime,
};

use async_recursion::async_recursion;
use audiotags::Tag;
use chrono::{DateTime, Duration};
use futures::{executor::ThreadPool, task::SpawnExt};

#[derive(Debug)]
pub struct AudioScanner {
    pool: ThreadPool,
    program: Weak<RwLock<Option<ProgramInstance>>>,
}

impl AudioScanner {
    #[inline]
    pub fn new(program: Arc<RwLock<Option<ProgramInstance>>>) -> Self {
        AudioScanner {
            pool: ThreadPool::new().unwrap(),
            program: Arc::downgrade(&program),
        }
    }

    #[inline]
    pub async fn get_all_tracks(&self) -> Arc<Mutex<Vec<DefaultTrack>>> {
        let tracks = Arc::new(Mutex::new(Vec::new()));
        let pool = self.pool.clone();

        AudioScanner::search_all_tracks(
            self.program
                .upgrade()
                .unwrap()
                .read()
                .unwrap()
                .as_ref()
                .unwrap()
                .params
                .read()
                .unwrap()
                .as_ref()
                .unwrap()
                .music_search_path
                .as_path(),
            tracks.clone(),
            pool,
        )
        .await;
        tracks.clone()
    }

    #[inline]
    async fn scan_file(file: &Path) -> Option<DefaultTrack> {
        match Tag::default().read_from_path(file) {
            Ok(tag) => Some(DefaultTrack::new(
                tag.title().map(|title| title.to_string()),
                tag.artist().map(|artist| artist.to_string()),
                tag.album().map(|album| album.title.to_string()),
                file.to_path_buf(),
                Duration::milliseconds(0), // TODO: Find real Duration
                DateTime::from(
                    fs::metadata(file)
                        .map(|md| md.created().unwrap_or(SystemTime::now()))
                        .unwrap_or(SystemTime::now()),
                ),
                tag.track_number().map(|x| x as isize).unwrap_or(-1),
            )),

            Err(_) => None,
        }
    }

    #[async_recursion]
    async fn search_all_tracks(
        dir: &Path,
        tracks: Arc<Mutex<Vec<DefaultTrack>>>,
        pool: ThreadPool,
    ) -> std::io::Result<()> {
        let dir = fs::read_dir(dir)?;
        let mut tasks = Vec::with_capacity(1000);

        for entry in dir {
            let path = entry?.path();
            let tracks_copy = tracks.clone();
            let pool1 = pool.clone();
            let pool2 = pool.clone();

            tasks.push(
                pool1
                    .spawn_with_handle(async move {
                        if path.is_dir() {
                            AudioScanner::search_all_tracks(
                                path.as_path(),
                                tracks_copy.clone(),
                                pool2.clone(),
                            )
                            .await
                            .unwrap();
                        } else {
                            if let Some(track) = AudioScanner::scan_file(path.as_path()).await {
                                tracks_copy.lock().unwrap().push(track)
                            }
                        }
                    })
                    .unwrap(),
            );
        }

        futures::future::join_all(tasks).await;
        Ok(())
    }
}
