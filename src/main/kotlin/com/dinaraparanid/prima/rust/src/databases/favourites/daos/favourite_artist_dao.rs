extern crate diesel;

use crate::{
    databases::favourites::schema::favourite_artists::{
        dsl, dsl::favourite_artists as artists_dsl,
    },
    entities::artists::favourite_artist::FavouriteArtist,
    EntityDao,
};

use crate::entities::artists::artist_trait::ArtistTrait;
use diesel::{prelude::*, SqliteConnection};

pub struct FavouriteArtistDao;

impl EntityDao<String, FavouriteArtist> for FavouriteArtistDao {
    #[inline]
    fn get_all(conn: &SqliteConnection) -> Vec<FavouriteArtist> {
        artists_dsl.load(conn).unwrap_or(vec![])
    }

    #[inline]
    fn get_by_key(key: String, conn: &SqliteConnection) -> Option<FavouriteArtist> {
        artists_dsl.find(key).first(conn).ok()
    }

    #[inline]
    fn insert(entities: Vec<FavouriteArtist>, conn: &SqliteConnection) {
        diesel::insert_into(artists_dsl)
            .values(entities)
            .execute(conn)
            .unwrap_or_default();
    }

    #[inline]
    fn remove(entities: Vec<FavouriteArtist>, conn: &SqliteConnection) {
        entities.into_iter().for_each(|artist| {
            diesel::delete(artists_dsl.filter(dsl::name.eq(artist.into_string())))
                .execute(conn)
                .unwrap_or_default();
        })
    }

    #[inline]
    fn update(new_entities: Vec<FavouriteArtist>, conn: &SqliteConnection) {
        new_entities.into_iter().for_each(|artist| {
            diesel::update(artists_dsl.filter(dsl::name.eq(artist.get_name().clone())))
                .set(artist)
                .execute(conn)
                .unwrap_or_default();
        })
    }
}
