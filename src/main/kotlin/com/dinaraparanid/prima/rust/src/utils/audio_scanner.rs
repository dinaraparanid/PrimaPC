extern crate async_recursion;
extern crate futures;
extern crate once_cell;

use crate::{
    entities::tracks::default_track::DefaultTrack,
    utils::{
        extensions::{jni_env_ext::JNIEnvExt, jobject_ext::JObjectExt, string_ext::StringExt},
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

use jni::{
    objects::{JObject, JValue},
    signature::JavaType,
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
        let pool = unsafe { AUDIO_SCANNER.read().unwrap().pool.clone() };

        AudioScanner::search_all_tracks(
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

        {
            let mut tracks = tracks.lock().unwrap();
            let track_order = unsafe { PARAMS.read().unwrap().as_ref().unwrap().track_order };

            tracks.sort_by(|f, s| match track_order.comparator {
                Comparator::TITLE => match track_order.order {
                    Ord::ASC => String::from_jbyte_vec(f.get_title().unwrap().clone())
                        .partial_cmp(&String::from_jbyte_vec(s.get_title().unwrap().clone()))
                        .unwrap(),

                    Ord::DESC => String::from_jbyte_vec(s.get_title().unwrap().clone())
                        .partial_cmp(&String::from_jbyte_vec(f.get_title().unwrap().clone()))
                        .unwrap(),
                },

                Comparator::ARTIST => match track_order.order {
                    Ord::ASC => String::from_jbyte_vec(f.get_artist().unwrap().clone())
                        .partial_cmp(&String::from_jbyte_vec(s.get_artist().unwrap().clone()))
                        .unwrap(),

                    Ord::DESC => String::from_jbyte_vec(s.get_artist().unwrap().clone())
                        .partial_cmp(&String::from_jbyte_vec(f.get_artist().unwrap().clone()))
                        .unwrap(),
                },

                Comparator::ALBUM => match track_order.order {
                    Ord::ASC => String::from_jbyte_vec(f.get_album().unwrap().clone())
                        .partial_cmp(&String::from_jbyte_vec(s.get_album().unwrap().clone()))
                        .unwrap(),

                    Ord::DESC => String::from_jbyte_vec(s.get_album().unwrap().clone())
                        .partial_cmp(&String::from_jbyte_vec(f.get_album().unwrap().clone()))
                        .unwrap(),
                },

                Comparator::DATE => match track_order.order {
                    Ord::ASC => f.get_add_date().partial_cmp(s.get_add_date()).unwrap(),
                    Ord::DESC => s.get_add_date().partial_cmp(f.get_add_date()).unwrap(),
                },
            })
        }

        tracks.clone()
    }

    #[inline]
    pub async fn scan_file(file: &Path) -> Option<DefaultTrack> {
        let jni_env = unsafe { &JVM.read().unwrap().jni_env }.clone();
        let jvm = jni_env.unwrap().get_java_vm().unwrap();
        let jni_env = jvm.attach_current_thread_permanently().unwrap();

        let file_name = jni_env
            .new_string(file.to_string_lossy().to_string())
            .unwrap();

        JObjectExt::array_to_track(
            &unsafe {
                JNIEnvExt::call_static_method(
                    &jni_env,
                    "com/dinaraparanid/prima/rust/RustLibs",
                    "getDataByPath",
                    "(Ljava/lang/String;)[Ljava/lang/Object;",
                    JavaType::Array(Box::new(JavaType::Object(String::from("java/lang/Object")))),
                    &[JValue::Object(JObject::from(file_name))],
                )
            }
            .l()
            .unwrap(),
            &jni_env,
            file.to_path_buf(),
        )
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
