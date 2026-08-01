-- KT-1B TDMS route migration
-- PostgreSQL 17
--
-- Converts the retained application routes from the retired portal root to
-- /general after external/unused menus have been removed. Historical audit
-- rows are intentionally not rewritten because they describe events that
-- occurred under the route that was valid at that time.

\set ON_ERROR_STOP on

BEGIN;

UPDATE docs_menu
   SET menu_url = CASE
       WHEN menu_url = '/inside' THEN '/general'
       ELSE regexp_replace(menu_url, '^/inside/', '/general/')
   END
 WHERE menu_url = '/inside'
    OR menu_url LIKE '/inside/%';

UPDATE docs_role_mapping
   SET menu_url = CASE
       WHEN menu_url = '/inside' THEN '/general'
       ELSE regexp_replace(menu_url, '^/inside/', '/general/')
   END
 WHERE menu_url = '/inside'
    OR menu_url LIKE '/inside/%';

UPDATE docs_form_info
   SET search_url = CASE
       WHEN search_url = '/inside' THEN '/general'
       ELSE regexp_replace(search_url, '^/inside/', '/general/')
   END
 WHERE search_url = '/inside'
    OR search_url LIKE '/inside/%';

UPDATE docs_system_config
   SET system_config_desc = 'TDMS 내부 문서파일 기초 자료'
 WHERE system_config_cd = 'ADAP_DOC_FILE_PATH';

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM docs_menu
         WHERE menu_url = '/inside' OR menu_url LIKE '/inside/%'
    ) OR EXISTS (
        SELECT 1 FROM docs_role_mapping
         WHERE menu_url = '/inside' OR menu_url LIKE '/inside/%'
    ) OR EXISTS (
        SELECT 1 FROM docs_form_info
         WHERE search_url = '/inside' OR search_url LIKE '/inside/%'
    ) THEN
        RAISE EXCEPTION 'A retired application route remains after migration.';
    END IF;

    IF EXISTS (
        SELECT 1 FROM docs_menu
         WHERE menu_url IN ('/inside/**', '/general/**')
    ) OR EXISTS (
        SELECT 1 FROM docs_role_mapping
         WHERE menu_url IN ('/inside/**', '/general/**')
    ) THEN
        RAISE EXCEPTION 'A broad application root authority remains after migration.';
    END IF;
END
$$;

COMMIT;
