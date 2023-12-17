CREATE KEYSPACE IF NOT EXISTS cswkotlin
    WITH replication = {'class':'NetworkTopologyStrategy', 'datacenter1':2}
     AND durable_writes = true;

DROP TABLE IF EXISTS cswkotlin.users;

CREATE TABLE IF NOT EXISTS cswkotlin.users
(
    id           uuid PRIMARY KEY,
    email        text,
    password     text,
    name         text,
    provider     text,
    picture      text,
    phone_number text,
    created_at   timestamp,
    updated_at   timestamp,
    last_login   timestamp,
    permissions  set<text>
);

DROP TABLE IF EXISTS cswkotlin.opaque_tokens;

CREATE TABLE IF NOT EXISTS cswkotlin.opaque_tokens
(
    id           uuid PRIMARY KEY,
    username     text,
    provider     text,
    opaque_token text,
    created_at   timestamp,
    updated_at   timestamp,
    expires_at   timestamp,
);
