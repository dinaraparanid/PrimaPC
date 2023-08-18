extern crate jni;

use jni::{
    objects::{JObject, JValue, JValueOwned},
    JNIEnv,
};

use std::{cell::RefCell, rc::Rc};

pub(crate) trait JNIEnvExt<'a> {
    fn get_field<'b>(
        this: Rc<RefCell<Self>>,
        object: &'b JObject<'b>,
        field: &str,
        sig: &str,
    ) -> JValueOwned<'a>
    where
        'a: 'b;

    fn call_static_method(
        this: Rc<RefCell<Self>>,
        class: &str,
        method_name: &str,
        sig: &str,
        args: &[JValue],
    ) -> JValueOwned<'a>;
}

impl<'a> JNIEnvExt<'a> for JNIEnv<'a> {
    #[inline]
    fn get_field<'b>(
        this: Rc<RefCell<Self>>,
        object: &'b JObject<'b>,
        field: &str,
        sig: &str,
    ) -> JValueOwned<'a>
    where
        'a: 'b,
    {
        this.borrow_mut().get_field(object, &field, sig).unwrap()
    }

    #[inline]
    fn call_static_method(
        this: Rc<RefCell<Self>>,
        class: &str,
        method_name: &str,
        sig: &str,
        args: &[JValue],
    ) -> JValueOwned<'a> {
        this.borrow_mut()
            .call_static_method(class, method_name, sig, args)
            .unwrap()
    }
}
