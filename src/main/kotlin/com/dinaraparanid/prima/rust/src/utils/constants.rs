extern crate once_cell;

use once_cell::sync::Lazy;

pub const NULL_CHARACTER: Lazy<u8> = Lazy::new(|| "\0".bytes().next().unwrap());
