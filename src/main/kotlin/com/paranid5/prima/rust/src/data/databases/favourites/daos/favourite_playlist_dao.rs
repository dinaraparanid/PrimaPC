extern crate diesel;
extern crate serde;

use crate::{
    data::{
        databases::favourites::schema::{
            favourite_playlists, favourite_playlists::dsl,
            favourite_playlists::dsl::favourite_playlists as playlists_dsl,
        },
        entities::playlists::favourite_playlist::FavouritePlaylist,
    },
    impl_dao, DBEntity, PlaylistType, TrackExt,
};

use diesel::{prelude::*, SqliteConnection};
use serde::{Deserialize, Serialize};

#[derive(Clone, Debug, Deserialize, Serialize, Queryable, Insertable, AsChangeset)]
#[diesel(table_name = favourite_playlists)]
pub(crate) struct FavouritePlaylistDBEntity {
    id: i32,
    title: Option<String>,
    tp: i32,
}

pub struct FavouritePlaylistDao;

impl DBEntity for FavouritePlaylistDBEntity {
    type PrimaryKey = i32;

    #[inline]
    fn get_key(&self) -> &i32 {
        &self.id
    }
}

impl<T: TrackExt> From<FavouritePlaylistDBEntity> for FavouritePlaylist<T> {
    #[inline]
    fn from(entity: FavouritePlaylistDBEntity) -> Self {
        Self::new(
            entity.id,
            entity.title,
            PlaylistType::from(entity.tp),
            vec![],
            0,
        )
    }
}

impl<T: TrackExt> From<FavouritePlaylist<T>> for FavouritePlaylistDBEntity {
    #[inline]
    fn from(playlist: FavouritePlaylist<T>) -> Self {
        playlist.into_db_entity()
    }
}

impl FavouritePlaylistDBEntity {
    #[inline]
    pub fn new(id: i32, title: Option<String>, tp: i32) -> Self {
        Self { id, title, tp }
    }

    #[inline]
    pub fn get_id(&self) -> i32 {
        self.id
    }

    #[inline]
    pub fn get_title(&self) -> &Option<String> {
        &self.title
    }

    #[inline]
    pub fn get_type(&self) -> i32 {
        self.tp
    }
}

impl_dao!(
    i32,
    id,
    |p: FavouritePlaylistDBEntity| p.id,
    |p: &FavouritePlaylistDBEntity| p.id,
    FavouritePlaylistDBEntity,
    FavouritePlaylistDao,
    playlists_dsl
);

impl FavouritePlaylistDao {
    #[inline]
    pub(crate) fn get_by_title_and_type(
        title: Option<String>,
        tp: i32,
        conn: &mut SqliteConnection,
    ) -> Option<FavouritePlaylistDBEntity> {
        playlists_dsl
            .filter(dsl::title.eq(title).and(dsl::tp.eq(tp)))
            .first(conn)
            .ok()
    }
}
