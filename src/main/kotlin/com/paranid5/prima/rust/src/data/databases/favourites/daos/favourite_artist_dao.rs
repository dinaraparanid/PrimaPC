use crate::{
    data::{
        databases::favourites::schema::favourite_artists::{
            dsl, dsl::favourite_artists as artists_dsl,
        },
        entities::artists::artist_trait::ArtistTrait,
    },
    impl_dao, FavouriteArtist,
};

pub struct FavouriteArtistDao;

impl_dao!(
    String,
    name,
    |a: FavouriteArtist| a.into_string(),
    |a: &FavouriteArtist| a.get_name().clone(),
    FavouriteArtist,
    FavouriteArtistDao,
    artists_dsl
);
