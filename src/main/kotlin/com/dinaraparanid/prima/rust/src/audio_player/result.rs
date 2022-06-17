#[derive(Copy, Clone, Debug)]
pub enum Error {
    FileOpeningError,
    FileNotSupportedError,
}

pub type Result<T> = std::result::Result<T, Error>;
