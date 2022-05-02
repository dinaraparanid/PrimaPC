use jni::{
    objects::JString,
    sys::{jclass, jstring},
    JNIEnv,
};

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
