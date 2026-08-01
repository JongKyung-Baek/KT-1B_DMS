-- Move user-domain administration screens below the active user-management
-- root. PostgreSQL 17+, safe to run repeatedly on an existing database.

\set ON_ERROR_STOP on

BEGIN;

UPDATE docs_menu
   SET parent_menu_cd = 'MENU_071',
       menu_level = '2',
       sort_seq = CASE menu_cd
           WHEN 'MENU_230' THEN 91
           WHEN 'MENU_231' THEN 92
           WHEN 'MENU_222' THEN 93
       END,
       tree_type = 'leaf',
       use_yn = 'Y',
       del_yn = 'N'
 WHERE menu_cd IN ('MENU_230', 'MENU_231', 'MENU_222');

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
          FROM docs_menu
         WHERE menu_cd = 'MENU_071'
           AND parent_menu_cd = 'ROOT'
           AND tree_type = 'root'
           AND use_yn = 'Y'
           AND del_yn = 'N'
    ) THEN
        RAISE EXCEPTION 'The active user-management root is missing.';
    END IF;

    IF (
        SELECT COUNT(*)
          FROM docs_menu
         WHERE (menu_cd, sort_seq) IN (
             ('MENU_230', 91),
             ('MENU_231', 92),
             ('MENU_222', 93)
         )
           AND parent_menu_cd = 'MENU_071'
           AND menu_level = '2'
           AND tree_type = 'leaf'
           AND use_yn = 'Y'
           AND del_yn = 'N'
    ) <> 3 THEN
        RAISE EXCEPTION 'User-management child menu relocation is incomplete.';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM docs_menu
         WHERE menu_cd = 'MENU_230'
           AND role_cd = 'ROLE_MENU_230'
           AND menu_url = '/general/organizationmanage/partner/**'
    ) OR NOT EXISTS (
        SELECT 1 FROM docs_menu
         WHERE menu_cd = 'MENU_231'
           AND role_cd = 'ROLE_MENU_231'
           AND menu_url = '/general/distribution/account-requests/'
    ) OR NOT EXISTS (
        SELECT 1 FROM docs_menu
         WHERE menu_cd = 'MENU_222'
           AND role_cd = 'ROLE_MENU_222'
           AND menu_url = '/general/system/securityaccess/'
    ) THEN
        RAISE EXCEPTION 'A relocated menu URL or role code changed unexpectedly.';
    END IF;
END
$$;

COMMIT;
