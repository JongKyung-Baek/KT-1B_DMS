BEGIN;

-- Durable state for the V2 download capability lifecycle. The application may
-- keep a local cache, but this table is the source of truth across restarts.
CREATE TABLE IF NOT EXISTS docs_download_runtime (
    ws_seq                varchar(32) PRIMARY KEY,
    request_no            varchar(100),
    doc_seq               varchar(60),
    file_no               varchar(60),
    file_seq              varchar(60),
    download_request_key  varchar(32) NOT NULL UNIQUE,
    request_type          varchar(30),
    object_type           varchar(30),
    original_file_name    varchar(512),
    saved_file_name       varchar(512),
    owner_user_cd         varchar(20) NOT NULL,
    owner_user_id         varchar(50),
    owner_user_nm         varchar(256),
    owner_session_id      varchar(128) NOT NULL,
    rest_sequence         varchar(100),
    temp_file_path        varchar(2048),
    status_cd             varchar(20) NOT NULL DEFAULT 'QUEUED',
    result_code           varchar(20),
    optional_data         varchar(2048),
    error_message         varchar(1000),
    sent_to_ws_at         timestamp without time zone,
    download_claimed      boolean NOT NULL DEFAULT FALSE,
    claimed_at            timestamp without time zone,
    audit_saved           boolean NOT NULL DEFAULT FALSE,
    created_at            timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expire_at             timestamp without time zone NOT NULL,
    CONSTRAINT ck_download_runtime_ws_seq
        CHECK (ws_seq ~ '^[0-9A-Fa-f]{32}$'),
    CONSTRAINT ck_download_runtime_key
        CHECK (download_request_key ~ '^[0-9A-Fa-f]{32}$'),
    CONSTRAINT ck_download_runtime_status
        CHECK (status_cd IN ('QUEUED', 'DOWNLOADING', 'SENT_TO_WS', 'COMPLETED', 'FAILED')),
    CONSTRAINT ck_download_runtime_claim
        CHECK ((download_claimed = FALSE AND claimed_at IS NULL)
            OR (download_claimed = TRUE AND claimed_at IS NOT NULL)),
    CONSTRAINT ck_download_runtime_audit
        CHECK (audit_saved = FALSE
            OR status_cd IN ('COMPLETED', 'FAILED')),
    CONSTRAINT ck_download_runtime_expiry
        CHECK (expire_at >= created_at)
);

CREATE INDEX IF NOT EXISTS idx_download_runtime_expire
    ON docs_download_runtime (expire_at);

CREATE INDEX IF NOT EXISTS idx_download_runtime_recovery
    ON docs_download_runtime (audit_saved, status_cd, created_at);

CREATE INDEX IF NOT EXISTS idx_download_runtime_file_name
    ON docs_download_runtime (saved_file_name, original_file_name, updated_at DESC);

COMMIT;
