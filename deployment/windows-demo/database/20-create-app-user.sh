#!/usr/bin/env bash
set -Eeuo pipefail

: "${APP_DB_USER:?APP_DB_USER is required}"
: "${APP_DB_PASSWORD:?APP_DB_PASSWORD is required}"

psql \
  --username="${POSTGRES_USER}" \
  --dbname="${POSTGRES_DB}" \
  --set=ON_ERROR_STOP=1 \
  --set=app_user="${APP_DB_USER}" \
  --set=app_database="${POSTGRES_DB}" \
  --set=app_password="${APP_DB_PASSWORD}" <<'SQL'
SELECT format(
    'CREATE ROLE %I LOGIN PASSWORD %L NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT',
    :'app_user',
    :'app_password'
)
WHERE NOT EXISTS (
    SELECT 1
      FROM pg_roles
     WHERE rolname = :'app_user'
)
\gexec

GRANT CONNECT ON DATABASE :"app_database" TO :"app_user";
GRANT USAGE ON SCHEMA public TO :"app_user";
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO :"app_user";
GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA public TO :"app_user";
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO :"app_user";
SQL

echo "Restricted application database user is ready."
