\set ON_ERROR_STOP on

BEGIN;

-- Install the nine reference PDFs as supplementary files on the GENERAL
-- sample document.  The identifiers are the first 32 lowercase hexadecimal
-- characters of each source file's SHA-256 digest.
LOCK TABLE docs_sw_sub_file IN SHARE ROW EXCLUSIVE MODE;
LOCK TABLE docs_sw, docs_file_security_label IN SHARE MODE;

CREATE TEMP TABLE desired_demo_reference_pdf (
    object_id         varchar(32)   PRIMARY KEY,
    parent_object_id  varchar(32)   NOT NULL,
    file_no           integer       NOT NULL,
    org_file_nm       varchar(255)  NOT NULL,
    file_path_nm      varchar(1000) NOT NULL,
    file_size         bigint        NOT NULL,
    UNIQUE (parent_object_id, file_no)
) ON COMMIT DROP;

INSERT INTO desired_demo_reference_pdf (
    object_id,
    parent_object_id,
    file_no,
    org_file_nm,
    file_path_nm,
    file_size
) VALUES
    (
        'e0e68cdc76374c31a1fb39b94fc88326',
        'KT1B-SAMPLE-DOC-001',
        101,
        'BOM Table Listing_SpinFire.pdf',
        '/data/kt1b/files/imports/3dpdf/e0e68cdc76374c31a1fb39b94fc88326.pdf',
        4128245
    ),
    (
        'c535e51333cd4c9eda4d888e9a179606',
        'KT1B-SAMPLE-DOC-001',
        102,
        'KAI_외부사용자_등록_및_프로비저닝_인증_매뉴얼.pdf',
        '/data/kt1b/files/imports/3dpdf/c535e51333cd4c9eda4d888e9a179606.pdf',
        414657
    ),
    (
        'a93e2c5ce3cfb60d968751c50483438e',
        'KT1B-SAMPLE-DOC-001',
        103,
        'KT1B_POC_Plan_Document_Types_v1.4.pdf',
        '/data/kt1b/files/imports/3dpdf/a93e2c5ce3cfb60d968751c50483438e.pdf',
        357232
    ),
    (
        '7c9bd39905c67397a4c80d4be73f9190',
        'KT1B-SAMPLE-DOC-001',
        104,
        'MIL-STD-31000B_SpinFire.pdf',
        '/data/kt1b/files/imports/3dpdf/7c9bd39905c67397a4c80d4be73f9190.pdf',
        2706431
    ),
    (
        '2a44854ea30cb163f38779e6a9b670b8',
        'KT1B-SAMPLE-DOC-001',
        105,
        'Model Viewer_SpinFire.pdf',
        '/data/kt1b/files/imports/3dpdf/2a44854ea30cb163f38779e6a9b670b8.pdf',
        3868217
    ),
    (
        '94a0f0cad77c0b330ee48bc1244dc2e0',
        'KT1B-SAMPLE-DOC-001',
        106,
        'PMI Analysis_SpinFire.pdf',
        '/data/kt1b/files/imports/3dpdf/94a0f0cad77c0b330ee48bc1244dc2e0.pdf',
        4597135
    ),
    (
        'cce7d038a5a3e4a4e15af06a8aa1c166',
        'KT1B-SAMPLE-DOC-001',
        107,
        'Technical Data Package_SpinFire.pdf',
        '/data/kt1b/files/imports/3dpdf/cce7d038a5a3e4a4e15af06a8aa1c166.pdf',
        6999035
    ),
    (
        '3f56288b805c19052d9d7ceb8de3c7cd',
        'KT1B-SAMPLE-DOC-001',
        108,
        'Work Instruction_SpinFire.pdf',
        '/data/kt1b/files/imports/3dpdf/3f56288b805c19052d9d7ceb8de3c7cd.pdf',
        8190369
    ),
    (
        '1f779d2686a5a33d03e831cd9b84a8a0',
        'KT1B-SAMPLE-DOC-001',
        109,
        '이솝소프트(주) 회사소개 및 제품소개서(약식)_202303.pdf',
        '/data/kt1b/files/imports/3dpdf/1f779d2686a5a33d03e831cd9b84a8a0.pdf',
        2106025
    );

DO $$
BEGIN
    IF (SELECT COUNT(*) FROM desired_demo_reference_pdf) <> 9 THEN
        RAISE EXCEPTION 'Expected exactly nine desired demo reference PDFs.';
    END IF;

    IF (
        SELECT COUNT(*)
          FROM docs_sw
         WHERE object_id = 'KT1B-SAMPLE-DOC-001'
    ) <> 1 THEN
        RAISE EXCEPTION 'Required parent SW document KT1B-SAMPLE-DOC-001 is missing or ambiguous.';
    END IF;

    IF (
        SELECT COUNT(*)
          FROM docs_file_security_label
         WHERE object_type = 'SW'
           AND object_id = 'KT1B-SAMPLE-DOC-001'
           AND file_no = '*'
           AND grade_cd = 'GENERAL'
    ) <> 1 THEN
        RAISE EXCEPTION 'Parent SW document must have exactly one wildcard GENERAL security label.';
    END IF;

    -- A digest-derived object identifier may be reused only by its exact
    -- desired row.  Operational timestamps are intentionally compared by
    -- invariant (present/absent), rather than against a newly generated time.
    IF EXISTS (
        SELECT 1
          FROM desired_demo_reference_pdf desired
          JOIN docs_sw_sub_file actual
            ON actual.object_id = desired.object_id
         WHERE actual.parent_object_id IS DISTINCT FROM desired.parent_object_id
            OR actual.file_no IS DISTINCT FROM desired.file_no
            OR actual.org_file_nm IS DISTINCT FROM desired.org_file_nm
            OR actual.file_path_nm IS DISTINCT FROM desired.file_path_nm
            OR actual.file_size IS DISTINCT FROM desired.file_size
            OR actual.use_yn IS DISTINCT FROM 'Y'
            OR actual.insert_uid IS DISTINCT FROM 'admin'
            OR actual.insert_dt IS NULL
            OR actual.update_uid IS NOT NULL
            OR actual.update_dt IS NOT NULL
            OR actual.processing_status IS DISTINCT FROM 'DONE'
            OR actual.processing_error IS NOT NULL
            OR actual.processed_at IS NULL
    ) THEN
        RAISE EXCEPTION 'An existing reference-PDF object_id does not exactly match its desired row.';
    END IF;

    IF EXISTS (
        SELECT 1
          FROM desired_demo_reference_pdf desired
          JOIN docs_sw_sub_file actual
            ON actual.parent_object_id = desired.parent_object_id
           AND actual.file_no = desired.file_no
         WHERE actual.object_id <> desired.object_id
    ) THEN
        RAISE EXCEPTION 'A desired parent/file_no slot is occupied by a different object_id.';
    END IF;
END
$$;

INSERT INTO docs_sw_sub_file (
    object_id,
    parent_object_id,
    file_no,
    org_file_nm,
    file_path_nm,
    file_size,
    use_yn,
    insert_uid,
    insert_dt,
    update_uid,
    update_dt,
    processing_status,
    processing_error,
    processed_at
)
SELECT desired.object_id,
       desired.parent_object_id,
       desired.file_no,
       desired.org_file_nm,
       desired.file_path_nm,
       desired.file_size,
       'Y',
       'admin',
       CURRENT_TIMESTAMP,
       NULL,
       NULL,
       'DONE',
       NULL,
       CURRENT_TIMESTAMP
  FROM desired_demo_reference_pdf desired
 WHERE NOT EXISTS (
       SELECT 1
         FROM docs_sw_sub_file actual
        WHERE actual.object_id = desired.object_id
 );

DO $$
BEGIN
    IF (
        SELECT COUNT(*)
          FROM desired_demo_reference_pdf desired
          JOIN docs_sw_sub_file actual
            ON actual.object_id = desired.object_id
           AND actual.parent_object_id = desired.parent_object_id
           AND actual.file_no = desired.file_no
           AND actual.org_file_nm = desired.org_file_nm
           AND actual.file_path_nm = desired.file_path_nm
           AND actual.file_size = desired.file_size
           AND actual.use_yn = 'Y'
           AND actual.insert_uid = 'admin'
           AND actual.insert_dt IS NOT NULL
           AND actual.update_uid IS NULL
           AND actual.update_dt IS NULL
           AND actual.processing_status = 'DONE'
           AND actual.processing_error IS NULL
           AND actual.processed_at IS NOT NULL
    ) <> 9 THEN
        RAISE EXCEPTION 'Post-install validation did not find all nine exact reference-PDF rows.';
    END IF;
END
$$;

COMMIT;
