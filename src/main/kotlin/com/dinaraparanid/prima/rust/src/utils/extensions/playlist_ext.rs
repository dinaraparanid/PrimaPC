extern crate jni;

use crate::{PlaylistTrait, TrackExt};

use jni::{
    objects::JObject,
    sys::{jobjectArray, jsize},
    JNIEnv,
};

pub trait PlaylistExt<T: TrackExt>: PlaylistTrait<T>
where
    <Self as IntoIterator>::Item: TrackExt,
{
    #[inline]
    fn into_jobject_array(self, env: &JNIEnv) -> jobjectArray {
        let java_tracks = env
            .new_object_array(
                self.len() as jsize,
                "com/dinaraparanid/prima/entities/Track",
                JObject::null(),
            )
            .unwrap();

        self.into_iter().enumerate().for_each(|(ind, track)| {
            env.set_object_array_element(java_tracks, ind as jsize, track.to_java_track(&env))
                .unwrap();
        });

        java_tracks
    }

    #[inline]
    fn to_jobject_array(&self, env: &JNIEnv) -> jobjectArray {
        self.clone().into_jobject_array(env)
    }
}

impl<T: TrackExt, P: PlaylistTrait<T>> PlaylistExt<T> for P where <P as IntoIterator>::Item: TrackExt
{}
