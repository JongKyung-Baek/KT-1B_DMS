-- KT-1B TDMS partner directory and navigation
-- PostgreSQL 17+
--
-- Partner users are distribution contacts, not TDMS login accounts. Keeping
-- them in a separate table prevents accidental authentication/ACL coupling.

\set ON_ERROR_STOP on

BEGIN;

CREATE SEQUENCE IF NOT EXISTS docs_partner_company_id_seq;
CREATE SEQUENCE IF NOT EXISTS docs_partner_user_id_seq;

CREATE TABLE IF NOT EXISTS docs_partner_company (
    partner_company_id bigint PRIMARY KEY,
    company_code       varchar(40) NOT NULL,
    company_name       varchar(200) NOT NULL,
    business_no        varchar(30),
    contact_email      varchar(254),
    contact_phone      varchar(40),
    address            varchar(500),
    use_yn             char(1) NOT NULL DEFAULT 'Y',
    del_yn             char(1) NOT NULL DEFAULT 'N',
    created_by         varchar(64) NOT NULL,
    created_at         timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by         varchar(64) NOT NULL,
    updated_at         timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_docs_partner_company_code UNIQUE (company_code),
    CONSTRAINT ck_docs_partner_company_use CHECK (use_yn IN ('Y', 'N')),
    CONSTRAINT ck_docs_partner_company_del CHECK (del_yn IN ('Y', 'N'))
);

ALTER SEQUENCE docs_partner_company_id_seq
    OWNED BY docs_partner_company.partner_company_id;

CREATE TABLE IF NOT EXISTS docs_partner_user (
    partner_user_id    bigint PRIMARY KEY,
    partner_company_id bigint NOT NULL,
    user_name          varchar(100) NOT NULL,
    email              varchar(254) NOT NULL,
    phone              varchar(40),
    position_name      varchar(100),
    representative_yn char(1) NOT NULL DEFAULT 'N',
    use_yn             char(1) NOT NULL DEFAULT 'Y',
    del_yn             char(1) NOT NULL DEFAULT 'N',
    created_by         varchar(64) NOT NULL,
    created_at         timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by         varchar(64) NOT NULL,
    updated_at         timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_docs_partner_user_company FOREIGN KEY (partner_company_id)
        REFERENCES docs_partner_company (partner_company_id),
    CONSTRAINT ck_docs_partner_user_representative CHECK (representative_yn IN ('Y', 'N')),
    CONSTRAINT ck_docs_partner_user_use CHECK (use_yn IN ('Y', 'N')),
    CONSTRAINT ck_docs_partner_user_del CHECK (del_yn IN ('Y', 'N'))
);

ALTER SEQUENCE docs_partner_user_id_seq
    OWNED BY docs_partner_user.partner_user_id;

CREATE UNIQUE INDEX IF NOT EXISTS uq_docs_partner_company_business_no
    ON docs_partner_company (business_no)
    WHERE business_no IS NOT NULL AND del_yn = 'N';

CREATE UNIQUE INDEX IF NOT EXISTS uq_docs_partner_user_active_email
    ON docs_partner_user (partner_company_id, lower(email))
    WHERE del_yn = 'N';

CREATE UNIQUE INDEX IF NOT EXISTS uq_docs_partner_user_representative
    ON docs_partner_user (partner_company_id)
    WHERE representative_yn = 'Y' AND use_yn = 'Y' AND del_yn = 'N';

CREATE INDEX IF NOT EXISTS idx_docs_partner_company_active
    ON docs_partner_company (use_yn, del_yn, company_name);

CREATE INDEX IF NOT EXISTS idx_docs_partner_user_company
    ON docs_partner_user (partner_company_id, use_yn, del_yn, representative_yn DESC);

-- The partial unique index above prevents two representatives. Deferred
-- constraint triggers also prevent an active company from ending a transaction
-- with no active representative, while still allowing representative transfer
-- (clear old, set new) inside one transaction.
CREATE OR REPLACE FUNCTION docs_check_partner_representative()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    target_company_id bigint;
    active_representatives integer;
BEGIN
    IF TG_OP = 'DELETE' THEN
        target_company_id := OLD.partner_company_id;
    ELSE
        target_company_id := NEW.partner_company_id;
    END IF;
    IF EXISTS (
        SELECT 1 FROM docs_partner_company
         WHERE partner_company_id = target_company_id
           AND use_yn = 'Y' AND del_yn = 'N'
    ) THEN
        SELECT COUNT(*)::integer
          INTO active_representatives
          FROM docs_partner_user
         WHERE partner_company_id = target_company_id
           AND representative_yn = 'Y'
           AND use_yn = 'Y'
           AND del_yn = 'N';
        IF active_representatives <> 1 THEN
            RAISE EXCEPTION
                'Active partner company % must have exactly one active representative (found %).',
                target_company_id, active_representatives;
        END IF;
    END IF;
    RETURN NULL;
END
$$;

DROP TRIGGER IF EXISTS trg_docs_partner_user_representative ON docs_partner_user;
CREATE CONSTRAINT TRIGGER trg_docs_partner_user_representative
AFTER INSERT OR UPDATE OR DELETE ON docs_partner_user
DEFERRABLE INITIALLY DEFERRED
FOR EACH ROW EXECUTE FUNCTION docs_check_partner_representative();

DROP TRIGGER IF EXISTS trg_docs_partner_company_representative ON docs_partner_company;
CREATE CONSTRAINT TRIGGER trg_docs_partner_company_representative
AFTER INSERT OR UPDATE ON docs_partner_company
DEFERRABLE INITIALLY DEFERRED
FOR EACH ROW EXECUTE FUNCTION docs_check_partner_representative();

COMMENT ON TABLE docs_partner_company IS
    'Distribution recipient company, independent from internal TDMS organization data';
COMMENT ON TABLE docs_partner_user IS
    'Non-authenticating partner recipient/contact; never stores credentials or roles';

INSERT INTO docs_menu (
    menu_cd, parent_menu_cd, menu_nm, message_cd, menu_level, menu_type,
    menu_url, sort_seq, tree_type, del_yn, use_yn, tooltip, menu_desc,
    role_cd, menu_icon
)
VALUES (
    'MENU_230', 'MENU_214', '협력업체 관리', 'menu.partnerManagement', '2', 'M',
    '/general/organizationmanage/partner/**', 6, 'leaf', 'N', 'Y',
    '배포 대상 협력업체와 수신 사용자 관리',
    '협력업체, 대표사용자 1명 및 일반 수신 사용자를 등록·수정',
    'ROLE_MENU_230', ''
)
ON CONFLICT (menu_cd) DO UPDATE SET
    parent_menu_cd = EXCLUDED.parent_menu_cd,
    menu_nm = EXCLUDED.menu_nm,
    message_cd = EXCLUDED.message_cd,
    menu_level = EXCLUDED.menu_level,
    menu_type = EXCLUDED.menu_type,
    menu_url = EXCLUDED.menu_url,
    sort_seq = EXCLUDED.sort_seq,
    tree_type = EXCLUDED.tree_type,
    del_yn = 'N',
    use_yn = 'Y',
    tooltip = EXCLUDED.tooltip,
    menu_desc = EXCLUDED.menu_desc,
    role_cd = EXCLUDED.role_cd,
    menu_icon = EXCLUDED.menu_icon;

INSERT INTO docs_rel_role_group (
    group_cd, role_cd, insert_user_cd, update_user_cd, insert_dt, update_dt
)
VALUES ('RG_001', 'ROLE_MENU_230', 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (group_cd, role_cd) DO NOTHING;

INSERT INTO docs_role_mapping (group_cd, group_nm, menu_nm, menu_url, menu_type)
SELECT assignment.group_cd,
       COALESCE(role_group.group_nm, assignment.group_cd),
       menu.menu_nm,
       menu.menu_url,
       menu.menu_type::varchar
  FROM docs_rel_role_group assignment
  JOIN docs_menu menu ON menu.role_cd = assignment.role_cd
  LEFT JOIN docs_role_group role_group ON role_group.group_code = assignment.group_cd
 WHERE assignment.role_cd = 'ROLE_MENU_230'
ON CONFLICT (group_cd, menu_url) DO UPDATE SET
    group_nm = EXCLUDED.group_nm,
    menu_nm = EXCLUDED.menu_nm,
    menu_type = EXCLUDED.menu_type;

INSERT INTO docs_lang (lang_type, lang_cd, lang_desc)
VALUES
    ('ko', 'menu.partnerManagement', '협력업체 관리'),
    ('en', 'menu.partnerManagement', 'Partner Management')
ON CONFLICT (lang_type, lang_cd) DO UPDATE
   SET lang_desc = EXCLUDED.lang_desc;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM docs_menu
         WHERE menu_cd = 'MENU_230'
           AND parent_menu_cd = 'MENU_214'
           AND role_cd = 'ROLE_MENU_230'
           AND use_yn = 'Y' AND del_yn = 'N'
    ) THEN
        RAISE EXCEPTION 'Partner-management navigation is missing.';
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM docs_rel_role_group
         WHERE group_cd = 'RG_001' AND role_cd = 'ROLE_MENU_230'
    ) THEN
        RAISE EXCEPTION 'Administrator partner-management permission is missing.';
    END IF;
    IF EXISTS (
        SELECT company.partner_company_id
          FROM docs_partner_company company
          LEFT JOIN docs_partner_user partner_user
            ON partner_user.partner_company_id = company.partner_company_id
           AND partner_user.representative_yn = 'Y'
           AND partner_user.use_yn = 'Y'
           AND partner_user.del_yn = 'N'
         WHERE company.use_yn = 'Y' AND company.del_yn = 'N'
         GROUP BY company.partner_company_id
        HAVING COUNT(partner_user.partner_user_id) <> 1
    ) THEN
        RAISE EXCEPTION 'An active partner company does not have exactly one active representative.';
    END IF;
END
$$;

COMMIT;
