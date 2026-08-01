-- Generic technical-data distribution-system account-request interface
-- PostgreSQL 17+
--
-- HMAC secrets are runtime configuration and are deliberately never stored in
-- this schema. Client/source identity and every user field below are immutable
-- request snapshots so approval evidence remains meaningful after an external
-- directory changes.

\set ON_ERROR_STOP on

BEGIN;

CREATE SEQUENCE IF NOT EXISTS docs_distribution_account_request_id_seq;
CREATE SEQUENCE IF NOT EXISTS docs_distribution_account_request_event_id_seq;

CREATE TABLE IF NOT EXISTS docs_distribution_account_request (
    request_id                  bigint PRIMARY KEY
                                DEFAULT nextval('docs_distribution_account_request_id_seq'),
    event_id                    uuid NOT NULL,
    correlation_id              varchar(128) NOT NULL,
    client_id                   varchar(100) NOT NULL,
    source_system_id            varchar(100) NOT NULL,
    request_type                varchar(30) NOT NULL,
    occurred_at                 timestamptz NOT NULL,
    received_at                 timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status                      varchar(20) NOT NULL DEFAULT 'PENDING',
    representative_id           varchar(100) NOT NULL,
    representative_name         varchar(200) NOT NULL,
    representative_email        varchar(254) NOT NULL,
    representative_phone        varchar(40),
    organization_code           varchar(100),
    organization_name           varchar(200),
    business_number             varchar(50),
    target_user_id              varchar(100) NOT NULL,
    target_user_name            varchar(200),
    target_user_email           varchar(254),
    target_user_phone           varchar(40),
    target_user_position        varchar(100),
    reason                      varchar(1000),
    metadata_json               jsonb NOT NULL DEFAULT '{}'::jsonb,
    content_sha256              char(64) NOT NULL,
    decision_comment            varchar(1000),
    decided_by_user_cd          varchar(64),
    decided_by_user_id          varchar(100),
    decided_by_user_name        varchar(256),
    decided_at                  timestamptz,
    updated_at                  timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_docs_dist_account_event
        UNIQUE (source_system_id, event_id),
    CONSTRAINT uq_docs_dist_account_correlation
        UNIQUE (source_system_id, correlation_id),
    CONSTRAINT ck_docs_dist_account_type CHECK (
        request_type IN ('REGISTER_USER', 'UNLOCK_ACCOUNT', 'RESET_PASSWORD')
    ),
    CONSTRAINT ck_docs_dist_account_status CHECK (
        status IN ('PENDING', 'APPROVED', 'REJECTED')
    ),
    CONSTRAINT ck_docs_dist_account_metadata_object CHECK (
        jsonb_typeof(metadata_json) = 'object'
    ),
    CONSTRAINT ck_docs_dist_account_content_hash CHECK (
        content_sha256 ~ '^[0-9a-f]{64}$'
    ),
    CONSTRAINT ck_docs_dist_account_decision CHECK (
        (status = 'PENDING'
            AND decision_comment IS NULL
            AND decided_by_user_cd IS NULL
            AND decided_at IS NULL)
        OR
        (status IN ('APPROVED', 'REJECTED')
            AND decided_by_user_cd IS NOT NULL
            AND decided_at IS NOT NULL)
    )
);

-- Existing installations originally required both fields for every request
-- type. Unlock and password-reset requests only require the external user ID.
-- Administrator codes use the same 64-character boundary as current TDMS
-- identity and distribution workflow records.
ALTER TABLE docs_distribution_account_request
    ALTER COLUMN target_user_name DROP NOT NULL,
    ALTER COLUMN target_user_email DROP NOT NULL,
    ALTER COLUMN decided_by_user_cd TYPE varchar(64);

ALTER SEQUENCE docs_distribution_account_request_id_seq
    OWNED BY docs_distribution_account_request.request_id;

CREATE TABLE IF NOT EXISTS docs_distribution_account_request_event (
    request_event_id            bigint PRIMARY KEY
                                DEFAULT nextval('docs_distribution_account_request_event_id_seq'),
    request_id                  bigint NOT NULL,
    event_type                  varchar(30) NOT NULL,
    from_status                 varchar(20),
    to_status                   varchar(20) NOT NULL,
    actor_type                  varchar(20) NOT NULL,
    actor_id                    varchar(100) NOT NULL,
    actor_name                  varchar(256),
    comment                     varchar(1000),
    occurred_at                 timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_docs_dist_account_event_request FOREIGN KEY (request_id)
        REFERENCES docs_distribution_account_request (request_id) ON DELETE RESTRICT,
    CONSTRAINT ck_docs_dist_account_event_type CHECK (
        event_type IN ('RECEIVED', 'APPROVED', 'REJECTED')
    ),
    CONSTRAINT ck_docs_dist_account_event_actor CHECK (
        actor_type IN ('EXTERNAL_SYSTEM', 'TDMS_USER')
    ),
    CONSTRAINT ck_docs_dist_account_event_from_status CHECK (
        from_status IS NULL OR from_status IN ('PENDING', 'APPROVED', 'REJECTED')
    ),
    CONSTRAINT ck_docs_dist_account_event_to_status CHECK (
        to_status IN ('PENDING', 'APPROVED', 'REJECTED')
    )
);

ALTER SEQUENCE docs_distribution_account_request_event_id_seq
    OWNED BY docs_distribution_account_request_event.request_event_id;

CREATE TABLE IF NOT EXISTS docs_distribution_account_request_nonce (
    client_id                   varchar(100) NOT NULL,
    nonce                       uuid NOT NULL,
    received_at                 timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (client_id, nonce)
);

CREATE INDEX IF NOT EXISTS idx_docs_dist_account_queue
    ON docs_distribution_account_request (status, received_at DESC, request_id DESC);

CREATE INDEX IF NOT EXISTS idx_docs_dist_account_source
    ON docs_distribution_account_request
       (source_system_id, request_type, received_at DESC);

CREATE INDEX IF NOT EXISTS idx_docs_dist_account_target
    ON docs_distribution_account_request
       (lower(target_user_id), lower(target_user_email));

CREATE INDEX IF NOT EXISTS idx_docs_dist_account_event_history
    ON docs_distribution_account_request_event (request_id, request_event_id);

CREATE INDEX IF NOT EXISTS idx_docs_dist_account_nonce_time
    ON docs_distribution_account_request_nonce (received_at);

COMMENT ON TABLE docs_distribution_account_request IS
    'HMAC-authenticated account-operation approval requests from any registered distribution system';
COMMENT ON TABLE docs_distribution_account_request_event IS
    'Immutable receipt and administrator decision history for distribution-system account requests';
COMMENT ON TABLE docs_distribution_account_request_nonce IS
    'Short-lived HMAC replay-prevention nonces; secrets remain runtime-only';
COMMENT ON COLUMN docs_distribution_account_request.status IS
    'PENDING, APPROVED, or REJECTED; approval is a contract result and never mutates TDMS credentials';

COMMIT;
