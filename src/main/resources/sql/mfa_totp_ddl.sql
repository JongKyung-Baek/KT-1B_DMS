CREATE TABLE IF NOT EXISTS docs_user_totp (
    user_cd varchar(20) PRIMARY KEY REFERENCES docs_user(user_cd) ON DELETE CASCADE,
    encrypted_secret bytea,
    last_step bigint NOT NULL DEFAULT -1,
    failures integer NOT NULL DEFAULT 0,
    blocked_until timestamptz
);
ALTER TABLE docs_user_totp ADD COLUMN IF NOT EXISTS generation bigint NOT NULL DEFAULT 0;
