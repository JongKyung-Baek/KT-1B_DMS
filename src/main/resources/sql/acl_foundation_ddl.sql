BEGIN;

-- ---------------------------------------------------------------------------
-- 1. Tables referenced by active PostgreSQL mappers but missing from the dump
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS docs_company (
    company_cd             varchar(20) PRIMARY KEY,
    company_nm             varchar(256) NOT NULL,
    company_type           char(1) NOT NULL DEFAULT 'E',
    business_area_cd       varchar(30),
    biz_no                 varchar(30),
    use_start_dt           varchar(8),
    use_end_dt             varchar(8),
    approval_user_cd       varchar(20),
    dist_purchaser_user_cd varchar(20),
    use_yn                 char(1) NOT NULL DEFAULT 'Y',
    del_yn                 char(1) NOT NULL DEFAULT 'N',
    insert_user_cd         varchar(20),
    insert_dt              timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_user_cd         varchar(20),
    update_dt              timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_docs_company_use CHECK (use_yn IN ('Y', 'N')),
    CONSTRAINT ck_docs_company_del CHECK (del_yn IN ('Y', 'N'))
);

-- The active HR/vendor integration mapper updates this legacy column.
ALTER TABLE docs_company
    ADD COLUMN IF NOT EXISTS business_area_cd varchar(30);

INSERT INTO docs_company (
    company_cd, company_nm, company_type, biz_no,
    use_start_dt, use_end_dt, use_yn, del_yn
)
VALUES ('COMP_0000000999', 'KT-1B', 'I', NULL, '19000101', '99991231', 'Y', 'N')
ON CONFLICT (company_cd) DO UPDATE SET
    company_nm = EXCLUDED.company_nm,
    company_type = EXCLUDED.company_type,
    use_yn = 'Y',
    del_yn = 'N',
    update_user_cd = 'SYSTEM',
    update_dt = CURRENT_TIMESTAMP;

CREATE TABLE IF NOT EXISTS docs_document_member (
    cn_serial   integer,
    object_id   varchar(60) NOT NULL,
    access_uid  varchar(50) NOT NULL,
    interface_dt timestamp without time zone,
    PRIMARY KEY (object_id, access_uid)
);

CREATE TABLE IF NOT EXISTS docs_drawing_member (
    cn_serial   integer,
    object_id   varchar(60) NOT NULL,
    access_uid  varchar(50) NOT NULL,
    interface_dt timestamp without time zone,
    PRIMARY KEY (object_id, access_uid)
);

CREATE TABLE IF NOT EXISTS docs_sw_member (
    cn_serial   integer,
    object_id   varchar(60) NOT NULL,
    access_uid  varchar(50) NOT NULL,
    interface_dt timestamp without time zone,
    PRIMARY KEY (object_id, access_uid)
);

CREATE TABLE IF NOT EXISTS docs_product_document_member (
    cn_serial   integer,
    object_id   varchar(60) NOT NULL,
    access_uid  varchar(50) NOT NULL,
    interface_dt timestamp without time zone,
    PRIMARY KEY (object_id, access_uid)
);

CREATE INDEX IF NOT EXISTS idx_docs_document_member_access
    ON docs_document_member (access_uid, object_id);
CREATE INDEX IF NOT EXISTS idx_docs_drawing_member_access
    ON docs_drawing_member (access_uid, object_id);
CREATE INDEX IF NOT EXISTS idx_docs_sw_member_access
    ON docs_sw_member (access_uid, object_id);
CREATE INDEX IF NOT EXISTS idx_docs_product_document_member_access
    ON docs_product_document_member (access_uid, object_id);

CREATE TABLE IF NOT EXISTS docs_viewer_key (
    disposable_key varchar(128) PRIMARY KEY,
    object_id      varchar(60) NOT NULL,
    object_type    varchar(30),
    file_no        varchar(60),
    file_name      varchar(255),
    user_cd        varchar(20),
    session_id     varchar(128),
    created_at     timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    delete_at      timestamp with time zone NOT NULL,
    used_at        timestamp with time zone,
    revoked_yn     char(1) NOT NULL DEFAULT 'N',
    CONSTRAINT ck_docs_viewer_key_revoked CHECK (revoked_yn IN ('Y', 'N'))
);
ALTER TABLE docs_viewer_key ADD COLUMN IF NOT EXISTS object_type varchar(30);
ALTER TABLE docs_viewer_key ADD COLUMN IF NOT EXISTS file_no varchar(60);
ALTER TABLE docs_viewer_key ADD COLUMN IF NOT EXISTS file_name varchar(255);
ALTER TABLE docs_viewer_key ADD COLUMN IF NOT EXISTS user_cd varchar(20);
ALTER TABLE docs_viewer_key ADD COLUMN IF NOT EXISTS session_id varchar(128);
ALTER TABLE docs_viewer_key ADD COLUMN IF NOT EXISTS created_at timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE docs_viewer_key ADD COLUMN IF NOT EXISTS used_at timestamp with time zone;
ALTER TABLE docs_viewer_key ADD COLUMN IF NOT EXISTS revoked_yn char(1) NOT NULL DEFAULT 'N';
CREATE INDEX IF NOT EXISTS idx_docs_viewer_key_expiry
    ON docs_viewer_key (delete_at) WHERE revoked_yn = 'N';

CREATE TABLE IF NOT EXISTS docs_eco_history (
    cn_serial                  integer,
    business_area_cd           varchar(30),
    business_type_cd           varchar(30),
    ecn_no                     varchar(100) NOT NULL,
    upper_part_no              varchar(100),
    upper_part_rev_no          varchar(50),
    upper_drawing_no           varchar(100),
    upper_drawing_rev_no       varchar(50),
    before_part_no             varchar(100),
    before_part_rev_no         varchar(50),
    before_drawing_no          varchar(100),
    before_drawing_rev_no      varchar(50),
    after_part_no              varchar(100),
    after_part_rev_no          varchar(50),
    after_drawing_no           varchar(100),
    after_drawing_rev_no       varchar(50),
    use_type_cd                varchar(30),
    ecn_insert_uid             varchar(50),
    ecn_approval_dt            timestamp without time zone,
    seq_no                     integer NOT NULL,
    interface_dt               timestamp without time zone,
    PRIMARY KEY (ecn_no, seq_no)
);
CREATE INDEX IF NOT EXISTS idx_docs_eco_history_serial
    ON docs_eco_history (cn_serial);

-- ---------------------------------------------------------------------------
-- 2. Security grade ACL
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS docs_security_grade (
    grade_cd       varchar(30) PRIMARY KEY,
    grade_nm       varchar(100) NOT NULL,
    grade_level    integer NOT NULL,
    description    varchar(500),
    default_yn     char(1) NOT NULL DEFAULT 'N',
    use_yn         char(1) NOT NULL DEFAULT 'Y',
    insert_user_cd varchar(20),
    insert_dt      timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_user_cd varchar(20),
    update_dt      timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_docs_security_grade_level CHECK (grade_level >= 0),
    CONSTRAINT ck_docs_security_grade_default CHECK (default_yn IN ('Y', 'N')),
    CONSTRAINT ck_docs_security_grade_use CHECK (use_yn IN ('Y', 'N'))
);
CREATE UNIQUE INDEX IF NOT EXISTS ux_docs_security_grade_level
    ON docs_security_grade (grade_level);
CREATE UNIQUE INDEX IF NOT EXISTS ux_docs_security_grade_default
    ON docs_security_grade (default_yn) WHERE default_yn = 'Y';

INSERT INTO docs_security_grade
    (grade_cd, grade_nm, grade_level, description, default_yn, use_yn)
VALUES
    ('GENERAL',      '일반',   10, '기존 자료에 적용되는 기본 등급', 'Y', 'Y'),
    ('INTERNAL',     '사내한', 20, '인가된 내부 사용자 전용',       'N', 'Y'),
    ('RESTRICTED',   '제한',   30, '업무상 지정된 사용자 전용',     'N', 'Y'),
    ('CONFIDENTIAL', '대외비', 40, '최고 수준의 별도 인가 필요',    'N', 'Y')
ON CONFLICT (grade_cd) DO NOTHING;

CREATE TABLE IF NOT EXISTS docs_user_security_clearance (
    user_cd       varchar(20) PRIMARY KEY,
    grade_cd      varchar(30) NOT NULL,
    valid_from    timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    valid_to      timestamp with time zone,
    grant_reason  varchar(500),
    granted_by    varchar(20) NOT NULL,
    granted_at    timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by    varchar(20),
    updated_at    timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_clearance_user FOREIGN KEY (user_cd)
        REFERENCES docs_user (user_cd) ON DELETE RESTRICT,
    CONSTRAINT fk_user_clearance_grade FOREIGN KEY (grade_cd)
        REFERENCES docs_security_grade (grade_cd) ON DELETE RESTRICT,
    CONSTRAINT ck_user_clearance_period CHECK (valid_to IS NULL OR valid_to > valid_from)
);
CREATE INDEX IF NOT EXISTS idx_user_clearance_grade
    ON docs_user_security_clearance (grade_cd, valid_to);

-- Existing active users receive only the lowest/default clearance during migration.
INSERT INTO docs_user_security_clearance
    (user_cd, grade_cd, valid_from, grant_reason, granted_by)
SELECT u.user_cd, 'GENERAL', CURRENT_TIMESTAMP, 'ACL 최초 이관', 'SYSTEM'
FROM docs_user u
WHERE u.use_yn = 'Y' AND u.del_yn = 'N'
ON CONFLICT (user_cd) DO NOTHING;

CREATE TABLE IF NOT EXISTS docs_user_action_permission (
    user_cd       varchar(20) NOT NULL,
    action_cd     varchar(30) NOT NULL,
    allow_yn      char(1) NOT NULL DEFAULT 'N',
    granted_by    varchar(20) NOT NULL,
    granted_at    timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by    varchar(20),
    updated_at    timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_cd, action_cd),
    CONSTRAINT fk_user_action_permission_user FOREIGN KEY (user_cd)
        REFERENCES docs_user (user_cd) ON DELETE RESTRICT,
    CONSTRAINT ck_user_action_permission_action CHECK (
        action_cd IN ('LIST', 'DETAIL', 'VIEW', 'DOWNLOAD_ORIGINAL', 'PRINT', 'MANAGE_ACL')
    ),
    CONSTRAINT ck_user_action_permission_allow CHECK (allow_yn IN ('Y', 'N'))
);

-- Safe rollout defaults: every active user may discover/view GENERAL data.
INSERT INTO docs_user_action_permission (user_cd, action_cd, allow_yn, granted_by)
SELECT u.user_cd, a.action_cd, 'Y', 'SYSTEM'
FROM docs_user u
CROSS JOIN (VALUES ('LIST'), ('DETAIL'), ('VIEW')) a(action_cd)
WHERE u.use_yn = 'Y' AND u.del_yn = 'N'
ON CONFLICT (user_cd, action_cd) DO NOTHING;

-- File export, printing and ACL administration remain administrator-only initially.
INSERT INTO docs_user_action_permission (user_cd, action_cd, allow_yn, granted_by)
SELECT u.user_cd, a.action_cd, 'Y', 'SYSTEM'
FROM docs_user u
JOIN docs_role_group_member rgm
  ON rgm.member_cd = u.user_cd
 AND rgm.group_type = 'USER'
 AND rgm.group_code = 'RG_001'
CROSS JOIN (VALUES ('DOWNLOAD_ORIGINAL'), ('PRINT'), ('MANAGE_ACL')) a(action_cd)
WHERE u.use_yn = 'Y' AND u.del_yn = 'N'
ON CONFLICT (user_cd, action_cd) DO NOTHING;

CREATE TABLE IF NOT EXISTS docs_file_security_label (
    label_id       bigserial PRIMARY KEY,
    object_type    varchar(30) NOT NULL,
    object_id      varchar(60) NOT NULL,
    file_no        varchar(60) NOT NULL DEFAULT '*',
    grade_cd       varchar(30) NOT NULL,
    label_reason   varchar(500),
    assigned_by    varchar(20) NOT NULL,
    assigned_at    timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by     varchar(20),
    updated_at     timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_file_security_label_grade FOREIGN KEY (grade_cd)
        REFERENCES docs_security_grade (grade_cd) ON DELETE RESTRICT,
    CONSTRAINT ux_file_security_label UNIQUE (object_type, object_id, file_no)
);
CREATE INDEX IF NOT EXISTS idx_file_security_label_grade
    ON docs_file_security_label (grade_cd, object_type, object_id);

-- A document-level named-user ACL is an additional gate over the user's
-- maximum clearance and global action permission. Sub-file types share their
-- parent document's permission subject.
CREATE TABLE IF NOT EXISTS docs_object_user_permission (
    object_type    varchar(30) NOT NULL,
    object_id      varchar(60) NOT NULL,
    user_cd        varchar(20) NOT NULL,
    action_cd      varchar(30) NOT NULL,
    allow_yn       char(1) NOT NULL DEFAULT 'Y',
    grant_reason   varchar(500) NOT NULL,
    granted_by     varchar(20) NOT NULL,
    granted_at     timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by     varchar(20),
    updated_at     timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (object_type, object_id, user_cd, action_cd),
    CONSTRAINT fk_object_user_permission_user FOREIGN KEY (user_cd)
        REFERENCES docs_user (user_cd) ON DELETE RESTRICT,
    CONSTRAINT ck_object_user_permission_type CHECK (
        object_type IN (
            'DOCUMENT', 'DRAWING', 'SW', 'PRODUCT_DOCUMENT',
            'PRODUCT_SW', 'DXF', 'PEER_REVIEW'
        )
    ),
    CONSTRAINT ck_object_user_permission_action CHECK (
        action_cd IN ('LIST', 'DETAIL', 'VIEW', 'DOWNLOAD_ORIGINAL', 'PRINT')
    ),
    CONSTRAINT ck_object_user_permission_allow CHECK (allow_yn IN ('Y', 'N'))
);
CREATE INDEX IF NOT EXISTS idx_object_user_permission_decision
    ON docs_object_user_permission (user_cd, action_cd, object_type, object_id);
CREATE INDEX IF NOT EXISTS idx_object_user_permission_object
    ON docs_object_user_permission (object_type, object_id, user_cd);

CREATE TABLE IF NOT EXISTS docs_acl_migration_state (
    migration_cd varchar(100) PRIMARY KEY,
    applied_at   timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Existing objects are explicitly labelled during the migration. Access checks
-- remain fail-closed: objects created later without a label are denied.
WITH latest AS (
    SELECT DISTINCT ON (object_id) object_id, security_type_cd
      FROM docs_document
     WHERE object_id IS NOT NULL
     ORDER BY object_id, interface_dt DESC NULLS LAST
)
INSERT INTO docs_file_security_label (
    object_type, object_id, file_no, grade_cd, label_reason, assigned_by
)
SELECT 'DOCUMENT', object_id, '*',
       CASE LOWER(COALESCE(security_type_cd, ''))
           WHEN 'confidentail' THEN 'CONFIDENTIAL'
           WHEN 'confidential' THEN 'CONFIDENTIAL'
           WHEN 'classified' THEN 'RESTRICTED'
           WHEN '' THEN 'GENERAL'
           WHEN 'blank' THEN 'GENERAL'
           ELSE 'CONFIDENTIAL'
       END,
       '기존 일반문서 ACL 이관', 'SYSTEM'
  FROM latest
ON CONFLICT (object_type, object_id, file_no) DO NOTHING;

WITH latest AS (
    SELECT DISTINCT ON (object_id) object_id, security_type_cd
      FROM docs_drawing
     WHERE object_id IS NOT NULL
     ORDER BY object_id, interface_dt DESC NULLS LAST
)
INSERT INTO docs_file_security_label (
    object_type, object_id, file_no, grade_cd, label_reason, assigned_by
)
SELECT 'DRAWING', object_id, '*',
       CASE LOWER(COALESCE(security_type_cd, ''))
           WHEN 'confidentail' THEN 'CONFIDENTIAL'
           WHEN 'confidential' THEN 'CONFIDENTIAL'
           WHEN 'classified' THEN 'RESTRICTED'
           WHEN '' THEN 'GENERAL'
           WHEN 'blank' THEN 'GENERAL'
           ELSE 'CONFIDENTIAL'
       END,
       '기존 도면 ACL 이관', 'SYSTEM'
  FROM latest
ON CONFLICT (object_type, object_id, file_no) DO NOTHING;

WITH latest AS (
    SELECT DISTINCT ON (object_id) object_id, security_type_cd
      FROM docs_sw
     WHERE object_id IS NOT NULL
     ORDER BY object_id, interface_dt DESC NULLS LAST
)
INSERT INTO docs_file_security_label (
    object_type, object_id, file_no, grade_cd, label_reason, assigned_by
)
SELECT 'SW', object_id, '*',
       CASE LOWER(COALESCE(security_type_cd, ''))
           WHEN 'confidentail' THEN 'CONFIDENTIAL'
           WHEN 'confidential' THEN 'CONFIDENTIAL'
           WHEN 'classified' THEN 'RESTRICTED'
           WHEN '' THEN 'GENERAL'
           WHEN 'blank' THEN 'GENERAL'
           ELSE 'CONFIDENTIAL'
       END,
       '기존 SW ACL 이관', 'SYSTEM'
  FROM latest
ON CONFLICT (object_type, object_id, file_no) DO NOTHING;

INSERT INTO docs_file_security_label (
    object_type, object_id, file_no, grade_cd, label_reason, assigned_by
)
SELECT object_type, object_id, '*', grade_cd, reason, 'SYSTEM'
  FROM (
        SELECT 'PRODUCT_DOCUMENT'::varchar AS object_type, object_id,
               CASE LOWER(COALESCE(security_type_cd, ''))
                   WHEN 'confidentail' THEN 'CONFIDENTIAL'
                   WHEN 'confidential' THEN 'CONFIDENTIAL'
                   WHEN 'classified' THEN 'RESTRICTED'
                   WHEN '' THEN 'GENERAL'
                   WHEN 'blank' THEN 'GENERAL'
                   ELSE 'CONFIDENTIAL'
               END AS grade_cd,
               '기존 생산문서 ACL 이관'::varchar AS reason
          FROM (
                SELECT DISTINCT ON (object_id) object_id, security_type_cd
                  FROM docs_product_document
                 WHERE object_id IS NOT NULL
                 ORDER BY object_id, interface_dt DESC NULLS LAST
          ) latest_product_document
        UNION ALL
        SELECT 'PRODUCT_SW', object_id,
               CASE LOWER(COALESCE(security_type_cd, ''))
                   WHEN 'confidentail' THEN 'CONFIDENTIAL'
                   WHEN 'confidential' THEN 'CONFIDENTIAL'
                   WHEN 'classified' THEN 'RESTRICTED'
                   WHEN '' THEN 'GENERAL'
                   WHEN 'blank' THEN 'GENERAL'
                   ELSE 'CONFIDENTIAL'
               END,
               '기존 생산 SW ACL 이관'
          FROM (
                SELECT DISTINCT ON (object_id) object_id, security_type_cd
                  FROM docs_product_sw
                 WHERE object_id IS NOT NULL
                 ORDER BY object_id, interface_dt DESC NULLS LAST
          ) latest_product_sw
        UNION ALL
        SELECT 'DXF', object_id,
               CASE LOWER(COALESCE(security_type_cd, ''))
                   WHEN 'confidentail' THEN 'CONFIDENTIAL'
                   WHEN 'confidential' THEN 'CONFIDENTIAL'
                   WHEN 'classified' THEN 'RESTRICTED'
                   WHEN '' THEN 'GENERAL'
                   WHEN 'blank' THEN 'GENERAL'
                   ELSE 'CONFIDENTIAL'
               END,
               '기존 DXF ACL 이관'
          FROM (
                SELECT DISTINCT ON (object_id) object_id, security_type_cd
                  FROM docs_dxf_document
                 WHERE object_id IS NOT NULL
                 ORDER BY object_id, interface_dt DESC NULLS LAST
          ) latest_dxf
  ) source
ON CONFLICT (object_type, object_id, file_no) DO NOTHING;

-- Sub-files dynamically inherit the parent object's wildcard grade. Remove only
-- untouched rollout copies so the parent remains the single source of truth;
-- administrator-assigned sub-file labels are preserved as stricter overrides.
DELETE FROM docs_file_security_label
 WHERE object_type IN (
           'DOCUMENT_SUB', 'DRAWING_SUB', 'SW_SUB',
           'PRODUCT_DOCUMENT_SUB', 'PRODUCT_SW_SUB', 'DXF_SUB'
       )
   AND file_no = '*'
   AND assigned_by = 'SYSTEM'
   AND updated_by IS NULL
   AND label_reason = 'Existing sub-file ACL migration';

INSERT INTO docs_file_security_label (
    object_type, object_id, file_no, grade_cd, label_reason, assigned_by
)
SELECT 'PEER_REVIEW', object_id, '*', 'GENERAL', 'Existing peer-review ACL migration', 'SYSTEM'
  FROM docs_peerreview
 WHERE object_id IS NOT NULL
   AND COALESCE(deleted_yn, 'N') = 'N'
ON CONFLICT (object_type, object_id, file_no) DO NOTHING;

-- Preserve the effective access that existed immediately before named-user ACL
-- enforcement. The marker prevents a rerun from restoring permissions that an
-- administrator later revoked.
WITH permission_source AS (
    SELECT DISTINCT
           CASE l.object_type
               WHEN 'DOCUMENT_SUB' THEN 'DOCUMENT'
               WHEN 'DRAWING_SUB' THEN 'DRAWING'
               WHEN 'SW_SUB' THEN 'SW'
               WHEN 'PRODUCT_DOCUMENT_SUB' THEN 'PRODUCT_DOCUMENT'
               WHEN 'PRODUCT_SW_SUB' THEN 'PRODUCT_SW'
               WHEN 'DXF_SUB' THEN 'DXF'
               ELSE l.object_type
           END AS object_type,
           l.object_id,
           u.user_cd,
           p.action_cd
      FROM docs_file_security_label l
      JOIN docs_security_grade fg
        ON fg.grade_cd = l.grade_cd
       AND fg.use_yn = 'Y'
      JOIN docs_user_security_clearance uc
        ON uc.valid_from <= CURRENT_TIMESTAMP
       AND (uc.valid_to IS NULL OR uc.valid_to > CURRENT_TIMESTAMP)
      JOIN docs_user u
        ON u.user_cd = uc.user_cd
       AND u.use_yn = 'Y'
       AND u.del_yn = 'N'
       AND COALESCE(u.lock_yn, 'N') != 'Y'
      JOIN docs_security_grade ug
        ON ug.grade_cd = uc.grade_cd
       AND ug.use_yn = 'Y'
       AND ug.grade_level >= fg.grade_level
      JOIN docs_user_action_permission p
        ON p.user_cd = u.user_cd
       AND p.allow_yn = 'Y'
       AND p.action_cd IN ('LIST', 'DETAIL', 'VIEW', 'DOWNLOAD_ORIGINAL', 'PRINT')
     WHERE NOT EXISTS (
         SELECT 1
           FROM docs_acl_migration_state m
          WHERE m.migration_cd = '20260724_DOCUMENT_USER_ACL'
     )
)
INSERT INTO docs_object_user_permission (
    object_type, object_id, user_cd, action_cd, allow_yn,
    grant_reason, granted_by
)
SELECT object_type, object_id, user_cd, action_cd, 'Y',
       '문서별 사용자 ACL 초기 이관', 'SYSTEM'
  FROM permission_source
ON CONFLICT (object_type, object_id, user_cd, action_cd) DO NOTHING;

INSERT INTO docs_acl_migration_state (migration_cd)
VALUES ('20260724_DOCUMENT_USER_ACL')
ON CONFLICT (migration_cd) DO NOTHING;

-- A single append-only server-side ledger for ACL changes and access decisions.
CREATE TABLE IF NOT EXISTS docs_access_audit_log (
    event_id       bigserial PRIMARY KEY,
    occurred_at    timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    event_type     varchar(40) NOT NULL,
    action_type    varchar(30) NOT NULL,
    result_cd      varchar(20) NOT NULL,
    reason_cd      varchar(50),
    result_message varchar(1000),
    actor_user_cd  varchar(20),
    actor_user_id  varchar(100),
    actor_user_nm  varchar(256),
    menu_cd        varchar(64),
    menu_nm        varchar(256),
    menu_url       varchar(512),
    object_type    varchar(30),
    object_id      varchar(60),
    file_no        varchar(60),
    request_no     varchar(100),
    grade_cd       varchar(30),
    action_nm      varchar(256),
    client_ip      varchar(64),
    session_id     varchar(128),
    correlation_id varchar(128),
    request_uri    varchar(1000),
    http_method    varchar(10),
    http_status    integer,
    duration_ms    bigint,
    detail_json    jsonb NOT NULL DEFAULT '{}'::jsonb
);
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
CREATE INDEX IF NOT EXISTS idx_access_audit_time
    ON docs_access_audit_log (occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_access_audit_actor
    ON docs_access_audit_log (actor_user_cd, occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_access_audit_object
    ON docs_access_audit_log (object_type, object_id, file_no, occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_access_audit_request
    ON docs_access_audit_log (request_no, occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_access_audit_menu_time
    ON docs_access_audit_log (menu_cd, occurred_at DESC)
    WHERE menu_cd IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_access_audit_event_result_time
    ON docs_access_audit_log (event_type, action_type, result_cd, occurred_at DESC);
CREATE UNIQUE INDEX IF NOT EXISTS uq_access_audit_legacy_correlation
    ON docs_access_audit_log (correlation_id)
    WHERE correlation_id LIKE 'LEGACY-AUDIT-%';

CREATE OR REPLACE FUNCTION fn_block_access_audit_mutation()
RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'docs_access_audit_log is append-only';
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_block_access_audit_mutation ON docs_access_audit_log;
CREATE TRIGGER trg_block_access_audit_mutation
BEFORE UPDATE OR DELETE ON docs_access_audit_log
FOR EACH ROW EXECUTE FUNCTION fn_block_access_audit_mutation();

-- Print is a two-phase operation. A job request is not counted as a successful
-- print; only an authenticated result callback may move it to SUCCESS.
CREATE TABLE IF NOT EXISTS docs_print_job (
    print_job_id   varchar(36) PRIMARY KEY,
    status_cd      varchar(20) NOT NULL DEFAULT 'STARTED',
    actor_user_cd  varchar(20) NOT NULL,
    actor_user_id  varchar(50),
    actor_user_nm  varchar(256),
    object_type    varchar(30) NOT NULL,
    object_id      varchar(60) NOT NULL,
    file_no        varchar(60) NOT NULL DEFAULT '*',
    request_no     varchar(100),
    page_count     integer,
    copy_count     integer,
    printer_nm     varchar(256),
    device_id      varchar(256),
    client_ip      varchar(64),
    requested_at   timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at   timestamp with time zone,
    count_applied_yn char(1) NOT NULL DEFAULT 'N',
    error_message  varchar(1000),
    CONSTRAINT fk_print_job_user FOREIGN KEY (actor_user_cd)
        REFERENCES docs_user (user_cd) ON DELETE RESTRICT,
    CONSTRAINT ck_print_job_status CHECK (status_cd IN ('STARTED', 'SUCCESS', 'FAILED', 'CANCELLED')),
    CONSTRAINT ck_print_job_count CHECK (count_applied_yn IN ('Y', 'N')),
    CONSTRAINT ck_print_job_page CHECK (page_count IS NULL OR page_count >= 0),
    CONSTRAINT ck_print_job_copy CHECK (copy_count IS NULL OR copy_count >= 0)
);
CREATE INDEX IF NOT EXISTS idx_print_job_actor_time
    ON docs_print_job (actor_user_cd, requested_at DESC);
CREATE INDEX IF NOT EXISTS idx_print_job_object_time
    ON docs_print_job (object_type, object_id, file_no, requested_at DESC);

CREATE TABLE IF NOT EXISTS docs_print_job_item (
    print_job_id varchar(36) NOT NULL,
    item_seq     integer NOT NULL,
    object_type varchar(30) NOT NULL,
    object_id   varchar(60) NOT NULL,
    file_no     varchar(60) NOT NULL DEFAULT '*',
    request_no  varchar(100),
    request_type varchar(30),
    count_required_yn char(1) NOT NULL DEFAULT 'N',
    PRIMARY KEY (print_job_id, item_seq),
    CONSTRAINT fk_print_job_item_job FOREIGN KEY (print_job_id)
        REFERENCES docs_print_job (print_job_id) ON DELETE CASCADE
);
ALTER TABLE IF EXISTS docs_print_job_item
    ADD COLUMN IF NOT EXISTS request_type varchar(30);
ALTER TABLE IF EXISTS docs_print_job_item
    ADD COLUMN IF NOT EXISTS count_required_yn char(1) NOT NULL DEFAULT 'N';
CREATE INDEX IF NOT EXISTS idx_print_job_item_object
    ON docs_print_job_item (object_type, object_id, file_no);

-- History management groups security access decisions, viewing records, and
-- verified print results under one administrator-facing root menu. The parent
-- URL is deliberately exact: a wildcard here would shadow the independent
-- child roles in Spring Security.
INSERT INTO docs_menu (
    menu_cd, parent_menu_cd, menu_nm, message_cd, menu_level, menu_type,
    menu_url, sort_seq, tree_type, del_yn, use_yn, tooltip, menu_desc,
    role_cd, menu_icon
)
VALUES (
    'MENU_223', 'ROOT', '이력관리', '', '1', 'T',
    '/general/history/', 115, 'root', 'N', 'Y',
    '접근·감사·열람·출력 이력 관리',
    '인증·메뉴·보안 접근 감사와 실제 열람·출력 기록을 구분하여 조회',
    'ROLE_MENU_223', 'tabler-history'
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
    tooltip = EXCLUDED.tooltip,
    menu_desc = EXCLUDED.menu_desc,
    role_cd = EXCLUDED.role_cd,
    use_yn = 'Y',
    del_yn = 'N';

INSERT INTO docs_menu (
    menu_cd, parent_menu_cd, menu_nm, message_cd, menu_level, menu_type,
    menu_url, sort_seq, tree_type, del_yn, use_yn, tooltip, menu_desc,
    role_cd, menu_icon
)
VALUES
    (
        'MENU_224', 'MENU_223', '열람이력', '', '2', 'M',
        '/general/history/view/**', 117, 'leaf', 'N', 'Y',
        '기술자료 열람 이력',
        '사용자별 기술자료 열람 허용·차단 및 이전 시스템 열람 기록 조회',
        'ROLE_MENU_224', ''
    ),
    (
        'MENU_225', 'MENU_223', '출력이력', '', '2', 'M',
        '/general/history/print/**', 118, 'leaf', 'N', 'Y',
        '기술자료 출력 이력',
        '사용자별 출력 요청과 검증된 성공·실패 결과 조회',
        'ROLE_MENU_225', ''
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
    tooltip = EXCLUDED.tooltip,
    menu_desc = EXCLUDED.menu_desc,
    role_cd = EXCLUDED.role_cd,
    use_yn = 'Y',
    del_yn = 'N';

-- Keep the established administrator assignment even when the source dump did
-- not include it, then clone every existing access-history group to the new
-- parent and its two sibling history menus.
INSERT INTO docs_rel_role_group
    (group_cd, role_cd, insert_user_cd, update_user_cd, insert_dt, update_dt)
VALUES
    ('RG_001', 'ROLE_MENU_206', 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (group_cd, role_cd) DO NOTHING;

INSERT INTO docs_rel_role_group
    (group_cd, role_cd, insert_user_cd, update_user_cd, insert_dt, update_dt)
SELECT source.group_cd,
       target.role_cd,
       'SYSTEM',
       'SYSTEM',
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
  FROM docs_rel_role_group source
 CROSS JOIN (
       VALUES
           ('ROLE_MENU_223'),
           ('ROLE_MENU_224'),
           ('ROLE_MENU_225')
 ) AS target(role_cd)
 WHERE source.role_cd = 'ROLE_MENU_206'
ON CONFLICT (group_cd, role_cd) DO NOTHING;

-- The legacy screen manages print approval and disposal rather than verified
-- print execution. Keep the workflow, but remove the misleading duplicate
-- "출력이력" label now used by MENU_225.
UPDATE docs_menu
   SET menu_nm = '출력 승인/폐기 관리',
       tooltip = '출력 승인 및 출력물 폐기 관리',
       menu_desc = '승인된 출력 요청, 출력 횟수 및 출력물 폐기 업무 관리'
 WHERE menu_cd = 'MENU_032';

-- Disable the legacy generic Excel action. It referenced the wrong mapper and
-- bypassed the access-history route's authorization boundary.
UPDATE docs_toolbar_info
   SET use_yn = 'N'
 WHERE toolbar_id = 'toolbarViewPrintHistory'
   AND button_id = 'btnExcel';

-- ACL administration menu: administrator group only.
INSERT INTO docs_menu (
    menu_cd, parent_menu_cd, menu_nm, message_cd, menu_level, menu_type,
    menu_url, sort_seq, tree_type, del_yn, use_yn, tooltip, menu_desc,
    role_cd, menu_icon
)
VALUES (
    'MENU_222', 'MENU_071', '보안등급/인가 관리', '', '2', 'M',
    '/general/system/securityaccess/', 93, 'leaf', 'N', 'Y', '',
    '파일 보안등급과 사용자 인가등급 관리', 'ROLE_MENU_222', ''
)
ON CONFLICT (menu_cd) DO UPDATE SET
    parent_menu_cd = EXCLUDED.parent_menu_cd,
    menu_nm = EXCLUDED.menu_nm,
    menu_level = EXCLUDED.menu_level,
    menu_type = EXCLUDED.menu_type,
    menu_url = EXCLUDED.menu_url,
    sort_seq = EXCLUDED.sort_seq,
    tree_type = EXCLUDED.tree_type,
    menu_desc = EXCLUDED.menu_desc,
    role_cd = EXCLUDED.role_cd,
    use_yn = 'Y',
    del_yn = 'N';

INSERT INTO docs_rel_role_group
    (group_cd, role_cd, insert_user_cd, update_user_cd, insert_dt, update_dt)
VALUES
    ('RG_001', 'ROLE_MENU_222', 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (group_cd, role_cd) DO NOTHING;

-- Technical-data list: expose the effective document security grade.
UPDATE docs_grid_info
   SET column_nm = '문서등급',
       column_seq = 52,
       column_size = 90,
       column_type = 'ro',
       column_align = 'center',
       column_format = 'str',
       column_hidden = 'N',
       sort_yn = 'N',
       formatter = 'formatDocumentGrade',
       column_editable = 'N'
 WHERE grid_id = 'gridSwRequestList'
   AND column_id = 'gradeNm';

INSERT INTO docs_grid_info (
    grid_id, column_id, column_nm, column_seq, column_size,
    column_type, column_align, column_format, column_hidden,
    sort_yn, formatter, column_editable
)
SELECT
    'gridSwRequestList', 'gradeNm', '문서등급', 52, 90,
    'ro', 'center', 'str', 'N',
    'N', 'formatDocumentGrade', 'N'
WHERE NOT EXISTS (
    SELECT 1
      FROM docs_grid_info
     WHERE grid_id = 'gridSwRequestList'
       AND column_id = 'gradeNm'
);

INSERT INTO docs_grid_info (
    grid_id, column_id, column_nm, column_seq, column_size,
    column_type, column_align, column_format, column_hidden,
    sort_yn, column_editable
)
SELECT
    'gridSwRequestList', 'gradeCd', 'gradeCd', 212, 0,
    'ro', 'center', 'str', 'Y',
    'N', 'N'
WHERE NOT EXISTS (
    SELECT 1
      FROM docs_grid_info
     WHERE grid_id = 'gridSwRequestList'
       AND column_id = 'gradeCd'
);

INSERT INTO docs_grid_info (
    grid_id, column_id, column_nm, column_seq, column_size,
    column_type, column_align, column_format, column_hidden,
    sort_yn, column_editable
)
SELECT
    'gridSwRequestList', 'gradeLevel', 'gradeLevel', 213, 0,
    'ro', 'center', 'str', 'Y',
    'N', 'N'
WHERE NOT EXISTS (
    SELECT 1
      FROM docs_grid_info
     WHERE grid_id = 'gridSwRequestList'
       AND column_id = 'gradeLevel'
);

-- Technical-data list: show the distinct extensions of every active file in
-- the transmittal without requiring the user to open the detail popup.
UPDATE docs_grid_info
   SET column_nm = '파일 확장자',
       column_seq = 54,
       column_size = 170,
       column_type = 'ro',
       column_align = 'center',
       column_format = 'str',
       column_hidden = 'N',
       sort_yn = 'N',
       formatter = 'formatFileExtensions',
       column_editable = 'N',
       lang_cd = 'grid.fileExtensions'
 WHERE grid_id = 'gridSwRequestList'
   AND column_id = 'fileExtensions';

INSERT INTO docs_grid_info (
    grid_id, column_id, column_nm, column_seq, column_size,
    column_type, column_align, column_format, column_hidden,
    sort_yn, formatter, column_editable, lang_cd
)
SELECT
    'gridSwRequestList', 'fileExtensions', '파일 확장자', 54, 170,
    'ro', 'center', 'str', 'N',
    'N', 'formatFileExtensions', 'N', 'grid.fileExtensions'
WHERE NOT EXISTS (
    SELECT 1
      FROM docs_grid_info
     WHERE grid_id = 'gridSwRequestList'
       AND column_id = 'fileExtensions'
);

INSERT INTO docs_lang (lang_type, lang_cd, lang_desc)
VALUES
    ('ko', 'grid.fileExtensions', '파일 확장자'),
    ('en', 'grid.fileExtensions', 'File Extensions')
ON CONFLICT (lang_type, lang_cd) DO UPDATE
   SET lang_desc = EXCLUDED.lang_desc;

-- User management: expose the user's assigned clearance together with enough
-- hidden metadata for a locale-aware, validity-aware grade formatter.
UPDATE docs_grid_info
   SET column_seq = 65
 WHERE grid_id = 'gridInsideUserList'
   AND column_id = 'lastLoginDt';

WITH clearance_columns (
    column_id, column_nm, column_seq, column_size,
    column_hidden, formatter, lang_cd
) AS (
    VALUES
        ('clearanceGradeNm',    '현재 인가등급', 64, 14, 'N', 'formatUserClearance', 'grid.currentClearance'),
        ('clearanceGradeCd',    'clearanceGradeCd', 91, 0, 'Y', NULL, NULL),
        ('clearanceGradeLevel', 'clearanceGradeLevel', 92, 0, 'Y', NULL, NULL),
        ('clearanceStatus',     'clearanceStatus', 93, 0, 'Y', NULL, NULL),
        ('clearanceValidFrom',  'clearanceValidFrom', 94, 0, 'Y', NULL, NULL),
        ('clearanceValidTo',    'clearanceValidTo', 95, 0, 'Y', NULL, NULL)
)
UPDATE docs_grid_info grid
   SET column_nm = clearance.column_nm,
       column_seq = clearance.column_seq,
       column_size = clearance.column_size,
       column_type = 'ro',
       column_align = 'center',
       column_format = 'str',
       column_hidden = clearance.column_hidden,
       sort_yn = 'N',
       formatter = clearance.formatter,
       column_editable = 'N',
       lang_cd = clearance.lang_cd
  FROM clearance_columns clearance
 WHERE grid.grid_id = 'gridInsideUserList'
   AND grid.column_id = clearance.column_id;

WITH clearance_columns (
    column_id, column_nm, column_seq, column_size,
    column_hidden, formatter, lang_cd
) AS (
    VALUES
        ('clearanceGradeNm',    '현재 인가등급', 64, 14, 'N', 'formatUserClearance', 'grid.currentClearance'),
        ('clearanceGradeCd',    'clearanceGradeCd', 91, 0, 'Y', NULL, NULL),
        ('clearanceGradeLevel', 'clearanceGradeLevel', 92, 0, 'Y', NULL, NULL),
        ('clearanceStatus',     'clearanceStatus', 93, 0, 'Y', NULL, NULL),
        ('clearanceValidFrom',  'clearanceValidFrom', 94, 0, 'Y', NULL, NULL),
        ('clearanceValidTo',    'clearanceValidTo', 95, 0, 'Y', NULL, NULL)
)
INSERT INTO docs_grid_info (
    grid_id, column_id, column_nm, column_seq, column_size,
    column_type, column_align, column_format, column_hidden,
    sort_yn, formatter, column_editable, lang_cd
)
SELECT
    'gridInsideUserList', clearance.column_id, clearance.column_nm,
    clearance.column_seq, clearance.column_size,
    'ro', 'center', 'str', clearance.column_hidden,
    'N', clearance.formatter, 'N', clearance.lang_cd
  FROM clearance_columns clearance
 WHERE NOT EXISTS (
    SELECT 1
      FROM docs_grid_info grid
     WHERE grid.grid_id = 'gridInsideUserList'
       AND grid.column_id = clearance.column_id
);

INSERT INTO docs_lang (lang_type, lang_cd, lang_desc)
VALUES
    ('ko', 'grid.currentClearance', '현재 인가등급'),
    ('en', 'grid.currentClearance', 'Current Clearance')
ON CONFLICT (lang_type, lang_cd) DO UPDATE
   SET lang_desc = EXCLUDED.lang_desc;

-- Technical-data status: remove fields that are not part of the current screen.
DELETE FROM docs_grid_info
 WHERE grid_id = 'gridSwRequestList'
   AND column_id IN (
       'swTypeNm',
       'revNo',
       'swVersionNo',
       'businessTypeNm',
       'distributeTypeNm',
       'businessAreaNm',
       'createDt',
       'ccbDate',
       'updateUserNm',
       'updateDt',
       'interfaceDt',
       'stdGappDt',
       'changeGappDt',
       'ecnUserNm',
       'ecnNo',
       'validType',
       'reviewerUser'
   );

DELETE FROM docs_form_info
 WHERE form_id = 'formSwRequest'
   AND column_id IN (
       'swTypeCd',
       'version',
       'distributeTypeCd',
       'businessAreaCd',
       'ccbDate',
       'interfaceStartDt,interfaceEndDt',
       'ecnNo',
       'validType'
   );

UPDATE docs_grid_info
   SET column_nm = '의뢰일자',
       column_size = 100
 WHERE grid_id = 'gridSwRequestList'
   AND column_id = 'insertDt';

UPDATE docs_form_info
   SET column_nm = '의뢰일자'
 WHERE form_id = 'formSwRequest'
   AND column_id = 'insertStartDt,insertEndDt';

-- Technical-data list: dashboard, update and withdrawal actions are not exposed.
UPDATE docs_toolbar_info
   SET use_yn = 'N'
 WHERE toolbar_id = 'toolbarSwRequest'
   AND button_id IN ('btnDashboard', 'btnUpdate', 'btnDelete');

-- System-management navigation repair.
-- MENU_137 was intentionally hidden with the legacy system-common features,
-- which also made its still-supported permission screens unreachable. Re-parent
-- those screens under the active MENU_214 root and use reader-facing names that
-- describe what each screen actually manages.
UPDATE docs_menu
   SET menu_nm = '분류/레벨 관리',
       message_cd = '',
       tooltip = '기술자료 분류 및 Level 관리',
       menu_desc = '기술자료 분류와 Level 코드를 등록·수정·삭제',
       sort_seq = 1,
       use_yn = 'Y',
       del_yn = 'N'
 WHERE menu_cd = 'MENU_215';

UPDATE docs_menu
   SET parent_menu_cd = 'MENU_214',
       menu_nm = '메뉴권한',
       menu_level = '2',
       sort_seq = 2,
       tree_type = 'leaf',
       use_yn = 'Y',
       del_yn = 'N'
 WHERE menu_cd = 'MENU_138';

UPDATE docs_menu
   SET parent_menu_cd = 'MENU_214',
       menu_nm = '사용자등급',
       menu_level = '2',
       sort_seq = 3,
       tree_type = 'leaf',
       use_yn = 'Y',
       del_yn = 'N'
 WHERE menu_cd = 'MENU_141';

UPDATE docs_menu
   SET parent_menu_cd = 'MENU_214',
       menu_nm = '메뉴권한배정',
       menu_level = '2',
       sort_seq = 4,
       tree_type = 'leaf',
       use_yn = 'Y',
       del_yn = 'N'
 WHERE menu_cd = 'MENU_160';

-- Security-grade and clearance administration belongs to the user-management
-- domain while retaining its stable URL and role code.
UPDATE docs_menu
   SET parent_menu_cd = 'MENU_071',
       menu_level = '2',
       sort_seq = 93,
       tree_type = 'leaf',
       use_yn = 'Y',
       del_yn = 'N'
 WHERE menu_cd = 'MENU_222';

-- Authentication/menu actions and document ACL decisions already share the
-- canonical docs_access_audit_log ledger. Expose that complete ledger through
-- one history menu and retire the former access-only subset screen.
INSERT INTO docs_menu (
    menu_cd, parent_menu_cd, menu_nm, message_cd, menu_level, menu_type,
    menu_url, sort_seq, tree_type, del_yn, use_yn, tooltip, menu_desc,
    role_cd, menu_icon
)
VALUES (
    'MENU_218', 'MENU_223', '접근·감사이력', 'menu.accessAuditHistory', '2', 'M',
    '/general/organizationmanage/auditlog/**', 116, 'leaf', 'N', 'Y',
    '사용자 접근 및 운영 감사 이력',
    '인증·메뉴 행위와 문서 ACL 접근 이벤트를 한 화면에서 조회',
    'ROLE_MENU_218', ''
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
    tooltip = EXCLUDED.tooltip,
    menu_desc = EXCLUDED.menu_desc,
    role_cd = EXCLUDED.role_cd,
    use_yn = 'Y',
    del_yn = 'N';

-- Preserve the union of both former menu audiences before removing the stale
-- ROLE_MENU_206 group links.
INSERT INTO docs_rel_role_group
    (group_cd, role_cd, insert_user_cd, update_user_cd, insert_dt, update_dt)
SELECT source.group_cd,
       'ROLE_MENU_218',
       'SYSTEM',
       'SYSTEM',
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
  FROM docs_rel_role_group source
 WHERE source.role_cd = 'ROLE_MENU_206'
ON CONFLICT (group_cd, role_cd) DO NOTHING;

-- A group must own both the root and child role for the recursive navigation
-- query. Preserve every established assignment on the combined menu.
INSERT INTO docs_rel_role_group
    (group_cd, role_cd, insert_user_cd, update_user_cd, insert_dt, update_dt)
SELECT source.group_cd,
       'ROLE_MENU_223',
       'SYSTEM',
       'SYSTEM',
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
  FROM docs_rel_role_group source
 WHERE source.role_cd = 'ROLE_MENU_218'
ON CONFLICT (group_cd, role_cd) DO NOTHING;

DELETE FROM docs_rel_role_group
 WHERE role_cd = 'ROLE_MENU_206';

DELETE FROM docs_menu
 WHERE menu_cd = 'MENU_206';

-- Reader-facing navigation and the metadata introduced by this migration must
-- use stable message codes. A future Indonesian rollout only needs matching
-- LANG_TYPE='id' rows; no menu or grid schema change is required.
WITH menu_i18n (menu_cd, message_cd) AS (
    VALUES
        ('MENU_223', 'menu.historyManagement'),
        ('MENU_224', 'menu.viewHistory'),
        ('MENU_225', 'menu.printHistory'),
        ('MENU_222', 'menu.securityAccess'),
        ('MENU_215', 'menu.classificationLevel'),
        ('MENU_138', 'menu.menuPermission'),
        ('MENU_141', 'menu.userGrade'),
        ('MENU_160', 'menu.menuPermissionAssignment'),
        ('MENU_032', 'menu.printApprovalDisposal'),
        ('MENU_218', 'menu.accessAuditHistory')
)
UPDATE docs_menu menu
   SET message_cd = menu_i18n.message_cd
  FROM menu_i18n
 WHERE menu.menu_cd = menu_i18n.menu_cd;

UPDATE docs_grid_info
   SET column_nm = CASE column_id
       WHEN 'swNo' THEN '자료번호'
       WHEN 'swNm' THEN '의뢰명'
       ELSE column_nm
   END,
       lang_cd = CASE column_id
       WHEN 'swNo' THEN 'grid.transmittalNo'
       WHEN 'swNm' THEN 'grid.requestName'
       WHEN 'gradeNm' THEN 'grid.documentGrade'
       WHEN 'insertDt' THEN 'grid.requestDate'
       WHEN 'insertUser' THEN 'grid.registrant'
       WHEN 'insertUserNm' THEN 'grid.registrant'
       ELSE lang_cd
   END
 WHERE grid_id = 'gridSwRequestList'
   AND column_id IN (
       'swNo', 'swNm', 'gradeNm', 'insertDt', 'insertUser', 'insertUserNm'
   );

UPDATE docs_form_info
   SET column_nm = CASE column_id
       WHEN 'swNo' THEN '자료번호'
       ELSE column_nm
   END,
       lang_cd = CASE column_id
       WHEN 'swNo' THEN 'form.transmittalNo'
       WHEN 'swNm' THEN 'form.requestName'
       WHEN 'insertStartDt,insertEndDt' THEN 'form.requestDate'
       ELSE lang_cd
   END
 WHERE form_id = 'formSwRequest'
   AND column_id IN ('swNo', 'swNm', 'insertStartDt,insertEndDt');

UPDATE docs_toolbar_info
   SET lang_cd = 'toolbar.excel'
 WHERE toolbar_id = 'toolbarSwRequest'
   AND button_id = 'btnExcel';

INSERT INTO docs_lang (lang_type, lang_cd, lang_desc)
VALUES
    ('ko', 'menu.historyManagement', '이력관리'),
    ('en', 'menu.historyManagement', 'History Management'),
    ('ko', 'menu.accessAuditHistory', '접근·감사이력'),
    ('en', 'menu.accessAuditHistory', 'Access & Audit History'),
    ('ko', 'menu.viewHistory', '열람이력'),
    ('en', 'menu.viewHistory', 'View History'),
    ('ko', 'menu.printHistory', '출력이력'),
    ('en', 'menu.printHistory', 'Print History'),
    ('ko', 'menu.securityAccess', '보안등급/인가 관리'),
    ('en', 'menu.securityAccess', 'Security Grade / Clearance'),
    ('ko', 'menu.classificationLevel', '분류/레벨 관리'),
    ('en', 'menu.classificationLevel', 'Classification / Level'),
    ('ko', 'menu.menuPermission', '메뉴권한'),
    ('en', 'menu.menuPermission', 'Menu Permissions'),
    ('ko', 'menu.userGrade', '사용자등급'),
    ('en', 'menu.userGrade', 'User Grades'),
    ('ko', 'menu.menuPermissionAssignment', '메뉴권한배정'),
    ('en', 'menu.menuPermissionAssignment', 'Menu Permission Assignment'),
    ('ko', 'menu.printApprovalDisposal', '출력 승인/폐기 관리'),
    ('en', 'menu.printApprovalDisposal', 'Print Approval / Disposal'),
    ('ko', 'grid.documentGrade', '문서등급'),
    ('en', 'grid.documentGrade', 'Document Grade'),
    ('ko', 'grid.transmittalNo', '자료번호'),
    ('en', 'grid.transmittalNo', 'Data No.'),
    ('ko', 'grid.requestName', '의뢰명'),
    ('en', 'grid.requestName', 'Request Name'),
    ('ko', 'grid.requestDate', '의뢰일자'),
    ('en', 'grid.requestDate', 'Request Date'),
    ('ko', 'grid.registrant', '등록자'),
    ('en', 'grid.registrant', 'Registered By'),
    ('ko', 'form.transmittalNo', '자료번호'),
    ('en', 'form.transmittalNo', 'Data No.'),
    ('ko', 'form.requestName', '의뢰명'),
    ('en', 'form.requestName', 'Request Name'),
    ('ko', 'form.requestDate', '의뢰일자'),
    ('en', 'form.requestDate', 'Request Date'),
    ('ko', 'toolbar.excel', '엑셀'),
    ('en', 'toolbar.excel', 'Excel')
ON CONFLICT (lang_type, lang_cd) DO UPDATE
   SET lang_desc = EXCLUDED.lang_desc;

DELETE FROM docs_lang
 WHERE lang_cd IN ('menu.accessHistory', 'menu.auditlog');

-- ---------------------------------------------------------------------------
-- Current menu ACL repair
-- ---------------------------------------------------------------------------
-- These disconnected rows belong to the removed request-approval,
-- external-company and user-approval workflows. Keeping them active creates
-- parentless jsTree nodes and exposes a workflow that this system does not use.
UPDATE docs_menu
   SET use_yn = 'N',
       del_yn = 'Y'
 WHERE menu_cd IN (
       'MENU_019', 'MENU_074', 'MENU_075', 'MENU_076', 'MENU_077',
       'MENU_079'
   );

DELETE FROM docs_rel_role_group
 WHERE role_cd IN (
       'ROLE_MENU_019', 'ROLE_MENU_074', 'ROLE_MENU_075',
       'ROLE_MENU_076', 'ROLE_MENU_077', 'ROLE_MENU_079'
   );

-- The administrator owns every active internal/shared menu role.
INSERT INTO docs_rel_role_group (
    group_cd, role_cd, insert_user_cd, update_user_cd, insert_dt, update_dt
)
SELECT 'RG_001',
       menu.role_cd,
       'SYSTEM',
       'SYSTEM',
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
  FROM docs_menu menu
 WHERE menu.del_yn = 'N'
   AND menu.use_yn = 'Y'
   AND NULLIF(BTRIM(menu.role_cd), '') IS NOT NULL
ON CONFLICT (group_cd, role_cd) DO NOTHING;

-- Fail deployment if an assignable menu is disconnected from the current
-- ROOT tree. Runtime queries are defensive too, but bad ACL data must not pass
-- an installation unnoticed.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
          FROM docs_menu
         WHERE parent_menu_cd IN ('I', 'B', 'E')
    ) AND EXISTS (
        WITH RECURSIVE eligible AS (
            SELECT menu_cd, parent_menu_cd
              FROM docs_menu
             WHERE del_yn = 'N'
               AND menu_type IN ('T', 'M', 'P')
        ),
        connected AS (
            SELECT menu_cd,
                   parent_menu_cd,
                   ARRAY[menu_cd::TEXT]::TEXT[] AS path
              FROM eligible
             WHERE parent_menu_cd = 'ROOT'

            UNION ALL

            SELECT child.menu_cd,
                   child.parent_menu_cd,
                   parent.path || child.menu_cd::TEXT
              FROM eligible child
              JOIN connected parent
                ON child.parent_menu_cd = parent.menu_cd
             WHERE NOT child.menu_cd::TEXT = ANY(parent.path)
        )
        SELECT 1
          FROM eligible menu
         WHERE NOT EXISTS (
               SELECT 1
                 FROM connected
                WHERE connected.menu_cd = menu.menu_cd
         )
    ) THEN
        RAISE EXCEPTION
            'Active internal menu tree contains disconnected nodes.';
    END IF;
END
$$;

COMMIT;
