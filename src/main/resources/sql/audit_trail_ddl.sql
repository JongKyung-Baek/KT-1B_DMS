BEGIN;

-- Canonical audit ledger extension. Existing rows remain valid and keep NULL
-- for request/menu attributes that were not captured at the time.
ALTER TABLE docs_access_audit_log ADD COLUMN IF NOT EXISTS menu_cd varchar(64);
ALTER TABLE docs_access_audit_log ADD COLUMN IF NOT EXISTS menu_nm varchar(256);
ALTER TABLE docs_access_audit_log ADD COLUMN IF NOT EXISTS menu_url varchar(512);
ALTER TABLE docs_access_audit_log ADD COLUMN IF NOT EXISTS action_nm varchar(256);
ALTER TABLE docs_access_audit_log ADD COLUMN IF NOT EXISTS request_uri varchar(1000);
ALTER TABLE docs_access_audit_log ADD COLUMN IF NOT EXISTS http_method varchar(10);
ALTER TABLE docs_access_audit_log ADD COLUMN IF NOT EXISTS http_status integer;
ALTER TABLE docs_access_audit_log ADD COLUMN IF NOT EXISTS duration_ms bigint;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
          FROM information_schema.columns
         WHERE table_schema = current_schema()
           AND table_name = 'docs_access_audit_log'
           AND column_name = 'actor_user_id'
           AND character_maximum_length IS NOT NULL
           AND character_maximum_length < 100
    ) THEN
        ALTER TABLE docs_access_audit_log
            ALTER COLUMN actor_user_id TYPE varchar(100);
    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE INDEX IF NOT EXISTS idx_access_audit_menu_time
    ON docs_access_audit_log (menu_cd, occurred_at DESC)
    WHERE menu_cd IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_access_audit_event_result_time
    ON docs_access_audit_log (event_type, action_type, result_cd, occurred_at DESC);

-- A normal request may legitimately create several events with one
-- correlation id. Only deterministic legacy import ids are unique.
CREATE UNIQUE INDEX IF NOT EXISTS uq_access_audit_legacy_correlation
    ON docs_access_audit_log (correlation_id)
    WHERE correlation_id LIKE 'LEGACY-AUDIT-%';

-- Preserve the legacy authentication/account audit once in the canonical
-- ledger. Legacy timestamp values were written as Korea local time.
DO $$
BEGIN
    IF to_regclass('docs_audit_log') IS NOT NULL THEN
        INSERT INTO docs_access_audit_log (
            occurred_at,
            event_type,
            action_type,
            action_nm,
            result_cd,
            reason_cd,
            actor_user_cd,
            actor_user_id,
            actor_user_nm,
            menu_cd,
            menu_nm,
            menu_url,
            object_type,
            object_id,
            client_ip,
            correlation_id,
            detail_json
        )
        SELECT legacy.log_dt AT TIME ZONE 'Asia/Seoul',
               'AUTH',
               CASE legacy.action_type
                   WHEN 'logIn' THEN 'LOGIN'
                   WHEN 'logOut' THEN 'LOGOUT'
                   WHEN 'loginFail' THEN 'LOGIN'
                   WHEN 'changePassword' THEN 'PASSWORD_CHANGE'
                   ELSE UPPER(legacy.action_type)
               END,
               CASE legacy.action_type
                   WHEN 'logIn' THEN '로그인'
                   WHEN 'logOut' THEN '로그아웃'
                   WHEN 'loginFail' THEN '로그인'
                   WHEN 'changePassword' THEN '비밀번호 변경'
                   ELSE legacy.action_type
               END,
               CASE WHEN legacy.action_type = 'loginFail' THEN 'FAILURE' ELSE 'SUCCESS' END,
               CASE WHEN legacy.action_type = 'loginFail' THEN 'AUTHENTICATION_FAILED' END,
               actor.user_cd,
               legacy.user_id,
               legacy.user_name,
               'AUTH',
               '인증 / 계정',
               '/login/**',
               'USER_ACCOUNT',
               legacy.user_id,
               legacy.access_ip,
               'LEGACY-AUDIT-' || legacy.log_no,
               jsonb_build_object(
                   'legacyTable', 'DOCS_AUDIT_LOG',
                   'legacyLogNo', legacy.log_no,
                   'legacyActionType', legacy.action_type,
                   'menuCaptured', false
               )
          FROM docs_audit_log legacy
          LEFT JOIN LATERAL (
              SELECT userInfo.user_cd
                FROM docs_user userInfo
               WHERE userInfo.user_id = legacy.user_id
               ORDER BY CASE
                            WHEN userInfo.use_yn = 'Y'
                             AND userInfo.del_yn = 'N'
                             AND COALESCE(userInfo.lock_yn, 'N') != 'Y' THEN 0
                            ELSE 1
                        END,
                        userInfo.user_cd
               LIMIT 1
          ) actor ON TRUE
        ON CONFLICT DO NOTHING;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- Result is always persisted for internal policy/audit integrity, but it is
-- not a meaningful operator-facing column. Remove stale presentation metadata
-- as well as its formatter so legacy dumps cannot break jqGrid initialization.
DELETE FROM docs_grid_info
 WHERE grid_id = 'gridInsideAuditLogList'
   AND lower(column_id) IN ('resultcd', 'result');

-- Search form metadata.
UPDATE docs_form_info
   SET use_yn = 'N'
 WHERE form_id = 'formInsideAuditLog'
   AND column_id NOT IN (
       'startDt,endDt', 'userId', 'userNm', 'menuNm', 'eventType',
       'actionType', 'targetKeyword', 'btnSearch'
   );

INSERT INTO docs_form_info (
    form_id, column_id, column_type, column_nm, column_seq, column_size,
    column_align, system_class_group, use_yn, lang_cd, default_value,
    detail_yn, search_url, column_hidden
)
VALUES
    ('formInsideAuditLog', 'startDt,endDt', 'calendar', '기간', 10, '120,120',
     'left', '', 'Y', '', ',today', 'N', '', 'N'),
    ('formInsideAuditLog', 'userId', 'input', '사용자 아이디', 20, '160',
     'left', '', 'Y', '', '', 'N', '', 'N'),
    ('formInsideAuditLog', 'userNm', 'input', '사용자 이름', 30, '160',
     'left', '', 'Y', '', '', 'N', '', 'N'),
    ('formInsideAuditLog', 'menuNm', 'input', '메뉴', 40, '160',
     'left', '', 'Y', '', '', 'N', '', 'N'),
    ('formInsideAuditLog', 'eventType', 'input', '이벤트', 50, '150',
     'left', '', 'Y', '', '', 'N', '', 'N'),
    ('formInsideAuditLog', 'actionType', 'input', '행위', 60, '150',
     'left', '', 'Y', '', '', 'N', '', 'N'),
    ('formInsideAuditLog', 'targetKeyword', 'input', '대상', 70, '190',
     'left', '', 'Y', '', '', 'N', '', 'N'),
    ('formInsideAuditLog', 'btnSearch', 'btnSearch', '조회', 1000, NULL,
     NULL, '', 'Y', '', '', 'N', '', 'N')
ON CONFLICT (form_id, column_id) DO UPDATE SET
    column_type = EXCLUDED.column_type,
    column_nm = EXCLUDED.column_nm,
    column_seq = EXCLUDED.column_seq,
    column_size = EXCLUDED.column_size,
    column_align = EXCLUDED.column_align,
    system_class_group = EXCLUDED.system_class_group,
    use_yn = EXCLUDED.use_yn,
    lang_cd = EXCLUDED.lang_cd,
    default_value = EXCLUDED.default_value,
    detail_yn = EXCLUDED.detail_yn,
    search_url = EXCLUDED.search_url,
    column_hidden = EXCLUDED.column_hidden;

-- Audit grid metadata. docs_grid_info has no unique constraint in the legacy
-- schema, so use UPDATE + INSERT WHERE NOT EXISTS rather than ON CONFLICT.
UPDATE docs_grid_info
   SET column_hidden = 'Y'
 WHERE grid_id = 'gridInsideAuditLogList';

WITH desired (
    column_id, column_nm, column_seq, column_size, column_align,
    sort_yn, formatter
) AS (
    VALUES
        ('occurredAt', '시각', 10, 115, 'center', 'Y', NULL::varchar),
        ('userNm', '사용자', 20, 115, 'left', 'Y', 'formatAuditUser'),
        ('menuNm', '메뉴', 30, 135, 'left', 'Y', 'formatAuditMenu'),
        ('actionNm', '행위', 40, 115, 'left', 'Y', 'formatAuditAction'),
        ('targetSummary', '대상', 50, 145, 'left', 'Y', 'formatAuditTarget'),
        ('reasonCd', '사유', 60, 95, 'left', 'Y', NULL::varchar),
        ('accessIp', '접속 IP', 70, 100, 'center', 'Y', NULL::varchar)
)
UPDATE docs_grid_info gridInfo
   SET column_nm = desired.column_nm,
       column_seq = desired.column_seq,
       column_size = desired.column_size,
       column_type = 'ro',
       column_align = desired.column_align,
       column_format = 'str',
       column_hidden = 'N',
       sort_yn = desired.sort_yn,
       formatter = desired.formatter,
       column_editable = 'N',
       sort_column_nm = desired.column_id
  FROM desired
 WHERE gridInfo.grid_id = 'gridInsideAuditLogList'
   AND gridInfo.column_id = desired.column_id;

WITH desired (
    column_id, column_nm, column_seq, column_size, column_align,
    sort_yn, formatter
) AS (
    VALUES
        ('occurredAt', '시각', 10, 115, 'center', 'Y', NULL::varchar),
        ('userNm', '사용자', 20, 115, 'left', 'Y', 'formatAuditUser'),
        ('menuNm', '메뉴', 30, 135, 'left', 'Y', 'formatAuditMenu'),
        ('actionNm', '행위', 40, 115, 'left', 'Y', 'formatAuditAction'),
        ('targetSummary', '대상', 50, 145, 'left', 'Y', 'formatAuditTarget'),
        ('reasonCd', '사유', 60, 95, 'left', 'Y', NULL::varchar),
        ('accessIp', '접속 IP', 70, 100, 'center', 'Y', NULL::varchar)
)
INSERT INTO docs_grid_info (
    grid_id, column_id, column_nm, column_seq, column_size,
    column_type, column_align, column_format, column_hidden,
    sort_yn, formatter, column_editable, sort_column_nm
)
SELECT 'gridInsideAuditLogList',
       desired.column_id,
       desired.column_nm,
       desired.column_seq,
       desired.column_size,
       'ro',
       desired.column_align,
       'str',
       'N',
       desired.sort_yn,
       desired.formatter,
       'N',
       desired.column_id
  FROM desired
 WHERE NOT EXISTS (
       SELECT 1
         FROM docs_grid_info currentGrid
        WHERE currentGrid.grid_id = 'gridInsideAuditLogList'
          AND currentGrid.column_id = desired.column_id
 );

-- Keep database-driven form and grid labels synchronized with the session
-- locale. Indonesian can be enabled later by adding LANG_TYPE='id' rows for
-- these same stable keys.
UPDATE docs_form_info
   SET lang_cd = CASE column_id
       WHEN 'startDt,endDt' THEN 'form.audit.period'
       WHEN 'userId' THEN 'form.audit.userId'
       WHEN 'userNm' THEN 'form.audit.userName'
       WHEN 'menuNm' THEN 'form.audit.menu'
       WHEN 'eventType' THEN 'form.audit.event'
       WHEN 'actionType' THEN 'form.audit.action'
       WHEN 'targetKeyword' THEN 'form.audit.target'
       WHEN 'btnSearch' THEN 'btn.search'
       ELSE lang_cd
   END
 WHERE form_id = 'formInsideAuditLog'
   AND column_id IN (
       'startDt,endDt', 'userId', 'userNm', 'menuNm', 'eventType',
       'actionType', 'targetKeyword', 'btnSearch'
   );

UPDATE docs_grid_info
   SET lang_cd = CASE column_id
       WHEN 'occurredAt' THEN 'grid.audit.occurredAt'
       WHEN 'userNm' THEN 'grid.audit.user'
       WHEN 'menuNm' THEN 'grid.audit.menu'
       WHEN 'actionNm' THEN 'grid.audit.action'
       WHEN 'targetSummary' THEN 'grid.audit.target'
       WHEN 'reasonCd' THEN 'grid.audit.reason'
       WHEN 'accessIp' THEN 'grid.audit.accessIp'
       ELSE lang_cd
   END
 WHERE grid_id = 'gridInsideAuditLogList'
   AND column_id IN (
       'occurredAt', 'userNm', 'menuNm', 'actionNm',
       'targetSummary', 'reasonCd', 'accessIp'
   );

UPDATE docs_toolbar_info
   SET lang_cd = 'toolbar.excel'
 WHERE toolbar_id = 'toolbarInsideAuditLog'
   AND button_id = 'btnExcel';

INSERT INTO docs_lang (lang_type, lang_cd, lang_desc)
VALUES
    ('ko', 'form.audit.period', '기간'),
    ('en', 'form.audit.period', 'Period'),
    ('ko', 'form.audit.userId', '사용자 아이디'),
    ('en', 'form.audit.userId', 'User ID'),
    ('ko', 'form.audit.userName', '사용자 이름'),
    ('en', 'form.audit.userName', 'User Name'),
    ('ko', 'form.audit.menu', '메뉴'),
    ('en', 'form.audit.menu', 'Menu'),
    ('ko', 'form.audit.event', '이벤트'),
    ('en', 'form.audit.event', 'Event'),
    ('ko', 'form.audit.action', '행위'),
    ('en', 'form.audit.action', 'Action'),
    ('ko', 'form.audit.target', '대상'),
    ('en', 'form.audit.target', 'Target'),
    ('ko', 'btn.search', '조회'),
    ('en', 'btn.search', 'Search'),
    ('ko', 'grid.audit.occurredAt', '시각'),
    ('en', 'grid.audit.occurredAt', 'Time'),
    ('ko', 'grid.audit.user', '사용자'),
    ('en', 'grid.audit.user', 'User'),
    ('ko', 'grid.audit.menu', '메뉴'),
    ('en', 'grid.audit.menu', 'Menu'),
    ('ko', 'grid.audit.action', '행위'),
    ('en', 'grid.audit.action', 'Action'),
    ('ko', 'grid.audit.target', '대상'),
    ('en', 'grid.audit.target', 'Target'),
    ('ko', 'grid.audit.reason', '사유'),
    ('en', 'grid.audit.reason', 'Reason'),
    ('ko', 'grid.audit.accessIp', '접속 IP'),
    ('en', 'grid.audit.accessIp', 'Access IP'),
    ('ko', 'toolbar.excel', '엑셀'),
    ('en', 'toolbar.excel', 'Excel')
ON CONFLICT (lang_type, lang_cd) DO UPDATE
   SET lang_desc = EXCLUDED.lang_desc;

COMMIT;
