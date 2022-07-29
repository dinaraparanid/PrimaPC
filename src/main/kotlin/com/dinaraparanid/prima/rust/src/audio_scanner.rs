extern crate async_recursion;
extern crate futures;
extern crate once_cell;

use crate::{
    entities::tracks::default_track::DefaultTrack,
    utils::{
        params::PARAMS,
        track_order::{Comparator, Ord},
    },
    TrackTrait, JVM,
};

use std::{
    fs,
    path::Path,
    sync::{Arc, Mutex, RwLock},
};

use async_recursion::async_recursion;
use futures::{executor::ThreadPool, task::SpawnExt};
use once_cell::sync::Lazy;

#[derive(Debug)]
pub struct AudioScanner {
    pool: ThreadPool,
}

static mut AUDIO_SCANNER: Lazy<Arc<RwLock<AudioScanner>>> =
    Lazy::new(|| Arc::new(RwLock::new(AudioScanner::new())));

impl AudioScanner {
    #[inline]
    pub fn new() -> Self {
        Self {
            pool: ThreadPool::new().unwrap(),
        }
    }

    #[inline]
    pub async fn get_all_tracks() -> Arc<Mutex<Vec<DefaultTrack>>> {
        let tracks = Arc::new(Mutex::new(Vec::new()));
        let pool = unsafe { AUDIO_SCANNER.read().unwrap_unchecked().pool.clone() };

        unsafe {
            AudioScanner::search_all_tracks(
                PARAMS
                    .read()
                    .unwrap_unchecked()
                    .as_ref()
                    .unwrap_unchecked()
                    .music_search_path
                    .as_path(),
                tracks.clone(),
                pool,
            )
            .await
            .unwrap_unchecked();
        }

        unsafe {
            let mut tracks = tracks.lock().unwrap_unchecked();

            let track_order = PARAMS
                .read()
                .unwrap_unchecked()
                .as_ref()
                .unwrap_unchecked()
                .track_order;

            tracks.sort_by(|f, s| match track_order.comparator {
                Comparator::Title => match track_order.order {
                    Ord::Asc => f
                        .get_title()
                        .unwrap_unchecked()
                        .clone()
                        .partial_cmp(&s.get_title().unwrap_unchecked().clone())
                        .unwrap_unchecked(),

                    Ord::Desc => s
                        .get_title()
                        .unwrap_unchecked()
                        .clone()
                        .partial_cmp(&f.get_title().unwrap_unchecked().clone())
                        .unwrap_unchecked(),
                },

                Comparator::Artist => match track_order.order {
                    Ord::Asc => f
                        .get_artist()
                        .unwrap_unchecked()
                        .clone()
                        .partial_cmp(&s.get_artist().unwrap_unchecked().clone())
                        .unwrap_unchecked(),

                    Ord::Desc => s
                        .get_artist()
                        .unwrap_unchecked()
                        .clone()
                        .partial_cmp(&f.get_artist().unwrap_unchecked().clone())
                        .unwrap_unchecked(),
                },

                Comparator::Album => match track_order.order {
                    Ord::Asc => f
                        .get_album()
                        .unwrap_unchecked()
                        .clone()
                        .partial_cmp(&s.get_album().unwrap_unchecked().clone())
                        .unwrap_unchecked(),

                    Ord::Desc => s
                        .get_album()
                        .unwrap_unchecked()
                        .clone()
                        .partial_cmp(&f.get_album().unwrap_unchecked().clone())
                        .unwrap_unchecked(),
                },

                Comparator::Date => match track_order.order {
                    Ord::Asc => f
                        .get_add_date()
                        .partial_cmp(s.get_add_date())
                        .unwrap_unchecked(),

                    Ord::Desc => s
                        .get_add_date()
                        .partial_cmp(f.get_add_date())
                        .unwrap_unchecked(),
                },

                Comparator::NumberInAlbum => match track_order.order {
                    Ord::Asc => f
                        .get_number_in_album()
                        .partial_cmp(&s.get_number_in_album())
                        .unwrap_unchecked(),

                    Ord::Desc => s
                        .get_number_in_album()
                        .partial_cmp(&f.get_number_in_album())
                        .unwrap_unchecked(),
                },
            })
        }

        tracks.clone()
    }

    #[inline]
    pub async fn scan_file(file: &Path) -> Option<DefaultTrack> {
        let jni_env = unsafe { &JVM.read().unwrap_unchecked().jni_env }.clone();
        let jvm = unsafe { jni_env.unwrap().get_java_vm().unwrap_unchecked() };
        let jni_env = unsafe { jvm.attach_current_thread_permanently().unwrap_unchecked() };
        DefaultTrack::from_path(&jni_env, file.to_string_lossy().to_string())
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
            let pool_ref_1 = pool.clone();
            let pool_ref_2 = pool.clone();

            tasks.push(
                pool_ref_1
                    .spawn_with_handle(async move {
                        if path.is_dir() {
                            AudioScanner::search_all_tracks(
                                path.as_path(),
                                tracks_copy.clone(),
                                pool_ref_2.clone(),
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
