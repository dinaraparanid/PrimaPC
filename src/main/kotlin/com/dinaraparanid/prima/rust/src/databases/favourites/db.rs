extern crate diesel;
extern crate diesel_migrations;
extern crate dotenv;

use diesel::{Connection, SqliteConnection};
use diesel_migrations::embed_migrations;
use dotenv::dotenv;
use std::env::{var, VarError};

embed_migrations!();

#[inline]
pub fn establish_connection() -> Result<SqliteConnection, VarError> {
    dotenv().ok();
    let db_url = var("FAVOURITE_DB_URL")?;
    Ok(SqliteConnection::establish(db_url.as_str()).unwrap())
}
