extern crate jni;

pub mod entities;
pub mod program;
mod traits;
mod utils;

#[cfg(test)]
mod tests;

use crate::program::Program;

use jni::{
    objects::JString,
    sys::{jclass, jobject, jstring},
    JNIEnv,
};

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_initRust(
    env: JNIEnv,
    _class: jclass,
) -> jobject {
    env.new_direct_byte_buffer(unsafe {
        std::slice::from_raw_parts_mut(
            Box::into_raw(Box::new(Program::new(env))) as *mut u8,
            std::mem::size_of::<*mut JNIEnv>(),
        )
    })
    .unwrap()
    .into_inner()
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern "system" fn Java_com_dinaraparanid_prima_rust_RustLibs_hello(
    env: JNIEnv,
    _class: jclass,
    name: JString,
) -> jstring {
    let name: String = env.get_string(name).unwrap().into();
    env.new_string(format!("Hello, {}!", name))
        .unwrap()
        .into_inner()
}
