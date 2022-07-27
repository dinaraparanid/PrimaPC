extern crate jni;

use crate::TrackExt;

use jni::{
    objects::JObject,
    sys::{jobjectArray, jsize},
    JNIEnv,
};

pub trait ExactSizeIteratorExt: ExactSizeIterator {
    #[inline]
    fn into_jobject_array(self, env: &JNIEnv) -> jobjectArray
    where
        Self: Sized,
        <Self as Iterator>::Item: TrackExt,
    {
        let java_tracks = env
            .new_object_array(
                self.len() as jsize,
                "com/dinaraparanid/prima/entities/Track",
                JObject::null(),
            )
            .unwrap();

        self.enumerate().for_each(|(ind, track)| {
            env.set_object_array_element(java_tracks, ind as jsize, track.to_java_track(&env))
                .unwrap();
        });

        java_tracks
    }
}

impl<I: ExactSizeIterator> ExactSizeIteratorExt for I {}
