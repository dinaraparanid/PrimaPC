pub trait Favourable<T> {
    fn to_favourable(&self) -> T;
    fn into_favourable(self) -> T;
    fn into_self(favourable: T) -> Self;
}
