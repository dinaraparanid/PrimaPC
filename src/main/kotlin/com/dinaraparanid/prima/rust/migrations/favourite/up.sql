CREATE TABLE IF NOT EXISTS favourite_tracks (
  title TEXT,
  artist TEXT,
  album TEXT,
  path TEXT PRIMARY KEY NOT NULL,
  duration BIGINT NOT NULL,
  add_date BIGINT NOT NULL,
  number_in_album INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS favourite_artists (name TEXT PRIMARY KEY NOT NULL);

CREATE TABLE IF NOT EXISTS favourite_playlists (
  id INTEGER PRIMARY KEY,
  title TEXT,
  tp INTEGER NOT NULL
);