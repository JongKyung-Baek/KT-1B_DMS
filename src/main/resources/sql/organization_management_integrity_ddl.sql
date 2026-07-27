BEGIN;

-- Internal-user creation now shares the sequence already used by the external
-- user workflow. Re-synchronize it on every deployment because older builds
-- allocated internal user codes with MAX(user_cd) + 1.
CREATE SEQUENCE IF NOT EXISTS public.docs_user_cd_sequence
    AS bigint MINVALUE 1 MAXVALUE 9999999999 START WITH 1 INCREMENT BY 1;

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

UPDATE public.docs_user
SET login_count = 0
WHERE login_count IS NULL;

ALTER TABLE public.docs_user
    ALTER COLUMN login_count SET DEFAULT 0,
    ALTER COLUMN login_count SET NOT NULL;

COMMIT;
