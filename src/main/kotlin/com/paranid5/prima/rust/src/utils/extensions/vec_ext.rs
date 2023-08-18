extern crate jni;

use crate::TrackExt;
use std::{cell::RefCell, rc::Rc};

use jni::{
    objects::{JObject, JObjectArray},
    sys::jsize,
    JNIEnv,
};

pub trait ExactSizeIteratorExt: ExactSizeIterator {
    #[inline]
    fn into_jobject_array(self, env: Rc<RefCell<JNIEnv>>) -> JObjectArray
    where
        Self: Sized,
        <Self as Iterator>::Item: TrackExt,
    {
        let java_tracks = env
            .borrow_mut()
            .new_object_array(
                self.len() as jsize,
                "com/paranid5/prima/entities/Track",
                &JObject::null(),
            )
            .unwrap();

        self.enumerate().for_each(|(ind, track)| {
            let jtrack = track.to_java_track(env.clone());

            env.borrow_mut()
                .set_object_array_element(&java_tracks, ind as jsize, &jtrack)
                .unwrap();
        });

        java_tracks
    }
}

impl<I: ExactSizeIterator> ExactSizeIteratorExt for I {}
