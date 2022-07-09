extern crate chrono;
extern crate diesel;
extern crate jni;
extern crate serde;

use crate::{
    databases::{
        db_entity::DBEntity,
        entity_dao::EntityDao,
        favourites::schema::{
            favourite_tracks, favourite_tracks::dsl,
            favourite_tracks::dsl::favourite_tracks as tracks_dsl,
        },
    },
    entities::tracks::favourite_track::FavouriteTrack,
    utils::extensions::path_buf_ext::PathBufExt,
};

use chrono::{DateTime, Duration};
use diesel::{prelude::*, SqliteConnection};
use jni::sys::jshort;
use serde::{Deserialize, Serialize};
use std::path::PathBuf;

#[derive(Clone, Debug, Deserialize, Serialize, Queryable, Insertable, AsChangeset)]
#[table_name = "favourite_tracks"]
pub(crate) struct FavouriteTrackDBEntity {
    pub title: Option<String>,
    pub artist: Option<String>,
    pub album: Option<String>,
    pub path: String,
    pub duration: i64,
    pub add_date: i64,
    pub number_in_album: i32,
}

pub struct FavouriteTrackDao;

impl DBEntity for FavouriteTrackDBEntity {
    type PrimaryKey = String;

    #[inline]
    fn get_key(&self) -> &String {
        &self.path
    }
}

impl From<FavouriteTrackDBEntity> for FavouriteTrack {
    #[inline]
    fn from(entity: FavouriteTrackDBEntity) -> Self {
        let path = PathBuf::from(entity.path);

        Self::new(
            entity.title,
            entity.artist,
            entity.album,
            path.clone(),
            Duration::milliseconds(entity.duration),
            DateTime::from(std::fs::metadata(path).unwrap().created().unwrap()),
            entity.number_in_album as jshort,
        )
    }
}

impl From<FavouriteTrack> for FavouriteTrackDBEntity {
    #[inline]
    fn from(track: FavouriteTrack) -> Self {
        track.into_db_entity()
    }
}

impl EntityDao<String, FavouriteTrackDBEntity> for FavouriteTrackDao {
    #[inline]
    fn get_all(conn: &SqliteConnection) -> Vec<FavouriteTrackDBEntity> {
        tracks_dsl.load(conn).unwrap_or(vec![])
    }

    #[inline]
    fn get_by_key(key: String, conn: &SqliteConnection) -> Option<FavouriteTrackDBEntity> {
        tracks_dsl.find(key).first(conn).ok()
    }

    #[inline]
    fn insert(entities: Vec<FavouriteTrackDBEntity>, conn: &SqliteConnection) {
        diesel::insert_into(tracks_dsl)
            .values(entities)
            .execute(conn)
            .unwrap_or_default();
    }

    #[inline]
    fn remove(entities: Vec<FavouriteTrackDBEntity>, conn: &SqliteConnection) {
        entities.into_iter().for_each(|t| {
            diesel::delete(tracks_dsl.filter(dsl::path.eq(t.path)))
                .execute(conn)
                .unwrap_or_default();
        });
    }

    #[inline]
    fn update(new_entities: Vec<FavouriteTrackDBEntity>, conn: &SqliteConnection) {
        new_entities.into_iter().for_each(|t| {
            diesel::update(tracks_dsl.filter(dsl::path.eq(t.path.clone())))
                .set(t)
                .execute(conn)
                .unwrap_or_default();
        })
    }
}

impl EntityDao<PathBuf, FavouriteTrack> for FavouriteTrackDao {
    #[inline]
    fn get_all(conn: &SqliteConnection) -> Vec<FavouriteTrack> {
        let entities: Vec<FavouriteTrackDBEntity> = FavouriteTrackDao::get_all(conn);
        entities.into_iter().map(FavouriteTrack::from).collect()
    }

    #[inline]
    fn get_by_key(key: PathBuf, conn: &SqliteConnection) -> Option<FavouriteTrack> {
        FavouriteTrackDao::get_by_key(key.to_string(), conn).map(FavouriteTrack::from)
    }

    #[inline]
    fn insert(entities: Vec<FavouriteTrack>, conn: &SqliteConnection) {
        FavouriteTrackDao::insert(
            entities
                .into_iter()
                .map(FavouriteTrackDBEntity::from)
                .collect(),
            conn,
        )
    }

    #[inline]
    fn remove(entities: Vec<FavouriteTrack>, conn: &SqliteConnection) {
        FavouriteTrackDao::remove(
            entities
                .into_iter()
                .map(FavouriteTrackDBEntity::from)
                .collect(),
            conn,
        )
    }

    #[inline]
    fn update(new_entities: Vec<FavouriteTrack>, conn: &SqliteConnection) {
        FavouriteTrackDao::update(
            new_entities
                .into_iter()
                .map(FavouriteTrackDBEntity::from)
                .collect(),
            conn,
        )
    }
}
