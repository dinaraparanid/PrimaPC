extern crate futures;

use futures::{
    executor::ThreadPool,
    task::{Spawn, SpawnExt},
};

use std::sync::{Arc, Mutex};

#[test]
fn multithreading_async_test() {
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

/*#[test]
fn scan_tracks_test() {
    let program = Program::new();

    futures::executor::block_on(async move {
        assert_eq!(
            *program
                .instance
                .read()
                .unwrap()
                .as_ref()
                .unwrap()
                .audio_scanner
                .read()
                .unwrap()
                .as_ref()
                .unwrap()
                .get_all_tracks()
                .await
                .lock()
                .unwrap(),
            Vec::<DefaultTrack>::new()
        )
    })
}*/
