-- Technical-data distribution workflow (MVP)
--
-- This migration is intentionally repeatable and PostgreSQL-only. It owns no
-- external HTTP integration. An approval produces one immutable HOLD outbox
-- snapshot for a later, separately authorized interface phase.

\set ON_ERROR_STOP on

BEGIN;

CREATE SEQUENCE IF NOT EXISTS docs_distribution_request_id_seq;
CREATE SEQUENCE IF NOT EXISTS docs_distribution_request_no_seq;
CREATE SEQUENCE IF NOT EXISTS docs_distribution_request_item_id_seq;
CREATE SEQUENCE IF NOT EXISTS docs_distribution_request_recipient_id_seq;
CREATE SEQUENCE IF NOT EXISTS docs_distribution_request_event_id_seq;
CREATE SEQUENCE IF NOT EXISTS docs_distribution_outbox_id_seq;

CREATE TABLE IF NOT EXISTS docs_distribution_request (
    request_id              bigint PRIMARY KEY
                            DEFAULT nextval('docs_distribution_request_id_seq'),
    request_no              varchar(40) NOT NULL,
    title                   varchar(200) NOT NULL,
    purpose                 varchar(2000) NOT NULL DEFAULT '',
    status                  varchar(30) NOT NULL DEFAULT 'DRAFT',
    requested_by_user_cd    varchar(64) NOT NULL,
    requested_by_user_id    varchar(64),
    requested_by_user_nm    varchar(200),
    requested_dept_cd       varchar(64),
    requested_dept_nm       varchar(200),
    partner_company_id      bigint NOT NULL,
    partner_company_code    varchar(40) NOT NULL,
    partner_company_name    varchar(200) NOT NULL,
    approver_user_cd        varchar(64) NOT NULL,
    approver_user_id        varchar(64),
    approver_user_nm        varchar(200) NOT NULL,
    distribution_start_date date NOT NULL
        DEFAULT ((CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Seoul')::date),
    distribution_end_date   date NOT NULL
        DEFAULT (((CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Seoul')::date) + 7),
    submitted_at            timestamptz,
    decided_at              timestamptz,
    decided_by_user_cd      varchar(64),
    decided_by_user_id      varchar(64),
    decided_by_user_nm      varchar(200),
    decision_comment        varchar(1000),
    version_no              integer NOT NULL DEFAULT 0,
    created_by              varchar(64) NOT NULL,
    created_at              timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by              varchar(64) NOT NULL,
    updated_at              timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_docs_distribution_request_no UNIQUE (request_no),
    CONSTRAINT fk_docs_distribution_partner_company FOREIGN KEY (partner_company_id)
        REFERENCES docs_partner_company (partner_company_id) ON DELETE RESTRICT,
    CONSTRAINT fk_docs_distribution_approver FOREIGN KEY (approver_user_cd)
        REFERENCES docs_user (user_cd) ON DELETE RESTRICT,
    CONSTRAINT ck_docs_distribution_period CHECK (
        distribution_end_date >= distribution_start_date
    ),
    CONSTRAINT ck_docs_distribution_request_status CHECK (
        status IN ('DRAFT', 'PENDING_APPROVAL', 'APPROVED', 'REJECTED', 'CANCELLED', 'EXPIRED')
    )
);

-- Keep the migration repeatable for development databases that already have
-- the initial workflow tables. Columns are added nullable first so legacy rows
-- can be normalized before the current NOT NULL and foreign-key contract is
-- enforced.
ALTER TABLE docs_distribution_request
    ADD COLUMN IF NOT EXISTS partner_company_id bigint,
    ADD COLUMN IF NOT EXISTS partner_company_code varchar(40),
    ADD COLUMN IF NOT EXISTS partner_company_name varchar(200),
    ADD COLUMN IF NOT EXISTS approver_user_cd varchar(64),
    ADD COLUMN IF NOT EXISTS approver_user_id varchar(64),
    ADD COLUMN IF NOT EXISTS approver_user_nm varchar(200),
    ADD COLUMN IF NOT EXISTS distribution_start_date date
        DEFAULT ((CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Seoul')::date),
    ADD COLUMN IF NOT EXISTS distribution_end_date date
        DEFAULT (((CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Seoul')::date) + 7);

-- Keep the partner allocator ahead of existing identifiers before reserving an
-- inactive destination for legacy requests whose original destination was not
-- captured by the initial MVP schema.
SELECT SETVAL(
    'docs_partner_company_id_seq',
    GREATEST(sequence_state.last_value, existing_company.max_value, 1),
    sequence_state.is_called
        OR existing_company.max_value >= sequence_state.last_value
)
  FROM docs_partner_company_id_seq sequence_state
 CROSS JOIN LATERAL (
       SELECT COALESCE(MAX(partner_company_id), 0) AS max_value
         FROM docs_partner_company
 ) existing_company;

INSERT INTO docs_partner_company (
    partner_company_id, company_code, company_name, use_yn, del_yn,
    created_by, created_at, updated_by, updated_at
)
SELECT nextval('docs_partner_company_id_seq'),
       'TDMS-LEGACY-UNASSIGNED',
       'Legacy request - destination unassigned',
       'N', 'Y', 'SYSTEM', CURRENT_TIMESTAMP, 'SYSTEM', CURRENT_TIMESTAMP
 WHERE EXISTS (
       SELECT 1
         FROM docs_distribution_request request_row
         LEFT JOIN docs_partner_company company
           ON company.partner_company_id = request_row.partner_company_id
        WHERE company.partner_company_id IS NULL
           OR COALESCE(BTRIM(company.company_code), '') = ''
           OR COALESCE(BTRIM(company.company_name), '') = ''
 )
ON CONFLICT (company_code) DO UPDATE SET
    company_name = EXCLUDED.company_name,
    use_yn = 'N',
    del_yn = 'Y',
    updated_by = 'SYSTEM',
    updated_at = CURRENT_TIMESTAMP;

-- Any row that needs a synthetic destination, fallback approver, or repaired
-- period is made terminal before backfill. This preserves the historical row
-- without allowing an incomplete legacy approval to enter a future sender.
UPDATE docs_distribution_request request_row
   SET status = CASE
           WHEN request_row.status IN ('REJECTED', 'CANCELLED', 'EXPIRED')
               THEN request_row.status
           ELSE 'CANCELLED'
       END,
       updated_by = 'SYSTEM',
       updated_at = CURRENT_TIMESTAMP
 WHERE request_row.partner_company_id IS NULL
    OR NOT EXISTS (
       SELECT 1
         FROM docs_partner_company company
        WHERE company.partner_company_id = request_row.partner_company_id
          AND COALESCE(BTRIM(company.company_code), '') <> ''
          AND COALESCE(BTRIM(company.company_name), '') <> ''
    )
    OR request_row.approver_user_cd IS NULL
    OR NOT EXISTS (
       SELECT 1
         FROM docs_user approver
        WHERE approver.user_cd = request_row.approver_user_cd
    )
    OR request_row.distribution_start_date IS NULL
    OR request_row.distribution_end_date IS NULL
    OR request_row.distribution_end_date < request_row.distribution_start_date;

UPDATE docs_distribution_request request_row
   SET partner_company_code = company.company_code,
       partner_company_name = company.company_name
  FROM docs_partner_company company
 WHERE company.partner_company_id = request_row.partner_company_id
   AND (
       COALESCE(BTRIM(request_row.partner_company_code), '') = ''
       OR COALESCE(BTRIM(request_row.partner_company_name), '') = ''
   );

UPDATE docs_distribution_request request_row
   SET partner_company_id = company.partner_company_id,
       partner_company_code = company.company_code,
       partner_company_name = company.company_name
  FROM docs_partner_company company
 WHERE company.company_code = 'TDMS-LEGACY-UNASSIGNED'
   AND (
       request_row.partner_company_id IS NULL
       OR NOT EXISTS (
          SELECT 1
            FROM docs_partner_company current_company
           WHERE current_company.partner_company_id = request_row.partner_company_id
             AND COALESCE(BTRIM(current_company.company_code), '') <> ''
             AND COALESCE(BTRIM(current_company.company_name), '') <> ''
       )
   );

-- Prefer the original requester as the legacy approver when that internal user
-- still exists. No approval action is implied because affected live requests
-- were made terminal above.
UPDATE docs_distribution_request request_row
   SET approver_user_cd = requester.user_cd,
       approver_user_id = requester.user_id,
       approver_user_nm = COALESCE(
           NULLIF(BTRIM(requester.user_nm), ''),
           NULLIF(BTRIM(requester.user_id), ''),
           requester.user_cd
       )
  FROM docs_user requester
 WHERE requester.user_cd = request_row.requested_by_user_cd
   AND (
       request_row.approver_user_cd IS NULL
       OR NOT EXISTS (
          SELECT 1
            FROM docs_user current_approver
           WHERE current_approver.user_cd = request_row.approver_user_cd
       )
   );

DO $backfill_distribution_approver$
DECLARE
    fallback_user_cd docs_user.user_cd%TYPE;
    fallback_user_id docs_user.user_id%TYPE;
    fallback_user_nm docs_user.user_nm%TYPE;
BEGIN
    IF EXISTS (
        SELECT 1
          FROM docs_distribution_request request_row
         WHERE request_row.approver_user_cd IS NULL
            OR NOT EXISTS (
               SELECT 1
                 FROM docs_user approver
                WHERE approver.user_cd = request_row.approver_user_cd
            )
    ) THEN
        SELECT candidate.user_cd,
               candidate.user_id,
               COALESCE(
                   NULLIF(BTRIM(candidate.user_nm), ''),
                   NULLIF(BTRIM(candidate.user_id), ''),
                   candidate.user_cd
               )
          INTO fallback_user_cd, fallback_user_id, fallback_user_nm
          FROM docs_user candidate
          LEFT JOIN docs_role_group_member administrator
            ON administrator.member_cd = candidate.user_cd
           AND administrator.group_type = 'USER'
           AND administrator.group_code = 'RG_001'
         ORDER BY CASE WHEN administrator.member_cd IS NOT NULL THEN 0 ELSE 1 END,
                  CASE
                      WHEN candidate.use_yn = 'Y' AND candidate.del_yn = 'N'
                          THEN 0
                      ELSE 1
                  END,
                  candidate.user_cd
         LIMIT 1;

        IF fallback_user_cd IS NULL THEN
            RAISE EXCEPTION
                'Cannot backfill legacy distribution approver: docs_user is empty.';
        END IF;

        UPDATE docs_distribution_request request_row
           SET approver_user_cd = fallback_user_cd,
               approver_user_id = fallback_user_id,
               approver_user_nm = fallback_user_nm
         WHERE request_row.approver_user_cd IS NULL
            OR NOT EXISTS (
               SELECT 1
                 FROM docs_user approver
                WHERE approver.user_cd = request_row.approver_user_cd
            );
    END IF;
END
$backfill_distribution_approver$;

UPDATE docs_distribution_request request_row
   SET approver_user_id = COALESCE(
           NULLIF(BTRIM(request_row.approver_user_id), ''),
           approver.user_id
       ),
       approver_user_nm = COALESCE(
           NULLIF(BTRIM(request_row.approver_user_nm), ''),
           NULLIF(BTRIM(approver.user_nm), ''),
           NULLIF(BTRIM(approver.user_id), ''),
           approver.user_cd
       )
  FROM docs_user approver
 WHERE approver.user_cd = request_row.approver_user_cd
   AND (
       COALESCE(BTRIM(request_row.approver_user_nm), '') = ''
       OR COALESCE(BTRIM(request_row.approver_user_id), '') = ''
   );

WITH normalized_period AS (
    SELECT request_id,
           COALESCE(
               distribution_start_date,
               (COALESCE(created_at, CURRENT_TIMESTAMP)
                   AT TIME ZONE 'Asia/Seoul')::date
           ) AS start_date,
           distribution_end_date
      FROM docs_distribution_request
)
UPDATE docs_distribution_request request_row
   SET distribution_start_date = period.start_date,
       distribution_end_date = CASE
           WHEN period.distribution_end_date IS NULL
             OR period.distribution_end_date < period.start_date
               THEN period.start_date + 7
           ELSE period.distribution_end_date
       END
  FROM normalized_period period
 WHERE period.request_id = request_row.request_id
   AND (
       request_row.distribution_start_date IS NULL
       OR request_row.distribution_end_date IS NULL
       OR request_row.distribution_end_date < period.start_date
   );

DO $validate_distribution_request_backfill$
BEGIN
    IF EXISTS (
        SELECT 1
          FROM docs_distribution_request request_row
          LEFT JOIN docs_partner_company company
            ON company.partner_company_id = request_row.partner_company_id
          LEFT JOIN docs_user approver
            ON approver.user_cd = request_row.approver_user_cd
         WHERE company.partner_company_id IS NULL
            OR approver.user_cd IS NULL
            OR COALESCE(BTRIM(request_row.partner_company_code), '') = ''
            OR COALESCE(BTRIM(request_row.partner_company_name), '') = ''
            OR COALESCE(BTRIM(request_row.approver_user_nm), '') = ''
            OR request_row.distribution_start_date IS NULL
            OR request_row.distribution_end_date IS NULL
            OR request_row.distribution_end_date < request_row.distribution_start_date
    ) THEN
        RAISE EXCEPTION
            'Legacy distribution request backfill did not satisfy the current contract.';
    END IF;
END
$validate_distribution_request_backfill$;

ALTER TABLE docs_distribution_request
    ALTER COLUMN partner_company_id SET NOT NULL,
    ALTER COLUMN partner_company_code SET NOT NULL,
    ALTER COLUMN partner_company_name SET NOT NULL,
    ALTER COLUMN approver_user_cd SET NOT NULL,
    ALTER COLUMN approver_user_nm SET NOT NULL,
    ALTER COLUMN distribution_start_date SET DEFAULT
        ((CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Seoul')::date),
    ALTER COLUMN distribution_start_date SET NOT NULL,
    ALTER COLUMN distribution_end_date SET DEFAULT
        (((CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Seoul')::date) + 7),
    ALTER COLUMN distribution_end_date SET NOT NULL;

ALTER TABLE docs_distribution_request
    DROP CONSTRAINT IF EXISTS fk_docs_distribution_partner_company,
    DROP CONSTRAINT IF EXISTS fk_docs_distribution_approver;
ALTER TABLE docs_distribution_request
    ADD CONSTRAINT fk_docs_distribution_partner_company FOREIGN KEY (partner_company_id)
        REFERENCES docs_partner_company (partner_company_id) ON DELETE RESTRICT,
    ADD CONSTRAINT fk_docs_distribution_approver FOREIGN KEY (approver_user_cd)
        REFERENCES docs_user (user_cd) ON DELETE RESTRICT;

ALTER TABLE docs_distribution_request
    DROP CONSTRAINT IF EXISTS ck_docs_distribution_request_status;
ALTER TABLE docs_distribution_request
    ADD CONSTRAINT ck_docs_distribution_request_status CHECK (
        status IN ('DRAFT', 'PENDING_APPROVAL', 'APPROVED', 'REJECTED', 'CANCELLED', 'EXPIRED')
    );
ALTER TABLE docs_distribution_request
    DROP CONSTRAINT IF EXISTS ck_docs_distribution_period;
ALTER TABLE docs_distribution_request
    ADD CONSTRAINT ck_docs_distribution_period CHECK (
        distribution_end_date >= distribution_start_date
    );

ALTER SEQUENCE docs_distribution_request_id_seq
    OWNED BY docs_distribution_request.request_id;

CREATE TABLE IF NOT EXISTS docs_distribution_request_item (
    item_id                 bigint PRIMARY KEY
                            DEFAULT nextval('docs_distribution_request_item_id_seq'),
    request_id              bigint NOT NULL,
    line_no                 integer NOT NULL,
    document_line_no        integer NOT NULL,
    file_line_no            integer NOT NULL,
    object_type             varchar(30) NOT NULL,
    object_id               varchar(128) NOT NULL,
    file_no                 varchar(50) NOT NULL,
    material_no             varchar(200) NOT NULL,
    material_name           varchar(500) NOT NULL DEFAULT '',
    original_file_name      varchar(500) NOT NULL,
    file_size               bigint NOT NULL DEFAULT 0,
    grade_cd                varchar(50) NOT NULL,
    tree_cd                 varchar(50) NOT NULL,
    tree_nm                 varchar(500) NOT NULL,
    parent_tree_cd          varchar(50) NOT NULL,
    parent_tree_nm          varchar(500) NOT NULL,
    snapshot_at             timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_docs_distribution_item_request FOREIGN KEY (request_id)
        REFERENCES docs_distribution_request (request_id) ON DELETE CASCADE,
    CONSTRAINT uq_docs_distribution_item_line UNIQUE (request_id, line_no),
    CONSTRAINT uq_docs_distribution_item_resource
        UNIQUE (request_id, object_type, object_id, file_no),
    CONSTRAINT ck_docs_distribution_item_type CHECK (object_type IN ('SW', 'SW_SUB')),
    CONSTRAINT ck_docs_distribution_item_line CHECK (line_no > 0),
    CONSTRAINT ck_docs_distribution_document_line CHECK (document_line_no > 0),
    CONSTRAINT ck_docs_distribution_file_line CHECK (file_line_no > 0),
    CONSTRAINT ck_docs_distribution_item_size CHECK (file_size >= 0)
);

ALTER TABLE docs_distribution_request_item
    ADD COLUMN IF NOT EXISTS document_line_no integer NOT NULL DEFAULT 1,
    ADD COLUMN IF NOT EXISTS file_line_no integer NOT NULL DEFAULT 1,
    ADD COLUMN IF NOT EXISTS tree_cd varchar(50) NOT NULL DEFAULT '',
    ADD COLUMN IF NOT EXISTS tree_nm varchar(500) NOT NULL DEFAULT '',
    ADD COLUMN IF NOT EXISTS parent_tree_cd varchar(50) NOT NULL DEFAULT '',
    ADD COLUMN IF NOT EXISTS parent_tree_nm varchar(500) NOT NULL DEFAULT '';

ALTER SEQUENCE docs_distribution_request_item_id_seq
    OWNED BY docs_distribution_request_item.item_id;

CREATE TABLE IF NOT EXISTS docs_distribution_request_recipient (
    recipient_id            bigint PRIMARY KEY
                            DEFAULT nextval('docs_distribution_request_recipient_id_seq'),
    request_id              bigint NOT NULL,
    line_no                 integer NOT NULL,
    partner_company_id      bigint NOT NULL,
    partner_user_id         bigint NOT NULL,
    user_name               varchar(100) NOT NULL,
    email                   varchar(254),
    phone                   varchar(40),
    representative_yn       char(1) NOT NULL DEFAULT 'N',
    snapshot_at             timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_docs_distribution_recipient_request FOREIGN KEY (request_id)
        REFERENCES docs_distribution_request (request_id) ON DELETE CASCADE,
    CONSTRAINT fk_docs_distribution_recipient_company FOREIGN KEY (partner_company_id)
        REFERENCES docs_partner_company (partner_company_id) ON DELETE RESTRICT,
    CONSTRAINT fk_docs_distribution_recipient_user FOREIGN KEY (partner_user_id)
        REFERENCES docs_partner_user (partner_user_id) ON DELETE RESTRICT,
    CONSTRAINT uq_docs_distribution_recipient_line UNIQUE (request_id, line_no),
    CONSTRAINT uq_docs_distribution_recipient_user UNIQUE (request_id, partner_user_id),
    CONSTRAINT ck_docs_distribution_recipient_line CHECK (line_no > 0),
    CONSTRAINT ck_docs_distribution_recipient_representative CHECK (
        representative_yn IN ('Y', 'N')
    )
);

ALTER SEQUENCE docs_distribution_request_recipient_id_seq
    OWNED BY docs_distribution_request_recipient.recipient_id;

CREATE TABLE IF NOT EXISTS docs_distribution_request_event (
    event_id                bigint PRIMARY KEY
                            DEFAULT nextval('docs_distribution_request_event_id_seq'),
    request_id              bigint NOT NULL,
    from_status             varchar(30),
    to_status               varchar(30) NOT NULL,
    event_type              varchar(30) NOT NULL,
    actor_user_cd           varchar(64) NOT NULL,
    actor_user_id           varchar(64),
    actor_user_nm           varchar(200),
    event_comment           varchar(1000),
    occurred_at             timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_docs_distribution_event_request FOREIGN KEY (request_id)
        REFERENCES docs_distribution_request (request_id) ON DELETE CASCADE,
    CONSTRAINT ck_docs_distribution_event_from_status CHECK (
        from_status IS NULL OR from_status IN
            ('DRAFT', 'PENDING_APPROVAL', 'APPROVED', 'REJECTED', 'CANCELLED', 'EXPIRED')
    ),
    CONSTRAINT ck_docs_distribution_event_to_status CHECK (
        to_status IN ('DRAFT', 'PENDING_APPROVAL', 'APPROVED', 'REJECTED', 'CANCELLED', 'EXPIRED')
    ),
    CONSTRAINT ck_docs_distribution_event_type CHECK (
        event_type IN ('CREATE', 'UPDATE_DRAFT', 'SUBMIT', 'APPROVE', 'REJECT', 'CANCEL', 'EXPIRE')
    )
);

ALTER TABLE docs_distribution_request_event
    DROP CONSTRAINT IF EXISTS ck_docs_distribution_event_from_status,
    DROP CONSTRAINT IF EXISTS ck_docs_distribution_event_to_status,
    DROP CONSTRAINT IF EXISTS ck_docs_distribution_event_type;
ALTER TABLE docs_distribution_request_event
    ADD CONSTRAINT ck_docs_distribution_event_from_status CHECK (
        from_status IS NULL OR from_status IN
            ('DRAFT', 'PENDING_APPROVAL', 'APPROVED', 'REJECTED', 'CANCELLED', 'EXPIRED')
    ),
    ADD CONSTRAINT ck_docs_distribution_event_to_status CHECK (
        to_status IN ('DRAFT', 'PENDING_APPROVAL', 'APPROVED', 'REJECTED', 'CANCELLED', 'EXPIRED')
    ),
    ADD CONSTRAINT ck_docs_distribution_event_type CHECK (
        event_type IN ('CREATE', 'UPDATE_DRAFT', 'SUBMIT', 'APPROVE', 'REJECT', 'CANCEL', 'EXPIRE')
    );

ALTER SEQUENCE docs_distribution_request_event_id_seq
    OWNED BY docs_distribution_request_event.event_id;

CREATE TABLE IF NOT EXISTS docs_distribution_outbox (
    outbox_id               bigint PRIMARY KEY
                            DEFAULT nextval('docs_distribution_outbox_id_seq'),
    request_id              bigint NOT NULL,
    aggregate_id            varchar(80) NOT NULL,
    event_type              varchar(60) NOT NULL,
    status                  varchar(20) NOT NULL DEFAULT 'HOLD',
    payload_json            jsonb NOT NULL,
    created_at              timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    released_at             timestamptz,
    sent_at                 timestamptz,
    attempt_count           integer NOT NULL DEFAULT 0,
    last_error              varchar(2000),
    CONSTRAINT fk_docs_distribution_outbox_request FOREIGN KEY (request_id)
        REFERENCES docs_distribution_request (request_id),
    CONSTRAINT uq_docs_distribution_outbox_request UNIQUE (request_id),
    CONSTRAINT uq_docs_distribution_outbox_event UNIQUE (event_type, aggregate_id),
    CONSTRAINT ck_docs_distribution_outbox_status CHECK (
        status IN ('HOLD', 'READY', 'SENDING', 'SENT', 'FAILED', 'DEAD')
    ),
    CONSTRAINT ck_docs_distribution_outbox_attempt CHECK (attempt_count >= 0),
    CONSTRAINT ck_docs_distribution_outbox_payload_object CHECK (
        jsonb_typeof(payload_json) = 'object'
    )
);

ALTER SEQUENCE docs_distribution_outbox_id_seq
    OWNED BY docs_distribution_outbox.outbox_id;

CREATE INDEX IF NOT EXISTS idx_docs_distribution_request_owner
    ON docs_distribution_request (requested_by_user_cd, updated_at DESC);

CREATE INDEX IF NOT EXISTS idx_docs_distribution_request_status
    ON docs_distribution_request (status, distribution_end_date, updated_at DESC);

CREATE INDEX IF NOT EXISTS idx_docs_distribution_request_approver
    ON docs_distribution_request (approver_user_cd, status, updated_at DESC);

CREATE INDEX IF NOT EXISTS idx_docs_distribution_request_item_request
    ON docs_distribution_request_item (request_id, document_line_no, file_line_no);

CREATE INDEX IF NOT EXISTS idx_docs_distribution_recipient_request
    ON docs_distribution_request_recipient (request_id, line_no);

CREATE INDEX IF NOT EXISTS idx_docs_distribution_request_event_request
    ON docs_distribution_request_event (request_id, occurred_at, event_id);

CREATE INDEX IF NOT EXISTS idx_docs_distribution_outbox_hold
    ON docs_distribution_outbox (status, created_at, outbox_id);

DROP VIEW IF EXISTS docs_approved_distribution_list;
CREATE VIEW docs_approved_distribution_list AS
SELECT request_row.request_id,
       request_row.request_no,
       request_row.title,
       request_row.purpose,
       request_row.requested_by_user_cd,
       request_row.requested_by_user_id,
       request_row.requested_by_user_nm,
       request_row.requested_dept_cd,
       request_row.requested_dept_nm,
       request_row.partner_company_id,
       request_row.partner_company_code,
       request_row.partner_company_name,
       request_row.approver_user_cd,
       request_row.approver_user_id,
       request_row.approver_user_nm,
       request_row.distribution_start_date,
       request_row.distribution_end_date,
       request_row.submitted_at,
       request_row.decided_at AS approved_at,
       request_row.decided_by_user_cd AS approved_by_user_cd,
       request_row.decided_by_user_id AS approved_by_user_id,
       request_row.decided_by_user_nm AS approved_by_user_nm,
       COUNT(item.item_id)::integer AS item_count
  FROM docs_distribution_request request_row
  JOIN docs_distribution_request_item item
    ON item.request_id = request_row.request_id
 WHERE request_row.status = 'APPROVED'
   AND request_row.distribution_start_date <= (CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Seoul')::date
   AND request_row.distribution_end_date >= (CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Seoul')::date
 GROUP BY request_row.request_id;

COMMENT ON TABLE docs_distribution_request IS
    'Server-owned technical-data distribution approval request';
COMMENT ON TABLE docs_distribution_request_item IS
    'Immutable-at-submission item metadata snapshot; never stores a file path';
COMMENT ON TABLE docs_distribution_request_recipient IS
    'Immutable partner-recipient snapshots for one distribution request';
COMMENT ON TABLE docs_distribution_outbox IS
    'Approved request snapshots held for a future external distribution interface';
COMMENT ON COLUMN docs_distribution_outbox.status IS
    'MVP creates HOLD only; no sender is enabled in this project phase';

COMMIT;
