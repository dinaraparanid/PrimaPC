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
    use crate::utils::{storage_util::StorageUtil, track_order::TrackOrder};
    assert!(StorageUtil::store_track_order(TrackOrder::default()).is_ok())
}

#[test]
fn load_track_order_test() {
    use crate::utils::{storage_util::StorageUtil, track_order::TrackOrder};
    assert_eq!(StorageUtil::load_track_order(), TrackOrder::default())
}
