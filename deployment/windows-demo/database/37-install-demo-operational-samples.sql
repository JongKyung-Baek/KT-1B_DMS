-- Additive operational demo records for an existing KT-1B TDMS database.
-- Technical documents, files, ACLs, users, history, and audit rows are read-only.

\set ON_ERROR_STOP on

BEGIN;

-- Keep technical data stable for the whole seed transaction and retain a
-- whole-row fingerprint so an accidental write cannot be committed silently.
LOCK TABLE docs_sw, docs_sw_file, docs_sw_sub_file IN SHARE MODE;

CREATE TEMP TABLE demo_technical_fingerprint (
    table_name  text PRIMARY KEY,
    row_count   bigint NOT NULL,
    fingerprint text NOT NULL
) ON COMMIT DROP;

INSERT INTO demo_technical_fingerprint (table_name, row_count, fingerprint)
SELECT 'docs_sw', COUNT(*),
       md5(COALESCE(string_agg(md5(to_jsonb(source_row)::text), ''
           ORDER BY md5(to_jsonb(source_row)::text)), ''))
  FROM docs_sw source_row
UNION ALL
SELECT 'docs_sw_file', COUNT(*),
       md5(COALESCE(string_agg(md5(to_jsonb(source_row)::text), ''
           ORDER BY md5(to_jsonb(source_row)::text)), ''))
  FROM docs_sw_file source_row
UNION ALL
SELECT 'docs_sw_sub_file', COUNT(*),
       md5(COALESCE(string_agg(md5(to_jsonb(source_row)::text), ''
           ORDER BY md5(to_jsonb(source_row)::text)), ''))
  FROM docs_sw_sub_file source_row;

CREATE TEMP TABLE demo_actor ON COMMIT DROP AS
SELECT user_cd, user_id, user_nm, dept_cd
  FROM docs_user
 WHERE user_cd = 'USER_0000000001'
   AND use_yn = 'Y'
   AND del_yn = 'N';

DO $$
BEGIN
    IF (SELECT COUNT(*) FROM demo_actor) <> 1 THEN
        RAISE EXCEPTION 'DEMO seed requires active administrator USER_0000000001';
    END IF;
END
$$;

-- Snapshot one existing safe technical-data row for workflow line items. This
-- creates no new technical material and changes none of the source rows.
CREATE TEMP TABLE demo_material_snapshot ON COMMIT DROP AS
SELECT sw.object_id,
       file_row.file_no::varchar(50) AS file_no,
       COALESCE(NULLIF(BTRIM(sw.sw_no), ''), sw.object_id) AS material_no,
       COALESCE(NULLIF(BTRIM(sw.sw_nm), ''), sw.object_id) AS material_name,
       COALESCE(NULLIF(BTRIM(file_row.org_file_nm), ''), file_row.file_nm)
           AS original_file_name,
       COALESCE(NULLIF(file_row.file_size::text, '')::bigint, 0) AS file_size,
       COALESCE(label.grade_cd, 'GENERAL') AS grade_cd,
       COALESCE(NULLIF(BTRIM(sw.tree_cd), ''), 'DEMO') AS tree_cd,
       'KT-1B Sample Technical Data'::varchar(500) AS tree_nm,
       'ROOT'::varchar(50) AS parent_tree_cd,
       'Technical Data'::varchar(500) AS parent_tree_nm
  FROM docs_sw sw
  JOIN docs_sw_file file_row
    ON file_row.object_id = sw.object_id
  LEFT JOIN LATERAL (
      SELECT security_label.grade_cd
        FROM docs_file_security_label security_label
       WHERE security_label.object_type = 'SW'
         AND security_label.object_id = sw.object_id
         AND security_label.file_no IN (file_row.file_no::text, '*')
       ORDER BY CASE WHEN security_label.file_no = file_row.file_no::text
                     THEN 0 ELSE 1 END
       LIMIT 1
  ) label ON true
 WHERE sw.object_id = 'KT1B-SAMPLE-DOC-003'
   AND COALESCE(sw.deleted_yn, 'N') = 'N'
   AND COALESCE(file_row.processing_status, 'DONE') = 'DONE'
 ORDER BY file_row.file_no::text
 LIMIT 1;

DO $$
BEGIN
    IF (SELECT COUNT(*) FROM demo_material_snapshot) <> 1 THEN
        RAISE EXCEPTION 'DEMO workflow source KT1B-SAMPLE-DOC-003 is unavailable';
    END IF;
END
$$;

-- Remove only records owned by this seed so the script can be safely rerun.
DELETE FROM docs_distribution_outbox
 WHERE request_id IN (
       SELECT request_id FROM docs_distribution_request
        WHERE request_no LIKE 'DREQ-DEMO-%'
 );

DELETE FROM docs_distribution_request_event
 WHERE request_id IN (
       SELECT request_id FROM docs_distribution_request
        WHERE request_no LIKE 'DREQ-DEMO-%'
 );

DELETE FROM docs_distribution_request_recipient
 WHERE request_id IN (
       SELECT request_id FROM docs_distribution_request
        WHERE request_no LIKE 'DREQ-DEMO-%'
 );

DELETE FROM docs_distribution_request_item
 WHERE request_id IN (
       SELECT request_id FROM docs_distribution_request
        WHERE request_no LIKE 'DREQ-DEMO-%'
 );

DELETE FROM docs_distribution_request
 WHERE request_no LIKE 'DREQ-DEMO-%';

DELETE FROM docs_distribution_account_request_event
 WHERE request_id IN (
       SELECT request_id FROM docs_distribution_account_request
        WHERE correlation_id LIKE 'DEMO-ACCOUNT-%'
 );

DELETE FROM docs_distribution_account_request
 WHERE correlation_id LIKE 'DEMO-ACCOUNT-%';

DELETE FROM docs_partner_user
 WHERE partner_company_id IN (
       SELECT partner_company_id FROM docs_partner_company
        WHERE company_code LIKE 'DEMO-PARTNER-%'
 );

DELETE FROM docs_partner_company
 WHERE company_code LIKE 'DEMO-PARTNER-%';

-- The partner tables intentionally use explicit sequence allocators.
SELECT setval(
    'docs_partner_company_id_seq',
    GREATEST(
        COALESCE((SELECT MAX(partner_company_id) FROM docs_partner_company), 0),
        (SELECT last_value FROM docs_partner_company_id_seq),
        1
    ),
    true
);

SELECT setval(
    'docs_partner_user_id_seq',
    GREATEST(
        COALESCE((SELECT MAX(partner_user_id) FROM docs_partner_user), 0),
        (SELECT last_value FROM docs_partner_user_id_seq),
        1
    ),
    true
);

INSERT INTO docs_partner_company (
    partner_company_id, company_code, company_name, business_no,
    contact_email, contact_phone, address, use_yn, del_yn,
    created_by, created_at, updated_by, updated_at
) VALUES
    (nextval('docs_partner_company_id_seq'),
     'DEMO-PARTNER-001', 'PT Nusantara Aviation', 'DEMO-ID-001',
     'td@nusantara.example', '+62-21-555-0101', 'Jakarta, Indonesia',
     'Y', 'N', 'USER_0000000001', CURRENT_TIMESTAMP,
     'USER_0000000001', CURRENT_TIMESTAMP),
    (nextval('docs_partner_company_id_seq'),
     'DEMO-PARTNER-002', 'PT Garuda Technical Services', 'DEMO-ID-002',
     'data@garuda-tech.example', '+62-22-555-0202', 'Bandung, Indonesia',
     'Y', 'N', 'USER_0000000001', CURRENT_TIMESTAMP,
     'USER_0000000001', CURRENT_TIMESTAMP);

INSERT INTO docs_partner_user (
    partner_user_id, partner_company_id, user_name, email, phone,
    position_name, representative_yn, use_yn, del_yn,
    created_by, created_at, updated_by, updated_at
)
SELECT nextval('docs_partner_user_id_seq'), company.partner_company_id,
       contact.user_name, contact.email, contact.phone,
       contact.position_name, contact.representative_yn,
       'Y', 'N', 'USER_0000000001', CURRENT_TIMESTAMP,
       'USER_0000000001', CURRENT_TIMESTAMP
  FROM (
      VALUES
          ('DEMO-PARTNER-001', 'Andi Pratama',
           'andi.pratama@nusantara.example', '+62-812-1000-1001',
           'Technical Data Lead', 'Y'),
          ('DEMO-PARTNER-001', 'Siti Rahma',
           'siti.rahma@nusantara.example', '+62-812-1000-1002',
           'Configuration Engineer', 'N'),
          ('DEMO-PARTNER-001', 'Budi Santoso',
           'budi.santoso@nusantara.example', '+62-812-1000-1003',
           'Quality Engineer', 'N'),
          ('DEMO-PARTNER-002', 'Dewi Lestari',
           'dewi.lestari@garuda-tech.example', '+62-812-2000-2001',
           'Program Coordinator', 'Y'),
          ('DEMO-PARTNER-002', 'Rizky Maulana',
           'rizky.maulana@garuda-tech.example', '+62-812-2000-2002',
           'Technical Librarian', 'N')
  ) contact(company_code, user_name, email, phone, position_name,
            representative_yn)
  JOIN docs_partner_company company
    ON company.company_code = contact.company_code;

-- Four records make every workflow state visible without triggering an
-- external transfer. Only the approved record receives a HOLD outbox row.
INSERT INTO docs_distribution_request (
    request_no, title, purpose, status,
    requested_by_user_cd, requested_by_user_id, requested_by_user_nm,
    requested_dept_cd, requested_dept_nm,
    partner_company_id, partner_company_code, partner_company_name,
    approver_user_cd, approver_user_id, approver_user_nm,
    distribution_start_date, distribution_end_date,
    submitted_at, decided_at,
    decided_by_user_cd, decided_by_user_id, decided_by_user_nm,
    decision_comment, version_no,
    created_by, created_at, updated_by, updated_at
)
SELECT sample.request_no, sample.title, sample.purpose, sample.status,
       actor.user_cd, actor.user_id, actor.user_nm,
       actor.dept_cd, NULL,
       company.partner_company_id, company.company_code, company.company_name,
       actor.user_cd, actor.user_id, actor.user_nm,
       CURRENT_DATE - sample.start_days,
       CURRENT_DATE + sample.end_days,
       CASE WHEN sample.status = 'DRAFT' THEN NULL
            ELSE CURRENT_TIMESTAMP - INTERVAL '2 days' END,
       CASE WHEN sample.status IN ('APPROVED', 'REJECTED')
            THEN CURRENT_TIMESTAMP - INTERVAL '1 day' ELSE NULL END,
       CASE WHEN sample.status IN ('APPROVED', 'REJECTED')
            THEN actor.user_cd ELSE NULL END,
       CASE WHEN sample.status IN ('APPROVED', 'REJECTED')
            THEN actor.user_id ELSE NULL END,
       CASE WHEN sample.status IN ('APPROVED', 'REJECTED')
            THEN actor.user_nm ELSE NULL END,
       sample.decision_comment,
       CASE WHEN sample.status = 'DRAFT' THEN 0
            WHEN sample.status = 'PENDING_APPROVAL' THEN 1 ELSE 2 END,
       actor.user_cd, CURRENT_TIMESTAMP - INTERVAL '3 days',
       actor.user_cd, CURRENT_TIMESTAMP
  FROM (
      VALUES
          ('DREQ-DEMO-DRAFT-001', 'Draft distribution sample',
           'Review a distribution request before submission.', 'DRAFT',
           'DEMO-PARTNER-001', 0, 14, NULL::varchar(1000)),
          ('DREQ-DEMO-PENDING-001', 'Pending approval sample',
           'Review an awaiting-approval distribution request.',
           'PENDING_APPROVAL', 'DEMO-PARTNER-001', 1, 10, NULL::varchar(1000)),
          ('DREQ-DEMO-APPROVED-001', 'Approved distribution sample',
           'Review an approved request whose delivery remains on hold.',
           'APPROVED', 'DEMO-PARTNER-002', 2, 7,
           'Approved for demonstration.'::varchar(1000)),
          ('DREQ-DEMO-REJECTED-001', 'Rejected distribution sample',
           'Review a rejected distribution request and its decision.',
           'REJECTED', 'DEMO-PARTNER-002', 3, 5,
           'Rejected for demonstration.'::varchar(1000))
  ) sample(request_no, title, purpose, status, company_code,
           start_days, end_days, decision_comment)
 CROSS JOIN demo_actor actor
  JOIN docs_partner_company company
    ON company.company_code = sample.company_code;

INSERT INTO docs_distribution_request_item (
    request_id, line_no, document_line_no, file_line_no,
    object_type, object_id, file_no,
    material_no, material_name, original_file_name, file_size, grade_cd,
    tree_cd, tree_nm, parent_tree_cd, parent_tree_nm, snapshot_at
)
SELECT request_row.request_id, 1, 1, 1,
       'SW', material.object_id, material.file_no,
       material.material_no, material.material_name,
       material.original_file_name, material.file_size, material.grade_cd,
       material.tree_cd, material.tree_nm,
       material.parent_tree_cd, material.parent_tree_nm, CURRENT_TIMESTAMP
  FROM docs_distribution_request request_row
 CROSS JOIN demo_material_snapshot material
 WHERE request_row.request_no LIKE 'DREQ-DEMO-%';

INSERT INTO docs_distribution_request_recipient (
    request_id, line_no, partner_company_id, partner_user_id,
    user_name, email, phone, representative_yn, snapshot_at
)
SELECT request_row.request_id, 1,
       company.partner_company_id, partner_user.partner_user_id,
       partner_user.user_name, partner_user.email, partner_user.phone,
       partner_user.representative_yn, CURRENT_TIMESTAMP
  FROM docs_distribution_request request_row
  JOIN docs_partner_company company
    ON company.partner_company_id = request_row.partner_company_id
  JOIN docs_partner_user partner_user
    ON partner_user.partner_company_id = company.partner_company_id
   AND partner_user.representative_yn = 'Y'
   AND partner_user.use_yn = 'Y'
   AND partner_user.del_yn = 'N'
 WHERE request_row.request_no LIKE 'DREQ-DEMO-%';

INSERT INTO docs_distribution_request_event (
    request_id, from_status, to_status, event_type,
    actor_user_cd, actor_user_id, actor_user_nm,
    event_comment, occurred_at
)
SELECT request_row.request_id, NULL, 'DRAFT', 'CREATE',
       actor.user_cd, actor.user_id, actor.user_nm,
       'DEMO request created', request_row.created_at
  FROM docs_distribution_request request_row
 CROSS JOIN demo_actor actor
 WHERE request_row.request_no LIKE 'DREQ-DEMO-%'
UNION ALL
SELECT request_row.request_id, 'DRAFT', 'PENDING_APPROVAL', 'SUBMIT',
       actor.user_cd, actor.user_id, actor.user_nm,
       'DEMO request submitted', request_row.submitted_at
  FROM docs_distribution_request request_row
 CROSS JOIN demo_actor actor
 WHERE request_row.request_no LIKE 'DREQ-DEMO-%'
   AND request_row.status <> 'DRAFT'
UNION ALL
SELECT request_row.request_id, 'PENDING_APPROVAL', 'APPROVED', 'APPROVE',
       actor.user_cd, actor.user_id, actor.user_nm,
       request_row.decision_comment, request_row.decided_at
  FROM docs_distribution_request request_row
 CROSS JOIN demo_actor actor
 WHERE request_row.request_no = 'DREQ-DEMO-APPROVED-001'
UNION ALL
SELECT request_row.request_id, 'PENDING_APPROVAL', 'REJECTED', 'REJECT',
       actor.user_cd, actor.user_id, actor.user_nm,
       request_row.decision_comment, request_row.decided_at
  FROM docs_distribution_request request_row
 CROSS JOIN demo_actor actor
 WHERE request_row.request_no = 'DREQ-DEMO-REJECTED-001';

INSERT INTO docs_distribution_outbox (
    request_id, aggregate_id, event_type, status, payload_json,
    created_at, released_at, sent_at, attempt_count, last_error
)
SELECT request_row.request_id, request_row.request_no,
       'DISTRIBUTION_APPROVED', 'HOLD',
       jsonb_build_object(
           'demo', true,
           'requestNo', request_row.request_no,
           'partnerCompanyCode', request_row.partner_company_code,
           'deliveryEnabled', false
       ),
       request_row.decided_at, NULL, NULL, 0, NULL
  FROM docs_distribution_request request_row
 WHERE request_row.request_no = 'DREQ-DEMO-APPROVED-001';

-- Three external account-request types, all left in a non-decided PENDING
-- state. No replay nonce is inserted or changed.
INSERT INTO docs_distribution_account_request (
    event_id, correlation_id, client_id, source_system_id,
    request_type, occurred_at, received_at, status,
    representative_id, representative_name, representative_email,
    representative_phone, organization_code, organization_name,
    business_number, target_user_id, target_user_name,
    target_user_email, target_user_phone, target_user_position,
    reason, metadata_json, content_sha256,
    decision_comment, decided_by_user_cd, decided_by_user_id,
    decided_by_user_name, decided_at, updated_at
) VALUES
    ('37000000-0000-4000-8000-000000000001',
     'DEMO-ACCOUNT-REGISTER-001', 'DEMO-DISTRIBUTION-CLIENT',
     'DISTRIBUTION-DEMO', 'REGISTER_USER',
     CURRENT_TIMESTAMP - INTERVAL '3 hours', CURRENT_TIMESTAMP - INTERVAL '3 hours',
     'PENDING', 'REP-DEMO-001', 'Andi Pratama',
     'andi.pratama@nusantara.example', '+62-812-1000-1001',
     'DEMO-PARTNER-001', 'PT Nusantara Aviation', 'DEMO-ID-001',
     'demo.new.user', 'Demo New User', 'demo.new.user@nusantara.example',
     '+62-812-1000-1099', 'Technical Data Engineer',
     'New distribution user sample', '{"demo":true,"channel":"api"}'::jsonb,
     '1111111111111111111111111111111111111111111111111111111111111111',
     NULL, NULL, NULL, NULL, NULL, CURRENT_TIMESTAMP),
    ('37000000-0000-4000-8000-000000000002',
     'DEMO-ACCOUNT-UNLOCK-001', 'DEMO-DISTRIBUTION-CLIENT',
     'DISTRIBUTION-DEMO', 'UNLOCK_ACCOUNT',
     CURRENT_TIMESTAMP - INTERVAL '2 hours', CURRENT_TIMESTAMP - INTERVAL '2 hours',
     'PENDING', 'REP-DEMO-002', 'Dewi Lestari',
     'dewi.lestari@garuda-tech.example', '+62-812-2000-2001',
     'DEMO-PARTNER-002', 'PT Garuda Technical Services', 'DEMO-ID-002',
     'demo.locked.user', 'Demo Locked User',
     'demo.locked.user@garuda-tech.example', NULL, NULL,
     'Locked distribution user sample', '{"demo":true,"channel":"api"}'::jsonb,
     '2222222222222222222222222222222222222222222222222222222222222222',
     NULL, NULL, NULL, NULL, NULL, CURRENT_TIMESTAMP),
    ('37000000-0000-4000-8000-000000000003',
     'DEMO-ACCOUNT-RESET-001', 'DEMO-DISTRIBUTION-CLIENT',
     'DISTRIBUTION-DEMO', 'RESET_PASSWORD',
     CURRENT_TIMESTAMP - INTERVAL '1 hour', CURRENT_TIMESTAMP - INTERVAL '1 hour',
     'PENDING', 'REP-DEMO-003', 'Siti Rahma',
     'siti.rahma@nusantara.example', '+62-812-1000-1002',
     'DEMO-PARTNER-001', 'PT Nusantara Aviation', 'DEMO-ID-001',
     'demo.reset.user', 'Demo Reset User',
     'demo.reset.user@nusantara.example', NULL, NULL,
     'Password reset distribution user sample',
     '{"demo":true,"channel":"api"}'::jsonb,
     '3333333333333333333333333333333333333333333333333333333333333333',
     NULL, NULL, NULL, NULL, NULL, CURRENT_TIMESTAMP);

INSERT INTO docs_distribution_account_request_event (
    request_id, event_type, from_status, to_status,
    actor_type, actor_id, actor_name, comment, occurred_at
)
SELECT request_row.request_id, 'RECEIVED', NULL, 'PENDING',
       'EXTERNAL_SYSTEM', request_row.source_system_id,
       'Distribution demo connector', 'DEMO request received',
       request_row.received_at
  FROM docs_distribution_account_request request_row
 WHERE request_row.client_id = 'DEMO-DISTRIBUTION-CLIENT'
   AND request_row.correlation_id LIKE 'DEMO-ACCOUNT-%';

-- Strong inventory and safety assertions make partial sample loads fail.
DO $$
BEGIN
    IF (SELECT COUNT(*) FROM docs_partner_company
         WHERE company_code LIKE 'DEMO-PARTNER-%') <> 2 THEN
        RAISE EXCEPTION 'DEMO partner company count must be 2';
    END IF;

    IF (SELECT COUNT(*) FROM docs_partner_user
         WHERE partner_company_id IN (
             SELECT partner_company_id FROM docs_partner_company
              WHERE company_code LIKE 'DEMO-PARTNER-%'
         )) <> 5 THEN
        RAISE EXCEPTION 'DEMO partner user count must be 5';
    END IF;

    IF (SELECT COUNT(*) FROM docs_distribution_request
         WHERE request_no LIKE 'DREQ-DEMO-%') <> 4 THEN
        RAISE EXCEPTION 'DEMO distribution request count must be 4';
    END IF;

    IF (SELECT COUNT(*) FROM docs_distribution_request_item
         WHERE request_id IN (
             SELECT request_id FROM docs_distribution_request
              WHERE request_no LIKE 'DREQ-DEMO-%'
         )) <> 4 THEN
        RAISE EXCEPTION 'DEMO distribution item count must be 4';
    END IF;

    IF (SELECT COUNT(*) FROM docs_distribution_request_recipient
         WHERE request_id IN (
             SELECT request_id FROM docs_distribution_request
              WHERE request_no LIKE 'DREQ-DEMO-%'
         )) <> 4 THEN
        RAISE EXCEPTION 'DEMO distribution recipient count must be 4';
    END IF;

    IF (SELECT COUNT(*) FROM docs_distribution_outbox
         WHERE aggregate_id LIKE 'DREQ-DEMO-%'
           AND status = 'HOLD') <> 1 THEN
        RAISE EXCEPTION 'DEMO approved request must have one HOLD outbox row';
    END IF;

    IF (SELECT COUNT(*) FROM docs_distribution_account_request
         WHERE correlation_id LIKE 'DEMO-ACCOUNT-%') <> 3 THEN
        RAISE EXCEPTION 'DEMO account request count must be 3';
    END IF;

    IF EXISTS (
        SELECT 1 FROM docs_distribution_account_request
         WHERE correlation_id LIKE 'DEMO-ACCOUNT-%'
           AND (status <> 'PENDING'
             OR decision_comment IS NOT NULL
             OR decided_by_user_cd IS NOT NULL
             OR decided_at IS NOT NULL)
    ) THEN
        RAISE EXCEPTION 'DEMO account requests must remain undecided PENDING rows';
    END IF;

    IF (SELECT COUNT(*) FROM docs_distribution_account_request_event event_row
         JOIN docs_distribution_account_request request_row
           ON request_row.request_id = event_row.request_id
        WHERE request_row.correlation_id LIKE 'DEMO-ACCOUNT-%'
          AND event_row.event_type = 'RECEIVED') <> 3 THEN
        RAISE EXCEPTION 'DEMO account requests must have three RECEIVED events';
    END IF;

    IF (SELECT COUNT(*) FROM docs_distribution_account_request_nonce
         WHERE client_id = 'DEMO-DISTRIBUTION-CLIENT') <> 0 THEN
        RAISE EXCEPTION 'DEMO seed must not create replay nonces';
    END IF;
END
$$;

CREATE TEMP TABLE demo_technical_fingerprint_after (
    table_name  text PRIMARY KEY,
    row_count   bigint NOT NULL,
    fingerprint text NOT NULL
) ON COMMIT DROP;

INSERT INTO demo_technical_fingerprint_after (table_name, row_count, fingerprint)
SELECT 'docs_sw', COUNT(*),
       md5(COALESCE(string_agg(md5(to_jsonb(source_row)::text), ''
           ORDER BY md5(to_jsonb(source_row)::text)), ''))
  FROM docs_sw source_row
UNION ALL
SELECT 'docs_sw_file', COUNT(*),
       md5(COALESCE(string_agg(md5(to_jsonb(source_row)::text), ''
           ORDER BY md5(to_jsonb(source_row)::text)), ''))
  FROM docs_sw_file source_row
UNION ALL
SELECT 'docs_sw_sub_file', COUNT(*),
       md5(COALESCE(string_agg(md5(to_jsonb(source_row)::text), ''
           ORDER BY md5(to_jsonb(source_row)::text)), ''))
  FROM docs_sw_sub_file source_row;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
          FROM demo_technical_fingerprint before_state
          FULL JOIN demo_technical_fingerprint_after after_state
            USING (table_name)
         WHERE before_state.table_name IS NULL
            OR after_state.table_name IS NULL
            OR before_state.row_count <> after_state.row_count
            OR before_state.fingerprint <> after_state.fingerprint
    ) THEN
        RAISE EXCEPTION 'Protected technical-data fingerprint changed';
    END IF;
END
$$;

COMMIT;
