extern crate jni;

use jni::{objects::JValue, signature::JavaType, sys::jobject, JNIEnv};

pub(crate) trait JNIEnvExt {
    unsafe fn get_field(&self, object: jobject, field: &str, sig: &str, tp: JavaType) -> JValue;

    unsafe fn call_static_method(
        &self,
        class: &str,
        method_name: &str,
        sig: &str,
        ret: JavaType,
        args: &[JValue],
    ) -> JValue;
}

impl JNIEnvExt for JNIEnv<'_> {
    #[inline]
    unsafe fn get_field(&self, object: jobject, field: &str, sig: &str, tp: JavaType) -> JValue {
        let class = self.get_object_class(object).unwrap();
        let id = self.get_field_id(class, field, sig).unwrap();
        self.get_field_unchecked(object, id, tp).unwrap()
    }

    #[inline]
    unsafe fn call_static_method(
        &self,
        class: &str,
        method_name: &str,
        sig: &str,
        ret: JavaType,
        args: &[JValue],
    ) -> JValue {
        self.call_static_method_unchecked(class, (class, method_name, sig), ret, args)
            .unwrap()
    }
}
