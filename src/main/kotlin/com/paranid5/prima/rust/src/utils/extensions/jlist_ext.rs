use jni::{
    objects::{JList, JObject},
    JNIEnv,
};

use std::{cell::RefCell, rc::Rc};

pub(crate) trait JListExt {
    fn map<'a, 'b: 'a, T, F: Fn(JObject<'a>, Rc<RefCell<JNIEnv<'b>>>) -> T>(
        &self,
        env: Rc<RefCell<JNIEnv<'b>>>,
        transformer: F,
    ) -> Vec<T>;
}

impl<'a, 'b, 'c> JListExt for JList<'a, 'b, 'c> {
    #[inline]
    fn map<'d, 'e: 'd, T, F: Fn(JObject<'d>, Rc<RefCell<JNIEnv<'e>>>) -> T>(
        &self,
        env: Rc<RefCell<JNIEnv<'e>>>,
        transformer: F,
    ) -> Vec<T> {
        let mut iter = self.iter(&mut *env.borrow_mut()).unwrap();
        let mut vec = Vec::new();

        while let Some(obj) = { iter.next(&mut *env.borrow_mut()).unwrap() } {
            vec.push(transformer(obj, env.clone()))
        }

        vec
    }
}
