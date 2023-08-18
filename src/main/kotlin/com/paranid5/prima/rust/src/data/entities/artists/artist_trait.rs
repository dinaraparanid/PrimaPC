use std::fmt::Debug;

pub trait ArtistTrait: PartialEq + PartialOrd + Debug + Clone {
    fn get_name(&self) -> &String;
}
