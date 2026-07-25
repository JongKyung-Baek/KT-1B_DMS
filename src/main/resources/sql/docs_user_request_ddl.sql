BEGIN;

-- Atomic per-day number allocation preserves the legacy YYYYMMDD-U0001 shape
-- without the race condition of MAX(request_no) + 1.
CREATE TABLE IF NOT EXISTS public.docs_user_request_number (
    request_date date PRIMARY KEY,
    last_value   integer NOT NULL,
    CONSTRAINT ck_docs_user_request_number_range
        CHECK (last_value BETWEEN 1 AND 9999)
);

-- PostgreSQL sequences replace concurrent MAX()+1 allocation for new users.
CREATE SEQUENCE IF NOT EXISTS public.docs_user_cd_sequence
    AS bigint MINVALUE 1 MAXVALUE 9999999999 START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS public.docs_external_user_id_sequence
    AS bigint MINVALUE 1 MAXVALUE 9999999999 START WITH 1 INCREMENT BY 1;
ALTER SEQUENCE public.docs_user_cd_sequence
    INCREMENT BY 1 MINVALUE 1 MAXVALUE 9999999999 NO CYCLE;
ALTER SEQUENCE public.docs_external_user_id_sequence
    INCREMENT BY 1 MINVALUE 1 MAXVALUE 9999999999 NO CYCLE;

SELECT SETVAL(
    'public.docs_user_cd_sequence',
    GREATEST(
        sequence_state.last_value,
        existing_user.max_value,
        1
    ),
    sequence_state.is_called
        OR existing_user.max_value >= sequence_state.last_value
)
FROM public.docs_user_cd_sequence sequence_state
CROSS JOIN LATERAL (
    SELECT COALESCE(MAX(SUBSTRING(user_cd FROM 6)::bigint), 0) AS max_value
    FROM public.docs_user
    WHERE user_cd ~ '^USER_[0-9]+$'
) existing_user;

SELECT SETVAL(
    'public.docs_external_user_id_sequence',
    GREATEST(
        sequence_state.last_value,
        existing_user.max_value,
        1
    ),
    sequence_state.is_called
        OR existing_user.max_value >= sequence_state.last_value
)
FROM public.docs_external_user_id_sequence sequence_state
CROSS JOIN LATERAL (
    SELECT COALESCE(MAX(user_id::bigint), 0) AS max_value
    FROM public.docs_user
    WHERE auth_site = 'E'
      AND user_id ~ '^[0-9]+$'
) existing_user;

-- External-user create/update/delete requests. A create request keeps only a
-- versioned PBKDF2-SHA256 value while pending; approval/rejection clears user_pwd in the same
-- statement that changes status.
CREATE TABLE IF NOT EXISTS public.docs_user_request (
    request_no       varchar(14) PRIMARY KEY,
    request_type     char(1) NOT NULL,
    status_cd        varchar(10) NOT NULL DEFAULT 'REQUEST',
    user_nm          varchar(256) NOT NULL,
    user_pwd         varchar(128),
    email            varchar(256),
    protect_yn       char(1) NOT NULL DEFAULT 'N',
    request_reason   varchar(1000) NOT NULL,
    reject_reason    varchar(1000),
    approval_dt      timestamp(0) without time zone,
    insert_user_cd   varchar(20) NOT NULL,
    insert_dt        timestamp(0) without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    approval_user_cd varchar(20) NOT NULL,
    target_user_cd   varchar(20),
    company_cd       varchar(20) NOT NULL,
    cr_yn            char(1) NOT NULL DEFAULT 'N'
);

-- Re-running this file also completes an older partial table definition.
ALTER TABLE public.docs_user_request
    ADD COLUMN IF NOT EXISTS request_no varchar(14),
    ADD COLUMN IF NOT EXISTS request_type char(1),
    ADD COLUMN IF NOT EXISTS status_cd varchar(10) DEFAULT 'REQUEST',
    ADD COLUMN IF NOT EXISTS user_nm varchar(256),
    ADD COLUMN IF NOT EXISTS user_pwd varchar(128),
    ADD COLUMN IF NOT EXISTS email varchar(256),
    ADD COLUMN IF NOT EXISTS protect_yn char(1) DEFAULT 'N',
    ADD COLUMN IF NOT EXISTS request_reason varchar(1000),
    ADD COLUMN IF NOT EXISTS reject_reason varchar(1000),
    ADD COLUMN IF NOT EXISTS approval_dt timestamp(0) without time zone,
    ADD COLUMN IF NOT EXISTS insert_user_cd varchar(20),
    ADD COLUMN IF NOT EXISTS insert_dt timestamp(0) without time zone DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS approval_user_cd varchar(20),
    ADD COLUMN IF NOT EXISTS target_user_cd varchar(20),
    ADD COLUMN IF NOT EXISTS company_cd varchar(20),
    ADD COLUMN IF NOT EXISTS cr_yn char(1) DEFAULT 'N';

-- Upgrade older partial definitions to the same invariants as a fresh table.
-- If legacy rows contain NULL in a required field, this intentionally stops the
-- deployment so the data can be corrected rather than silently bypassing checks.
ALTER TABLE public.docs_user_request
    ALTER COLUMN request_type SET NOT NULL,
    ALTER COLUMN status_cd SET DEFAULT 'REQUEST',
    ALTER COLUMN status_cd SET NOT NULL,
    ALTER COLUMN user_nm SET NOT NULL,
    ALTER COLUMN protect_yn SET DEFAULT 'N',
    ALTER COLUMN protect_yn SET NOT NULL,
    ALTER COLUMN request_reason SET NOT NULL,
    ALTER COLUMN insert_user_cd SET NOT NULL,
    ALTER COLUMN insert_dt SET DEFAULT CURRENT_TIMESTAMP,
    ALTER COLUMN insert_dt SET NOT NULL,
    ALTER COLUMN approval_user_cd SET NOT NULL,
    ALTER COLUMN company_cd SET NOT NULL,
    ALTER COLUMN cr_yn SET DEFAULT 'N',
    ALTER COLUMN cr_yn SET NOT NULL;

-- When upgrading a database that already has requests, initialize each daily
-- counter at the greatest allocated suffix before the application starts.
INSERT INTO public.docs_user_request_number (request_date, last_value)
SELECT
    TO_DATE(SUBSTRING(request_no FROM 1 FOR 8), 'YYYYMMDD'),
    MAX(SUBSTRING(request_no FROM 11 FOR 4)::INTEGER)
FROM public.docs_user_request
WHERE request_no ~ '^[0-9]{8}-U[0-9]{4}$'
GROUP BY TO_DATE(SUBSTRING(request_no FROM 1 FOR 8), 'YYYYMMDD')
ON CONFLICT (request_date)
DO UPDATE
SET last_value = GREATEST(
    public.docs_user_request_number.last_value,
    EXCLUDED.last_value
);

-- Recreate canonical named constraints so an older, weaker definition cannot
-- survive merely because it uses the expected name.
ALTER TABLE public.docs_user_request
    DROP CONSTRAINT IF EXISTS ck_docs_user_request_type,
    DROP CONSTRAINT IF EXISTS ck_docs_user_request_status,
    DROP CONSTRAINT IF EXISTS ck_docs_user_request_flags,
    DROP CONSTRAINT IF EXISTS ck_docs_user_request_no_format,
    DROP CONSTRAINT IF EXISTS ck_docs_user_request_password_lifecycle,
    DROP CONSTRAINT IF EXISTS fk_dur_insert_user,
    DROP CONSTRAINT IF EXISTS fk_dur_approval_user,
    DROP CONSTRAINT IF EXISTS fk_dur_target_user,
    DROP CONSTRAINT IF EXISTS fk_dur_company;

DO $ddl$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conrelid = 'public.docs_user_request'::regclass
          AND contype = 'p'
    ) THEN
        ALTER TABLE public.docs_user_request
            ADD CONSTRAINT docs_user_request_pk PRIMARY KEY (request_no);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conrelid = 'public.docs_user_request'::regclass
          AND conname = 'ck_docs_user_request_type'
    ) THEN
        ALTER TABLE public.docs_user_request
            ADD CONSTRAINT ck_docs_user_request_type
            CHECK (request_type IN ('I', 'U', 'D')) NOT VALID;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conrelid = 'public.docs_user_request'::regclass
          AND conname = 'ck_docs_user_request_status'
    ) THEN
        ALTER TABLE public.docs_user_request
            ADD CONSTRAINT ck_docs_user_request_status
            CHECK (status_cd IN ('REQUEST', 'ACCEPT', 'APPROVAL', 'REJECT')) NOT VALID;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conrelid = 'public.docs_user_request'::regclass
          AND conname = 'ck_docs_user_request_flags'
    ) THEN
        ALTER TABLE public.docs_user_request
            ADD CONSTRAINT ck_docs_user_request_flags
            CHECK (protect_yn IN ('Y', 'N') AND cr_yn IN ('Y', 'N')) NOT VALID;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conrelid = 'public.docs_user_request'::regclass
          AND conname = 'ck_docs_user_request_no_format'
    ) THEN
        ALTER TABLE public.docs_user_request
            ADD CONSTRAINT ck_docs_user_request_no_format
            CHECK (request_no ~ '^[0-9]{8}-U[0-9]{4}$') NOT VALID;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conrelid = 'public.docs_user_request'::regclass
          AND conname = 'ck_docs_user_request_password_lifecycle'
    ) THEN
        ALTER TABLE public.docs_user_request
            ADD CONSTRAINT ck_docs_user_request_password_lifecycle
            CHECK (
                (
                    request_type = 'I'
                    AND status_cd = 'REQUEST'
                    AND user_pwd IS NOT NULL
                    AND user_pwd ~ '^pbkdf2-sha256[$][1-9][0-9]{5}[$][A-Za-z0-9_-]{21}[AQgw][$][A-Za-z0-9_-]{42}[AEIMQUYcgkosw048]$'
                )
                OR (
                    (request_type <> 'I' OR status_cd <> 'REQUEST')
                    AND user_pwd IS NULL
                )
            ) NOT VALID;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conrelid = 'public.docs_user_request'::regclass
          AND conname = 'fk_dur_insert_user'
    ) THEN
        ALTER TABLE public.docs_user_request
            ADD CONSTRAINT fk_dur_insert_user
            FOREIGN KEY (insert_user_cd)
            REFERENCES public.docs_user (user_cd) NOT VALID;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conrelid = 'public.docs_user_request'::regclass
          AND conname = 'fk_dur_approval_user'
    ) THEN
        ALTER TABLE public.docs_user_request
            ADD CONSTRAINT fk_dur_approval_user
            FOREIGN KEY (approval_user_cd)
            REFERENCES public.docs_user (user_cd) NOT VALID;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conrelid = 'public.docs_user_request'::regclass
          AND conname = 'fk_dur_target_user'
    ) THEN
        ALTER TABLE public.docs_user_request
            ADD CONSTRAINT fk_dur_target_user
            FOREIGN KEY (target_user_cd)
            REFERENCES public.docs_user (user_cd) NOT VALID;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conrelid = 'public.docs_user_request'::regclass
          AND conname = 'fk_dur_company'
    ) THEN
        ALTER TABLE public.docs_user_request
            ADD CONSTRAINT fk_dur_company
            FOREIGN KEY (company_cd)
            REFERENCES public.docs_company (company_cd) NOT VALID;
    END IF;
END
$ddl$;

-- Do not leave upgraded databases with constraints that only protect new rows.
-- Existing invalid data must be corrected explicitly before application startup.
ALTER TABLE public.docs_user_request
    VALIDATE CONSTRAINT ck_docs_user_request_type,
    VALIDATE CONSTRAINT ck_docs_user_request_status,
    VALIDATE CONSTRAINT ck_docs_user_request_flags,
    VALIDATE CONSTRAINT ck_docs_user_request_no_format,
    VALIDATE CONSTRAINT ck_docs_user_request_password_lifecycle,
    VALIDATE CONSTRAINT fk_dur_insert_user,
    VALIDATE CONSTRAINT fk_dur_approval_user,
    VALIDATE CONSTRAINT fk_dur_target_user,
    VALIDATE CONSTRAINT fk_dur_company;

CREATE INDEX IF NOT EXISTS idx_docs_user_request_approver_queue
    ON public.docs_user_request
       (approval_user_cd, status_cd, request_type, insert_dt DESC, request_no DESC);

CREATE INDEX IF NOT EXISTS idx_docs_user_request_requester_history
    ON public.docs_user_request
       (insert_user_cd, insert_dt DESC, request_no DESC);

CREATE INDEX IF NOT EXISTS idx_docs_user_request_company
    ON public.docs_user_request (company_cd);

CREATE INDEX IF NOT EXISTS idx_docs_user_request_target
    ON public.docs_user_request (target_user_cd)
    WHERE target_user_cd IS NOT NULL;

COMMIT;
