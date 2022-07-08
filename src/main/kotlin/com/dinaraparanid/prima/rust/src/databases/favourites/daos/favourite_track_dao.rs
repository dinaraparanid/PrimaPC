extern crate chrono;
extern crate diesel;
extern crate jni;
extern crate serde;

use crate::databases::{
    db_entity::DBEntity,
    entity_dao::EntityDao,
    favourites::schema::{
        favourite_tracks, favourite_tracks::dsl,
        favourite_tracks::dsl::favourite_tracks as tracks_dsl,
    },
};

use diesel::{prelude::*, SqliteConnection};
use serde::{Deserialize, Serialize};
use std::rc::Rc;

#[derive(Clone, Debug, Deserialize, Serialize, Queryable, Insertable, AsChangeset)]
#[table_name = "favourite_tracks"]
pub struct FavouriteTrackDBEntity {
    pub title: Option<String>,
    pub artist: Option<String>,
    pub album: Option<String>,
    pub path: String,
    pub duration: i64,
    pub add_date: i64,
    pub number_in_album: i32,
}

pub struct FavouriteTrackDao {
    connection: Rc<SqliteConnection>,
}

impl DBEntity for FavouriteTrackDBEntity {
    type PrimaryKey = String;

    #[inline]
    fn get_key(&self) -> &String {
        &self.path
    }
}

impl EntityDao<String, FavouriteTrackDBEntity> for FavouriteTrackDao {
    #[inline]
    fn get_all(&self) -> Vec<FavouriteTrackDBEntity> {
        tracks_dsl.load(self.connection.as_ref()).unwrap_or(vec![])
    }

    #[inline]
    fn get_by_key(&self, key: String) -> Option<FavouriteTrackDBEntity> {
        tracks_dsl.find(key).first(self.connection.as_ref()).ok()
    }

    #[inline]
    fn insert(&self, entities: Vec<FavouriteTrackDBEntity>) {
        diesel::insert_into(tracks_dsl)
            .values(entities)
            .execute(self.connection.as_ref())
            .unwrap_or_default();
    }

    #[inline]
    fn remove(&self, entities: Vec<FavouriteTrackDBEntity>) {
        entities.into_iter().for_each(|t| {
            diesel::delete(tracks_dsl.filter(dsl::path.eq(t.path)))
                .execute(self.connection.as_ref())
                .unwrap_or_default();
        });
    }

    #[inline]
    fn update(&self, new_entities: Vec<FavouriteTrackDBEntity>) {
        new_entities.into_iter().for_each(|t| {
            diesel::update(tracks_dsl.filter(dsl::path.eq(t.path.clone())))
                .set(t)
                .execute(self.connection.as_ref())
                .unwrap_or_default();
        })
    }
}
