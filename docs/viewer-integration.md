# TDMS - 외부 뷰어 연동

## 1. 연동 범위

TDMS는 문서 ACL을 먼저 확인한 뒤 파일 형식에 따라 PDF 또는 STEP 뷰어 제공자를 선택하고, 짧은 수명의 실행 상관관계 ID를 발급한다. 뷰어 ingest와 launch URL 생성 및 launch 레코드 저장이 모두 성공하면 TDMS가 즉시 열람이력을 정확히 한 번 저장한다. 외부 제공자의 HMAC 서명 `VIEW_OPENED` callback은 서명·nonce·상관관계·사용자·파일 일치와 이벤트 멱등성 검증에만 사용하며 열람이력을 추가로 만들지 않는다.

개발·운영 뷰어 주소는 환경변수로 관리하되 아래 경로는 양쪽 애플리케이션의 고정 계약이므로 설정값으로 변경하지 않는다.

| 방향 | 방식과 고정 경로 | 용도 |
|---|---|---|
| TDMS → 외부 뷰어 | `POST /api/integrations/tdms/v1/documents` | 원본 파일과 메타데이터 전달 |
| TDMS → 외부 뷰어 | `POST /api/integrations/tdms/v1/launch` | 단기 실행 URL 발급 |
| 외부 뷰어 → TDMS | `POST /api/integrations/cv/v1/events` | 서명된 열람 이벤트 통지 |

기존 `ADAP_POST_URL`, `ADAP_PDF_URL` 및 `/cv_post` 흐름은 신규 연동에서 사용하지 않는다. `viewer_integration_ddl.sql`은 과거 로컬 덤프의 loopback ADAP URL만 제거하며, 비상 롤백을 위한 비-loopback 행이 남더라도 신규 코드는 읽지 않는다.

## 2. TDMS 런타임 설정

PDF와 STEP 제공자는 서로 독립적으로 활성화한다. 한 제공자의 주소, client ID 또는
secret을 다른 제공자에 재사용하지 않는다. 두 제공자 모두 같은 고정 API 경로 계약을
사용하지만 origin과 자격증명은 별도다.

### 2.1 PDF 제공자

| 환경변수 | 기본값 | 활성화 시 | 설명 |
|---|---:|---|---|
| `TDMS_VIEWER_ENABLED` | `false` | 필수 | 연동 기능 활성화 여부 |
| `TDMS_VIEWER_BASE_URL` | 빈 값 | 필수 | 외부 뷰어 HTTPS origin만 지정. 누락 시 외부 전송 금지 |
| `TDMS_VIEWER_CLIENT_ID` | 빈 값 | 필수 | TDMS가 외부 뷰어에 제시하는 client ID |
| `TDMS_VIEWER_SHARED_SECRET` | 빈 값 | 필수 | 양쪽 서비스가 공유하는 서명 비밀키 |
| `TDMS_VIEWER_CALLBACK_CLIENT_ID` | 빈 값 | callback 활성화 시 필수 | 외부 뷰어 callback 발신자 client ID |
| `TDMS_VIEWER_WORK_DIR` | `${java.io.tmpdir}/kt1b-viewer` | 선택 | 요청별 임시 PDF 작업 폴더(절대 경로) |
| `TDMS_VIEWER_CONNECT_TIMEOUT_MS` | `5000` | 선택 | 연결 제한 시간(ms) |
| `TDMS_VIEWER_READ_TIMEOUT_MS` | `30000` | 선택 | 응답 제한 시간(ms) |
| `TDMS_VIEWER_SIGNATURE_CLOCK_SKEW_SECONDS` | `300` | 선택 | 서명 시각 허용 오차(초) |
| `TDMS_VIEWER_STATE_RETENTION_DAYS` | `30` | 선택 | 만료 launch 상태 보존기간(일) |

`TDMS_VIEWER_ENABLED=true`인데 base URL, outbound client ID 또는 shared secret이 비어 있으면 파일 전송은 fail-closed로 동작한다. callback을 활성화할 때 `TDMS_VIEWER_CALLBACK_CLIENT_ID`는 외부 뷰어의 `CV_TDMS_CLIENT_ID`와 반드시 같은 값으로 설정한다.

### 2.2 STEP 제공자

| 환경변수 | 기본값 | 활성화 시 | 설명 |
|---|---:|---|---|
| `TDMS_STEP_VIEWER_ENABLED` | `false` | 필수 | STEP 연동 기능 활성화 여부 |
| `TDMS_STEP_VIEWER_BASE_URL` | 빈 값 | 필수 | 3D-CV HTTPS origin만 지정. 누락 시 외부 전송 금지 |
| `TDMS_STEP_VIEWER_CLIENT_ID` | 빈 값 | 필수 | TDMS가 STEP 제공자에 제시하는 client ID |
| `TDMS_STEP_VIEWER_SHARED_SECRET` | 빈 값 | 필수 | TDMS와 STEP 제공자가 공유하는 별도 서명 비밀키 |
| `TDMS_STEP_VIEWER_CALLBACK_CLIENT_ID` | 빈 값 | callback 활성화 시 필수 | STEP 제공자 callback 발신자 client ID |
| `TDMS_STEP_VIEWER_WORK_DIR` | `${java.io.tmpdir}/kt1b-step-viewer` | 선택 | 요청별 임시 STEP 작업 폴더(절대 경로) |
| `TDMS_STEP_VIEWER_CONNECT_TIMEOUT_MS` | `5000` | 선택 | 연결 제한 시간(ms) |
| `TDMS_STEP_VIEWER_READ_TIMEOUT_MS` | `30000` | 선택 | 응답 제한 시간(ms) |
| `TDMS_STEP_VIEWER_SIGNATURE_CLOCK_SKEW_SECONDS` | `300` | 선택 | 서명 시각 허용 오차(초) |
| `TDMS_STEP_VIEWER_STATE_RETENTION_DAYS` | `30` | 선택 | 만료 launch 상태 보존기간(일) |

`TDMS_STEP_VIEWER_ENABLED=true`일 때도 필수값 누락, 32 UTF-8 byte 미만 secret,
상대 작업 경로 또는 HTTPS가 아닌 비-loopback origin은 fail-closed로 거부된다. 검증 오류는
반드시 `TDMS_STEP_VIEWER_*` 환경변수 이름을 표시한다.

### 2.3 외부 제공자 설정 대응

외부 뷰어 배포에서는 별도로 다음 값을 맞춘다. PDF 제공자는 `TDMS_VIEWER_*` 값을
그대로 대응하고, 3D-CV 배포의 outbound 수신 설정에는 TDMS의
`TDMS_STEP_VIEWER_*` 값을 대응한다.

| 외부 뷰어 환경변수 | TDMS와의 관계 |
|---|---|
| `TDMS_VIEWER_CLIENT_ID` | 해당 TDMS 제공자의 outbound client ID와 동일 |
| `TDMS_VIEWER_SHARED_SECRET` | 해당 TDMS 제공자의 shared secret과 동일 |
| `CV_TDMS_CLIENT_ID` | 해당 제공자의 TDMS callback client ID와 동일 |
| `TDMS_CALLBACK_URL` | `https://<tdms-host>/api/integrations/cv/v1/events` |

마지막 두 callback 설정을 함께 지정하면 CV의 최초 열람 이벤트 송신이 활성화된다. 운영 환경의 callback URL은 CV 서버에서 접근 가능한 TDMS HTTPS 주소여야 하며 localhost를 사용하면 안 된다.

### 2.4 VIEW와 원본 다운로드의 경계

현재 2D 뷰어는 브라우저 PDF 렌더러이므로 `VIEW` 권한은 짧은 수명의 세션에 묶인 PDF 바이트 스트리밍을 포함한다. `DOWNLOAD_ORIGINAL`은 TDMS가 제공하는 정식 원본 다운로드 기능과 그 이력을 별도로 통제하지만, 브라우저 개발자 도구까지 포함한 DRM 수준의 복제 방지를 의미하지 않는다. VIEW 사용자에게 원본과 다른 파생본만 제공해야 하는 보안 정책이라면 사용자 식별 워터마크·평탄화 또는 전용 DRM 변환 단계를 추가한 뒤 활성화해야 한다.

## 3. 비밀정보 관리

- shared secret은 DB, Git, WAR, `application.properties`, BAT/셸 스크립트, 명령행 인수 및 로그에 저장하지 않는다.
- 서비스 계정의 보안 환경 또는 운영 비밀 저장소에서 프로세스 시작 시에만 주입한다.
- 로그에는 client ID, correlation ID와 결과만 남기고 서명 원문, secret, 파일 본문은 남기지 않는다.
- DB 마이그레이션은 알려진 viewer-secret 설정 키를 삭제하고 `docs_system_config`에 다시 저장하지 못하도록 제약조건을 설치한다.
- 키 교체 시 두 서비스를 같은 점검 창에서 재시작하고, 이전 키로 서명된 요청이 더 이상 허용되지 않는지 확인한다.

## 4. DB 마이그레이션

`fresh_database_migration.psql`이 샘플 데이터 반영 전에 `viewer_integration_ddl.sql`을 실행한다. 스크립트는 반복 실행할 수 있으며 다음 테이블을 관리한다.

| 테이블 | 목적 |
|---|---|
| `docs_viewer_launch` | `viewer_provider`(`PDF`/`STEP`), ACL 대상, 사용자, 파일 및 만료 시각을 correlation ID와 연결 |
| `docs_history` | 성공한 TDMS launch를 `source_system_cd=TDMS`인 열람이력으로 요청당 한 번 저장 |
| `docs_viewer_event` | 검증된 callback 이벤트를 멱등 저장 |
| `docs_viewer_callback_nonce` | callback 재전송 공격 차단 |

launch와 TDMS 열람이력은 `WITH inserted_launch AS (INSERT ... RETURNING ...) INSERT INTO docs_history ...`
형태의 PostgreSQL 단일 문장으로 저장한다. 서비스는 이 원자 저장 결과가 정확히 1행일 때만
뷰어 launch URL을 반환한다.

기존 DB에 마이그레이션을 다시 실행하면 `viewer_provider`가 없는 과거 launch는 `PDF`로
채워지고 `PDF`/`STEP` 이외의 값은 제약조건으로 거부된다. callback 처리 시 1일보다
오래된 nonce는 정리한다. `docs_viewer_key`는 기존 기능과 테스트 호환을 위해 당장
삭제하지 않지만 신규 연동에서는 사용하지 않는다.

## 5. Windows 테스트/데모

1. Windows 서비스 계정의 환경변수에 TDMS 설정을 등록한다. secret을 저장소의 `start-server.bat` 같은 파일에 직접 기록하지 않는다.
2. 방화벽에서 TDMS 서버가 운영 뷰어 host와 port로 나가는 TCP를 허용한다.
3. Java 17 truststore가 운영 뷰어의 인증서 체인을 신뢰하는지 확인한다.
4. 먼저 `TDMS_VIEWER_ENABLED=false`, `TDMS_STEP_VIEWER_ENABLED=false`로 기동하여 DB 마이그레이션과 기존 문서 조회를 확인한다.
5. 제공자별 client ID, callback URL과 secret을 맞춘 후 서비스를 재시작하고 필요한 제공자만 활성화한다.
6. ACL 허용 사용자와 차단 사용자 각각으로 실행한다. 차단 요청은 파일 전송 전에 403으로 종료되어야 한다.
7. 동일 callback 재전송, 만료된 서명, 잘못된 client ID/서명은 거부되고 정상 이벤트만 한 번 저장되는지 확인한다.

## 6. AIX 7.3 운영

- IBM Semeru/OpenJ9 Java 17의 truststore에 운영 뷰어 및 TDMS reverse proxy의 CA 체인을 설치한다.
- 서비스 관리 환경 파일을 사용할 경우 서비스 계정만 읽도록 권한을 `600`으로 제한하고 배포 백업물에 secret이 섞이지 않게 한다.
- secret을 `java -D...` 또는 셸 명령행 인수로 전달하지 않는다. 프로세스 목록과 기동 로그에 노출될 수 있다.
- AIX에서 외부 뷰어 HTTPS 포트로 나가는 통신과 외부 뷰어에서 TDMS callback HTTPS endpoint로 들어오는 통신을 각각 방화벽 allowlist에 반영한다.
- TDMS callback은 reverse proxy에서 TLS를 종료하더라도 원래 client IP/host 처리 정책과 최대 요청 크기를 고정한다.
- 서버 간 시각 차이가 서명 허용 오차보다 작도록 NTP를 확인한다.

## 7. 활성화 체크리스트

- [ ] DB 마이그레이션을 두 번 실행해도 오류나 중복 행이 없다.
- [ ] TDMS가 비활성 상태에서는 기존 문서 조회/다운로드가 영향을 받지 않는다.
- [ ] 활성 상태에서 필수 환경변수 누락 시 연동이 닫힌 상태로 실패한다.
- [ ] PDF launch는 `viewer_provider=PDF`, STEP launch는 `viewer_provider=STEP`으로 저장된다.
- [ ] 허용된 문서의 주파일/보조파일이 기대한 파일 번호와 해시로 전달된다.
- [ ] ACL 차단 문서는 외부 뷰어로 바이트가 전송되지 않는다.
- [ ] launch 만료 후 실행 URL을 다시 사용할 수 없다.
- [ ] callback 서명, client ID, 시각, nonce, correlation ID, 사용자/파일 일치 검증이 모두 동작한다.
- [ ] PDF/STEP launch 성공은 TDMS 열람이력에 요청당 한 번 기록되고 callback은 열람이력을 추가하지 않는다.
- [ ] 애플리케이션 및 reverse proxy 로그에 secret, 서명 원문 또는 파일 본문이 없다.

## 8. 상태 보존기간

`TDMS_VIEWER_STATE_RETENTION_DAYS`와 `TDMS_STEP_VIEWER_STATE_RETENTION_DAYS`는
각 제공자의 만료 launch 상태를 토큰 만료 후 보존할 기간을 일 단위로 지정한다.
기본값은 30일이며 허용 범위는 1~3650일이다. 새 launch를 만들기 전에 이 기간보다
오래된 만료 launch를 정리하며, 연결된 viewer event는 DB의 `ON DELETE CASCADE`
제약으로 함께 정리된다. callback의 `occurredAt`은 저장된 launch 생성시각보다 서명
시각 허용오차 이상 앞설 수 없다.
