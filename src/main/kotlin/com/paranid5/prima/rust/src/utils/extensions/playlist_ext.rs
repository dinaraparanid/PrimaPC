extern crate jni;

use crate::{PlaylistTrait, TrackExt};
use std::{cell::RefCell, rc::Rc};

use jni::{
    objects::{JObject, JObjectArray},
    sys::jsize,
    JNIEnv,
};

pub trait PlaylistExt<T: TrackExt>: PlaylistTrait<T>
where
    <Self as IntoIterator>::Item: TrackExt,
{
    #[inline]
    fn into_jobject_array(self, env: Rc<RefCell<JNIEnv>>) -> JObjectArray {
        let java_tracks = env
            .borrow_mut()
            .new_object_array(
                self.len() as jsize,
                "com/paranid5/prima/entities/Track",
                &JObject::null(),
            )
            .unwrap();

        for (ind, track) in self.into_iter().enumerate() {
            let jtrack = track.to_java_track(env.clone());

            env.borrow_mut()
                .set_object_array_element(&java_tracks, ind as jsize, &jtrack)
                .unwrap();
        }

        java_tracks
    }

    #[inline]
    fn to_jobject_array<'a>(&self, env: Rc<RefCell<JNIEnv<'a>>>) -> JObjectArray<'a> {
        self.clone().into_jobject_array(env)
    }
}

impl<T: TrackExt, P: PlaylistTrait<T>> PlaylistExt<T> for P where <P as IntoIterator>::Item: TrackExt
{}
