use crate::{
    entities::artists::{artist_trait::ArtistTrait, favourite_artist::FavouriteArtist},
    Favourable,
};

#[derive(Debug, Clone, PartialEq, PartialOrd)]
pub struct DefaultArtist {
    name: String,
}

impl ArtistTrait for DefaultArtist {
    #[inline]
    fn get_name(&self) -> &String {
        &self.name
    }
}

impl ArtistTrait for &DefaultArtist {
    #[inline]
    fn get_name(&self) -> &String {
        &self.name
    }
}

impl Favourable<FavouriteArtist> for DefaultArtist {
    #[inline]
    fn to_favourable(&self) -> FavouriteArtist {
        FavouriteArtist::new(self.name.clone())
    }

    #[inline]
    fn into_favourable(self) -> FavouriteArtist {
        FavouriteArtist::new(self.name)
    }

    #[inline]
    fn into_self(favourable: FavouriteArtist) -> Self {
        favourable.into_default()
    }
}

impl DefaultArtist {
    #[inline]
    pub fn new(name: String) -> Self {
        Self { name }
    }
}
