extern crate serde;

use crate::{
    databases::{db_entity::DBEntity, favourites::schema::favourite_artists},
    entities::artists::{artist_trait::ArtistTrait, default_artist::DefaultArtist},
    Favourable,
};

use serde::{Deserialize, Serialize};

#[derive(
    Debug, Clone, PartialEq, PartialOrd, Deserialize, Serialize, Queryable, Insertable, AsChangeset,
)]
#[table_name = "favourite_artists"]
pub struct FavouriteArtist {
    name: String,
}

impl ArtistTrait for FavouriteArtist {
    #[inline]
    fn get_name(&self) -> &String {
        &self.name
    }
}

impl ArtistTrait for &FavouriteArtist {
    #[inline]
    fn get_name(&self) -> &String {
        &self.name
    }
}

impl DBEntity for FavouriteArtist {
    type PrimaryKey = String;

    #[inline]
    fn get_key(&self) -> &String {
        &self.name
    }
}

impl DBEntity for &FavouriteArtist {
    type PrimaryKey = String;

    #[inline]
    fn get_key(&self) -> &String {
        &self.name
    }
}

impl From<DefaultArtist> for FavouriteArtist {
    #[inline]
    fn from(artist: DefaultArtist) -> Self {
        artist.into_favourable()
    }
}

impl FavouriteArtist {
    #[inline]
    pub fn new(name: String) -> Self {
        Self { name }
    }

    #[inline]
    pub fn into_default(self) -> DefaultArtist {
        DefaultArtist::new(self.name)
    }

    #[inline]
    pub fn into_string(self) -> String {
        self.name
    }
}
