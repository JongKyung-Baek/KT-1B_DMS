BEGIN;

-- Never leave TDMS offline indefinitely behind an unrelated open transaction.
-- PostgreSQL aborts this transaction cleanly if the protected tables cannot be
-- locked promptly; the deployment runner then restores the previous release.
SET LOCAL lock_timeout = '15s';
SET LOCAL statement_timeout = '5min';

-- DOCS_SW_TREE is the canonical technical-data classification tree.  The
-- PRODUCT/DXF copies are retired only when no business rows can still depend
-- on them and every active duplicate row is represented by the canonical
-- tree.  Locks keep those safety checks true until the DROP statements run.
DO $migration$
DECLARE
    target_tree_name text;
    document_table_name text;
    file_table_name text;
    guarded_table_name text;
    guarded_table regclass;
    target_tree regclass;
    has_rows boolean;
    has_mismatch boolean;
BEGIN
    IF to_regclass('public.docs_product_tree') IS NULL
       AND to_regclass('public.docs_dxf_tree') IS NULL THEN
        RETURN;
    END IF;

    IF to_regclass('public.docs_sw_tree') IS NULL THEN
        RAISE EXCEPTION
            'Cannot retire duplicate technical trees: canonical public.docs_sw_tree is missing.';
    END IF;

    LOCK TABLE public.docs_sw_tree IN ACCESS EXCLUSIVE MODE;

    IF NOT EXISTS (
        SELECT 1
          FROM public.docs_sw_tree
         WHERE tree_cd = 'ROOT'
    ) THEN
        RAISE EXCEPTION
            'Cannot retire duplicate technical trees: canonical ROOT is missing from public.docs_sw_tree.';
    END IF;

    FOR target_tree_name, document_table_name, file_table_name IN
        VALUES
            ('public.docs_product_tree',
             'public.docs_product_document',
             'public.docs_product_document_file'),
            ('public.docs_dxf_tree',
             'public.docs_dxf_document',
             'public.docs_dxf_document_file')
    LOOP
        target_tree := to_regclass(target_tree_name);
        IF target_tree IS NULL THEN
            CONTINUE;
        END IF;

        EXECUTE format(
            'LOCK TABLE %s IN ACCESS EXCLUSIVE MODE',
            target_tree
        );

        FOREACH guarded_table_name IN ARRAY
            ARRAY[document_table_name, file_table_name]
        LOOP
            guarded_table := to_regclass(guarded_table_name);
            IF guarded_table IS NOT NULL THEN
                EXECUTE format(
                    'LOCK TABLE %s IN ACCESS EXCLUSIVE MODE',
                    guarded_table
                );
                EXECUTE format(
                    'SELECT EXISTS (SELECT 1 FROM %s LIMIT 1)',
                    guarded_table
                ) INTO has_rows;

                IF has_rows THEN
                    RAISE EXCEPTION
                        'Cannot retire %: protected table % is not empty.',
                        target_tree_name,
                        guarded_table_name;
                END IF;
            END IF;
        END LOOP;

        -- Active non-ROOT rows must have a fully identical canonical row.
        -- IS NOT DISTINCT FROM makes the comparison exact and NULL-safe.
        EXECUTE format($query$
            SELECT EXISTS (
                SELECT 1
                  FROM %s legacy
                 WHERE COALESCE(legacy.use_yn, 'Y') = 'Y'
                   AND legacy.tree_cd IS DISTINCT FROM 'ROOT'
                   AND NOT EXISTS (
                       SELECT 1
                         FROM public.docs_sw_tree canonical
                        WHERE canonical.tree_cd
                                  IS NOT DISTINCT FROM legacy.tree_cd
                          AND canonical.function_cd
                                  IS NOT DISTINCT FROM legacy.function_cd
                          AND canonical.upper_tree_cd
                                  IS NOT DISTINCT FROM legacy.upper_tree_cd
                          AND canonical.tree_nm
                                  IS NOT DISTINCT FROM legacy.tree_nm
                          AND canonical.tree_depth
                                  IS NOT DISTINCT FROM legacy.tree_depth
                          AND canonical.sort_order
                                  IS NOT DISTINCT FROM legacy.sort_order
                          AND canonical.tree_path
                                  IS NOT DISTINCT FROM legacy.tree_path
                          AND canonical.use_yn
                                  IS NOT DISTINCT FROM legacy.use_yn
                   )
            )
        $query$, target_tree) INTO has_mismatch;

        IF has_mismatch THEN
            RAISE EXCEPTION
                'Cannot retire %: an active non-ROOT row differs from public.docs_sw_tree.',
                target_tree_name;
        END IF;

        -- ROOT display names were historically domain-specific.  All other
        -- active ROOT attributes must still match the canonical ROOT row.
        EXECUTE format($query$
            SELECT EXISTS (
                SELECT 1
                  FROM %s legacy
                 WHERE COALESCE(legacy.use_yn, 'Y') = 'Y'
                   AND legacy.tree_cd IS NOT DISTINCT FROM 'ROOT'
                   AND NOT EXISTS (
                       SELECT 1
                         FROM public.docs_sw_tree canonical
                        WHERE canonical.tree_cd
                                  IS NOT DISTINCT FROM legacy.tree_cd
                          AND canonical.function_cd
                                  IS NOT DISTINCT FROM legacy.function_cd
                          AND canonical.upper_tree_cd
                                  IS NOT DISTINCT FROM legacy.upper_tree_cd
                          AND canonical.tree_depth
                                  IS NOT DISTINCT FROM legacy.tree_depth
                          AND canonical.sort_order
                                  IS NOT DISTINCT FROM legacy.sort_order
                          AND canonical.tree_path
                                  IS NOT DISTINCT FROM legacy.tree_path
                          AND canonical.use_yn
                                  IS NOT DISTINCT FROM legacy.use_yn
                   )
            )
        $query$, target_tree) INTO has_mismatch;

        IF has_mismatch THEN
            RAISE EXCEPTION
                'Cannot retire %: active ROOT attributes other than TREE_NM differ from public.docs_sw_tree.',
                target_tree_name;
        END IF;
    END LOOP;
END
$migration$;

-- An unexpected dependency must abort this transaction instead of deleting
-- additional schema objects.
DROP TABLE IF EXISTS public.docs_product_tree;
DROP TABLE IF EXISTS public.docs_dxf_tree;

COMMIT;
