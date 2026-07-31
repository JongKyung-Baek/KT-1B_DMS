-- Technical-data distribution workflow (MVP)
--
-- This migration is intentionally repeatable and PostgreSQL-only. It owns no
-- external HTTP integration. An approval produces one immutable HOLD outbox
-- snapshot for a later, separately authorized interface phase.

CREATE SEQUENCE IF NOT EXISTS docs_distribution_request_id_seq;
CREATE SEQUENCE IF NOT EXISTS docs_distribution_request_no_seq;
CREATE SEQUENCE IF NOT EXISTS docs_distribution_request_item_id_seq;
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
    CONSTRAINT ck_docs_distribution_request_status CHECK (
        status IN ('DRAFT', 'PENDING_APPROVAL', 'APPROVED', 'REJECTED', 'CANCELLED')
    )
);

ALTER SEQUENCE docs_distribution_request_id_seq
    OWNED BY docs_distribution_request.request_id;

CREATE TABLE IF NOT EXISTS docs_distribution_request_item (
    item_id                 bigint PRIMARY KEY
                            DEFAULT nextval('docs_distribution_request_item_id_seq'),
    request_id              bigint NOT NULL,
    line_no                 integer NOT NULL,
    object_type             varchar(30) NOT NULL,
    object_id               varchar(128) NOT NULL,
    file_no                 varchar(50) NOT NULL,
    material_no             varchar(200) NOT NULL,
    material_name           varchar(500) NOT NULL DEFAULT '',
    original_file_name      varchar(500) NOT NULL,
    file_size               bigint NOT NULL DEFAULT 0,
    grade_cd                varchar(50) NOT NULL,
    snapshot_at             timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_docs_distribution_item_request FOREIGN KEY (request_id)
        REFERENCES docs_distribution_request (request_id) ON DELETE CASCADE,
    CONSTRAINT uq_docs_distribution_item_line UNIQUE (request_id, line_no),
    CONSTRAINT uq_docs_distribution_item_resource
        UNIQUE (request_id, object_type, object_id, file_no),
    CONSTRAINT ck_docs_distribution_item_type CHECK (object_type IN ('SW', 'SW_SUB')),
    CONSTRAINT ck_docs_distribution_item_line CHECK (line_no > 0),
    CONSTRAINT ck_docs_distribution_item_size CHECK (file_size >= 0)
);

ALTER SEQUENCE docs_distribution_request_item_id_seq
    OWNED BY docs_distribution_request_item.item_id;

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
            ('DRAFT', 'PENDING_APPROVAL', 'APPROVED', 'REJECTED', 'CANCELLED')
    ),
    CONSTRAINT ck_docs_distribution_event_to_status CHECK (
        to_status IN ('DRAFT', 'PENDING_APPROVAL', 'APPROVED', 'REJECTED', 'CANCELLED')
    ),
    CONSTRAINT ck_docs_distribution_event_type CHECK (
        event_type IN ('CREATE', 'UPDATE_DRAFT', 'SUBMIT', 'APPROVE', 'REJECT', 'CANCEL')
    )
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
    ON docs_distribution_request (status, updated_at DESC);

CREATE INDEX IF NOT EXISTS idx_docs_distribution_request_item_request
    ON docs_distribution_request_item (request_id, line_no);

CREATE INDEX IF NOT EXISTS idx_docs_distribution_request_event_request
    ON docs_distribution_request_event (request_id, occurred_at, event_id);

CREATE INDEX IF NOT EXISTS idx_docs_distribution_outbox_hold
    ON docs_distribution_outbox (status, created_at, outbox_id);

CREATE OR REPLACE VIEW docs_approved_distribution_list AS
SELECT request_row.request_id,
       request_row.request_no,
       request_row.title,
       request_row.purpose,
       request_row.requested_by_user_cd,
       request_row.requested_by_user_id,
       request_row.requested_by_user_nm,
       request_row.requested_dept_cd,
       request_row.requested_dept_nm,
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
 GROUP BY request_row.request_id;

COMMENT ON TABLE docs_distribution_request IS
    'Server-owned technical-data distribution approval request';
COMMENT ON TABLE docs_distribution_request_item IS
    'Immutable-at-submission item metadata snapshot; never stores a file path';
COMMENT ON TABLE docs_distribution_outbox IS
    'Approved request snapshots held for a future external distribution interface';
COMMENT ON COLUMN docs_distribution_outbox.status IS
    'MVP creates HOLD only; no sender is enabled in this project phase';
