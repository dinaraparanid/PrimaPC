use crate::TrackOrder;

#[test]
fn multithreading_async_test() {
    extern crate futures;

    use futures::{
        executor::ThreadPool,
        task::{Spawn, SpawnExt},
    };

    use std::sync::{Arc, Mutex};

    let vector = Arc::new(Mutex::new(Some(Vec::new())));
    let mut tasks = Vec::new();
    let pool1 = ThreadPool::new().unwrap();
    let pool2 = ThreadPool::new().unwrap();

    futures::executor::block_on(async move {
        for i in 0..10 {
            let vector_copy = vector.clone();
            let pool2_clone = pool2.clone();

            tasks.push(
                pool1
                    .spawn_with_handle(async move {
                        println!("NUMBER: {}", i);
                        vector_copy.lock().unwrap().as_mut().unwrap().push(
                            pool2_clone
                                .spawn_with_handle(async move {
                                    for _ in 0..1000 {
                                        println!("{}", i);
                                    }
                                    i
                                })
                                .unwrap(),
                        )
                    })
                    .unwrap(),
            );

            println!("ITERATION: {} NEW THREADS {:?}", i, pool2.status());
        }

        futures::future::join_all(tasks).await;
        let v = vector.lock().unwrap().take().unwrap();
        let mut res = futures::future::join_all(v).await;
        res.sort();
        assert_eq!(res, (0..10).collect::<Vec<_>>());
        println!("SUCCESS!");
    });
}

#[test]
fn store_music_search_path_test() {
    extern crate dirs2;
    use crate::utils::storage_util::StorageUtil;
    use dirs2::audio_dir;

    assert!(StorageUtil::store_music_search_path(audio_dir().unwrap()).is_ok())
}

#[test]
fn load_music_search_path_test() {
    extern crate dirs2;
    use crate::utils::storage_util::StorageUtil;
    use dirs2::audio_dir;

    assert_eq!(
        StorageUtil::load_music_search_path(),
        Some(audio_dir().unwrap())
    )
}

#[test]
fn store_track_order_test() {
    use crate::utils::storage_util::StorageUtil;
    assert!(StorageUtil::store_track_order(TrackOrder::default()).is_ok())
}

#[test]
fn load_track_order_test() {
    use crate::utils::storage_util::StorageUtil;
    assert_eq!(StorageUtil::load_track_order(), TrackOrder::default())
}
