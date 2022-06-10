#[derive(Debug, Copy, Clone)]
pub enum Comparator {
    TITLE,
    ARTIST,
    ALBUM,
    DATE,
}

#[derive(Debug, Copy, Clone)]
pub enum Ord {
    ASC,
    DESC,
}

#[derive(Debug, Default, Copy, Clone)]
pub struct TrackOrder {
    pub comparator: Comparator,
    pub order: Ord,
}

impl Default for Comparator {
    #[inline]
    fn default() -> Self {
        Self::TITLE
    }
}

impl Default for Ord {
    #[inline]
    fn default() -> Self {
        Self::ASC
    }
}

impl TrackOrder {
    #[inline]
    pub fn new(comparator: Comparator, order: Ord) -> Self {
        Self { comparator, order }
    }
}
