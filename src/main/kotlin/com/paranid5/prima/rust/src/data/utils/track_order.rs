extern crate jni;
extern crate yaml_rust;

use jni::sys::jint;
use yaml_rust::{yaml::Hash, Yaml};

#[derive(Debug, Copy, Clone, Eq, PartialEq)]
pub enum Comparator {
    Title,
    Artist,
    Album,
    Date,
    NumberInAlbum,
}

#[derive(Debug, Copy, Clone, Eq, PartialEq)]
pub enum Ord {
    Asc,
    Desc,
}

#[derive(Debug, Default, Copy, Clone, Eq, PartialEq)]
pub struct TrackOrder {
    pub comparator: Comparator,
    pub order: Ord,
}

impl Default for Comparator {
    #[inline]
    fn default() -> Self {
        Self::Title
    }
}

impl From<jint> for Comparator {
    #[inline]
    fn from(comparator: jint) -> Self {
        match comparator {
            0 => Comparator::Title,
            1 => Comparator::Artist,
            2 => Comparator::Album,
            3 => Comparator::Date,
            4 => Comparator::NumberInAlbum,
            _ => unreachable!(),
        }
    }
}

impl From<i64> for Comparator {
    #[inline]
    fn from(comparator: i64) -> Self {
        match comparator {
            0 => Comparator::Title,
            1 => Comparator::Artist,
            2 => Comparator::Album,
            3 => Comparator::Date,
            4 => Comparator::NumberInAlbum,
            _ => unreachable!(),
        }
    }
}

impl From<Comparator> for jint {
    #[inline]
    fn from(comparator: Comparator) -> Self {
        match comparator {
            Comparator::Title => 0,
            Comparator::Artist => 1,
            Comparator::Album => 2,
            Comparator::Date => 3,
            Comparator::NumberInAlbum => 4,
        }
    }
}

impl From<Comparator> for i64 {
    #[inline]
    fn from(comparator: Comparator) -> Self {
        match comparator {
            Comparator::Title => 0,
            Comparator::Artist => 1,
            Comparator::Album => 2,
            Comparator::Date => 3,
            Comparator::NumberInAlbum => 4,
        }
    }
}

impl Default for Ord {
    #[inline]
    fn default() -> Self {
        Self::Asc
    }
}

impl From<jint> for Ord {
    #[inline]
    fn from(order: jint) -> Self {
        match order {
            0 => Ord::Asc,
            1 => Ord::Desc,
            _ => unreachable!(),
        }
    }
}

impl From<i64> for Ord {
    #[inline]
    fn from(order: i64) -> Self {
        match order {
            0 => Ord::Asc,
            1 => Ord::Desc,
            _ => unreachable!(),
        }
    }
}

impl From<Ord> for jint {
    #[inline]
    fn from(order: Ord) -> Self {
        match order {
            Ord::Asc => 0,
            Ord::Desc => 1,
        }
    }
}

impl From<Ord> for i64 {
    #[inline]
    fn from(order: Ord) -> Self {
        match order {
            Ord::Asc => 0,
            Ord::Desc => 1,
        }
    }
}

impl TrackOrder {
    #[inline]
    pub fn new(comparator: Comparator, order: Ord) -> Self {
        Self { comparator, order }
    }
}

impl From<TrackOrder> for Yaml {
    #[inline]
    fn from(track_order: TrackOrder) -> Self {
        let mut order = Hash::new();

        order.insert(
            Self::String("comparator".to_string()),
            Self::Integer(track_order.comparator.into()),
        );

        order.insert(
            Self::String("order".to_string()),
            Self::Integer(track_order.order.into()),
        );

        Self::Hash(order)
    }
}

impl From<&Hash> for TrackOrder {
    #[inline]
    fn from(hash: &Hash) -> Self {
        let comparator = hash
            .get(&Yaml::String("comparator".to_string()))
            .map(|yml| yml.as_i64().map(|cmp| Comparator::from(cmp)))
            .flatten()
            .unwrap_or(Comparator::default());

        let order = hash
            .get(&Yaml::String("order".to_string()))
            .map(|yml| yml.as_i64().map(|ord| Ord::from(ord)))
            .flatten()
            .unwrap_or(Ord::default());

        Self::new(comparator, order)
    }
}
