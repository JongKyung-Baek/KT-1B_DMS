-- KT-1B internal-only cleanup
-- PostgreSQL 17
--
-- This migration is intentionally repeatable. It removes the retired external
-- portal/menu ACLs and external-user request schema while retaining the stable
-- internal company key referenced by existing rows.

\set ON_ERROR_STOP on

BEGIN;

CREATE TEMP TABLE kt1b_removed_menu ON COMMIT DROP AS
WITH RECURSIVE removed_menu (
    menu_cd,
    parent_menu_cd,
    role_cd,
    menu_url
) AS (
    SELECT menu.menu_cd,
           menu.parent_menu_cd,
           menu.role_cd,
           menu.menu_url
      FROM docs_menu menu
     WHERE menu.auth_site = 'E'
        OR menu.parent_menu_cd = 'E'
        OR COALESCE(menu.menu_url, '') ~* '(^|/)outside/'
        OR COALESCE(menu.menu_url, '') ~*
           '^/inside/(unregisted|outregisted)(/|$)'
        OR COALESCE(menu.menu_url, '') ~*
           '^/inside/organizationmanage/(outsideuser|approval)(/|$)'
        OR menu.menu_cd IN (
           'MENU_041', 'MENU_042', 'MENU_043', 'MENU_044', 'MENU_045',
           'MENU_046', 'MENU_047', 'MENU_048', 'MENU_049',
           'MENU_073', 'MENU_074', 'MENU_075', 'MENU_076', 'MENU_077',
           'MENU_078', 'MENU_079',
           'MENU_151', 'MENU_152', 'MENU_153', 'MENU_154',
           'MENU_155', 'MENU_156', 'MENU_157'
        )

    UNION

    SELECT child.menu_cd,
           child.parent_menu_cd,
           child.role_cd,
           child.menu_url
      FROM docs_menu child
      JOIN removed_menu parent
        ON child.parent_menu_cd = parent.menu_cd
)
SELECT DISTINCT menu_cd, parent_menu_cd, role_cd, menu_url
  FROM removed_menu;

-- Remove both generations of role data before deleting menu definitions.
-- Older dumps stored MENU_* in ROLE_CD; newer rows store ROLE_MENU_*.
DELETE FROM docs_rel_role_group assignment
 USING kt1b_removed_menu removed
 WHERE assignment.role_cd = removed.menu_cd
    OR assignment.role_cd = removed.role_cd;

DELETE FROM docs_role_mapping mapping
 USING kt1b_removed_menu removed
 WHERE mapping.menu_url = removed.menu_url;

DELETE FROM docs_role_mapping
 WHERE group_cd = 'RG_006'
    OR COALESCE(menu_url, '') ~* '(^|/)outside/'
    OR COALESCE(menu_url, '') ~*
       '^/inside/(unregisted|outregisted)(/|$)'
    OR COALESCE(menu_url, '') ~*
       '^/inside/organizationmanage/(outsideuser|approval)(/|$)';

DELETE FROM docs_rel_role_group
 WHERE group_cd = 'RG_006';

DELETE FROM docs_role_group_member
 WHERE group_code = 'RG_006';

DELETE FROM docs_menu menu
 USING kt1b_removed_menu removed
 WHERE menu.menu_cd = removed.menu_cd;

DELETE FROM docs_role_group
 WHERE group_code = 'RG_006';

-- Shared rows become ordinary internal rows. Retained external identities are
-- disabled before their marker is normalized so historical foreign-key
-- references do not force destructive user deletion.
UPDATE docs_user
   SET use_yn = 'N',
       del_yn = 'Y',
       role_group = NULL,
       auth_site = 'I'
 WHERE auth_site = 'E'
    OR role_group = 'RG_006';

UPDATE docs_user
   SET auth_site = 'I'
 WHERE auth_site = 'B'
    OR auth_site IS NULL
    OR BTRIM(auth_site) = '';

UPDATE docs_menu
   SET auth_site = 'I'
 WHERE auth_site = 'B'
    OR auth_site IS NULL
    OR BTRIM(auth_site) = '';

UPDATE docs_menu
   SET parent_menu_cd = 'I'
 WHERE parent_menu_cd = 'B';

-- Keep the established key because it is referenced throughout the dump, but
-- remove the former customer identity from the company master.
UPDATE docs_company
   SET company_nm = 'KT-1B',
       company_type = 'I',
       use_yn = 'Y',
       del_yn = 'N',
       update_user_cd = 'SYSTEM',
       update_dt = CURRENT_TIMESTAMP
 WHERE company_cd = 'COMP_0000000999';

-- Historical references may prevent a physical delete in an upgraded system.
-- Such rows are disabled and anonymized; the demo reset deletes them after it
-- removes sample business data.
UPDATE docs_company
   SET company_nm = 'REMOVED-' || company_cd,
       use_yn = 'N',
       del_yn = 'Y',
       update_user_cd = 'SYSTEM',
       update_dt = CURRENT_TIMESTAMP
 WHERE company_cd <> 'COMP_0000000999';

-- Preserve configured values while moving the two customer-specific keys to
-- neutral application keys. A pre-existing neutral value wins unless blank.
INSERT INTO docs_system_config AS target (
    system_config_group,
    system_config_cd,
    system_config_value,
    system_config_desc
)
SELECT legacy.system_config_group,
       CASE legacy.system_config_cd
           WHEN 'KAI_DOWNLOAD' THEN 'FILE_DOWNLOAD_URL'
           WHEN 'KAI_VIEW' THEN 'FILE_VIEW_URL'
       END,
       legacy.system_config_value,
       CASE legacy.system_config_cd
           WHEN 'KAI_DOWNLOAD' THEN 'File download API URL'
           WHEN 'KAI_VIEW' THEN 'File view API URL'
       END
  FROM docs_system_config legacy
 WHERE legacy.system_config_cd IN ('KAI_DOWNLOAD', 'KAI_VIEW')
ON CONFLICT (system_config_group, system_config_cd)
DO UPDATE SET
    system_config_value = CASE
        WHEN COALESCE(BTRIM(target.system_config_value), '') = ''
            THEN EXCLUDED.system_config_value
        ELSE target.system_config_value
    END,
    system_config_desc = EXCLUDED.system_config_desc;

DELETE FROM docs_system_config
 WHERE system_config_cd IN ('KAI_DOWNLOAD', 'KAI_VIEW');

-- External-user account requests and their allocators have no internal use.
DROP TABLE IF EXISTS public.docs_user_request CASCADE;
DROP TABLE IF EXISTS public.docs_user_request_number CASCADE;
DROP SEQUENCE IF EXISTS public.docs_external_user_id_sequence CASCADE;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
          FROM docs_menu
         WHERE auth_site IS DISTINCT FROM 'I'
            OR parent_menu_cd = 'E'
            OR COALESCE(menu_url, '') ~* '(^|/)outside/'
            OR COALESCE(menu_url, '') ~*
               '^/inside/(unregisted|outregisted)(/|$)'
            OR COALESCE(menu_url, '') ~*
               '^/inside/organizationmanage/(outsideuser|approval)(/|$)'
    ) THEN
        RAISE EXCEPTION 'An external menu remains after internal-only cleanup.';
    END IF;

    IF EXISTS (
        SELECT 1
          FROM docs_user
         WHERE auth_site IS DISTINCT FROM 'I'
    ) THEN
        RAISE EXCEPTION 'A non-internal user marker remains after cleanup.';
    END IF;

    IF EXISTS (
        SELECT 1
          FROM docs_role_group
         WHERE group_code = 'RG_006'
    ) OR EXISTS (
        SELECT 1
          FROM docs_rel_role_group
         WHERE group_cd = 'RG_006'
    ) THEN
        RAISE EXCEPTION 'The retired external role group remains.';
    END IF;

    IF NOT EXISTS (
        SELECT 1
          FROM docs_company
         WHERE company_cd = 'COMP_0000000999'
           AND company_nm = 'KT-1B'
           AND company_type = 'I'
           AND use_yn = 'Y'
           AND del_yn = 'N'
    ) THEN
        RAISE EXCEPTION 'The internal KT-1B company row is missing.';
    END IF;

    IF EXISTS (
        SELECT 1
          FROM docs_company
         WHERE company_cd <> 'COMP_0000000999'
           AND (use_yn <> 'N' OR del_yn <> 'Y')
    ) THEN
        RAISE EXCEPTION 'An external company remains active.';
    END IF;

    IF EXISTS (
        SELECT 1
          FROM docs_system_config
         WHERE system_config_cd IN ('KAI_DOWNLOAD', 'KAI_VIEW')
    ) THEN
        RAISE EXCEPTION 'A customer-specific file API key remains.';
    END IF;

    IF to_regclass('public.docs_user_request') IS NOT NULL
       OR to_regclass('public.docs_user_request_number') IS NOT NULL
       OR to_regclass('public.docs_external_user_id_sequence') IS NOT NULL THEN
        RAISE EXCEPTION 'External-user request database objects remain.';
    END IF;
END
$$;

COMMIT;
