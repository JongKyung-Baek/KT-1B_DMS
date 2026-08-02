-- Restore the Korean labels for active legacy navigation and organization
-- management entries. The legacy metadata mappers deliberately fall back to
-- English when a DOCS_LANG row is absent, so these rows must exist even though
-- the metadata tables already contain Korean fallback labels.
-- PostgreSQL 17+, safe to run repeatedly on an existing database.

\set ON_ERROR_STOP on

BEGIN;

INSERT INTO docs_lang (lang_type, lang_cd, lang_desc)
VALUES
    ('ko', 'menu.datamanage', '기술자료관리'),
    ('ko', 'menu.usermanage', '사용자 관리'),
    ('ko', 'menu.deptmanage', '부서 관리'),
    ('ko', 'menu.search', '조회'),
    ('ko', 'menu.register', '등록'),
    ('ko', 'btn.resetPwd', '비밀번호 초기화'),
    ('ko', 'btn.unlockAccount', '계정 잠금 해제'),
    ('ko', 'btn.create', '사용자 생성'),
    ('ko', 'btn.createDept', '부서 생성'),
    ('ko', 'grid.id', '사용자 계정'),
    ('ko', 'grid.name', '사용자 성명'),
    ('ko', 'grid.active', '사용 여부'),
    ('ko', 'grid.deptCode', '부서 코드'),
    ('ko', 'grid.accountLock', '계정 잠금 여부'),
    ('ko', 'grid.incorrectPwd', '비밀번호 오류 횟수'),
    ('ko', 'grid.lastLogin', '최종 로그인'),
    ('ko', 'grid.position', '직급'),
    ('ko', 'grid.role', '사용자 권한')
ON CONFLICT (lang_type, lang_cd) DO UPDATE
   SET lang_desc = EXCLUDED.lang_desc;

DO $korean_menu_locale_validation$
BEGIN
    IF (
        SELECT COUNT(*)
          FROM docs_lang
         WHERE lang_type = 'ko'
           AND (lang_cd, lang_desc) IN (
               ('menu.datamanage', '기술자료관리'),
               ('menu.usermanage', '사용자 관리'),
               ('menu.deptmanage', '부서 관리'),
               ('menu.search', '조회'),
               ('menu.register', '등록'),
               ('btn.resetPwd', '비밀번호 초기화'),
               ('btn.unlockAccount', '계정 잠금 해제'),
               ('btn.create', '사용자 생성'),
               ('btn.createDept', '부서 생성'),
               ('grid.id', '사용자 계정'),
               ('grid.name', '사용자 성명'),
               ('grid.active', '사용 여부'),
               ('grid.deptCode', '부서 코드'),
               ('grid.accountLock', '계정 잠금 여부'),
               ('grid.incorrectPwd', '비밀번호 오류 횟수'),
               ('grid.lastLogin', '최종 로그인'),
               ('grid.position', '직급'),
               ('grid.role', '사용자 권한')
           )
    ) <> 18 THEN
        RAISE EXCEPTION 'Korean legacy UI translations are incomplete.';
    END IF;

    IF EXISTS (
        SELECT 1
          FROM docs_menu menu
         WHERE menu.use_yn = 'Y'
           AND menu.del_yn = 'N'
           AND menu.message_cd IN (
               'menu.datamanage',
               'menu.usermanage',
               'menu.deptmanage',
               'menu.search',
               'menu.register'
           )
           AND NOT EXISTS (
               SELECT 1
                 FROM docs_lang language
                WHERE language.lang_type = 'ko'
                  AND language.lang_cd = menu.message_cd
                  AND NULLIF(BTRIM(language.lang_desc), '') IS NOT NULL
           )
    ) THEN
        RAISE EXCEPTION 'An active legacy navigation entry still lacks Korean text.';
    END IF;
END
$korean_menu_locale_validation$;

COMMIT;
