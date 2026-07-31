#!/usr/bin/env bash
set -Eeuo pipefail

: "${KT1B_DEMO_PORT:=3508}"

if [[ ! "${KT1B_DEMO_PORT}" =~ ^[0-9]+$ ]] \
    || (( KT1B_DEMO_PORT < 1 || KT1B_DEMO_PORT > 65535 )); then
  echo "Invalid KT1B_DEMO_PORT: ${KT1B_DEMO_PORT}" >&2
  exit 1
fi

psql \
  --username="${POSTGRES_USER}" \
  --dbname="${POSTGRES_DB}" \
  --set=ON_ERROR_STOP=1 \
  --set=demo_port="${KT1B_DEMO_PORT}" <<'SQL'
UPDATE docs_system_config
   SET system_config_value =
       'http://127.0.0.1:' || :'demo_port' || '/'
 WHERE system_config_group = 'SYSTEM_CONFIG'
   AND system_config_cd IN (
       'SERVER_DOMAIN_IN',
       'SERVER_URL_INSIDE'
   );

INSERT INTO docs_system_config (
    system_config_group,
    system_config_cd,
    system_config_value,
    system_config_desc
) VALUES (
    'DEMO_CONFIG',
    'READY',
    'Y',
    'Sanitized local demo database is ready'
)
ON CONFLICT (system_config_group, system_config_cd)
DO UPDATE
      SET system_config_value = EXCLUDED.system_config_value,
          system_config_desc = EXCLUDED.system_config_desc;
SQL

echo "Demo URL configuration and readiness marker are set."
