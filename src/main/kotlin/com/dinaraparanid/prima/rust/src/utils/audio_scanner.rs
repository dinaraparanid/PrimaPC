extern crate async_recursion;
extern crate audiotags;
extern crate chrono;
extern crate futures;
extern crate once_cell;

use crate::{entities::default_track::DefaultTrack, JVM};

use std::{
    fs,
    path::Path,
    sync::{Arc, Mutex, RwLock},
    time::SystemTime,
};

use crate::utils::params::PARAMS;
use async_recursion::async_recursion;
use audiotags::Tag;
use chrono::{DateTime, Duration};
use futures::{executor::ThreadPool, task::SpawnExt};
use jni::objects::{JClass, JObject, JValue};
use once_cell::sync::Lazy;

#[derive(Debug)]
pub struct AudioScanner {
    pool: ThreadPool,
}

pub static mut AUDIO_SCANNER: Lazy<Arc<RwLock<AudioScanner>>> =
    Lazy::new(|| Arc::new(RwLock::new(AudioScanner::new())));

impl AudioScanner {
    #[inline]
    pub fn new() -> Self {
        Self {
            pool: ThreadPool::new().unwrap(),
        }
    }

    #[inline]
    pub async fn get_all_tracks(&'static self) -> Arc<Mutex<Vec<DefaultTrack>>> {
        let tracks = Arc::new(Mutex::new(Vec::new()));
        let pool = self.pool.clone();

        self.search_all_tracks(
            unsafe {
                PARAMS
                    .read()
                    .unwrap()
                    .as_ref()
                    .unwrap()
                    .music_search_path
                    .as_path()
            },
            tracks.clone(),
            pool,
        )
        .await
        .unwrap();

        tracks.clone()
    }

    #[inline]
    pub async fn scan_file(&self, file: &Path) -> Option<DefaultTrack> {
        let jni_env = unsafe { &JVM.read().unwrap().jni_env.unwrap() };

        let rust_libs_class =
            JClass::from(unsafe { &JVM }.read().unwrap().rust_libs_class.unwrap());

        let file_name = jni_env
            .new_string(file.to_string_lossy().to_string())
            .unwrap();

        let duration = jni_env
            .call_static_method(
                rust_libs_class,
                "getTrackDuration",
                "(Ljava/lang/String;)I",
                &[JValue::Object(JObject::from(file_name))],
            )
            .unwrap()
            .i()
            .unwrap();

        match Tag::default().read_from_path(file) {
            Ok(tag) => Some(DefaultTrack::new(
                tag.title().map(|title| title.to_string()),
                tag.artist().map(|artist| artist.to_string()),
                tag.album().map(|album| album.title.to_string()),
                file.to_path_buf(),
                Duration::milliseconds(duration as i64),
                DateTime::from(
                    fs::metadata(file)
                        .map(|md| md.created().unwrap_or(SystemTime::now()))
                        .unwrap_or(SystemTime::now()),
                ),
                tag.track_number().map(|x| x as i16).unwrap_or(-1),
            )),

            Err(_) => None,
        }
    }

    #[async_recursion]
    async fn search_all_tracks(
        &'static self,
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
                            self.search_all_tracks(
                                path.as_path(),
                                tracks_copy.clone(),
                                pool2.clone(),
                            )
                            .await
                            .unwrap();
                        } else {
                            if let Some(track) = self.scan_file(path.as_path()).await {
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
