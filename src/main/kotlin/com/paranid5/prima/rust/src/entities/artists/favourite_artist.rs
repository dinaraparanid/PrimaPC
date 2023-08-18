extern crate diesel;
extern crate serde;

use crate::{
    databases::{db_entity::DBEntity, favourites::schema::favourite_artists},
    entities::artists::default_artist::DefaultArtist,
    impl_artist_traits, Favourable,
};

use serde::{Deserialize, Serialize};

#[derive(
    Debug, Clone, PartialEq, PartialOrd, Deserialize, Serialize, Queryable, Insertable, AsChangeset,
)]
#[diesel(table_name = favourite_artists)]
pub struct FavouriteArtist {
    name: String,
}

impl_artist_traits!(FavouriteArtist);

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
