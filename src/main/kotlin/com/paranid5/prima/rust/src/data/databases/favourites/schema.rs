diesel::table! {
    favourite_artists (name) {
        name -> Text,
    }
}

diesel::table! {
    favourite_playlists (id) {
        id -> Integer,
        title -> Nullable<Text>,
        tp -> Integer,
    }
}

diesel::table! {
    favourite_tracks (path) {
        title -> Nullable<Text>,
        artist -> Nullable<Text>,
        album -> Nullable<Text>,
        path -> Text,
        duration -> BigInt,
        add_date -> BigInt,
        number_in_album -> Integer,
    }
}

diesel::allow_tables_to_appear_in_same_query!(
    favourite_artists,
    favourite_playlists,
    favourite_tracks,
);
