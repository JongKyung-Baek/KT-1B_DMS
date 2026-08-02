\set ON_ERROR_STOP on

BEGIN;

-- Preload the forty-four STP assets for the CollabView3D integration as
-- supplementary files on the GENERAL sample document. TDMS sends these raw
-- STEP bytes to the dedicated signed 3D viewer route; they are never sent to
-- the PDF viewer or passed through the legacy PDF conversion flow.
-- The identifiers are the first 32 lowercase hexadecimal characters of each
-- source file's SHA-256 digest.
LOCK TABLE docs_sw_sub_file IN SHARE ROW EXCLUSIVE MODE;
LOCK TABLE docs_sw, docs_file_security_label IN SHARE MODE;

CREATE TEMP TABLE desired_demo_step_model (
    object_id         varchar(32)   PRIMARY KEY,
    parent_object_id  varchar(32)   NOT NULL,
    file_no           integer       NOT NULL,
    org_file_nm       varchar(255)  NOT NULL,
    file_path_nm      varchar(1000) NOT NULL,
    file_size         bigint        NOT NULL,
    UNIQUE (parent_object_id, file_no)
) ON COMMIT DROP;

INSERT INTO desired_demo_step_model (
    object_id,
    parent_object_id,
    file_no,
    org_file_nm,
    file_path_nm,
    file_size
) VALUES
    ('420076d7edd3f626f9b49c5eb126e932', 'KT1B-SAMPLE-DOC-001', 201,
     '1797609in.stp',
     '/data/kt1b/files/imports/step/420076d7edd3f626f9b49c5eb126e932.stp', 166214),
    ('3ad5f999b29e7c8720c45c4b23c7ca70', 'KT1B-SAMPLE-DOC-001', 202,
     'ap224_997423743.stp',
     '/data/kt1b/files/imports/step/3ad5f999b29e7c8720c45c4b23c7ca70.stp', 265608),
    ('1bb1a0e55dc4a0169e329529eb520596', 'KT1B-SAMPLE-DOC-001', 203,
     'as1-ac-214.stp',
     '/data/kt1b/files/imports/step/1bb1a0e55dc4a0169e329529eb520596.stp', 83424),
    ('7fe95aa99b807f97def923d5ea492587', 'KT1B-SAMPLE-DOC-001', 204,
     'blower.stp',
     '/data/kt1b/files/imports/step/7fe95aa99b807f97def923d5ea492587.stp', 207370),
    ('b8b7333c60d2481da679363348603072', 'KT1B-SAMPLE-DOC-001', 205,
     'boxy_with_cylindricity.stp',
     '/data/kt1b/files/imports/step/b8b7333c60d2481da679363348603072.stp', 87040),
    ('29c1240fce2827aa3729397c267360fd', 'KT1B-SAMPLE-DOC-001', 206,
     'Centering pin mechanism_case study1.stp',
     '/data/kt1b/files/imports/step/29c1240fce2827aa3729397c267360fd.stp', 153685),
    ('6307ac9e61d9b05550c715d5fc815e4a', 'KT1B-SAMPLE-DOC-001', 207,
     'Centering pin mechanism_dlm.stp',
     '/data/kt1b/files/imports/step/6307ac9e61d9b05550c715d5fc815e4a.stp', 154031),
    ('f002b6aaeeaca13dd5329f850c7dfbfc', 'KT1B-SAMPLE-DOC-001', 208,
     'Engine assembly_case study2.stp',
     '/data/kt1b/files/imports/step/f002b6aaeeaca13dd5329f850c7dfbfc.stp', 377125),
    ('e081d518484d5c708c6729353237f94a', 'KT1B-SAMPLE-DOC-001', 209,
     'nist_ctc_01_asme1_rd.stp',
     '/data/kt1b/files/imports/step/e081d518484d5c708c6729353237f94a.stp', 235171),
    ('e6df6d0ed68fd8a91bd3f8360c7047f2', 'KT1B-SAMPLE-DOC-001', 210,
     'nist_ctc_02_asme1_rc.stp',
     '/data/kt1b/files/imports/step/e6df6d0ed68fd8a91bd3f8360c7047f2.stp', 1154600),
    ('95a775b1acdbc34de11319cb83b4cf6c', 'KT1B-SAMPLE-DOC-001', 211,
     'nist_ctc_03_asme1_rc.stp',
     '/data/kt1b/files/imports/step/95a775b1acdbc34de11319cb83b4cf6c.stp', 252128),
    ('cdb59a085a3364941b881f96b511265a', 'KT1B-SAMPLE-DOC-001', 212,
     'nist_ctc_04_asme1_rd.stp',
     '/data/kt1b/files/imports/step/cdb59a085a3364941b881f96b511265a.stp', 793048),
    ('5140b8f41fa1d0a069ea978e28c92a59', 'KT1B-SAMPLE-DOC-001', 213,
     'nist_ctc_05_asme1_rd.stp',
     '/data/kt1b/files/imports/step/5140b8f41fa1d0a069ea978e28c92a59.stp', 326817),
    ('5e30f0903f9470c14a98e70395ca005f', 'KT1B-SAMPLE-DOC-001', 214,
     'nist_ftc_06_asme1_rd.stp',
     '/data/kt1b/files/imports/step/5e30f0903f9470c14a98e70395ca005f.stp', 222799),
    ('a0bc551656780b26d7a0ee1beedbc07d', 'KT1B-SAMPLE-DOC-001', 215,
     'nist_ftc_07_asme1_rd.stp',
     '/data/kt1b/files/imports/step/a0bc551656780b26d7a0ee1beedbc07d.stp', 402531),
    ('4b7011b0b86cf6301058aa954b76a958', 'KT1B-SAMPLE-DOC-001', 216,
     'nist_ftc_08_asme1_rc.stp',
     '/data/kt1b/files/imports/step/4b7011b0b86cf6301058aa954b76a958.stp', 450741),
    ('e29c09bd2d454601ff1a0d77c4c4ffaf', 'KT1B-SAMPLE-DOC-001', 217,
     'nist_ftc_09_asme1_rd.stp',
     '/data/kt1b/files/imports/step/e29c09bd2d454601ff1a0d77c4c4ffaf.stp', 263916),
    ('2bd726f9fe86aeb96ab517ce876e5696', 'KT1B-SAMPLE-DOC-001', 218,
     'nist_ftc_10_asme1_rb.stp',
     '/data/kt1b/files/imports/step/2bd726f9fe86aeb96ab517ce876e5696.stp', 323601),
    ('a5abfea460499dc31bc812aca6891794', 'KT1B-SAMPLE-DOC-001', 219,
     'nist_ftc_11_asme1_rb.stp',
     '/data/kt1b/files/imports/step/a5abfea460499dc31bc812aca6891794.stp', 7634),
    ('a3058f47323c7e0e24249eeaa1993986', 'KT1B-SAMPLE-DOC-001', 220,
     'nist_ctc_01_asme1_ap203.stp',
     '/data/kt1b/files/imports/step/a3058f47323c7e0e24249eeaa1993986.stp', 1190836),
    ('ae0049980cb11cb224447e1738b499d8', 'KT1B-SAMPLE-DOC-001', 221,
     'nist_ctc_02_asme1_ap203.stp',
     '/data/kt1b/files/imports/step/ae0049980cb11cb224447e1738b499d8.stp', 3361535),
    ('294444aadbe1b427013250cd07ece07d', 'KT1B-SAMPLE-DOC-001', 222,
     'nist_ctc_03_asme1_ap203.stp',
     '/data/kt1b/files/imports/step/294444aadbe1b427013250cd07ece07d.stp', 2020297),
    ('d6d8f9b3439bb334b85b5c94edd613c0', 'KT1B-SAMPLE-DOC-001', 223,
     'nist_ctc_04_asme1_ap203.stp',
     '/data/kt1b/files/imports/step/d6d8f9b3439bb334b85b5c94edd613c0.stp', 1388915),
    ('9df8bf141d5e5b50856021b7c209dea9', 'KT1B-SAMPLE-DOC-001', 224,
     'nist_ctc_05_asme1_ap203.stp',
     '/data/kt1b/files/imports/step/9df8bf141d5e5b50856021b7c209dea9.stp', 1069563),
    ('85a5752da05f53c456ca3a9e038c9035', 'KT1B-SAMPLE-DOC-001', 225,
     'nist_ctc_01_asme1_ap242-e1.stp',
     '/data/kt1b/files/imports/step/85a5752da05f53c456ca3a9e038c9035.stp', 396445),
    ('99a0a2079ddeb64d05c2432cbe931fa1', 'KT1B-SAMPLE-DOC-001', 226,
     'nist_ctc_02_asme1_ap242-e2.stp',
     '/data/kt1b/files/imports/step/99a0a2079ddeb64d05c2432cbe931fa1.stp', 1981342),
    ('196b665776e759282f80fc8fb27d7bce', 'KT1B-SAMPLE-DOC-001', 227,
     'nist_ctc_03_asme1_ap242-e2.stp',
     '/data/kt1b/files/imports/step/196b665776e759282f80fc8fb27d7bce.stp', 673846),
    ('20b43b54ce25d4ed17cff794084c406e', 'KT1B-SAMPLE-DOC-001', 228,
     'nist_ctc_04_asme1_ap242-e1.stp',
     '/data/kt1b/files/imports/step/20b43b54ce25d4ed17cff794084c406e.stp', 1287291),
    ('59bbc09a34621c03106e4c1b2a5bc909', 'KT1B-SAMPLE-DOC-001', 229,
     'nist_ctc_05_asme1_ap242-e1.stp',
     '/data/kt1b/files/imports/step/59bbc09a34621c03106e4c1b2a5bc909.stp', 878030),
    ('b87afb7f25adc0d44d12e182209d50f0', 'KT1B-SAMPLE-DOC-001', 230,
     'nist_ftc_06_asme1_ap242-e2.stp',
     '/data/kt1b/files/imports/step/b87afb7f25adc0d44d12e182209d50f0.stp', 1971192),
    ('aa8115c12e12a99cc439477cbfd22291', 'KT1B-SAMPLE-DOC-001', 231,
     'nist_ftc_07_asme1_ap242-e2.stp',
     '/data/kt1b/files/imports/step/aa8115c12e12a99cc439477cbfd22291.stp', 1478375),
    ('841a9ac51a32fe25b872f283b0b8770c', 'KT1B-SAMPLE-DOC-001', 232,
     'nist_ftc_08_asme1_ap242-e1-tg.stp',
     '/data/kt1b/files/imports/step/841a9ac51a32fe25b872f283b0b8770c.stp', 2120356),
    ('7b9283b4288d152fd181d76c39285d7c', 'KT1B-SAMPLE-DOC-001', 233,
     'nist_ftc_08_asme1_ap242-e2.stp',
     '/data/kt1b/files/imports/step/7b9283b4288d152fd181d76c39285d7c.stp', 4722264),
    ('f1215fe15a78085a9fa78dd81714caf7', 'KT1B-SAMPLE-DOC-001', 234,
     'nist_ftc_09_asme1_ap242-e1.stp',
     '/data/kt1b/files/imports/step/f1215fe15a78085a9fa78dd81714caf7.stp', 6109836),
    ('9d7711f48e81831c5d1838fdac5c11c0', 'KT1B-SAMPLE-DOC-001', 235,
     'nist_ftc_10_asme1_ap242-e2.stp',
     '/data/kt1b/files/imports/step/9d7711f48e81831c5d1838fdac5c11c0.stp', 1879881),
    ('20a92edf514ae0989d556f9c7b9f065a', 'KT1B-SAMPLE-DOC-001', 236,
     'nist_ftc_11_asme1_ap242-e2.stp',
     '/data/kt1b/files/imports/step/20a92edf514ae0989d556f9c7b9f065a.stp', 1946454),
    ('71777c28da76da0e8a667e4cbe792d5f', 'KT1B-SAMPLE-DOC-001', 237,
     'nist_stc_06_asme1_ap242-e3.stp',
     '/data/kt1b/files/imports/step/71777c28da76da0e8a667e4cbe792d5f.stp', 1006401),
    ('2dddf9b8894bc241f7ffddf3dc4502ba', 'KT1B-SAMPLE-DOC-001', 238,
     'nist_stc_07_asme1_ap242-e3.stp',
     '/data/kt1b/files/imports/step/2dddf9b8894bc241f7ffddf3dc4502ba.stp', 1945387),
    ('64f24b914910ce8e20c9e4bd59637b4f', 'KT1B-SAMPLE-DOC-001', 239,
     'nist_stc_08_asme1_ap242-e3.stp',
     '/data/kt1b/files/imports/step/64f24b914910ce8e20c9e4bd59637b4f.stp', 2251152),
    ('737423afcb223b87d92a4ea7646a97d8', 'KT1B-SAMPLE-DOC-001', 240,
     'nist_stc_09_asme1_ap242-e3.stp',
     '/data/kt1b/files/imports/step/737423afcb223b87d92a4ea7646a97d8.stp', 5287678),
    ('fba4f46cab1ce1402362038036884631', 'KT1B-SAMPLE-DOC-001', 241,
     'nist_stc_10_asme1_ap242-e2.stp',
     '/data/kt1b/files/imports/step/fba4f46cab1ce1402362038036884631.stp', 4875056),
    ('c1a17cf53d2eaed106be4917bec4b68c', 'KT1B-SAMPLE-DOC-001', 242,
     'Centering pin mechanism for DLM.STP',
     '/data/kt1b/files/imports/step/c1a17cf53d2eaed106be4917bec4b68c.stp', 417974),
    ('67052e1ce631b1f1e0de9cdd8583e17a', 'KT1B-SAMPLE-DOC-001', 243,
     'CENTERING PIN MECHANISM.STP',
     '/data/kt1b/files/imports/step/67052e1ce631b1f1e0de9cdd8583e17a.stp', 407957),
    ('c5cc91515c359273bab68da68a5a72ae', 'KT1B-SAMPLE-DOC-001', 244,
     'ENGINE ASSEMBLY.STP',
     '/data/kt1b/files/imports/step/c5cc91515c359273bab68da68a5a72ae.stp', 886163);

DO $$
BEGIN
    IF (SELECT COUNT(*) FROM desired_demo_step_model) <> 44 THEN
        RAISE EXCEPTION 'Expected exactly forty-four desired demo STP models.';
    END IF;

    IF (
        SELECT COUNT(*)
          FROM desired_demo_step_model
         WHERE parent_object_id = 'KT1B-SAMPLE-DOC-001'
           AND file_no BETWEEN 201 AND 244
           AND object_id ~ '^[0-9a-f]{32}$'
           AND file_path_nm = '/data/kt1b/files/imports/step/' || object_id || '.stp'
    ) <> 44 THEN
        RAISE EXCEPTION 'The desired demo STP manifest is not canonical.';
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
    -- desired row.  Operational timestamps are compared by invariant rather
    -- than against the time of this rerun.
    IF EXISTS (
        SELECT 1
          FROM desired_demo_step_model desired
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
        RAISE EXCEPTION 'An existing demo-STP object_id does not exactly match its desired row.';
    END IF;

    IF EXISTS (
        SELECT 1
          FROM desired_demo_step_model desired
          JOIN docs_sw_sub_file actual
            ON actual.parent_object_id = desired.parent_object_id
           AND actual.file_no = desired.file_no
         WHERE actual.object_id <> desired.object_id
    ) THEN
        RAISE EXCEPTION 'A desired demo-STP parent/file_no slot is occupied by a different object_id.';
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
  FROM desired_demo_step_model desired
 WHERE NOT EXISTS (
       SELECT 1
         FROM docs_sw_sub_file actual
        WHERE actual.object_id = desired.object_id
 );

DO $$
BEGIN
    IF (
        SELECT COUNT(*)
          FROM desired_demo_step_model desired
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
    ) <> 44 THEN
        RAISE EXCEPTION 'Post-install validation did not find all forty-four exact demo-STP rows.';
    END IF;
END
$$;

COMMIT;
