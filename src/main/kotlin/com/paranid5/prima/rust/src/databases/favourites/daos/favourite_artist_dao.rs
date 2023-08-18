use crate::{
    databases::favourites::schema::favourite_artists::{
        dsl, dsl::favourite_artists as artists_dsl,
    },
    entities::artists::{artist_trait::ArtistTrait, favourite_artist::FavouriteArtist},
    impl_dao,
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
