\set ON_ERROR_STOP on

BEGIN;

-- Keep the known administrator password, but remove personal and runtime traces.
UPDATE docs_user
   SET user_nm = '관리자',
       -- Public local-demo credential: admin / esob!
       user_pwd =
           'sWR1Lx/3eov8dD3oxTx1Ww==:'
           'HQ1Qup6I4H48FIAIZdc5GEsJcy5eKHqRmxGQl7r5Q+Y=',
       email = 'admin@kt1b.local',
       first_login_dt = NULL,
       last_login_dt = NULL,
       last_login_ip = NULL,
       login_count = 0,
       pwd_update_dt = CURRENT_TIMESTAMP,
       update_uid = 'admin',
       update_dt = CURRENT_TIMESTAMP
 WHERE user_cd = 'USER_0000000001'
   AND user_id = 'admin';

-- The demo ships one harmless PDF which is used as both the main and
-- supplementary file for each sample technical document.
UPDATE docs_sw_file
   SET file_path_nm = '/data/kt1b/files/demo/file.pdf',
       file_size = 47093;

UPDATE docs_sw_sub_file
   SET file_path_nm = '/data/kt1b/files/demo/file.pdf',
       file_size = 47093,
       processing_status = 'DONE',
       processing_error = NULL,
       processed_at = COALESCE(processed_at, CURRENT_TIMESTAMP);

-- Replace every machine-specific storage location with a container-local path.
UPDATE docs_system_config
   SET system_config_value =
       '/data/kt1b/files/config/' || lower(system_config_group) || '/' ||
       lower(system_config_cd)
 WHERE system_config_cd LIKE '%PATH%';

-- Disable every external integration, legacy ActiveX/CAB address and secret.
UPDATE docs_system_config
   SET system_config_value = ''
 WHERE system_config_cd ~ '(URL|ENDPOINT|SERVER_IP|DOMAIN|HOST|EMAIL|KEY|PASSWORD|CAB)';

UPDATE docs_system_config
   SET system_config_value = ''
 WHERE COALESCE(system_config_value, '') ~*
       '(@|192[.]168|175[.]113|^[a-z]:[\\/]|\\\\|^//)';

-- Values intentionally available inside this local-only demo.
UPDATE docs_system_config
   SET system_config_value = 'http://127.0.0.1:3508/'
 WHERE system_config_group = 'SYSTEM_CONFIG'
   AND system_config_cd IN (
       'SERVER_DOMAIN_IN',
       'SERVER_URL_INSIDE'
   );

UPDATE docs_system_config
   SET system_config_value = '/data/kt1b/files/general'
 WHERE system_config_group = 'DB_INTERFACE_CONFIG'
   AND system_config_cd = 'GENERAL_FILE_PATH';

UPDATE docs_system_config
   SET system_config_value = '/data/kt1b/files/protected'
 WHERE system_config_group = 'DB_INTERFACE_CONFIG'
   AND system_config_cd = 'PROTECTED_FILE_PATH';

UPDATE docs_system_config
   SET system_config_value = '0'
 WHERE system_config_cd IN ('SMTP_PORT', 'UPDOWN_SERVER_PORT');

UPDATE docs_system_config
   SET system_config_value = 'KT1B Local Demo'
 WHERE system_config_group = 'DB_ADAP_CONFIG'
   AND system_config_cd = 'FROM_MAIL_NAME';

DO $$
DECLARE
    unsafe_config_count integer;
BEGIN
    IF (SELECT COUNT(*) FROM docs_user) <> 6 THEN
        RAISE EXCEPTION 'Expected six demo users.';
    END IF;

    IF (SELECT COUNT(*) FROM docs_dept) <> 6 THEN
        RAISE EXCEPTION 'Expected six demo departments.';
    END IF;

    IF (SELECT COUNT(*) FROM docs_sw) <> 16
       OR (SELECT COUNT(*) FROM docs_sw_file) <> 16
       OR (SELECT COUNT(*) FROM docs_sw_sub_file) <> 16 THEN
        RAISE EXCEPTION 'Expected sixteen documents with one main and one supplementary file.';
    END IF;

    SELECT COUNT(*)
      INTO unsafe_config_count
      FROM docs_system_config
     WHERE COALESCE(system_config_value, '') ~*
           '(@|192[.]168|175[.]113|^[a-z]:[\\/]|\\\\|^//)';

    IF unsafe_config_count <> 0 THEN
        RAISE EXCEPTION 'Unsafe system configuration values remain: %',
            unsafe_config_count;
    END IF;

    IF EXISTS (
        SELECT 1
          FROM docs_sw_file
         WHERE file_path_nm <> '/data/kt1b/files/demo/file.pdf'
    ) OR EXISTS (
        SELECT 1
          FROM docs_sw_sub_file
         WHERE file_path_nm <> '/data/kt1b/files/demo/file.pdf'
    ) THEN
        RAISE EXCEPTION 'A non-portable demo file path remains.';
    END IF;
END
$$;

COMMIT;
