BEGIN;

-- Durable TDMS <-> external viewer integration state. Endpoint paths are owned by
-- the application contract and credentials are supplied only at runtime.
-- The legacy history ledger did not identify the exact file. Keep existing rows
-- compatible while allowing signed viewer callbacks to persist that identity.
ALTER TABLE public.docs_history
    ADD COLUMN IF NOT EXISTS file_no varchar(60);

CREATE TABLE IF NOT EXISTS docs_viewer_launch (
    correlation_id     varchar(64) PRIMARY KEY,
    object_type        varchar(30) NOT NULL,
    object_id          varchar(60) NOT NULL,
    acl_object_type    varchar(30) NOT NULL,
    acl_object_id      varchar(60) NOT NULL,
    file_no            varchar(60) NOT NULL DEFAULT '*',
    request_no         varchar(100),
    actor_user_cd      varchar(20) NOT NULL,
    actor_user_id      varchar(100) NOT NULL,
    actor_user_nm      varchar(256),
    distribution_type varchar(100),
    drawing_no         varchar(200),
    org_file_nm        varchar(500),
    revision           varchar(100),
    expires_at         timestamp with time zone NOT NULL,
    created_at         timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    viewed_at          timestamp with time zone,
    CONSTRAINT ck_viewer_launch_expiry CHECK (expires_at > created_at)
);

CREATE INDEX IF NOT EXISTS idx_viewer_launch_object_created
    ON docs_viewer_launch (object_type, object_id, file_no, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_viewer_launch_actor_created
    ON docs_viewer_launch (actor_user_cd, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_viewer_launch_expiry
    ON docs_viewer_launch (expires_at);

CREATE TABLE IF NOT EXISTS docs_viewer_event (
    event_id        uuid PRIMARY KEY,
    correlation_id varchar(64) NOT NULL,
    event_type      varchar(40) NOT NULL,
    occurred_at    timestamp with time zone NOT NULL,
    object_id      varchar(60) NOT NULL,
    file_no        varchar(60) NOT NULL,
    user_id        varchar(100) NOT NULL,
    content_sha256 char(64) NOT NULL,
    received_at    timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_viewer_event_launch
        FOREIGN KEY (correlation_id)
        REFERENCES docs_viewer_launch (correlation_id) ON DELETE CASCADE,
    CONSTRAINT uq_viewer_event_correlation_type
        UNIQUE (correlation_id, event_type),
    CONSTRAINT ck_viewer_event_sha256
        CHECK (content_sha256 ~ '^[0-9A-Fa-f]{64}$')
);

CREATE INDEX IF NOT EXISTS idx_viewer_event_correlation_time
    ON docs_viewer_event (correlation_id, occurred_at DESC);

CREATE INDEX IF NOT EXISTS idx_viewer_event_object_time
    ON docs_viewer_event (object_id, file_no, occurred_at DESC);

CREATE TABLE IF NOT EXISTS docs_viewer_callback_nonce (
    client_id  varchar(100) NOT NULL,
    nonce      uuid NOT NULL,
    created_at timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (client_id, nonce)
);

CREATE INDEX IF NOT EXISTS idx_viewer_callback_nonce_created
    ON docs_viewer_callback_nonce (created_at);

COMMENT ON TABLE docs_viewer_launch IS
    'Short-lived launch correlation and ACL context for the external viewer';
COMMENT ON TABLE docs_viewer_event IS
    'Signed, idempotent viewer callbacks accepted by TDMS';
COMMENT ON TABLE docs_viewer_callback_nonce IS
    'Replay-protection nonces for signed external viewer callbacks';
COMMENT ON COLUMN public.docs_history.file_no IS
    'Exact file number for a persisted view or file-level history event';

-- Retire only the loopback ADAP endpoints from old development dumps. A
-- non-loopback row is retained for emergency rollback, but the new integration
-- never reads ADAP_PDF_URL or ADAP_POST_URL.
--
-- Shared secrets must never be persisted in the database. Clean up known
-- accidental keys and prevent them from being inserted again.
DO $viewer_config_cleanup$
BEGIN
    IF to_regclass('public.docs_system_config') IS NOT NULL THEN
        DELETE FROM public.docs_system_config
         WHERE UPPER(BTRIM(system_config_group)) = 'DB_ADAP_CONFIG'
           AND UPPER(BTRIM(system_config_cd)) IN ('ADAP_PDF_URL', 'ADAP_POST_URL')
           AND LOWER(BTRIM(COALESCE(system_config_value, ''))) ~
               '^https?://(localhost|127\.0\.0\.1|\[::1\])([:/]|$)';

        DELETE FROM public.docs_system_config
         WHERE UPPER(BTRIM(system_config_cd)) IN (
             'TDMS_VIEWER_SHARED_SECRET',
             'TDMS_VIEWER_SECRET',
             'TDMS_SHARED_SECRET',
             'VIEWER_SHARED_SECRET',
             'CV_SHARED_SECRET',
             'COLLABVIEW_SECRET',
             'VIEWER_SECRET'
         );

        ALTER TABLE public.docs_system_config
            DROP CONSTRAINT IF EXISTS ck_docs_system_config_no_viewer_secret;
        ALTER TABLE public.docs_system_config
            ADD CONSTRAINT ck_docs_system_config_no_viewer_secret
            CHECK (UPPER(BTRIM(system_config_cd)) NOT IN (
                'TDMS_VIEWER_SHARED_SECRET',
                'TDMS_VIEWER_SECRET',
                'TDMS_SHARED_SECRET',
                'VIEWER_SHARED_SECRET',
                'CV_SHARED_SECRET',
                'COLLABVIEW_SECRET',
                'VIEWER_SECRET'
            ));
    END IF;
END;
$viewer_config_cleanup$ LANGUAGE plpgsql;

COMMIT;
