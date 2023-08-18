extern crate diesel;

use crate::DBEntity;
use diesel::SqliteConnection;

pub trait EntityDao<PK, T>
where
    T: DBEntity<PrimaryKey = PK>,
{
    fn get_all(conn: &mut SqliteConnection) -> Vec<T>;
    fn get_by_key(key: PK, conn: &mut SqliteConnection) -> Option<T>;
    fn insert(entities: Vec<T>, conn: &mut SqliteConnection);
    fn remove(entities: Vec<T>, conn: &mut SqliteConnection);
    fn update(new_entities: Vec<T>, conn: &mut SqliteConnection);
}
