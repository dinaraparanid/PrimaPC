/// Gets value that matches borders
///
/// # Parameters
/// **v** - value itself
///
/// **min** - minimum
///
/// **max** - maximum
///
/// # Returns
/// **v* itself if it's in [min..max] or min / max if not
#[macro_export]
macro_rules! get_in_borders {
    ($v:tt, $min:tt, $max:tt, $min_path:path, $max_path:path) => {
        $min_path($max, $max_path($min, $v))
    };
}

#[macro_export]
macro_rules! impl_track_traits {
    ($track_type:ty) => {
        impl crate::data::entities::tracks::track_trait::TrackTrait for $track_type {
            #[inline]
            fn get_title(&self) -> Option<&String> {
                self.title.as_ref()
            }

            #[inline]
            fn get_artist(&self) -> Option<&String> {
                self.artist.as_ref()
            }

            #[inline]
            fn get_album(&self) -> Option<&String> {
                self.album.as_ref()
            }

            #[inline]
            fn get_path(&self) -> &std::path::PathBuf {
                &self.path
            }

            #[inline]
            fn get_duration(&self) -> &chrono::Duration {
                &self.duration
            }

            #[inline]
            fn get_add_date(&self) -> &chrono::DateTime<chrono::Local> {
                &self.add_date
            }

            #[inline]
            fn get_number_in_album(&self) -> i16 {
                self.number_in_album
            }
        }

        impl crate::data::entities::tracks::track_trait::TrackTrait for &$track_type {
            #[inline]
            fn get_title(&self) -> Option<&String> {
                self.title.as_ref()
            }

            #[inline]
            fn get_artist(&self) -> Option<&String> {
                self.artist.as_ref()
            }

            #[inline]
            fn get_album(&self) -> Option<&String> {
                self.album.as_ref()
            }

            #[inline]
            fn get_path(&self) -> &std::path::PathBuf {
                &self.path
            }

            #[inline]
            fn get_duration(&self) -> &chrono::Duration {
                &self.duration
            }

            #[inline]
            fn get_add_date(&self) -> &chrono::DateTime<chrono::Local> {
                &self.add_date
            }

            #[inline]
            fn get_number_in_album(&self) -> i16 {
                self.number_in_album
            }
        }

        impl PartialEq for $track_type {
            #[inline]
            fn eq(&self, other: &Self) -> bool {
                self.path
                    .eq(crate::data::entities::tracks::track_trait::TrackTrait::get_path(other))
            }
        }
    };
}

#[macro_export]
macro_rules! impl_artist_traits {
    ($artist_type:ty) => {
        impl crate::data::entities::artists::artist_trait::ArtistTrait for $artist_type {
            #[inline]
            fn get_name(&self) -> &String {
                &self.name
            }
        }

        impl crate::data::entities::artists::artist_trait::ArtistTrait for &$artist_type {
            #[inline]
            fn get_name(&self) -> &String {
                &self.name
            }
        }
    };
}

#[macro_export]
macro_rules! impl_playlist_traits {
    ($playlist_type:ident) => {
        impl<T: crate::data::utils::extensions::track_ext::TrackExt> IntoIterator
            for $playlist_type<T>
        {
            type Item = T;
            type IntoIter = std::vec::IntoIter<Self::Item>;

            #[inline]
            fn into_iter(self) -> Self::IntoIter {
                self.tracks.into_iter()
            }
        }

        impl<Tr: crate::data::utils::extensions::track_ext::TrackExt> Extend<Tr>
            for $playlist_type<Tr>
        {
            #[inline]
            fn extend<T: IntoIterator<Item = Tr>>(&mut self, iter: T) {
                crate::data::entities::playlists::playlist_trait::PlaylistTrait::push_all(
                    self, iter,
                )
            }
        }

        impl<T: crate::data::utils::extensions::track_ext::TrackExt> From<$playlist_type<T>>
            for yaml_rust::Yaml
        {
            #[inline]
            fn from(playlist: $playlist_type<T>) -> Self {
                use yaml_rust::{
                    yaml::{Array, Hash},
                    Yaml,
                };

                let mut hash = Hash::new();

                hash.insert(
                    Yaml::String("current_index".to_string()),
                    Yaml::Integer(playlist.cur_ind as i64),
                );

                hash.insert(
                    Yaml::String("tracks".to_string()),
                    Yaml::Array(Array::from_iter(playlist.into_iter().map(|t| {
                        crate::data::utils::extensions::track_ext::TrackExt::to_yaml(&t)
                    }))),
                );

                Yaml::Hash(hash)
            }
        }

        impl<T: crate::data::utils::extensions::track_ext::TrackExt>
            crate::data::entities::playlists::playlist_trait::PlaylistTrait<T>
            for $playlist_type<T>
        {
            #[inline]
            fn get_title(&self) -> Option<&String> {
                self.title.as_ref()
            }

            #[inline]
            fn get_type(&self) -> crate::data::entities::playlists::playlist_type::PlaylistType {
                self.tp
            }

            #[inline]
            fn get_cur_ind(&self) -> usize {
                self.cur_ind
            }

            #[inline]
            fn get_tracks(&self) -> &Vec<T> {
                &self.tracks
            }
        }
    };
}

#[macro_export]
macro_rules! impl_playlist_methods {
    () => {
        #[inline]
        pub(in crate::data::entities::playlists) fn set_cur_ind(&mut self, new_ind: usize) {
            self.cur_ind = new_ind
        }

        #[inline]
        pub(in crate::data::entities::playlists) fn get_tracks_mut(&mut self) -> &mut Vec<T> {
            &mut self.tracks
        }
    };
}

#[macro_export]
macro_rules! impl_dao {
    ($pk_type:ty, $pk_ident:ident, $pk_getter_move:expr, $pk_getter_clone:expr, $entity_type:ty, $entity_dao_type:ty, $dsl:ident) => {
        impl crate::EntityDao<$pk_type, $entity_type> for $entity_dao_type {
            #[inline]
            fn get_all(conn: &mut diesel::SqliteConnection) -> Vec<$entity_type> {
                use diesel::prelude::*;
                $dsl.load(conn).unwrap_or_default()
            }

            #[inline]
            fn get_by_key(
                key: $pk_type,
                conn: &mut diesel::SqliteConnection,
            ) -> Option<$entity_type> {
                use diesel::prelude::*;
                $dsl.find(key).first(conn).ok()
            }

            #[inline]
            fn insert(entities: Vec<$entity_type>, conn: &mut diesel::SqliteConnection) {
                use diesel::prelude::*;
                diesel::insert_into($dsl)
                    .values(entities)
                    .execute(conn)
                    .unwrap_or_default();
            }

            #[inline]
            fn remove(entities: Vec<$entity_type>, conn: &mut diesel::SqliteConnection) {
                use diesel::prelude::*;
                entities.into_iter().for_each(|t| {
                    diesel::delete($dsl.filter(dsl::$pk_ident.eq($pk_getter_move(t))))
                        .execute(conn)
                        .unwrap_or_default();
                });
            }

            #[inline]
            fn update(new_entities: Vec<$entity_type>, conn: &mut diesel::SqliteConnection) {
                use diesel::prelude::*;
                new_entities.into_iter().for_each(|t| {
                    diesel::update($dsl.filter(dsl::$pk_ident.eq($pk_getter_clone(&t))))
                        .set(t)
                        .execute(conn)
                        .unwrap_or_default();
                })
            }
        }
    };
}
