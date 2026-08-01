-- Administrator navigation for distribution-system account requests
-- PostgreSQL 17+, safe to run repeatedly.

\set ON_ERROR_STOP on

BEGIN;

INSERT INTO docs_menu (
    menu_cd, parent_menu_cd, menu_nm, message_cd, menu_level, menu_type,
    menu_url, sort_seq, tree_type, del_yn, use_yn, tooltip, menu_desc,
    role_cd, menu_icon
)
VALUES (
    'MENU_231', 'MENU_071', '배포시스템 계정요청',
    'menu.distributionAccountRequest', '2', 'M',
    '/general/distribution/account-requests/', 92, 'leaf', 'N', 'Y',
    '연계 배포시스템 계정요청 승인 및 반려',
    '자사·타사 기술자료배포시스템에서 접수한 사용자등록·잠금해제·비밀번호 초기화 요청 관리',
    'ROLE_MENU_231', ''
)
ON CONFLICT (menu_cd) DO UPDATE SET
    parent_menu_cd = EXCLUDED.parent_menu_cd,
    menu_nm = EXCLUDED.menu_nm,
    message_cd = EXCLUDED.message_cd,
    menu_level = EXCLUDED.menu_level,
    menu_type = EXCLUDED.menu_type,
    menu_url = EXCLUDED.menu_url,
    sort_seq = EXCLUDED.sort_seq,
    tree_type = EXCLUDED.tree_type,
    del_yn = 'N',
    use_yn = 'Y',
    tooltip = EXCLUDED.tooltip,
    menu_desc = EXCLUDED.menu_desc,
    role_cd = EXCLUDED.role_cd,
    menu_icon = EXCLUDED.menu_icon;

-- Decisions change security-sensitive account state in the external system,
-- so the queue is deliberately limited to the administrator group.
INSERT INTO docs_rel_role_group (
    group_cd, role_cd, insert_user_cd, update_user_cd, insert_dt, update_dt
)
VALUES ('RG_001', 'ROLE_MENU_231', 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (group_cd, role_cd) DO NOTHING;

DELETE FROM docs_rel_role_group
 WHERE role_cd = 'ROLE_MENU_231'
   AND group_cd <> 'RG_001';

INSERT INTO docs_role_mapping (group_cd, group_nm, menu_nm, menu_url, menu_type)
SELECT assignment.group_cd,
       COALESCE(role_group.group_nm, assignment.group_cd),
       menu.menu_nm,
       menu.menu_url,
       menu.menu_type::varchar
  FROM docs_rel_role_group assignment
  JOIN docs_menu menu ON menu.role_cd = assignment.role_cd
  LEFT JOIN docs_role_group role_group ON role_group.group_code = assignment.group_cd
 WHERE assignment.role_cd = 'ROLE_MENU_231'
ON CONFLICT (group_cd, menu_url) DO UPDATE SET
    group_nm = EXCLUDED.group_nm,
    menu_nm = EXCLUDED.menu_nm,
    menu_type = EXCLUDED.menu_type;

DELETE FROM docs_role_mapping
 WHERE menu_url = '/general/distribution/account-requests/'
   AND group_cd <> 'RG_001';

INSERT INTO docs_lang (lang_type, lang_cd, lang_desc)
VALUES
    ('ko', 'menu.distributionAccountRequest', '배포시스템 계정요청'),
    ('en', 'menu.distributionAccountRequest', 'Distribution Account Requests')
ON CONFLICT (lang_type, lang_cd) DO UPDATE
   SET lang_desc = EXCLUDED.lang_desc;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
          FROM docs_menu
         WHERE menu_cd = 'MENU_231'
           AND parent_menu_cd = 'MENU_071'
           AND role_cd = 'ROLE_MENU_231'
           AND menu_url = '/general/distribution/account-requests/'
           AND use_yn = 'Y'
           AND del_yn = 'N'
    ) THEN
        RAISE EXCEPTION 'Distribution account-request navigation is missing.';
    END IF;

    IF NOT EXISTS (
        SELECT 1
          FROM docs_rel_role_group
         WHERE group_cd = 'RG_001'
           AND role_cd = 'ROLE_MENU_231'
    ) OR EXISTS (
        SELECT 1
          FROM docs_rel_role_group
         WHERE group_cd <> 'RG_001'
           AND role_cd = 'ROLE_MENU_231'
    ) THEN
        RAISE EXCEPTION 'Distribution account-request menu authorization is invalid.';
    END IF;
END
$$;

COMMIT;
