DROP KEYSPACE IF EXISTS cswkotlin;

CREATE KEYSPACE IF NOT EXISTS cswkotlin
    WITH replication = {'class':'NetworkTopologyStrategy', 'datacenter1':2}
     AND durable_writes = true;

CREATE TABLE IF NOT EXISTS cswkotlin.users
(
    id            uuid PRIMARY KEY,
    email         text,
    password      text,
    nickname      text,
    profile_image text,
    phone_number  text,
    created_at    timestamp,
    updated_at    timestamp,
    last_login    timestamp,
    authorities   text
);
