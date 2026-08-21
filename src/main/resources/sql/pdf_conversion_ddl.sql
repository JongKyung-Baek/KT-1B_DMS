BEGIN;

-- TDMS owns this durable outbox. Source documents remain immutable; converted
-- PDFs are separately referenced and may be reused only after hash validation.
CREATE TABLE IF NOT EXISTS public.docs_pdf_conversion (
    conversion_id       uuid PRIMARY KEY,
    object_type         varchar(30) NOT NULL,
    object_id           varchar(100) NOT NULL,
    file_no             varchar(60) NOT NULL DEFAULT '*',
    source_file_name    varchar(500) NOT NULL,
    source_file_path    varchar(2000) NOT NULL,
    source_size_bytes   bigint NOT NULL,
    source_sha256       char(64) NOT NULL,
    output_file_name    varchar(500),
    output_file_path    varchar(2000),
    output_size_bytes   bigint,
    output_sha256       char(64),
    status_cd           varchar(20) NOT NULL DEFAULT 'PENDING',
    attempt_count       integer NOT NULL DEFAULT 0,
    max_attempts        integer NOT NULL DEFAULT 3,
    next_attempt_at     timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    claim_token         uuid,
    claimed_at          timestamp with time zone,
    claim_expires_at    timestamp with time zone,
    started_at          timestamp with time zone,
    completed_at        timestamp with time zone,
    current_yn          boolean NOT NULL DEFAULT TRUE,
    last_error          text,
    created_at          timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_pdf_conversion_source_identity
        UNIQUE (object_type, object_id, file_no, source_sha256),
    CONSTRAINT ck_pdf_conversion_source_sha256
        CHECK (source_sha256 ~ '^[0-9a-f]{64}$'),
    CONSTRAINT ck_pdf_conversion_output_sha256
        CHECK (output_sha256 IS NULL OR output_sha256 ~ '^[0-9a-f]{64}$'),
    CONSTRAINT ck_pdf_conversion_sizes
        CHECK (source_size_bytes >= 0
               AND (output_size_bytes IS NULL OR output_size_bytes >= 0)),
    CONSTRAINT ck_pdf_conversion_attempts
        CHECK (attempt_count >= 0 AND max_attempts > 0
               AND attempt_count <= max_attempts),
    CONSTRAINT ck_pdf_conversion_status
        CHECK (status_cd IN (
            'PENDING', 'PROCESSING', 'SUCCEEDED', 'FAILED',
            'NOT_REQUIRED', 'SUPERSEDED'
        )),
    CONSTRAINT ck_pdf_conversion_claim
        CHECK (
            (status_cd = 'PROCESSING'
             AND claim_token IS NOT NULL
             AND claimed_at IS NOT NULL
             AND claim_expires_at IS NOT NULL)
            OR
            (status_cd <> 'PROCESSING'
             AND claim_token IS NULL
             AND claimed_at IS NULL
             AND claim_expires_at IS NULL)
        ),
    CONSTRAINT ck_pdf_conversion_completed_output
        CHECK (
            status_cd NOT IN ('SUCCEEDED', 'NOT_REQUIRED')
            OR (output_file_name IS NOT NULL
                AND output_file_path IS NOT NULL
                AND output_size_bytes IS NOT NULL
                AND output_sha256 IS NOT NULL
                AND completed_at IS NOT NULL)
    )
);

-- Upgrade an earlier preview of the outbox in place. The immutable source
-- identity columns are required input and intentionally fail fast if absent;
-- result, lease and scheduling columns can be introduced safely.
ALTER TABLE public.docs_pdf_conversion
    ADD COLUMN IF NOT EXISTS output_file_name varchar(500),
    ADD COLUMN IF NOT EXISTS output_file_path varchar(2000),
    ADD COLUMN IF NOT EXISTS output_size_bytes bigint,
    ADD COLUMN IF NOT EXISTS output_sha256 char(64),
    ADD COLUMN IF NOT EXISTS status_cd varchar(20),
    ADD COLUMN IF NOT EXISTS attempt_count integer,
    ADD COLUMN IF NOT EXISTS max_attempts integer,
    ADD COLUMN IF NOT EXISTS next_attempt_at timestamp with time zone,
    ADD COLUMN IF NOT EXISTS claim_token uuid,
    ADD COLUMN IF NOT EXISTS claimed_at timestamp with time zone,
    ADD COLUMN IF NOT EXISTS claim_expires_at timestamp with time zone,
    ADD COLUMN IF NOT EXISTS started_at timestamp with time zone,
    ADD COLUMN IF NOT EXISTS completed_at timestamp with time zone,
    ADD COLUMN IF NOT EXISTS current_yn boolean,
    ADD COLUMN IF NOT EXISTS last_error text,
    ADD COLUMN IF NOT EXISTS created_at timestamp with time zone,
    ADD COLUMN IF NOT EXISTS updated_at timestamp with time zone;

UPDATE public.docs_pdf_conversion
   SET source_sha256 = LOWER(BTRIM(source_sha256)),
       output_sha256 = CASE WHEN output_sha256 IS NULL THEN NULL
                            ELSE LOWER(BTRIM(output_sha256)) END,
       status_cd = UPPER(COALESCE(NULLIF(BTRIM(status_cd), ''), 'PENDING')),
       attempt_count = GREATEST(COALESCE(attempt_count, 0), 0),
       max_attempts = GREATEST(COALESCE(max_attempts, 3),
                               COALESCE(attempt_count, 0), 1),
       created_at = COALESCE(created_at, CURRENT_TIMESTAMP),
       updated_at = COALESCE(updated_at, created_at, CURRENT_TIMESTAMP),
       next_attempt_at = COALESCE(next_attempt_at, created_at, CURRENT_TIMESTAMP);

WITH ranked AS (
    SELECT ctid AS row_locator,
           ROW_NUMBER() OVER (
               PARTITION BY object_type, object_id, file_no
               ORDER BY current_yn DESC NULLS LAST,
                        updated_at DESC NULLS LAST,
                        created_at DESC NULLS LAST,
                        conversion_id DESC) AS source_rank
      FROM public.docs_pdf_conversion
)
UPDATE public.docs_pdf_conversion AS conversion
   SET current_yn = (ranked.source_rank = 1)
  FROM ranked
 WHERE conversion.ctid = ranked.row_locator
   AND conversion.current_yn IS DISTINCT FROM (ranked.source_rank = 1);

UPDATE public.docs_pdf_conversion
   SET status_cd = 'PENDING',
       attempt_count = 0,
       claim_token = NULL,
       claimed_at = NULL,
       claim_expires_at = NULL,
       completed_at = NULL,
       next_attempt_at = CURRENT_TIMESTAMP
 WHERE status_cd NOT IN (
        'PENDING', 'PROCESSING', 'SUCCEEDED', 'FAILED',
        'NOT_REQUIRED', 'SUPERSEDED');

UPDATE public.docs_pdf_conversion
   SET status_cd = 'PENDING',
       attempt_count = 0,
       claim_token = NULL,
       claimed_at = NULL,
       claim_expires_at = NULL,
       completed_at = NULL,
       next_attempt_at = CURRENT_TIMESTAMP
 WHERE status_cd = 'PROCESSING'
   AND (claim_token IS NULL OR claimed_at IS NULL OR claim_expires_at IS NULL);

UPDATE public.docs_pdf_conversion
   SET status_cd = 'PENDING',
       attempt_count = 0,
       completed_at = NULL,
       next_attempt_at = CURRENT_TIMESTAMP
 WHERE status_cd IN ('SUCCEEDED', 'NOT_REQUIRED')
   AND (output_file_name IS NULL OR output_file_path IS NULL
        OR output_size_bytes IS NULL OR output_sha256 IS NULL
        OR completed_at IS NULL);

UPDATE public.docs_pdf_conversion
   SET claim_token = NULL,
       claimed_at = NULL,
       claim_expires_at = NULL
 WHERE status_cd <> 'PROCESSING';

ALTER TABLE public.docs_pdf_conversion
    ALTER COLUMN status_cd SET DEFAULT 'PENDING',
    ALTER COLUMN status_cd SET NOT NULL,
    ALTER COLUMN attempt_count SET DEFAULT 0,
    ALTER COLUMN attempt_count SET NOT NULL,
    ALTER COLUMN max_attempts SET DEFAULT 3,
    ALTER COLUMN max_attempts SET NOT NULL,
    ALTER COLUMN next_attempt_at SET DEFAULT CURRENT_TIMESTAMP,
    ALTER COLUMN next_attempt_at SET NOT NULL,
    ALTER COLUMN current_yn SET DEFAULT TRUE,
    ALTER COLUMN current_yn SET NOT NULL,
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP,
    ALTER COLUMN created_at SET NOT NULL,
    ALTER COLUMN updated_at SET DEFAULT CURRENT_TIMESTAMP,
    ALTER COLUMN updated_at SET NOT NULL;

DO $migration$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
         WHERE conrelid = 'public.docs_pdf_conversion'::regclass
           AND conname = 'uq_pdf_conversion_source_identity'
    ) THEN
        ALTER TABLE public.docs_pdf_conversion
            ADD CONSTRAINT uq_pdf_conversion_source_identity
            UNIQUE (object_type, object_id, file_no, source_sha256);
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
         WHERE conrelid = 'public.docs_pdf_conversion'::regclass
           AND conname = 'ck_pdf_conversion_source_sha256'
    ) THEN
        ALTER TABLE public.docs_pdf_conversion
            ADD CONSTRAINT ck_pdf_conversion_source_sha256
            CHECK (source_sha256 ~ '^[0-9a-f]{64}$');
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
         WHERE conrelid = 'public.docs_pdf_conversion'::regclass
           AND conname = 'ck_pdf_conversion_output_sha256'
    ) THEN
        ALTER TABLE public.docs_pdf_conversion
            ADD CONSTRAINT ck_pdf_conversion_output_sha256
            CHECK (output_sha256 IS NULL OR output_sha256 ~ '^[0-9a-f]{64}$');
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
         WHERE conrelid = 'public.docs_pdf_conversion'::regclass
           AND conname = 'ck_pdf_conversion_sizes'
    ) THEN
        ALTER TABLE public.docs_pdf_conversion
            ADD CONSTRAINT ck_pdf_conversion_sizes
            CHECK (source_size_bytes >= 0
                   AND (output_size_bytes IS NULL OR output_size_bytes >= 0));
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
         WHERE conrelid = 'public.docs_pdf_conversion'::regclass
           AND conname = 'ck_pdf_conversion_attempts'
    ) THEN
        ALTER TABLE public.docs_pdf_conversion
            ADD CONSTRAINT ck_pdf_conversion_attempts
            CHECK (attempt_count >= 0 AND max_attempts > 0
                   AND attempt_count <= max_attempts);
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
         WHERE conrelid = 'public.docs_pdf_conversion'::regclass
           AND conname = 'ck_pdf_conversion_status'
    ) THEN
        ALTER TABLE public.docs_pdf_conversion
            ADD CONSTRAINT ck_pdf_conversion_status
            CHECK (status_cd IN (
                'PENDING', 'PROCESSING', 'SUCCEEDED', 'FAILED',
                'NOT_REQUIRED', 'SUPERSEDED'));
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
         WHERE conrelid = 'public.docs_pdf_conversion'::regclass
           AND conname = 'ck_pdf_conversion_claim'
    ) THEN
        ALTER TABLE public.docs_pdf_conversion
            ADD CONSTRAINT ck_pdf_conversion_claim CHECK (
                (status_cd = 'PROCESSING' AND claim_token IS NOT NULL
                 AND claimed_at IS NOT NULL AND claim_expires_at IS NOT NULL)
                OR
                (status_cd <> 'PROCESSING' AND claim_token IS NULL
                 AND claimed_at IS NULL AND claim_expires_at IS NULL));
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
         WHERE conrelid = 'public.docs_pdf_conversion'::regclass
           AND conname = 'ck_pdf_conversion_completed_output'
    ) THEN
        ALTER TABLE public.docs_pdf_conversion
            ADD CONSTRAINT ck_pdf_conversion_completed_output CHECK (
                status_cd NOT IN ('SUCCEEDED', 'NOT_REQUIRED')
                OR (output_file_name IS NOT NULL AND output_file_path IS NOT NULL
                    AND output_size_bytes IS NOT NULL AND output_sha256 IS NOT NULL
                    AND completed_at IS NOT NULL));
    END IF;
END
$migration$;

-- One source version is current for each logical file, while the complete
-- source-hash history remains queryable for audit and safe output reuse.
CREATE UNIQUE INDEX IF NOT EXISTS uq_pdf_conversion_current_file
    ON public.docs_pdf_conversion (object_type, object_id, file_no)
    WHERE current_yn = TRUE;

CREATE INDEX IF NOT EXISTS idx_pdf_conversion_current_lookup
    ON public.docs_pdf_conversion (object_type, object_id, file_no, updated_at DESC)
    WHERE current_yn = TRUE;

CREATE INDEX IF NOT EXISTS idx_pdf_conversion_reusable_hash
    ON public.docs_pdf_conversion (source_sha256, completed_at DESC)
    WHERE status_cd IN ('SUCCEEDED', 'NOT_REQUIRED')
      AND output_file_path IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_pdf_conversion_due
    ON public.docs_pdf_conversion (next_attempt_at, created_at)
    WHERE current_yn = TRUE AND status_cd = 'PENDING';

CREATE INDEX IF NOT EXISTS idx_pdf_conversion_stale_claim
    ON public.docs_pdf_conversion (claim_expires_at)
    WHERE current_yn = TRUE AND status_cd = 'PROCESSING';

-- Older dumps created the SW file tables before conversion-state columns were
-- introduced. Add columns without a PENDING default first: PostgreSQL would
-- otherwise label every historical row as work that has actually been queued.
ALTER TABLE IF EXISTS public.docs_sw_file
    ADD COLUMN IF NOT EXISTS processing_status varchar(30),
    ADD COLUMN IF NOT EXISTS processing_error text,
    ADD COLUMN IF NOT EXISTS processed_at timestamp with time zone;

ALTER TABLE IF EXISTS public.docs_sw_sub_file
    ADD COLUMN IF NOT EXISTS processing_status varchar(30),
    ADD COLUMN IF NOT EXISTS processing_error text,
    ADD COLUMN IF NOT EXISTS processed_at timestamp with time zone;

-- A legacy row has no durable conversion job. Preserve its pre-conversion
-- availability as DONE; only registration code may create a genuinely PENDING
-- row together with its outbox record. This also repairs the earlier migration
-- draft that backfilled historical auxiliary files as PENDING.
UPDATE public.docs_sw_file AS source_file
   SET processing_status = 'DONE',
       processing_error = NULL,
       processed_at = COALESCE(source_file.processed_at, CURRENT_TIMESTAMP)
 WHERE NULLIF(BTRIM(source_file.processing_status), '') IS NULL
    OR (UPPER(BTRIM(source_file.processing_status)) = 'PENDING'
        AND NOT EXISTS (
            SELECT 1
              FROM public.docs_pdf_conversion AS conversion
             WHERE conversion.object_type = 'SW'
               AND conversion.object_id = source_file.object_id
               AND conversion.file_no = source_file.file_no::text
               AND conversion.current_yn = TRUE));

UPDATE public.docs_sw_sub_file AS source_file
   SET processing_status = 'DONE',
       processing_error = NULL,
       processed_at = COALESCE(source_file.processed_at, CURRENT_TIMESTAMP)
 WHERE NULLIF(BTRIM(source_file.processing_status), '') IS NULL
    OR (UPPER(BTRIM(source_file.processing_status)) = 'PENDING'
        AND NOT EXISTS (
            SELECT 1
              FROM public.docs_pdf_conversion AS conversion
             WHERE conversion.object_type = 'SW_SUB'
               AND conversion.object_id = source_file.object_id
               AND conversion.file_no = source_file.file_no::text
               AND conversion.current_yn = TRUE));

ALTER TABLE public.docs_sw_file
    ALTER COLUMN processing_status SET DEFAULT 'PENDING',
    ALTER COLUMN processing_status SET NOT NULL;

ALTER TABLE public.docs_sw_sub_file
    ALTER COLUMN processing_status SET DEFAULT 'PENDING',
    ALTER COLUMN processing_status SET NOT NULL;

COMMENT ON TABLE public.docs_pdf_conversion IS
    'Durable, source-hash-idempotent TDMS PDF conversion outbox and result index';
COMMENT ON COLUMN public.docs_pdf_conversion.current_yn IS
    'Only one source version per logical object/file may be current';
COMMENT ON COLUMN public.docs_pdf_conversion.claim_token IS
    'Lease fencing token required for terminal worker updates';

COMMIT;
