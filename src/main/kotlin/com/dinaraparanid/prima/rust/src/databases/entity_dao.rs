extern crate diesel;

use crate::databases::db_entity::DBEntity;
use diesel::SqliteConnection;

pub trait EntityDao<PK, T>
where
    T: DBEntity<PrimaryKey = PK>,
{
    fn get_all(conn: &SqliteConnection) -> Vec<T>;
    fn get_by_key(key: PK, conn: &SqliteConnection) -> Option<T>;
    fn insert(entities: Vec<T>, conn: &SqliteConnection);
    fn remove(entities: Vec<T>, conn: &SqliteConnection);
    fn update(new_entities: Vec<T>, conn: &SqliteConnection);
}
