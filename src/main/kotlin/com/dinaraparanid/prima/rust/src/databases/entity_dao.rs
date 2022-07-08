use crate::databases::db_entity::DBEntity;

pub trait EntityDao<PK, T>
where
    T: DBEntity<PrimaryKey = PK>,
{
    fn get_all(&self) -> Vec<T>;
    fn get_by_key(&self, key: PK) -> Option<T>;
    fn insert(&self, entities: Vec<T>);
    fn remove(&self, entities: Vec<T>);
    fn update(&self, new_entities: Vec<T>);
}
