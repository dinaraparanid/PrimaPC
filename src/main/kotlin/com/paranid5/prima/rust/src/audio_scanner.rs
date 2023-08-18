extern crate async_recursion;
extern crate jni;
extern crate once_cell;
extern crate tokio;

use crate::{
    entities::tracks::default_track::DefaultTrack,
    utils::{
        storage_util::StorageUtil,
        track_order::{Comparator, Ord, TrackOrder},
        types::{AMutex, TokioRuntime},
    },
    TrackTrait,
};

use async_recursion::async_recursion;
use jni::JavaVM;

use std::{
    cell::RefCell,
    cmp::Ordering,
    fs,
    path::{Path, PathBuf},
    rc::Rc,
    sync::Arc,
};

use tokio::sync::Mutex;

#[derive(Debug)]
pub struct AudioScanner;

impl AudioScanner {
    #[inline]
    pub async fn get_all_tracks(
        jvm: Arc<JavaVM>,
        tokio_runtime: TokioRuntime,
    ) -> AMutex<Vec<DefaultTrack>> {
        let tracks = Arc::new(Mutex::new(Vec::new()));

        let music_search_path = match StorageUtil::load_music_search_path().await {
            None => return tracks,
            Some(msp) => msp,
        };

        Self::search_all_tracks(&music_search_path, tracks.clone(), jvm, tokio_runtime)
            .await
            .unwrap();

        {
            let mut tracks = tracks.lock().await;
            let track_order = StorageUtil::load_track_order().await;

            tracks.sort_by(|f, s| match track_order.comparator {
                Comparator::Title => Self::compare_by_title(track_order, f, s),
                Comparator::Artist => Self::compare_by_artist(track_order, f, s),
                Comparator::Album => Self::compare_by_album(track_order, f, s),
                Comparator::Date => Self::compare_by_date(track_order, f, s),
                Comparator::NumberInAlbum => Self::compare_by_number_in_album(track_order, f, s),
            })
        }

        tracks.clone()
    }

    #[inline]
    fn compare_by_title(track_order: TrackOrder, f: &DefaultTrack, s: &DefaultTrack) -> Ordering {
        match track_order.order {
            Ord::Asc => f
                .get_title()
                .unwrap()
                .clone()
                .partial_cmp(&s.get_title().unwrap().clone())
                .unwrap(),

            Ord::Desc => s
                .get_title()
                .unwrap()
                .clone()
                .partial_cmp(&f.get_title().unwrap().clone())
                .unwrap(),
        }
    }

    #[inline]
    fn compare_by_artist(track_order: TrackOrder, f: &DefaultTrack, s: &DefaultTrack) -> Ordering {
        match track_order.order {
            Ord::Asc => f
                .get_artist()
                .unwrap()
                .clone()
                .partial_cmp(&s.get_artist().unwrap().clone())
                .unwrap(),

            Ord::Desc => s
                .get_artist()
                .unwrap()
                .clone()
                .partial_cmp(&f.get_artist().unwrap().clone())
                .unwrap(),
        }
    }

    #[inline]
    fn compare_by_album(track_order: TrackOrder, f: &DefaultTrack, s: &DefaultTrack) -> Ordering {
        match track_order.order {
            Ord::Asc => f
                .get_album()
                .unwrap()
                .clone()
                .partial_cmp(&s.get_album().unwrap().clone())
                .unwrap(),

            Ord::Desc => s
                .get_album()
                .unwrap()
                .clone()
                .partial_cmp(&f.get_album().unwrap().clone())
                .unwrap(),
        }
    }

    #[inline]
    fn compare_by_date(track_order: TrackOrder, f: &DefaultTrack, s: &DefaultTrack) -> Ordering {
        match track_order.order {
            Ord::Asc => f.get_add_date().partial_cmp(s.get_add_date()).unwrap(),
            Ord::Desc => s.get_add_date().partial_cmp(f.get_add_date()).unwrap(),
        }
    }

    #[inline]
    fn compare_by_number_in_album(
        track_order: TrackOrder,
        f: &DefaultTrack,
        s: &DefaultTrack,
    ) -> Ordering {
        match track_order.order {
            Ord::Asc => f
                .get_number_in_album()
                .partial_cmp(&s.get_number_in_album())
                .unwrap(),

            Ord::Desc => s
                .get_number_in_album()
                .partial_cmp(&f.get_number_in_album())
                .unwrap(),
        }
    }

    #[inline]
    pub async fn scan_file(file: &Path, jvm: Arc<JavaVM>) -> Option<DefaultTrack> {
        let jni_env = Rc::new(RefCell::new(
            jvm.attach_current_thread_permanently().unwrap(),
        ));

        DefaultTrack::from_path(jni_env, file.to_string_lossy().to_string())
    }

    #[async_recursion]
    async fn search_all_tracks(
        dir: &Path,
        tracks: AMutex<Vec<DefaultTrack>>,
        jvm: Arc<JavaVM>,
        tokio_runtime: TokioRuntime,
    ) -> std::io::Result<()> {
        let dir = fs::read_dir(dir)?;
        let mut tasks = Vec::with_capacity(1000);

        for entry in dir {
            let path = entry?.path();
            let tracks = tracks.clone();
            let jvm = jvm.clone();
            let trc = tokio_runtime.clone();

            tasks.push(
                tokio_runtime.spawn(async move { Self::search_step(path, tracks, jvm, trc) }),
            );
        }

        futures::future::join_all(tasks).await;
        Ok(())
    }

    #[inline]
    async fn search_step(
        path: PathBuf,
        tracks: AMutex<Vec<DefaultTrack>>,
        jvm: Arc<JavaVM>,
        tokio_runtime: TokioRuntime,
    ) {
        if path.is_dir() {
            Self::search_all_tracks(path.as_path(), tracks, jvm.clone(), tokio_runtime)
                .await
                .unwrap();
        } else {
            if let Some(track) = Self::scan_file(path.as_path(), jvm.clone()).await {
                tracks.lock().await.push(track)
            }
        }
    }
}
