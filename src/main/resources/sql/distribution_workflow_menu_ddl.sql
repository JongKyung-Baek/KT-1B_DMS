-- Technical-data distribution workflow navigation and menu authorization
-- PostgreSQL 17+
--
-- The application uses DOCS_REL_ROLE_GROUP as its normalized menu-role
-- assignment table. DOCS_ROLE_MAPPING is retained as a compatibility catalog
-- for older administration/reporting code, so both representations are kept
-- in sync here. This migration is safe to run repeatedly.

\set ON_ERROR_STOP on

BEGIN;

INSERT INTO docs_menu (
    menu_cd, parent_menu_cd, menu_nm, message_cd, menu_level, menu_type,
    menu_url, sort_seq, tree_type, del_yn, use_yn, tooltip, menu_desc,
    role_cd, menu_icon
)
VALUES
    (
        'MENU_229', 'ROOT', '기술자료배포',
        'menu.technicalDataDistribution', '1', 'T',
        '/general/distribution/workflow/', 5, 'root', 'N', 'Y',
        '기술자료 배포요청·승인·승인목록 관리',
        '기술자료 배포요청부터 승인완료 목록까지 관리',
        'ROLE_MENU_229', 'tabler-package-export'
    ),
    (
        'MENU_226', 'MENU_229', '배포요청',
        'menu.distributionMyRequests', '2', 'M',
        '/general/distribution/workflow/requests/**', 1, 'leaf', 'N', 'Y',
        '기술자료 배포요청 작성 및 진행 현황',
        '기술자료를 선택하여 배포를 요청하고 요청별 처리 현황을 조회',
        'ROLE_MENU_226', ''
    ),
    (
        'MENU_227', 'MENU_229', '배포승인',
        'menu.distributionApproval', '2', 'M',
        '/general/distribution/workflow/approval/**', 2, 'leaf', 'N', 'Y',
        '기술자료 배포요청 승인 및 반려',
        '지정 승인자가 제출된 기술자료 배포요청을 검토하여 승인 또는 반려',
        'ROLE_MENU_227', ''
    ),
    (
        'MENU_228', 'MENU_229', '승인목록',
        'menu.distributionApprovedList', '2', 'M',
        '/general/distribution/workflow/approved/**', 3, 'leaf', 'N', 'Y',
        '승인 완료된 기술자료 배포목록',
        '문서별 접근권한 범위에서 승인 완료된 배포대상 목록을 조회',
        'ROLE_MENU_228', ''
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

-- Distribution approvals use a dedicated role group so the requester and the
-- assigned approver can be separated without granting full administrator
-- authority. The group inherits the ordinary user menus and adds approval.
INSERT INTO docs_role_group (
    group_code, group_nm, group_type, query_id, use_yn,
    insert_user_cd, insert_dt, update_user_cd, update_dt, insert_order
)
VALUES (
    'RG_012', '배포승인자', 'USER', NULL, 'Y',
    'SYSTEM', CURRENT_TIMESTAMP, 'SYSTEM', CURRENT_TIMESTAMP, 12
)
ON CONFLICT (group_code) DO UPDATE SET
    group_nm = EXCLUDED.group_nm,
    group_type = EXCLUDED.group_type,
    use_yn = 'Y',
    update_user_cd = 'SYSTEM',
    update_dt = CURRENT_TIMESTAMP;

INSERT INTO docs_rel_role_group (
    group_cd, role_cd, insert_user_cd, update_user_cd, insert_dt, update_dt
)
SELECT 'RG_012', role_cd, 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
  FROM docs_rel_role_group
 WHERE group_cd = 'RG_011'
ON CONFLICT (group_cd, role_cd) DO NOTHING;

-- Users who can open the technical-data search may create/inspect their own
-- requests and inspect approved items allowed by the document ACL service.
INSERT INTO docs_rel_role_group (
    group_cd, role_cd, insert_user_cd, update_user_cd, insert_dt, update_dt
)
SELECT source.group_cd,
       target.role_cd,
       'SYSTEM',
       'SYSTEM',
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
 FROM docs_rel_role_group source
 CROSS JOIN (
       VALUES ('ROLE_MENU_229'), ('ROLE_MENU_226'), ('ROLE_MENU_228')
 ) AS target(role_cd)
 WHERE source.role_cd = 'ROLE_MENU_220'
ON CONFLICT (group_cd, role_cd) DO NOTHING;

-- The top-level distribution navigation follows the same audience as the
-- technical-data search. It must not remain assigned to unrelated groups.
DELETE FROM docs_rel_role_group assignment
 WHERE assignment.role_cd = 'ROLE_MENU_229'
   AND NOT EXISTS (
       SELECT 1
         FROM docs_rel_role_group viewer
        WHERE viewer.group_cd = assignment.group_cd
          AND viewer.role_cd = 'ROLE_MENU_220'
   );

-- Keep administrator access deterministic even when an older database has an
-- incomplete technical-data menu assignment.
INSERT INTO docs_rel_role_group (
    group_cd, role_cd, insert_user_cd, update_user_cd, insert_dt, update_dt
)
VALUES
    ('RG_001', 'ROLE_MENU_229', 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('RG_001', 'ROLE_MENU_226', 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('RG_001', 'ROLE_MENU_227', 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('RG_001', 'ROLE_MENU_228', 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (group_cd, role_cd) DO NOTHING;

INSERT INTO docs_rel_role_group (
    group_cd, role_cd, insert_user_cd, update_user_cd, insert_dt, update_dt
)
VALUES
    ('RG_012', 'ROLE_MENU_229', 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('RG_012', 'ROLE_MENU_227', 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (group_cd, role_cd) DO NOTHING;

-- Only administrators and the dedicated approval group may open the approval
-- queue. Other accidental legacy assignments are removed deterministically.
DELETE FROM docs_rel_role_group
 WHERE role_cd = 'ROLE_MENU_227'
   AND group_cd NOT IN ('RG_001', 'RG_012');

-- Maintain the legacy URL mapping catalog for tools that still inspect it.
INSERT INTO docs_role_mapping (
    group_cd, group_nm, menu_nm, menu_url, menu_type
)
SELECT assignment.group_cd,
       COALESCE(role_group.group_nm, assignment.group_cd),
       menu.menu_nm,
       menu.menu_url,
       menu.menu_type::varchar
  FROM docs_rel_role_group assignment
  JOIN docs_menu menu
    ON menu.role_cd = assignment.role_cd
  LEFT JOIN docs_role_group role_group
    ON role_group.group_code = assignment.group_cd
 WHERE assignment.role_cd IN (
       'ROLE_MENU_229', 'ROLE_MENU_226', 'ROLE_MENU_227', 'ROLE_MENU_228'
 )
ON CONFLICT (group_cd, menu_url) DO UPDATE SET
    group_nm = EXCLUDED.group_nm,
    menu_nm = EXCLUDED.menu_nm,
    menu_type = EXCLUDED.menu_type;

DELETE FROM docs_role_mapping
 WHERE menu_url = '/general/distribution/workflow/approval/**'
   AND group_cd NOT IN ('RG_001', 'RG_012');

DELETE FROM docs_role_mapping mapping
 WHERE mapping.menu_url = '/general/distribution/workflow/'
   AND NOT EXISTS (
       SELECT 1
         FROM docs_rel_role_group viewer
        WHERE viewer.group_cd = mapping.group_cd
          AND viewer.role_cd = 'ROLE_MENU_220'
   );

INSERT INTO docs_lang (lang_type, lang_cd, lang_desc)
VALUES
    ('ko', 'menu.technicalDataDistribution', '기술자료배포'),
    ('en', 'menu.technicalDataDistribution', 'Technical Data Distribution'),
    ('ko', 'menu.distributionMyRequests', '배포요청'),
    ('en', 'menu.distributionMyRequests', 'Distribution Requests'),
    ('ko', 'menu.distributionApproval', '배포승인'),
    ('en', 'menu.distributionApproval', 'Distribution Approval'),
    ('ko', 'menu.distributionApprovedList', '승인목록'),
    ('en', 'menu.distributionApprovedList', 'Approved List')
ON CONFLICT (lang_type, lang_cd) DO UPDATE
   SET lang_desc = EXCLUDED.lang_desc;

DO $$
BEGIN
    IF (
        SELECT COUNT(*)
          FROM docs_menu
         WHERE menu_cd = 'MENU_229'
           AND parent_menu_cd = 'ROOT'
           AND menu_level = '1'
           AND menu_type = 'T'
           AND tree_type = 'root'
           AND use_yn = 'Y'
           AND del_yn = 'N'
    ) <> 1 THEN
        RAISE EXCEPTION 'Technical-data distribution root navigation is missing.';
    END IF;

    IF (
        SELECT COUNT(*)
          FROM docs_menu
         WHERE menu_cd IN ('MENU_226', 'MENU_227', 'MENU_228')
           AND parent_menu_cd = 'MENU_229'
           AND menu_level = '2'
           AND menu_type = 'M'
           AND tree_type = 'leaf'
           AND use_yn = 'Y'
           AND del_yn = 'N'
    ) <> 3 THEN
        RAISE EXCEPTION 'Distribution workflow navigation is incomplete.';
    END IF;

    IF NOT EXISTS (
        SELECT 1
          FROM docs_rel_role_group
         WHERE group_cd = 'RG_001'
           AND role_cd = 'ROLE_MENU_227'
    ) OR NOT EXISTS (
        SELECT 1
          FROM docs_rel_role_group
         WHERE group_cd = 'RG_012'
           AND role_cd = 'ROLE_MENU_227'
    ) OR EXISTS (
        SELECT 1
          FROM docs_rel_role_group
         WHERE group_cd NOT IN ('RG_001', 'RG_012')
           AND role_cd = 'ROLE_MENU_227'
    ) THEN
        RAISE EXCEPTION 'Distribution approval role-group assignments are invalid.';
    END IF;

    IF EXISTS (
        SELECT 1
          FROM docs_rel_role_group source
         WHERE source.role_cd = 'ROLE_MENU_220'
           AND NOT EXISTS (
               SELECT 1
                 FROM docs_rel_role_group target
                WHERE target.group_cd = source.group_cd
                  AND target.role_cd IN (
                      'ROLE_MENU_229', 'ROLE_MENU_226', 'ROLE_MENU_228'
                  )
                GROUP BY target.group_cd
               HAVING COUNT(DISTINCT target.role_cd) = 3
           )
    ) THEN
        RAISE EXCEPTION 'A technical-data viewer is missing distribution workflow menus.';
    END IF;
END
$$;

COMMIT;
