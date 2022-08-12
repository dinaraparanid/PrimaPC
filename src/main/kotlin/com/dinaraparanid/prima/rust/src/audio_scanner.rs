extern crate async_recursion;
extern crate jni;
extern crate once_cell;
extern crate tokio;

use crate::{
    entities::tracks::default_track::DefaultTrack,
    utils::{
        params::PARAMS,
        track_order::{Comparator, Ord},
    },
    TrackTrait, JVM,
};

use async_recursion::async_recursion;
use jni::JavaVM;
use std::{fs, path::Path, sync::Arc};
use tokio::sync::Mutex;

#[derive(Debug)]
pub struct AudioScanner;

impl AudioScanner {
    #[inline]
    pub async fn get_all_tracks() -> Arc<Mutex<Vec<DefaultTrack>>> {
        let tracks = Arc::new(Mutex::new(Vec::new()));

        unsafe {
            AudioScanner::search_all_tracks(
                PARAMS
                    .read()
                    .await
                    .as_ref()
                    .unwrap_unchecked()
                    .music_search_path
                    .as_path(),
                tracks.clone(),
                {
                    let jvm = JVM.read();
                    let jni_env = jvm.await.jni_env.clone();
                    Arc::new(jni_env.unwrap().get_java_vm().unwrap_unchecked())
                },
            )
            .await
            .unwrap_unchecked();
        }

        unsafe {
            let mut tracks = tracks.lock().await;
            let track_order = PARAMS.read().await.as_ref().unwrap_unchecked().track_order;

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
    pub async fn scan_file(file: &Path, jvm: Arc<JavaVM>) -> Option<DefaultTrack> {
        let jni_env = unsafe { jvm.attach_current_thread_permanently().unwrap_unchecked() };
        DefaultTrack::from_path(&jni_env, file.to_string_lossy().to_string())
    }

    #[async_recursion]
    async fn search_all_tracks(
        dir: &Path,
        tracks: Arc<Mutex<Vec<DefaultTrack>>>,
        jvm: Arc<JavaVM>,
    ) -> std::io::Result<()> {
        let dir = fs::read_dir(dir)?;
        let mut tasks = Vec::with_capacity(1000);

        for entry in dir {
            let path = entry?.path();
            let tracks_copy = tracks.clone();
            let jvm = jvm.clone();

            tasks.push(unsafe {
                let params = PARAMS.read().await;

                params
                    .as_ref()
                    .unwrap_unchecked()
                    .tokio_runtime
                    .spawn(async move {
                        if path.is_dir() {
                            AudioScanner::search_all_tracks(
                                path.as_path(),
                                tracks_copy.clone(),
                                jvm.clone(),
                            )
                            .await
                            .unwrap();
                        } else {
                            if let Some(track) =
                                AudioScanner::scan_file(path.as_path(), jvm.clone()).await
                            {
                                tracks_copy.lock().await.push(track)
                            }
                        }
                    })
            });
        }

        futures::future::join_all(tasks).await;
        Ok(())
    }
}
