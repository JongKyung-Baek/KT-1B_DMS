-- KT-1B internal-only cleanup
-- PostgreSQL 17
--
-- This migration is intentionally repeatable. It removes the retired external
-- portal/menu ACLs and external-user request schema while retaining the stable
-- internal company key referenced by existing rows.

\set ON_ERROR_STOP on

BEGIN;

-- Capture external menu markers only while upgrading the legacy dump. The
-- physical selector column is removed later, so repeat runs must not reference
-- it statically.
CREATE TEMP TABLE kt1b_legacy_external_menu (
    menu_cd varchar(32) PRIMARY KEY
) ON COMMIT DROP;

DO $capture_external_menu$
BEGIN
    IF EXISTS (
        SELECT 1
          FROM information_schema.columns
         WHERE table_schema = 'public'
           AND table_name = 'docs_menu'
           AND column_name = 'auth_site'
    ) THEN
        EXECUTE $sql$
            INSERT INTO kt1b_legacy_external_menu (menu_cd)
            SELECT menu_cd
              FROM public.docs_menu
             WHERE auth_site = 'E'
        $sql$;
    END IF;
END
$capture_external_menu$;

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
     WHERE COALESCE(menu.use_yn, 'N') <> 'Y'
        OR COALESCE(menu.del_yn, 'Y') <> 'N'
        OR menu.menu_cd IN (SELECT menu_cd FROM kt1b_legacy_external_menu)
        OR menu.parent_menu_cd = 'E'
        OR COALESCE(menu.menu_url, '') ~* '(^|/)outside/'
        OR COALESCE(menu.menu_url, '') ~*
           '^/(inside|general)/(unregisted|outregisted)(/|$)'
        OR COALESCE(menu.menu_url, '') ~*
           '^/(inside|general)/organizationmanage/(outsideuser|approval)(/|$)'
        OR menu.menu_cd IN (
           'MENU_124',
           'MENU_041', 'MENU_042', 'MENU_043', 'MENU_044', 'MENU_045',
           'MENU_046', 'MENU_047', 'MENU_048', 'MENU_049',
           'MENU_073', 'MENU_074', 'MENU_075', 'MENU_076', 'MENU_077',
           'MENU_078', 'MENU_079',
           'MENU_151', 'MENU_152', 'MENU_153', 'MENU_154',
           'MENU_155', 'MENU_156', 'MENU_157'
         )
        OR (
           menu.tree_type = 'root'
           AND menu.menu_type IN ('T', 'M', 'P')
           AND menu.menu_cd NOT IN (
               'MENU_013', 'MENU_071', 'MENU_214', 'MENU_223',
               'MENU_229'
           )
        )
        OR (
           COALESCE(BTRIM(menu.parent_menu_cd), '') IN ('', 'MENU_000')
           AND menu.menu_type IN ('T', 'M', 'P')
           AND menu.menu_cd NOT IN ('MENU_189', 'MENU_190')
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

-- MENU_124 was a legacy broad portal authority. Neither the old nor current
-- root wildcard may survive in the single-portal ACL model, including mappings
-- that do not carry the original menu role code.
DELETE FROM docs_role_mapping
 WHERE menu_url IN ('/inside/**', '/general/**');

DELETE FROM docs_role_mapping
 WHERE group_cd = 'RG_006'
    OR COALESCE(menu_url, '') ~* '(^|/)outside/'
    OR COALESCE(menu_url, '') ~*
       '^/(inside|general)/(unregisted|outregisted)(/|$)'
    OR COALESCE(menu_url, '') ~*
       '^/(inside|general)/organizationmanage/(outsideuser|approval)(/|$)';

DELETE FROM docs_rel_role_group
 WHERE group_cd = 'RG_006';

DELETE FROM docs_role_group_member
 WHERE group_code = 'RG_006';

DELETE FROM docs_menu menu
 USING kt1b_removed_menu removed
 WHERE menu.menu_cd = removed.menu_cd;

-- REL_ROLE_GROUP is a menu-role relation. Remove every historical assignment
-- that no longer resolves to one retained active menu role, including dumps
-- that stored MENU_* identifiers instead of ROLE_MENU_* identifiers.
DELETE FROM docs_rel_role_group assignment
 WHERE NOT EXISTS (
       SELECT 1
         FROM docs_menu menu
        WHERE menu.del_yn = 'N'
          AND menu.use_yn = 'Y'
          AND NULLIF(BTRIM(menu.role_cd), '') IS NOT NULL
          AND menu.role_cd = assignment.role_cd
   );

DELETE FROM docs_role_group
 WHERE group_code = 'RG_006';

-- Disable retained external identities before removing the legacy user portal
-- selector. Dynamic SQL keeps this migration repeatable after the column has
-- already been removed.
DO $cleanup_external_user$
BEGIN
    IF EXISTS (
        SELECT 1
          FROM information_schema.columns
         WHERE table_schema = 'public'
           AND table_name = 'docs_user'
           AND column_name = 'auth_site'
    ) THEN
        EXECUTE $sql$
            UPDATE public.docs_user
               SET use_yn = 'N',
                   del_yn = 'Y',
                   role_group = NULL
             WHERE auth_site = 'E'
                OR role_group = 'RG_006'
        $sql$;
    ELSE
        UPDATE public.docs_user
           SET use_yn = 'N',
               del_yn = 'Y',
               role_group = NULL
         WHERE role_group = 'RG_006';
    END IF;
END
$cleanup_external_user$;

UPDATE docs_menu
   SET parent_menu_cd = 'ROOT'
 WHERE parent_menu_cd IN ('I', 'B');

-- Keep the five current navigation roots visually distinct in every locale.
UPDATE docs_menu
   SET menu_icon = CASE menu_cd
       WHEN 'MENU_013' THEN 'tabler-file-stack'
       WHEN 'MENU_229' THEN 'tabler-package-export'
       WHEN 'MENU_071' THEN 'tabler-users-group'
       WHEN 'MENU_214' THEN 'tabler-settings'
       WHEN 'MENU_223' THEN 'tabler-history'
       END
 WHERE menu_cd IN (
       'MENU_013', 'MENU_229', 'MENU_071', 'MENU_214', 'MENU_223'
 );

-- The application now has one portal. Remove both physical selector columns
-- after their legacy values have served the one-time cleanup above.
ALTER TABLE public.docs_menu DROP COLUMN IF EXISTS auth_site;
ALTER TABLE public.docs_user DROP COLUMN IF EXISTS auth_site;

-- Two active user-management actions were orphaned in the legacy dump. Attach
-- them to the current user-management menu so assignment and runtime ACLs use
-- the same tree.
UPDATE docs_menu
   SET parent_menu_cd = 'MENU_072',
       menu_nm = CASE menu_cd
           WHEN 'MENU_189' THEN '사용자 생성'
           WHEN 'MENU_190' THEN '사용자 정보 수정'
       END,
       message_cd = CASE menu_cd
           WHEN 'MENU_189' THEN 'menu.userCreate'
           WHEN 'MENU_190' THEN 'menu.userEdit'
       END,
       menu_level = '3',
       tree_type = 'leaf',
       role_cd = 'ROLE_' || menu_cd,
       use_yn = 'Y',
       del_yn = 'N'
 WHERE menu_cd IN ('MENU_189', 'MENU_190');

UPDATE docs_menu
   SET menu_nm = CASE menu_cd
           WHEN 'MENU_072' THEN '사용자 관리'
           WHEN 'MENU_199' THEN '부서 관리'
       END
 WHERE menu_cd IN ('MENU_072', 'MENU_199');

INSERT INTO docs_lang (lang_type, lang_cd, lang_desc)
VALUES
    ('ko', 'menu.userCreate', '사용자 생성'),
    ('en', 'menu.userCreate', 'Create User'),
    ('ko', 'menu.userEdit', '사용자 정보 수정'),
    ('en', 'menu.userEdit', 'Edit User')
ON CONFLICT (lang_type, lang_cd) DO UPDATE
   SET lang_desc = EXCLUDED.lang_desc;

INSERT INTO docs_rel_role_group (
    group_cd, role_cd, insert_user_cd, update_user_cd, insert_dt, update_dt
)
SELECT 'RG_001', menu.role_cd, 'SYSTEM', 'SYSTEM',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
  FROM docs_menu menu
 WHERE menu.menu_cd IN ('MENU_189', 'MENU_190')
ON CONFLICT (group_cd, role_cd) DO NOTHING;

-- Replace the legacy portal-specific toolbar and callback names with one
-- neutral menu-administration toolbar.
INSERT INTO docs_toolbar_info (
    toolbar_id, button_id, button_seq, button_label, button_img,
    button_align, call_func, use_yn, lang_cd, button_type,
    system_class_group, role_cd
)
VALUES
    ('toolbarSystemMenu', 'btnAdd', 10, '추가', '', 'right',
     'addMenu()', 'Y', '', 'save', '', NULL),
    ('toolbarSystemMenu', 'btnEdit', 20, '수정', '', 'right',
     'modMenu()', 'Y', '', 'save', '', NULL),
    ('toolbarSystemMenu', 'btnDelete', 30, '삭제', '', 'right',
     'delMenu()', 'Y', '', 'save', '', NULL),
    ('toolbarSystemMenu', 'btnSaveOrder', 100, '메뉴순서저장', '', 'right',
     'saveMenu()', 'Y', '', 'save', '', NULL)
ON CONFLICT (toolbar_id, button_id) DO UPDATE SET
    button_seq = EXCLUDED.button_seq,
    button_label = EXCLUDED.button_label,
    button_img = EXCLUDED.button_img,
    button_align = EXCLUDED.button_align,
    call_func = EXCLUDED.call_func,
    use_yn = EXCLUDED.use_yn,
    lang_cd = EXCLUDED.lang_cd,
    button_type = EXCLUDED.button_type,
    system_class_group = EXCLUDED.system_class_group,
    role_cd = EXCLUDED.role_cd;

DELETE FROM docs_toolbar_info
 WHERE toolbar_id IN (
       'toolbarSystemInsideMenu',
       'toolbarSystemOutsideMenu',
       'toolbarSystemMenuOutside'
   );

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
         WHERE parent_menu_cd IN ('I', 'B', 'E')
            OR COALESCE(menu_url, '') ~* '(^|/)outside/'
            OR COALESCE(menu_url, '') ~*
               '^/(inside|general)/(unregisted|outregisted)(/|$)'
            OR COALESCE(menu_url, '') ~*
               '^/(inside|general)/organizationmanage/(outsideuser|approval)(/|$)'
    ) THEN
        RAISE EXCEPTION 'A legacy portal marker remains in the menu catalog.';
    END IF;

    IF (
        SELECT ARRAY_AGG(menu_cd ORDER BY menu_cd)
          FROM docs_menu
         WHERE tree_type = 'root'
           AND menu_type IN ('T', 'M', 'P')
           AND use_yn = 'Y'
           AND del_yn = 'N'
           AND parent_menu_cd = 'ROOT'
    ) IS DISTINCT FROM ARRAY[
        'MENU_013', 'MENU_071', 'MENU_214', 'MENU_223',
        'MENU_229'
    ]::varchar[] THEN
        RAISE EXCEPTION 'The active menu roots do not match the five current navigation roots.';
    END IF;

    IF EXISTS (
        WITH RECURSIVE eligible AS (
            SELECT menu_cd, parent_menu_cd
              FROM docs_menu
             WHERE del_yn = 'N'
               AND use_yn = 'Y'
               AND menu_type IN ('T', 'M', 'P')
        ), connected AS (
            SELECT menu_cd, parent_menu_cd,
                   ARRAY[menu_cd::TEXT]::TEXT[] AS path
              FROM eligible
             WHERE parent_menu_cd = 'ROOT'
            UNION ALL
            SELECT child.menu_cd, child.parent_menu_cd,
                   parent.path || child.menu_cd::TEXT
              FROM eligible child
              JOIN connected parent ON child.parent_menu_cd = parent.menu_cd
             WHERE NOT child.menu_cd::TEXT = ANY(parent.path)
        )
        SELECT 1
          FROM eligible menu
         WHERE NOT EXISTS (
               SELECT 1 FROM connected
                WHERE connected.menu_cd = menu.menu_cd
         )
    ) THEN
        RAISE EXCEPTION 'The active permission tree contains a disconnected menu.';
    END IF;

    IF EXISTS (
        SELECT 1
          FROM docs_toolbar_info
         WHERE toolbar_id IN (
               'toolbarSystemInsideMenu',
               'toolbarSystemOutsideMenu',
               'toolbarSystemMenuOutside'
           )
            OR (toolbar_id = 'toolbarSystemMenu'
                AND (button_id ~* '(inside|outside)'
                     OR call_func ~* '(inside|outside)'))
    ) THEN
        RAISE EXCEPTION 'A portal-specific menu toolbar remains.';
    END IF;

    IF EXISTS (
        SELECT 1
          FROM docs_menu
         WHERE menu_cd = 'MENU_124'
            OR menu_url IN ('/inside/**', '/general/**')
    ) OR EXISTS (
        SELECT 1
          FROM docs_role_mapping
         WHERE menu_url IN ('/inside/**', '/general/**')
    ) THEN
        RAISE EXCEPTION 'The retired broad portal ACL remains.';
    END IF;

    IF EXISTS (
        SELECT 1
          FROM docs_rel_role_group assignment
         WHERE NOT EXISTS (
               SELECT 1
                 FROM docs_menu menu
                WHERE menu.del_yn = 'N'
                  AND menu.use_yn = 'Y'
                  AND NULLIF(BTRIM(menu.role_cd), '') IS NOT NULL
                  AND menu.role_cd = assignment.role_cd
         )
    ) THEN
        RAISE EXCEPTION 'An orphan menu-role assignment remains.';
    END IF;

    IF EXISTS (
        SELECT 1
          FROM information_schema.columns
         WHERE table_schema = 'public'
           AND table_name IN ('docs_menu', 'docs_user')
           AND column_name = 'auth_site'
    ) THEN
        RAISE EXCEPTION 'A retired portal selector column remains after cleanup.';
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
