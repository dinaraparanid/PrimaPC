extern crate chrono;
extern crate diesel;
extern crate jni;
extern crate serde;

use crate::{
    data::{
        databases::favourites::schema::{
            favourite_tracks, favourite_tracks::dsl,
            favourite_tracks::dsl::favourite_tracks as tracks_dsl,
        },
        utils::extensions::path_buf_ext::PathBufExt,
    },
    impl_dao, DBEntity, EntityDao, FavouriteTrack,
};

use chrono::{DateTime, Duration};
use diesel::SqliteConnection;
use jni::sys::jshort;
use serde::{Deserialize, Serialize};
use std::path::PathBuf;

#[derive(Clone, Debug, Deserialize, Serialize, Queryable, Insertable, AsChangeset)]
#[diesel(table_name = favourite_tracks)]
pub(crate) struct FavouriteTrackDBEntity {
    title: Option<String>,
    artist: Option<String>,
    album: Option<String>,
    path: String,
    duration: i64,
    add_date: i64,
    number_in_album: i32,
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

impl FavouriteTrackDBEntity {
    #[inline]
    pub fn new(
        title: Option<String>,
        artist: Option<String>,
        album: Option<String>,
        path: String,
        duration: i64,
        add_date: i64,
        number_in_album: i32,
    ) -> Self {
        Self {
            title,
            artist,
            album,
            path,
            duration,
            add_date,
            number_in_album,
        }
    }
}

impl_dao!(
    String,
    path,
    |t: FavouriteTrackDBEntity| t.path,
    |t: &FavouriteTrackDBEntity| t.path.clone(),
    FavouriteTrackDBEntity,
    FavouriteTrackDao,
    tracks_dsl
);

impl EntityDao<PathBuf, FavouriteTrack> for FavouriteTrackDao {
    #[inline]
    fn get_all(conn: &mut SqliteConnection) -> Vec<FavouriteTrack> {
        let entities: Vec<FavouriteTrackDBEntity> = FavouriteTrackDao::get_all(conn);
        entities.into_iter().map(FavouriteTrack::from).collect()
    }

    #[inline]
    fn get_by_key(key: PathBuf, conn: &mut SqliteConnection) -> Option<FavouriteTrack> {
        FavouriteTrackDao::get_by_key(key.to_string(), conn).map(FavouriteTrack::from)
    }

    #[inline]
    fn insert(entities: Vec<FavouriteTrack>, conn: &mut SqliteConnection) {
        FavouriteTrackDao::insert(
            entities
                .into_iter()
                .map(FavouriteTrackDBEntity::from)
                .collect(),
            conn,
        )
    }

    #[inline]
    fn remove(entities: Vec<FavouriteTrack>, conn: &mut SqliteConnection) {
        FavouriteTrackDao::remove(
            entities
                .into_iter()
                .map(FavouriteTrackDBEntity::from)
                .collect(),
            conn,
        )
    }

    #[inline]
    fn update(new_entities: Vec<FavouriteTrack>, conn: &mut SqliteConnection) {
        FavouriteTrackDao::update(
            new_entities
                .into_iter()
                .map(FavouriteTrackDBEntity::from)
                .collect(),
            conn,
        )
    }
}
