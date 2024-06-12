// Create keyspace
CREATE KEYSPACE IF NOT EXISTS songs_platform_db WITH REPLICATION = { 'class' : 'NetworkTopologyStrategy', 'datacenter1': 1 };
        
// Create user table
CREATE TABLE IF NOT EXISTS songs_platform_db.users (
        username text,
        email text,
        pass text,
        created_at timestamp,
        updated_at timestamp,
        PRIMARY KEY(email, username)
);

// Create refresh tokens by user table
CREATE TABLE IF NOT EXISTS songs_platform_db.refresh_tokens_by_user (
        username text,
        refresh_token text,
        revoked boolean,
        created_at timestamp,
        updated_at timestamp,
        PRIMARY KEY (refresh_token, username)
);

// Create songs by user table
CREATE TABLE IF NOT EXISTS songs_platform_db.songs_by_user (
        username text,
        song_id text,
        song_poster_id text,
        song_title text,
        created_at timestamp,
        updated_at timestamp,
        PRIMARY KEY (song_id, username)
);

// Create playlists by user table
CREATE TABLE IF NOT EXISTS songs_platform_db.playlists_by_user (
        username text,
        playlist_title text,
        playlist_desc text,
        playlist_poster_id text,
        is_public boolean,
        created_at timestamp,
        updated_at timestamp,
        PRIMARY KEY (playlist_title, username)
);

// Create songs by playlist table
CREATE TABLE IF NOT EXISTS songs_platform_db.songs_by_playlist (
        playlist_title text,
        playlist_poster_id text,
        song_id text,
        song_title text,
        created_at timestamp,
        updated_at timestamp,
        PRIMARY KEY (song_id, playlist_title)
);
