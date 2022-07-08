pub trait DBEntity {
    type PrimaryKey;

    fn get_key(&self) -> &Self::PrimaryKey;
}
