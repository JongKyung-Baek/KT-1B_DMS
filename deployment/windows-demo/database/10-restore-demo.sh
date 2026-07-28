#!/usr/bin/env bash
set -Eeuo pipefail

backup_file="/docker-entrypoint-initdb.d/kt1b-demo.backup"

if [[ ! -s "${backup_file}" ]]; then
  echo "Demo database backup is missing: ${backup_file}" >&2
  exit 1
fi

echo "Restoring the sanitized KT-1B sample database..."
pg_restore \
  --username="${POSTGRES_USER}" \
  --dbname="${POSTGRES_DB}" \
  --no-owner \
  --no-privileges \
  --exit-on-error \
  --single-transaction \
  "${backup_file}"

echo "KT-1B sample database restore completed."

