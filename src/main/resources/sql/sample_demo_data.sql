-- KT-1B TDMS local demonstration data reset
-- PostgreSQL 17
--
-- Preserved:
--   * admin account row, including the existing password
--   * menu, multilingual labels, common codes, roles, grids and system settings
--   * security-grade catalog and all classification trees
--
-- Replaced:
--   * business documents and files
--   * non-admin users, departments and user-derived ACL records
--   * access, view, print, login and legacy audit history
--   * requests, approvals, notices, mail and viewer annotations
--
-- Local demo users use the password supplied separately with the development
-- environment. Never run this script in production.
-- Run internal_only_cleanup_ddl.sql first; the Windows package builder does so
-- automatically before applying this reset.

\set ON_ERROR_STOP on

BEGIN;

LOCK TABLE docs_user IN SHARE ROW EXCLUSIVE MODE;

DO $$
BEGIN
    IF (
        SELECT COUNT(*)
          FROM docs_user
         WHERE user_cd = 'USER_0000000001'
           AND user_id = 'admin'
           AND user_pwd IS NOT NULL
    ) <> 1 THEN
        RAISE EXCEPTION 'The preserved admin account was not found.';
    END IF;
END
$$;

-- Explicit list only. Do not add CASCADE: several reference tables must remain.
TRUNCATE TABLE
    docs_print_job_item,
    docs_print_job,
    docs_download_runtime,
    docs_viewer_key,
    docs_viewer_log,
    docs_down_history,
    docs_document_act_log,
    docs_history,
    docs_login_history,
    docs_audit_log,
    docs_access_audit_log,
    docs_object_user_permission,
    docs_file_security_label,
    docs_sw_approval_comment,
    docs_sw_member,
    docs_sw_sub_file,
    docs_sw_file,
    docs_sw,
    docs_doc_approval_comment,
    docs_document_member,
    docs_document_sub_file,
    docs_document_file,
    docs_document,
    docs_drawing_approval_comment,
    docs_drawing_member,
    docs_drawing_sub_file,
    docs_drawing,
    docs_dxf_approval_comment,
    docs_dxf_sub_file,
    docs_dxf_document_file,
    docs_dxf_document,
    docs_pdf_document_file,
    docs_peerreview_approval_comment,
    docs_peerreview,
    docs_product_document_member,
    docs_product_document_file,
    docs_product_document,
    docs_product_sw_file,
    docs_product_sw,
    docs_product_status,
    docs_production_approval_comment,
    docs_production_sub_file,
    docs_eco_history,
    docs_file,
    docs_approval_file,
    docs_approval_line_detail,
    docs_approval_line,
    docs_distribution_approval_detail,
    docs_destroy_request_mapping,
    docs_destroy_request_detail,
    docs_destroy_request,
    docs_destroy_file,
    docs_destroy,
    docs_request_mapping,
    docs_request_file,
    docs_request_detail,
    docs_request_deploy,
    docs_request,
    docs_notice_file,
    docs_notice,
    docs_conf_notice_file,
    docs_conf_notice,
    docs_qna,
    docs_conf_qna_file,
    docs_conf_qna,
    docs_mail_revision,
    docs_mail,
    public."CV_VIEW_SELECTED",
    public."CV_VIEW_SPEECHBUBBLE_TEXT",
    public."CV_VIEW_SPEECHBUBBLE",
    public."CV_VIEW_STAMP",
    public."CV_VIEW_THREAD",
    public."CV_VIEW_MARKUP",
    public."CV_VIEW_STYLE",
    view_annotations_log,
    view_annotations
RESTART IDENTITY;

-- Remove every non-admin identity and all old test departments.
DELETE FROM docs_user_action_permission
 WHERE user_cd <> 'USER_0000000001';

DELETE FROM docs_user_security_clearance
 WHERE user_cd <> 'USER_0000000001';

DELETE FROM docs_role_group_member
 WHERE member_cd <> 'USER_0000000001';

DELETE FROM docs_user
 WHERE user_cd <> 'USER_0000000001';

-- The closed-network demo contains one neutral internal organization only.
DELETE FROM docs_company
 WHERE company_cd <> 'COMP_0000000999';

UPDATE docs_company
   SET company_nm = 'KT-1B',
       company_type = 'I',
       use_yn = 'Y',
       del_yn = 'N',
       update_user_cd = 'admin',
       update_dt = CURRENT_TIMESTAMP
 WHERE company_cd = 'COMP_0000000999';

TRUNCATE TABLE docs_dept;

INSERT INTO docs_dept (
    dept_cd,
    dept_cd_custom,
    parent_dept_cd,
    dept_nm,
    dept_eng_nm,
    company_cd,
    use_yn,
    del_yn,
    sort_seq,
    insert_uid,
    insert_dt,
    dept_short_path,
    dept_long_path,
    lvl,
    dept_type,
    start_date
) VALUES
    ('DMS000', 'KT1B', NULL, 'KT-1B 사업단', 'KT-1B Program Office',
     'COMP_0000000999', 'Y', 'N', '001', 'admin', CURRENT_TIMESTAMP,
     'KT-1B', 'KT-1B 사업단', 1, 'PROGRAM', CURRENT_TIMESTAMP),
    ('DMS100', 'PM', 'DMS000', '사업관리팀', 'Program Management',
     'COMP_0000000999', 'Y', 'N', '010', 'admin', CURRENT_TIMESTAMP,
     '사업관리', 'KT-1B 사업단 > 사업관리팀', 2, 'TEAM', CURRENT_TIMESTAMP),
    ('DMS200', 'SE', 'DMS000', '체계종합팀', 'System Engineering',
     'COMP_0000000999', 'Y', 'N', '020', 'admin', CURRENT_TIMESTAMP,
     '체계종합', 'KT-1B 사업단 > 체계종합팀', 2, 'TEAM', CURRENT_TIMESTAMP),
    ('DMS300', 'DESIGN', 'DMS000', '기체설계팀', 'Airframe Design',
     'COMP_0000000999', 'Y', 'N', '030', 'admin', CURRENT_TIMESTAMP,
     '기체설계', 'KT-1B 사업단 > 기체설계팀', 2, 'TEAM', CURRENT_TIMESTAMP),
    ('DMS400', 'QA', 'DMS000', '품질보증팀', 'Quality Assurance',
     'COMP_0000000999', 'Y', 'N', '040', 'admin', CURRENT_TIMESTAMP,
     '품질보증', 'KT-1B 사업단 > 품질보증팀', 2, 'TEAM', CURRENT_TIMESTAMP),
    ('DMS500', 'SECURITY', 'DMS000', '기술정보보안팀', 'Technical Data Security',
     'COMP_0000000999', 'Y', 'N', '050', 'admin', CURRENT_TIMESTAMP,
     '기술보안', 'KT-1B 사업단 > 기술정보보안팀', 2, 'TEAM', CURRENT_TIMESTAMP);

INSERT INTO docs_user (
    user_cd,
    user_id,
    user_nm,
    user_pwd,
    company_cd,
    dept_cd,
    position_cd,
    email,
    auth_level,
    auth_approval_yn,
    use_yn,
    del_yn,
    insert_uid,
    insert_dt,
    update_uid,
    update_dt,
    pwd_update_dt,
    login_count,
    role_group,
    business_area_cd,
    protect_yn,
    work_place_cd,
    lock_yn,
    distribution_person_yn
) VALUES
    (
        'USER_0000020001', 'general.han', '한지민',
        'pbkdf2-sha256$310000$fG44JoZOEoE4y58-ub2ZBA$WGJe4792UzdhLVGY6jw3AOYca41610dTG1hZ5IQ0Wqk',
        'COMP_0000000999', 'DMS100', 'POSI_0000000008', 'general.han@kt1b.local',
        4, 'N', 'Y', 'N', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP, 0, 'RG_011', '1210', 'N', 'L0329', 'N', 'N'
    ),
    (
        'USER_0000020002', 'design.park', '박서준',
        'pbkdf2-sha256$310000$T2Dyvwms60wFMtZbl-NJEw$skbWAZHsnabyJl6mu_DvHIOO8BhefoHWphgyS-OBlA0',
        'COMP_0000000999', 'DMS300', 'POSI_0000000004', 'design.park@kt1b.local',
        4, 'N', 'Y', 'N', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP, 0, 'RG_011', '1210', 'N', 'L0329', 'N', 'N'
    ),
    (
        'USER_0000020003', 'restricted.lee', '이수빈',
        'pbkdf2-sha256$310000$tnyQjC6sR-nOxusu88oJqg$3Q34S273EM10pXzIykT1uGJiDZqK555I8e-co-hySs4',
        'COMP_0000000999', 'DMS200', 'POSI_0000000002', 'restricted.lee@kt1b.local',
        4, 'N', 'Y', 'N', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP, 0, 'RG_011', '1210', 'N', 'L0329', 'N', 'N'
    ),
    (
        'USER_0000020004', 'confidential.kim', '김도현',
        'pbkdf2-sha256$310000$WCwwy53KLhCw6-Mj7r8Xtw$OXce3BOeWF_Qqgpoi-gos_9lQlA-RXO1FaSU_N3qHIs',
        'COMP_0000000999', 'DMS100', 'POSI_0000000001', 'confidential.kim@kt1b.local',
        4, 'N', 'Y', 'N', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP, 0, 'RG_011', '1210', 'N', 'L0329', 'N', 'N'
    ),
    (
        'USER_0000020005', 'security.yoon', '윤하늘',
        'pbkdf2-sha256$310000$Jif9LE_bol0fW-gL8mvt_Q$0WWFmD1G1cL8QP61LHMckQ21OTzdWj65GRK8zX5UUuU',
        'COMP_0000000999', 'DMS500', 'POSI_0000000002', 'security.yoon@kt1b.local',
        4, 'N', 'Y', 'N', 'admin', CURRENT_TIMESTAMP, 'admin', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP, 0, 'RG_011', '1210', 'N', 'L0329', 'N', 'N'
    );

INSERT INTO docs_role_group_member (group_code, member_cd, group_type)
SELECT 'RG_011', user_cd, 'USER'
  FROM docs_user
 WHERE user_cd LIKE 'USER_000002%';

-- The administrator keeps the original account/password but receives the
-- highest demo clearance so every sample grade can be managed.
INSERT INTO docs_user_security_clearance (
    user_cd,
    grade_cd,
    valid_from,
    valid_to,
    grant_reason,
    granted_by,
    granted_at,
    updated_by,
    updated_at
) VALUES
    ('USER_0000000001', 'CONFIDENTIAL', CURRENT_TIMESTAMP - INTERVAL '1 day', NULL,
     '샘플 데이터 전체 관리', 'USER_0000000001', CURRENT_TIMESTAMP,
     'USER_0000000001', CURRENT_TIMESTAMP)
ON CONFLICT (user_cd)
DO UPDATE SET
    grade_cd = EXCLUDED.grade_cd,
    valid_from = EXCLUDED.valid_from,
    valid_to = NULL,
    grant_reason = EXCLUDED.grant_reason,
    updated_by = EXCLUDED.updated_by,
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO docs_user_security_clearance (
    user_cd,
    grade_cd,
    valid_from,
    valid_to,
    grant_reason,
    granted_by,
    granted_at,
    updated_by,
    updated_at
) VALUES
    ('USER_0000020001', 'GENERAL', CURRENT_TIMESTAMP - INTERVAL '30 days', NULL,
     '일반 기술자료 열람', 'USER_0000000001', CURRENT_TIMESTAMP, 'USER_0000000001', CURRENT_TIMESTAMP),
    ('USER_0000020002', 'INTERNAL', CURRENT_TIMESTAMP - INTERVAL '30 days', NULL,
     '사내 기술자료 업무', 'USER_0000000001', CURRENT_TIMESTAMP, 'USER_0000000001', CURRENT_TIMESTAMP),
    ('USER_0000020003', 'RESTRICTED', CURRENT_TIMESTAMP - INTERVAL '30 days', NULL,
     '제한 기술자료 검토', 'USER_0000000001', CURRENT_TIMESTAMP, 'USER_0000000001', CURRENT_TIMESTAMP),
    ('USER_0000020004', 'CONFIDENTIAL', CURRENT_TIMESTAMP - INTERVAL '30 days', NULL,
     '사업책임자 전체 자료 검토', 'USER_0000000001', CURRENT_TIMESTAMP, 'USER_0000000001', CURRENT_TIMESTAMP),
    ('USER_0000020005', 'CONFIDENTIAL', CURRENT_TIMESTAMP - INTERVAL '30 days', NULL,
     '기술정보 보안관리', 'USER_0000000001', CURRENT_TIMESTAMP, 'USER_0000000001', CURRENT_TIMESTAMP);

INSERT INTO docs_user_action_permission (
    user_cd,
    action_cd,
    allow_yn,
    granted_by,
    granted_at,
    updated_by,
    updated_at
)
SELECT 'USER_0000000001',
       action_cd,
       'Y',
       'USER_0000000001',
       CURRENT_TIMESTAMP,
       'USER_0000000001',
       CURRENT_TIMESTAMP
  FROM (
      VALUES
          ('LIST'),
          ('DETAIL'),
          ('VIEW'),
          ('DOWNLOAD_ORIGINAL'),
          ('PRINT'),
          ('MANAGE_ACL')
  ) admin_action(action_cd)
ON CONFLICT (user_cd, action_cd)
DO UPDATE SET
    allow_yn = 'Y',
    updated_by = 'USER_0000000001',
    updated_at = CURRENT_TIMESTAMP;

WITH user_profile(user_cd, profile_level) AS (
    VALUES
        ('USER_0000020001', 1),
        ('USER_0000020002', 2),
        ('USER_0000020003', 3),
        ('USER_0000020004', 3),
        ('USER_0000020005', 3)
),
available_action(action_cd, required_level) AS (
    VALUES
        ('LIST', 1),
        ('DETAIL', 1),
        ('VIEW', 1),
        ('DOWNLOAD_ORIGINAL', 2),
        ('PRINT', 3)
)
INSERT INTO docs_user_action_permission (
    user_cd,
    action_cd,
    allow_yn,
    granted_by,
    granted_at,
    updated_by,
    updated_at
)
SELECT profile.user_cd,
       action.action_cd,
       'Y',
       'USER_0000000001',
       CURRENT_TIMESTAMP,
       'USER_0000000001',
       CURRENT_TIMESTAMP
  FROM user_profile profile
 CROSS JOIN available_action action
 WHERE profile.profile_level >= action.required_level;

CREATE TEMP TABLE sample_document (
    seq_no integer PRIMARY KEY,
    object_id varchar(32) NOT NULL,
    sub_object_id varchar(32) NOT NULL,
    transmittal_no varchar(50) NOT NULL,
    request_no varchar(20) NOT NULL,
    request_name varchar(255) NOT NULL,
    tree_cd varchar(100) NOT NULL,
    owner_user_id varchar(20) NOT NULL,
    owner_user_nm varchar(20) NOT NULL,
    owner_dept_nm varchar(100) NOT NULL,
    grade_cd varchar(30) NOT NULL,
    days_ago integer NOT NULL,
    main_file_nm varchar(255) NOT NULL,
    sub_file_nm varchar(255) NOT NULL
) ON COMMIT DROP;

INSERT INTO sample_document VALUES
    (1,  'KT1B-SAMPLE-DOC-001', 'KT1B-SAMPLE-ATT-001',
     'KT1B-TR-2026-001', 'REQ-2026-001', 'KT-1B 형상 배치도',
     'TRB000013', 'general.han', '한지민', '사업관리팀', 'GENERAL', 2,
     'KT1B_General_Arrangement_RevA.pdf', 'KT1B_General_Arrangement_Checklist.pdf'),
    (2,  'KT1B-SAMPLE-DOC-002', 'KT1B-SAMPLE-ATT-002',
     'KT1B-TR-2026-002', 'REQ-2026-002', '탑재체 인터페이스 제어도',
     'TRB000013', 'design.park', '박서준', '기체설계팀', 'INTERNAL', 4,
     'KT1B_Payload_Interface_Drawing_RevA.pdf', 'KT1B_Payload_Interface_Notes.pdf'),
    (3,  'KT1B-SAMPLE-DOC-003', 'KT1B-SAMPLE-ATT-003',
     'KT1B-TR-2026-003', 'REQ-2026-003', '체계 요구조건 규격서',
     'TRB000015', 'general.han', '한지민', '사업관리팀', 'GENERAL', 6,
     'KT1B_System_Requirement_Specification.pdf', 'KT1B_Requirement_Traceability_Matrix.pdf'),
    (4,  'KT1B-SAMPLE-DOC-004', 'KT1B-SAMPLE-ATT-004',
     'KT1B-TR-2026-004', 'REQ-2026-004', 'SAR 탑재체 인터페이스 규격서',
     'TRB000016', 'restricted.lee', '이수빈', '체계종합팀', 'RESTRICTED', 8,
     'KT1B_SAR_Payload_Interface_Specification.pdf', 'KT1B_SAR_Interface_Verification.pdf'),
    (5,  'KT1B-SAMPLE-DOC-005', 'KT1B-SAMPLE-ATT-005',
     'KT1B-TR-2026-005', 'REQ-2026-005', '지상체 통합 업무기술서',
     'TRB000017', 'design.park', '박서준', '기체설계팀', 'INTERNAL', 10,
     'KT1B_Ground_Segment_Integration_SOW.pdf', 'KT1B_Integration_Milestone_Plan.pdf'),
    (6,  'KT1B-SAMPLE-DOC-006', 'KT1B-SAMPLE-ATT-006',
     'KT1B-TR-2026-006', 'REQ-2026-006', '상세설계검토 납품자료 패키지',
     'TRB000019', 'restricted.lee', '이수빈', '체계종합팀', 'RESTRICTED', 12,
     'KT1B_CDR_Data_Package.pdf', 'KT1B_CDR_Action_Item_List.pdf'),
    (7,  'KT1B-SAMPLE-DOC-007', 'KT1B-SAMPLE-ATT-007',
     'KT1B-TR-2026-007', 'REQ-2026-007', '비행 SW 검증결과 납품자료',
     'TRB000020', 'confidential.kim', '김도현', '사업관리팀', 'CONFIDENTIAL', 14,
     'KT1B_Flight_SW_Verification_Report.pdf', 'KT1B_Flight_SW_Test_Evidence.pdf'),
    (8,  'KT1B-SAMPLE-DOC-008', 'KT1B-SAMPLE-ATT-008',
     'KT1B-TR-2026-008', 'REQ-2026-008', '월간 사업관리 보고서',
     'TRB000021', 'general.han', '한지민', '사업관리팀', 'GENERAL', 16,
     'KT1B_Monthly_Program_Report.pdf', 'KT1B_Program_Schedule_Snapshot.pdf'),
    (9,  'KT1B-SAMPLE-DOC-009', 'KT1B-SAMPLE-ATT-009',
     'KT1B-TR-2026-009', 'REQ-2026-009', '위험관리 및 대응계획',
     'TRB000022', 'design.park', '박서준', '기체설계팀', 'INTERNAL', 18,
     'KT1B_Risk_Management_Plan.pdf', 'KT1B_Risk_Register.pdf'),
    (10, 'KT1B-SAMPLE-DOC-010', 'KT1B-SAMPLE-ATT-010',
     'KT1B-TR-2026-010', 'REQ-2026-010', '열진공시험 특별작업지시서',
     'TRB000023', 'restricted.lee', '이수빈', '체계종합팀', 'RESTRICTED', 20,
     'KT1B_Thermal_Vacuum_Special_Order.pdf', 'KT1B_TVAC_Safety_Checklist.pdf'),
    (11, 'KT1B-SAMPLE-DOC-011', 'KT1B-SAMPLE-ATT-011',
     'KT1B-TR-2026-011', 'REQ-2026-011', '위성 기능시험 절차서',
     'TRB000025', 'design.park', '박서준', '기체설계팀', 'INTERNAL', 22,
     'KT1B_Satellite_Functional_Test_Procedure.pdf', 'KT1B_Functional_Test_Data_Sheet.pdf'),
    (12, 'KT1B-SAMPLE-DOC-012', 'KT1B-SAMPLE-ATT-012',
     'KT1B-TR-2026-012', 'REQ-2026-012', 'EMC 적합성 시험 절차서',
     'TRB000026', 'confidential.kim', '김도현', '사업관리팀', 'CONFIDENTIAL', 24,
     'KT1B_EMC_Qualification_Test_Procedure.pdf', 'KT1B_EMC_Limit_Table.pdf'),
    (13, 'KT1B-SAMPLE-DOC-013', 'KT1B-SAMPLE-ATT-013',
     'KT1B-TR-2026-013', 'REQ-2026-013', '안테나 전개 설계검토 메모',
     'TRB000027', 'confidential.kim', '김도현', '사업관리팀', 'CONFIDENTIAL', 35,
     'KT1B_Antenna_Deployment_Engineering_Memo.pdf', 'KT1B_Antenna_Deployment_Analysis.pdf'),
    (14, 'KT1B-SAMPLE-DOC-014', 'KT1B-SAMPLE-ATT-014',
     'KT1B-TR-2026-014', 'REQ-2026-014', '비행 SW 소스 기준선 명세',
     'TRB000029', 'security.yoon', '윤하늘', '기술정보보안팀', 'CONFIDENTIAL', 40,
     'KT1B_Flight_SW_Source_Baseline.pdf', 'KT1B_SW_Baseline_Hash_List.pdf'),
    (15, 'KT1B-SAMPLE-DOC-015', 'KT1B-SAMPLE-ATT-015',
     'KT1B-TR-2026-015', 'REQ-2026-015', '기술조정회의 결과서',
     'TRB000031', 'general.han', '한지민', '사업관리팀', 'GENERAL', 45,
     'KT1B_Technical_Coordination_Minutes.pdf', 'KT1B_Technical_Action_Items.pdf'),
    (16, 'KT1B-SAMPLE-DOC-016', 'KT1B-SAMPLE-ATT-016',
     'KT1B-TR-2026-016', 'REQ-2026-016', '탑재체 정렬 검사 기준서',
     'TRB000034', 'restricted.lee', '이수빈', '체계종합팀', 'RESTRICTED', 50,
     'KT1B_Payload_Alignment_Inspection.pdf', 'KT1B_Alignment_Inspection_Record.pdf');

INSERT INTO docs_sw (
    cn_serial,
    object_id,
    business_area_cd,
    business_type_cd,
    sw_no,
    rev_no,
    sw_nm,
    status_cd,
    sw_version_no,
    doc_version_no,
    distribute_type_cd,
    description,
    protect_yn,
    insert_dept_nm,
    insert_uid,
    insert_dt,
    update_dept_nm,
    update_uid,
    update_dt,
    interface_dt,
    register_user,
    create_dt,
    input_date,
    attachment_count,
    requested_no,
    requested_date,
    deleted_yn,
    tree_cd,
    ccbdate,
    approver,
    status,
    approved_users,
    revieweruser
)
SELECT document.seq_no,
       document.object_id,
       '1210',
       document.tree_cd,
       document.transmittal_no,
       'A',
       document.request_name,
       'COMPLETE',
       '1.0',
       '1.0',
       'hdCADRegisteredDrawing',
       'KT-1B 기술자료관리시스템 샘플 자료',
       CASE WHEN document.grade_cd = 'GENERAL' THEN 'N' ELSE 'Y' END,
       document.owner_dept_nm,
       document.owner_user_id,
       CURRENT_TIMESTAMP - make_interval(days => document.days_ago),
       document.owner_dept_nm,
       document.owner_user_id,
       CURRENT_TIMESTAMP - make_interval(days => GREATEST(document.days_ago - 1, 0)),
       CURRENT_TIMESTAMP - make_interval(days => document.days_ago),
       document.owner_user_nm,
       CURRENT_TIMESTAMP - make_interval(days => document.days_ago),
       CURRENT_TIMESTAMP - make_interval(days => document.days_ago),
       1,
       document.request_no,
       CURRENT_TIMESTAMP - make_interval(days => document.days_ago),
       'N',
       document.tree_cd,
       NULL,
       NULL,
       NULL,
       NULL,
       NULL
  FROM sample_document document;

INSERT INTO docs_sw_file (
    cn_serial,
    object_id,
    file_no,
    file_path_nm,
    org_file_nm,
    file_nm,
    file_size,
    distribute_type_cd,
    interface_dt,
    register_user,
    create_dt,
    processing_status,
    processing_error,
    processed_at
)
SELECT document.seq_no,
       document.object_id,
       '1',
       'deployment/windows-demo/assets/demo-document.pdf',
       document.main_file_nm,
       lower(replace(document.object_id, '-', '_')) || '.pdf',
       '47093',
       'hdCADRegisteredDrawing',
       CURRENT_TIMESTAMP - make_interval(days => document.days_ago),
       document.owner_user_nm,
       CURRENT_TIMESTAMP - make_interval(days => document.days_ago),
       'DONE',
       NULL,
       CURRENT_TIMESTAMP - make_interval(days => document.days_ago)
  FROM sample_document document;

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
SELECT document.sub_object_id,
       document.object_id,
       1,
       document.sub_file_nm,
       'deployment/windows-demo/assets/demo-document.pdf',
       47093,
       'Y',
       document.owner_user_id,
       CURRENT_TIMESTAMP - make_interval(days => document.days_ago),
       NULL,
       NULL,
       'DONE',
       NULL,
       CURRENT_TIMESTAMP - make_interval(days => document.days_ago)
  FROM sample_document document;

INSERT INTO docs_file_security_label (
    object_type,
    object_id,
    file_no,
    grade_cd,
    label_reason,
    assigned_by,
    assigned_at,
    updated_by,
    updated_at
)
SELECT 'SW',
       document.object_id,
       '*',
       document.grade_cd,
       '샘플 문서 보안등급',
       'USER_0000000001',
       CURRENT_TIMESTAMP,
       'USER_0000000001',
       CURRENT_TIMESTAMP
  FROM sample_document document;

CREATE TEMP TABLE sample_document_access (
    user_cd varchar(20) NOT NULL,
    object_id varchar(32) NOT NULL,
    PRIMARY KEY (user_cd, object_id)
) ON COMMIT DROP;

INSERT INTO sample_document_access
SELECT 'USER_0000000001', object_id
  FROM sample_document
UNION ALL
SELECT 'USER_0000020001', object_id
  FROM sample_document
 WHERE grade_cd = 'GENERAL'
UNION ALL
SELECT 'USER_0000020002', object_id
  FROM sample_document
 WHERE grade_cd IN ('GENERAL', 'INTERNAL')
UNION ALL
SELECT 'USER_0000020003', object_id
  FROM sample_document
 WHERE grade_cd IN ('GENERAL', 'INTERNAL', 'RESTRICTED')
UNION ALL
SELECT 'USER_0000020004', object_id
  FROM sample_document
UNION ALL
SELECT 'USER_0000020005', object_id
  FROM sample_document
 WHERE grade_cd IN ('RESTRICTED', 'CONFIDENTIAL');

INSERT INTO docs_object_user_permission (
    object_type,
    object_id,
    user_cd,
    action_cd,
    allow_yn,
    grant_reason,
    granted_by,
    granted_at,
    updated_by,
    updated_at
)
SELECT 'SW',
       access.object_id,
       access.user_cd,
       action.action_cd,
       'Y',
       '샘플 데이터 사용자별 문서 권한',
       'USER_0000000001',
       CURRENT_TIMESTAMP,
       'USER_0000000001',
       CURRENT_TIMESTAMP
  FROM sample_document_access access
  JOIN docs_user_action_permission action
    ON action.user_cd = access.user_cd
   AND action.allow_yn = 'Y'
   AND action.action_cd IN ('LIST', 'DETAIL', 'VIEW', 'DOWNLOAD_ORIGINAL', 'PRINT');

-- Twenty-four realistic view events, distributed across the sample users.
WITH ranked_access AS (
    SELECT access.user_cd,
           access.object_id,
           ROW_NUMBER() OVER (
               PARTITION BY access.user_cd
               ORDER BY document.seq_no
           ) AS user_row,
           CASE access.user_cd
               WHEN 'USER_0000000001' THEN 0
               WHEN 'USER_0000020001' THEN 1
               WHEN 'USER_0000020002' THEN 2
               WHEN 'USER_0000020003' THEN 3
               WHEN 'USER_0000020004' THEN 4
               ELSE 5
           END AS user_order
      FROM sample_document_access access
      JOIN sample_document document
        ON document.object_id = access.object_id
),
selected_access AS (
    SELECT *
      FROM ranked_access
     WHERE user_row <= 4
)
INSERT INTO docs_history (
    distribution_type,
    drawing_no,
    object_id,
    org_file_nm,
    revision,
    user_id,
    insert_date,
    user_nm,
    distribution_user,
    approval_user,
    log_type,
    count
)
SELECT 'CCB',
       document.transmittal_no,
       document.object_id,
       document.main_file_nm,
       'A',
       actor.user_id,
       CURRENT_TIMESTAMP
           - make_interval(days => (selected.user_order + selected.user_row)::integer)
           - make_interval(hours => selected.user_row::integer),
       actor.user_nm,
       NULL,
       NULL,
       'VIEWING',
       1
  FROM selected_access selected
  JOIN sample_document document
    ON document.object_id = selected.object_id
  JOIN docs_user actor
    ON actor.user_cd = selected.user_cd;

CREATE TEMP TABLE sample_print_event (
    print_no integer PRIMARY KEY,
    print_job_id varchar(50) NOT NULL,
    user_cd varchar(20) NOT NULL,
    object_id varchar(32) NOT NULL,
    request_no varchar(20) NOT NULL
) ON COMMIT DROP;

INSERT INTO sample_print_event
SELECT ROW_NUMBER() OVER (ORDER BY access.user_cd, document.seq_no)::integer,
       'SAMPLE-PRINT-' || LPAD(
           ROW_NUMBER() OVER (ORDER BY access.user_cd, document.seq_no)::text,
           3,
           '0'
       ),
       access.user_cd,
       access.object_id,
       document.request_no
  FROM sample_document_access access
  JOIN docs_user_action_permission action
    ON action.user_cd = access.user_cd
   AND action.action_cd = 'PRINT'
   AND action.allow_yn = 'Y'
  JOIN sample_document document
    ON document.object_id = access.object_id
 WHERE access.user_cd <> 'USER_0000000001'
 ORDER BY access.user_cd, document.seq_no
 LIMIT 8;

INSERT INTO docs_print_job (
    print_job_id,
    status_cd,
    actor_user_cd,
    actor_user_id,
    actor_user_nm,
    object_type,
    object_id,
    file_no,
    request_no,
    page_count,
    copy_count,
    printer_nm,
    device_id,
    client_ip,
    requested_at,
    completed_at,
    count_applied_yn,
    error_message
)
SELECT event.print_job_id,
       'SUCCESS',
       actor.user_cd,
       actor.user_id,
       actor.user_nm,
       'SW',
       event.object_id,
       '1',
       event.request_no,
       8 + event.print_no * 3,
       1,
       'KT1B 보안프린터 ' || CASE WHEN event.print_no % 2 = 0 THEN '02' ELSE '01' END,
       'SECURE-PRINT-' || CASE WHEN event.print_no % 2 = 0 THEN '02' ELSE '01' END,
       '10.20.30.' || (20 + event.print_no),
       CURRENT_TIMESTAMP - make_interval(days => event.print_no + 1),
       CURRENT_TIMESTAMP - make_interval(days => event.print_no + 1) + INTERVAL '2 minutes',
       'Y',
       NULL
  FROM sample_print_event event
  JOIN docs_user actor
    ON actor.user_cd = event.user_cd;

INSERT INTO docs_print_job_item (
    print_job_id,
    item_seq,
    object_type,
    object_id,
    file_no,
    request_no,
    request_type,
    count_required_yn
)
SELECT event.print_job_id,
       1,
       'SW',
       event.object_id,
       '1',
       event.request_no,
       'TECHNICAL_DATA',
       'N'
  FROM sample_print_event event;

-- Canonical audit rows. Result codes remain as internal mandatory values, while
-- the user-facing history grids intentionally omit result/result-message fields.
INSERT INTO docs_access_audit_log (
    occurred_at,
    event_type,
    action_type,
    result_cd,
    reason_cd,
    result_message,
    actor_user_cd,
    actor_user_id,
    actor_user_nm,
    object_type,
    object_id,
    file_no,
    request_no,
    grade_cd,
    client_ip,
    session_id,
    correlation_id,
    detail_json,
    menu_cd,
    menu_nm,
    menu_url,
    action_nm,
    request_uri,
    http_method,
    http_status,
    duration_ms
)
SELECT CURRENT_TIMESTAMP - make_interval(hours => actor_order * 2),
       'MENU_ACTION',
       'SEARCH',
       'SUCCESS',
       'USER_REQUEST',
       NULL,
       actor.user_cd,
       actor.user_id,
       actor.user_nm,
       NULL,
       NULL,
       NULL,
       NULL,
       NULL,
       '10.20.10.' || (10 + actor_order),
       'SAMPLE-SESSION-' || actor_order,
       'SAMPLE-MENU-SEARCH-' || actor_order,
       jsonb_build_object('sample', true, 'screen', 'technical-data-search'),
       'MENU_220',
       '조회',
       '/inside/distribution/swRequest/**',
       '기술자료 조회',
       '/inside/distribution/swRequest/selectList',
       'POST',
       200,
       80 + actor_order * 7
  FROM (
      SELECT userInfo.*,
             ROW_NUMBER() OVER (ORDER BY userInfo.user_cd)::integer AS actor_order
        FROM docs_user userInfo
  ) actor;

WITH first_document AS (
    SELECT access.user_cd,
           document.*,
           ROW_NUMBER() OVER (
               PARTITION BY access.user_cd
               ORDER BY document.seq_no
           ) AS user_row
      FROM sample_document_access access
      JOIN sample_document document
        ON document.object_id = access.object_id
     WHERE access.user_cd <> 'USER_0000000001'
)
INSERT INTO docs_access_audit_log (
    occurred_at,
    event_type,
    action_type,
    result_cd,
    reason_cd,
    actor_user_cd,
    actor_user_id,
    actor_user_nm,
    object_type,
    object_id,
    file_no,
    request_no,
    grade_cd,
    client_ip,
    session_id,
    correlation_id,
    detail_json,
    menu_cd,
    menu_nm,
    menu_url,
    action_nm,
    request_uri,
    http_method,
    http_status,
    duration_ms
)
SELECT CURRENT_TIMESTAMP - make_interval(hours => document.seq_no + 2),
       'MENU_ACTION',
       'OPEN_DETAIL',
       'SUCCESS',
       'USER_REQUEST',
       actor.user_cd,
       actor.user_id,
       actor.user_nm,
       'SW',
       document.object_id,
       '1',
       document.request_no,
       document.grade_cd,
       '10.20.10.' || (30 + document.seq_no),
       'SAMPLE-DETAIL-' || actor.user_cd,
       'SAMPLE-OPEN-DETAIL-' || actor.user_cd,
       jsonb_build_object('sample', true, 'transmittalNo', document.transmittal_no),
       'MENU_220',
       '조회',
       '/inside/distribution/swRequest/**',
       '상세조회',
       '/inside/distribution/swRequest/detail',
       'GET',
       200,
       110 + document.seq_no * 5
  FROM first_document document
  JOIN docs_user actor
    ON actor.user_cd = document.user_cd
 WHERE document.user_row = 1;

WITH ranked_access AS (
    SELECT access.user_cd,
           document.*,
           ROW_NUMBER() OVER (ORDER BY access.user_cd, document.seq_no) AS audit_row
      FROM sample_document_access access
      JOIN sample_document document
        ON document.object_id = access.object_id
     WHERE access.user_cd <> 'USER_0000000001'
)
INSERT INTO docs_access_audit_log (
    occurred_at,
    event_type,
    action_type,
    result_cd,
    reason_cd,
    actor_user_cd,
    actor_user_id,
    actor_user_nm,
    object_type,
    object_id,
    file_no,
    request_no,
    grade_cd,
    client_ip,
    session_id,
    correlation_id,
    detail_json,
    menu_cd,
    menu_nm,
    menu_url,
    action_nm,
    request_uri,
    http_method,
    http_status,
    duration_ms
)
SELECT CURRENT_TIMESTAMP - make_interval(hours => (audit_row + 8)::integer),
       'FILE_ACCESS',
       'DETAIL',
       'ALLOW',
       'ACCESS_ALLOWED',
       actor.user_cd,
       actor.user_id,
       actor.user_nm,
       'SW',
       access.object_id,
       '1',
       access.request_no,
       access.grade_cd,
       '10.20.20.' || (10 + audit_row),
       'SAMPLE-ACCESS-' || audit_row,
       'SAMPLE-FILE-DETAIL-' || audit_row,
       jsonb_build_object('sample', true, 'policy', 'grade-and-named-acl'),
       'MENU_220',
       '조회',
       '/inside/distribution/swRequest/**',
       '상세정보 접근',
       '/inside/distribution/swRequest/detail',
       'GET',
       200,
       60 + audit_row * 4
  FROM ranked_access access
  JOIN docs_user actor
    ON actor.user_cd = access.user_cd
 WHERE audit_row <= 8;

WITH downloadable_access AS (
    SELECT access.user_cd,
           document.*,
           ROW_NUMBER() OVER (ORDER BY access.user_cd, document.seq_no) AS audit_row
      FROM sample_document_access access
      JOIN docs_user_action_permission action
        ON action.user_cd = access.user_cd
       AND action.action_cd = 'DOWNLOAD_ORIGINAL'
       AND action.allow_yn = 'Y'
      JOIN sample_document document
        ON document.object_id = access.object_id
     WHERE access.user_cd <> 'USER_0000000001'
)
INSERT INTO docs_access_audit_log (
    occurred_at,
    event_type,
    action_type,
    result_cd,
    reason_cd,
    actor_user_cd,
    actor_user_id,
    actor_user_nm,
    object_type,
    object_id,
    file_no,
    request_no,
    grade_cd,
    client_ip,
    session_id,
    correlation_id,
    detail_json,
    menu_cd,
    menu_nm,
    menu_url,
    action_nm,
    request_uri,
    http_method,
    http_status,
    duration_ms
)
SELECT CURRENT_TIMESTAMP - make_interval(days => 1) - make_interval(hours => audit_row::integer),
       'DOWNLOAD_RESULT',
       'DOWNLOAD_ORIGINAL',
       'SUCCESS',
       'DOWNLOAD_COMPLETED',
       actor.user_cd,
       actor.user_id,
       actor.user_nm,
       'SW',
       access.object_id,
       '1',
       access.request_no,
       access.grade_cd,
       '10.20.30.' || (40 + audit_row),
       'SAMPLE-DOWNLOAD-' || audit_row,
       'SAMPLE-DOWNLOAD-RESULT-' || audit_row,
       jsonb_build_object('sample', true, 'fileType', 'main'),
       'MENU_220',
       '조회',
       '/inside/distribution/swRequest/**',
       '원문 다운로드',
       '/inside/distribution/swRequest/file',
       'GET',
       200,
       140 + audit_row * 9
  FROM downloadable_access access
  JOIN docs_user actor
    ON actor.user_cd = access.user_cd
 WHERE audit_row <= 6;

INSERT INTO docs_access_audit_log (
    occurred_at,
    event_type,
    action_type,
    result_cd,
    reason_cd,
    actor_user_cd,
    actor_user_id,
    actor_user_nm,
    object_type,
    object_id,
    file_no,
    request_no,
    grade_cd,
    client_ip,
    session_id,
    correlation_id,
    detail_json,
    menu_cd,
    menu_nm,
    menu_url,
    action_nm,
    request_uri,
    http_method,
    http_status,
    duration_ms
)
SELECT CURRENT_TIMESTAMP - make_interval(days => 2) - make_interval(hours => document.seq_no),
       'ACL_CHANGE',
       'MANAGE_DOCUMENT_PERMISSION',
       'SUCCESS',
       'ADMIN_GRANT',
       admin_user.user_cd,
       admin_user.user_id,
       admin_user.user_nm,
       'SW',
       document.object_id,
       '*',
       document.request_no,
       document.grade_cd,
       '127.0.0.1',
       'SAMPLE-ACL-' || document.seq_no,
       'SAMPLE-ACL-CHANGE-' || document.seq_no,
       jsonb_build_object('sample', true, 'change', 'grant-document-permission'),
       'MENU_222',
       '보안등급/인가 관리',
       '/inside/system/securityaccess/',
       '문서권한 변경',
       '/inside/system/securityaccess/document-permissions',
       'POST',
       200,
       95 + document.seq_no * 6
  FROM sample_document document
 CROSS JOIN (
      SELECT user_cd, user_id, user_nm
        FROM docs_user
       WHERE user_cd = 'USER_0000000001'
  ) admin_user
 WHERE document.seq_no <= 6;

UPDATE docs_seq_table
   SET seq = 0
 WHERE table_nm = 'DOCS_LOGIN_HISTORY';

SELECT setval('docs_user_cd_sequence', 20005, true);
SELECT setval('cn_serial_seq', 16, true);

DO $$
DECLARE
    invalid_file_count integer;
BEGIN
    IF (SELECT COUNT(*) FROM docs_user) <> 6 THEN
        RAISE EXCEPTION 'Expected admin plus five sample users.';
    END IF;

    IF (
        SELECT COUNT(*)
          FROM docs_user
         WHERE user_cd = 'USER_0000000001'
           AND user_id = 'admin'
           AND user_pwd IS NOT NULL
    ) <> 1 THEN
        RAISE EXCEPTION 'The admin account was not preserved.';
    END IF;

    IF (
        SELECT COUNT(*)
          FROM docs_role_group_member
         WHERE member_cd = 'USER_0000000001'
           AND group_code = 'RG_001'
    ) <> 1 THEN
        RAISE EXCEPTION 'The admin role membership was not preserved.';
    END IF;

    IF (
        SELECT COUNT(*)
          FROM docs_company
         WHERE company_cd = 'COMP_0000000999'
           AND company_nm = 'KT-1B'
           AND company_type = 'I'
           AND use_yn = 'Y'
           AND del_yn = 'N'
    ) <> 1 OR (SELECT COUNT(*) FROM docs_company) <> 1 THEN
        RAISE EXCEPTION 'The demo must contain only the neutral KT-1B company.';
    END IF;

    IF EXISTS (
        SELECT 1
          FROM docs_menu
         WHERE parent_menu_cd IN ('I', 'B', 'E')
            OR COALESCE(menu_url, '') ~* '(^|/)outside/'
            OR COALESCE(menu_url, '') ~*
               '^/inside/(unregisted|outregisted)(/|$)'
            OR COALESCE(menu_url, '') ~*
               '^/inside/organizationmanage/(outsideuser|approval)(/|$)'
    ) THEN
        RAISE EXCEPTION 'The demo contains a retired external menu.';
    END IF;

    IF (
        SELECT ARRAY_AGG(menu_cd ORDER BY menu_cd)
          FROM docs_menu
         WHERE tree_type = 'root'
           AND menu_type IN ('T', 'M', 'P')
           AND use_yn = 'Y'
           AND del_yn = 'N'
           AND parent_menu_cd = 'ROOT'
    ) IS DISTINCT FROM ARRAY[
        'MENU_013', 'MENU_071', 'MENU_214', 'MENU_223'
    ]::varchar[] THEN
        RAISE EXCEPTION 'The demo menu roots do not match current navigation.';
    END IF;

    IF EXISTS (
        SELECT 1
          FROM information_schema.columns
         WHERE table_schema = 'public'
           AND table_name IN ('docs_menu', 'docs_user')
           AND column_name = 'auth_site'
    ) THEN
        RAISE EXCEPTION 'The demo schema contains a retired portal selector.';
    END IF;

    IF to_regclass('public.docs_user_request') IS NOT NULL
       OR to_regclass('public.docs_user_request_number') IS NOT NULL
       OR to_regclass('public.docs_external_user_id_sequence') IS NOT NULL THEN
        RAISE EXCEPTION 'External-user request database objects remain.';
    END IF;

    IF (
        SELECT COUNT(*)
          FROM docs_user_action_permission
         WHERE user_cd = 'USER_0000000001'
           AND allow_yn = 'Y'
    ) <> 6 THEN
        RAISE EXCEPTION 'The admin must have six global action permissions.';
    END IF;

    IF (
        SELECT grade_cd
          FROM docs_user_security_clearance
         WHERE user_cd = 'USER_0000000001'
    ) <> 'CONFIDENTIAL' THEN
        RAISE EXCEPTION 'The admin demo clearance must be CONFIDENTIAL.';
    END IF;

    IF (SELECT COUNT(*) FROM docs_sw) <> 16 THEN
        RAISE EXCEPTION 'Expected sixteen sample technical documents.';
    END IF;

    SELECT COUNT(*)
      INTO invalid_file_count
      FROM docs_sw document
     WHERE (SELECT COUNT(*) FROM docs_sw_file main_file
             WHERE main_file.object_id = document.object_id) <> 1
        OR (SELECT COUNT(*) FROM docs_sw_sub_file sub_file
             WHERE sub_file.parent_object_id = document.object_id
               AND sub_file.use_yn = 'Y') <> 1;

    IF invalid_file_count <> 0 THEN
        RAISE EXCEPTION 'Every sample document must have one main and one supplementary file.';
    END IF;

    IF (
        SELECT COUNT(*)
          FROM docs_file_security_label
         WHERE object_type = 'SW'
           AND file_no = '*'
    ) <> 16 THEN
        RAISE EXCEPTION 'Every sample document must have one security label.';
    END IF;

    IF EXISTS (
        SELECT 1
          FROM docs_sw
         WHERE requested_no IS NULL
            OR requested_date IS NULL
            OR status IS NOT NULL
            OR approver IS NOT NULL
            OR revieweruser IS NOT NULL
    ) THEN
        RAISE EXCEPTION 'Sample requests need request dates and no approval workflow fields.';
    END IF;

    IF EXISTS (
        SELECT grade_cd
          FROM docs_file_security_label
         WHERE object_type = 'SW'
           AND file_no = '*'
         GROUP BY grade_cd
        HAVING COUNT(*) <> 4
    ) OR (
        SELECT COUNT(DISTINCT grade_cd)
          FROM docs_file_security_label
         WHERE object_type = 'SW'
           AND file_no = '*'
    ) <> 4 THEN
        RAISE EXCEPTION 'Expected four documents in each of four security grades.';
    END IF;

    IF (
        SELECT COUNT(*)
          FROM docs_object_user_permission
         WHERE user_cd = 'USER_0000000001'
           AND allow_yn = 'Y'
    ) <> 80 THEN
        RAISE EXCEPTION 'The admin must have five permissions on all sixteen documents.';
    END IF;

    IF EXISTS (
        SELECT 1
          FROM docs_object_user_permission permission
          LEFT JOIN docs_user user_info
            ON user_info.user_cd = permission.user_cd
         WHERE user_info.user_cd IS NULL
    ) THEN
        RAISE EXCEPTION 'Orphan document-user permission found.';
    END IF;
END
$$;

COMMIT;

-- Reader-facing verification summary.
SELECT 'users' AS metric, COUNT(*)::text AS value FROM docs_user
UNION ALL
SELECT 'departments', COUNT(*)::text FROM docs_dept
UNION ALL
SELECT 'technical_documents', COUNT(*)::text FROM docs_sw
UNION ALL
SELECT 'main_files', COUNT(*)::text FROM docs_sw_file
UNION ALL
SELECT 'supplementary_files', COUNT(*)::text FROM docs_sw_sub_file
UNION ALL
SELECT 'security_labels', COUNT(*)::text FROM docs_file_security_label
UNION ALL
SELECT 'document_permissions', COUNT(*)::text FROM docs_object_user_permission
UNION ALL
SELECT 'view_history', COUNT(*)::text FROM docs_history
UNION ALL
SELECT 'print_history', COUNT(*)::text FROM docs_print_job
UNION ALL
SELECT 'canonical_audit', COUNT(*)::text FROM docs_access_audit_log
ORDER BY metric;
