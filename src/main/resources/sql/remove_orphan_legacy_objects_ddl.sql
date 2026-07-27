BEGIN;

-- Verified against the runtime source, the 2026-07-27 live PostgreSQL catalog,
-- foreign keys, defaults, views, functions, triggers, and current row counts.
-- These objects are empty/unowned remnants of the discontinued simple BBS and
-- item-use prototypes.
DROP TABLE IF EXISTS public.docs_bbs;
DROP SEQUENCE IF EXISTS public.mysimple_bbs_seq;
DROP SEQUENCE IF EXISTS public.item_use_info_seq;
DROP SEQUENCE IF EXISTS public.idx_seq;

COMMIT;
