# AIX 7.3 배포 가이드

## 1. 적용 범위

- 애플리케이션: Spring Boot 2.7.18, JDK 17, 실행형 WAR
- 운영체제: AIX 7.3, 64-bit POWER(ppc64)
- 현재 검증 기준 DB: PostgreSQL 17
- 서버 런타임은 ePDF JNI DLL/클래스에 의존하지 않는 현재 빌드를 사용한다. Windows DLL이나 `java.library.path`를 다시 추가하지 않는다.
- 외부 인터페이스(Oracle, MSSQL, ADAP 등)는 이번 배포의 합격 범위에서 제외하고 마지막 단계에서 별도로 연계한다.

현재 Windows/Docker 기능 시험과 AIX 최초 배포의 데이터베이스 기준은 PostgreSQL 17로 통일한다. 신규 환경은 추적 중인 기본 덤프를 복원한 뒤 전체 마이그레이션과 환경별 초기 데이터를 적용하여 생성한다.

## 2. JVM 및 운영체제 준비

IBM Semeru Runtime Certified Edition 17 또는 조직이 승인한 동등한 OpenJ9 기반 JDK 17의 **AIX ppc64용 Service Release/Fix Pack**을 설치한다. Windows에서 사용한 HotSpot JDK만으로 AIX 호환을 판정하지 말고, 실제 AIX JVM에서 OpenJ9가 실행되는지 확인한다.

```sh
oslevel -s
getconf KERNEL_BITMODE
$JAVA_HOME/bin/java -version
```

확인 기준은 AIX 7.3, 64-bit, Java 17, IBM Semeru/OpenJ9이다. Windows에서 빌드·검증한 동일 WAR를 AIX OpenJ9에서 다시 Smoke Test해야 한다. 특히 기본 문자셋, 시간대, TLS truststore, 파일 권한·대소문자, GC/JIT 차이를 별도로 확인한다. 애플리케이션은 내장 Tomcat의 순수 Java NIO 커넥터를 사용하며 Tomcat Native/APR은 설치하지 않는다.

## 3. 환경변수와 디렉터리

서비스 계정의 시작 스크립트 또는 비밀관리 도구에서 다음 값을 주입한다. 아래 `<...>` 값은 형식만 보여 주는 placeholder이며 실제 운영값이 아니다. 비밀번호와 암호키를 WAR, Git, 로그, 프로세스 명령행에 기록하지 않는다.

```sh
export JAVA_HOME=/opt/ibm/semeru-17
export PATH="$JAVA_HOME/bin:$PATH"

# `locale -a`에서 실제 설치된 UTF-8 로케일 이름을 확인해 사용한다.
export LANG=ko_KR.UTF-8
export LC_ALL=ko_KR.UTF-8
export TZ=Asia/Seoul

export KT1B_DB_URL='jdbc:postgresql://<pg17-host>:5432/kt1b'
export KT1B_DB_USERNAME='<db-service-user>'
export KT1B_DB_PASSWORD='<db-password>'

export KT1B_FILE_ROOT=/data/kt1b/files
export KT1B_TEMP_ROOT=/data/kt1b/tmp
export KT1B_LOG_DIR=/var/log/kt1b
export OPENJ9_JAVA_OPTIONS="-Dfile.encoding=UTF-8 -Duser.language=ko -Duser.country=KR -Duser.timezone=Asia/Seoul -Djava.io.tmpdir=$KT1B_TEMP_ROOT"
```

외부 인터페이스를 승인한 뒤에만 필요한 값을 추가한다. 핵심 문서 기능만 시험하는 단계에서는 설정하지 않는다.

```sh
# 레거시 SEED 전송·출력 연계를 활성화할 때만 설정한다.
# 아래 값은 placeholder이며, 실제 키는 UTF-8 인코딩 기준 정확히 16바이트여야 한다.
export KT1B_LEGACY_CRYPTO_KEY='<legacy-key-exactly-16-UTF8-bytes>'
```

| 설정 | 현재 구현의 동작 | AIX 운영 기준 |
|---|---|---|
| `KT1B_DB_PASSWORD` | `application.properties`에 기본값이 없다. | 필수. 비밀관리 도구 또는 권한이 제한된 서비스 설정에서 주입한다. |
| `KT1B_LOG_DIR` | 없으면 상대경로 `run-logs`를 사용한다. | 필수로 간주하고 쓰기 가능한 AIX 절대경로를 지정한다. 상대경로 기본값은 로컬 개발용으로만 사용한다. |
| `KT1B_LEGACY_CRYPTO_KEY` | 환경변수 또는 같은 이름의 JVM 시스템 속성을 읽고, 레거시 SEED 기능을 호출할 때 값이 없거나 UTF-8 기준 16바이트가 아니면 실패한다. | 레거시 외부연계를 승인한 경우에만 설정한다. |

실제 키를 화면에 출력하지 않고 바이트 길이만 검사한다.

```sh
key_bytes=$(printf %s "$KT1B_LEGACY_CRYPTO_KEY" | wc -c | tr -d ' ')
[ "$key_bytes" = "16" ] || {
  echo "KT1B_LEGACY_CRYPTO_KEY must be exactly 16 UTF-8 bytes" >&2
  exit 1
}
unset key_bytes
```

`KT1B_DB_*`는 현재 `application.properties`가 직접 읽고, `KT1B_LOG_DIR`는 Logback 설정이 읽는다. `KT1B_FILE_ROOT`와 `KT1B_TEMP_ROOT`는 AIX 배포 표준 변수일 뿐 애플리케이션이 모든 저장경로를 이 변수에서 자동 파생하지는 않는다. 따라서 다음 조건도 함께 충족해야 한다.

- 서비스 계정이 파일 루트, 임시 디렉터리, 로그 디렉터리를 읽고 쓰고 탐색할 수 있어야 한다.
- `DOCS_SYSTEM_CONFIG`의 `*_PATH` 값은 `/data/kt1b/files/...` 아래 AIX 절대 경로로 변경한다. 환경변수만 설정해도 DB 설정값이 자동 변경되지는 않는다.
- `2D_FILE_PATH`, `DOCUMENT_PATH`, `SW_PATH`, `PRODUCTION_PATH`, `DXF_PATH`, `GENERAL_FILE_PATH`, `PROTECTED_FILE_PATH`, `UPDOWN_ORG_FILE_PATH`, `MERGE_PATH`, `VIEWER_NETWORK_PATH`, `VIEWER_CACHE_PATH`, `ADAP_PDF_PATH` 등 실제 사용 키를 전부 점검한다.
- 드라이브 문자(`C:`), UNC 경로, 역슬래시(`\`)를 저장하지 않는다. DB에는 가능하면 `/` 형식의 상대 경로를 보관하고 파일시스템 경계에서 파일 루트와 결합한다.
- AIX 파일시스템은 대소문자를 구분하므로 DB 파일명과 실제 파일명의 대소문자가 같아야 한다.
- 공유 저장소를 마운트한다면 애플리케이션 시작 전에 마운트 완료 여부와 서비스 계정의 UID/GID 및 쓰기 권한을 검사한다.

```sh
umask 027
mkdir -p "$KT1B_FILE_ROOT" "$KT1B_TEMP_ROOT" "$KT1B_LOG_DIR"
chmod 750 "$KT1B_FILE_ROOT" "$KT1B_TEMP_ROOT" "$KT1B_LOG_DIR"

for path in "$KT1B_FILE_ROOT" "$KT1B_TEMP_ROOT" "$KT1B_LOG_DIR"; do
  [ -d "$path" ] && [ -r "$path" ] && [ -w "$path" ] && [ -x "$path" ] || exit 1
done
```

## 4. 데이터베이스 기반 DDL 적용

현재 프로젝트는 Flyway/Liquibase로 DDL을 자동 적용하지 않는다. 신규 덤프 복원 또는 운영 반영 시 애플리케이션을 중지한 상태에서 PostgreSQL 서비스 계정으로 다음 파일을 순서대로 적용한다. 사용자 번호 시퀀스를 현재 데이터와 맞추는 동안 `NEXTVAL` 호출이 들어오지 않아야 한다. `ON_ERROR_STOP`을 반드시 켜서 기존 데이터가 제약조건을 위반하면 배포를 중단한다.

```sh
psql -h <pg17-host> -p 5432 -U "$KT1B_DB_USERNAME" -d kt1b \
  -v ON_ERROR_STOP=1 \
  -f /opt/kt1b/sql/acl_foundation_ddl.sql
psql -h <pg17-host> -p 5432 -U "$KT1B_DB_USERNAME" -d kt1b \
  -v ON_ERROR_STOP=1 \
  -f /opt/kt1b/sql/internal_only_cleanup_ddl.sql
psql -h <pg17-host> -p 5432 -U "$KT1B_DB_USERNAME" -d kt1b \
  -v ON_ERROR_STOP=1 \
  -f /opt/kt1b/sql/general_route_migration_ddl.sql
```

두 스크립트는 재실행 가능하게 작성되어 있다. 적용 후 메뉴와 사용자 구분값이 모두 내부(`I`)로 정규화되고, 폐기된 외부 사용자 요청 테이블과 시퀀스가 남지 않았는지 확인한다.

## 5. 외부 인터페이스의 명시적 활성화

외부 인터페이스를 활성화하기 전에는 `KT1B_LEGACY_CRYPTO_KEY`와 `DOCS_SYSTEM_CONFIG`의 `UPDOWN_SECRET_KEY`, `FILE_API_KEY` 등 모든 기존 연계 키를 새 값으로 교체하고 상대 시스템과 별도로 동기화한다. `KT1B_LEGACY_CRYPTO_KEY`는 환경변수 또는 같은 이름의 JVM 시스템 속성으로만 주입하며 WAR, Git, 로그, 명령행에 실제 값을 남기지 않는다. 이 값이 없어도 핵심 애플리케이션은 기동되지만, 레거시 SEED 연계 호출은 명확한 설정 오류로 실패한다.

### 5.1 HTTPS 전송·뷰어 URL

- 신규 외부 뷰어 연동은 `TDMS_VIEWER_BASE_URL`에 HTTPS origin만 설정하고, `/api/integrations/tdms/v1/documents`, `/api/integrations/tdms/v1/launch`, `/api/integrations/cv/v1/events` 고정 경로를 사용한다.
- `ADAP_PDF_URL`, `ADAP_POST_URL` 및 `/cv_post`는 신규 연동에서 사용하지 않는다. 받은 덤프의 loopback ADAP 주소는 `viewer_integration_ddl.sql`이 제거한다.
- 브라우저가 접근하는 공개 애플리케이션 URL과 뷰어가 다시 호출하는 파일 URL도 HTTPS여야 하며, reverse proxy의 외부 host·port·context path와 일치해야 한다.
- 상대 서버 인증서 체인을 AIX JVM의 전용 truststore에 등록하고 `javax.net.ssl.trustStore` 계열 옵션으로 지정한다. 인증서 또는 호스트명 검증을 우회하지 않는다. 자체서명 인증서를 사용한다면 운영 승인된 CA 또는 인증서를 먼저 배포한다.

URL 예시는 실제 주소가 아닌 placeholder만 사용한다.

```text
TDMS_VIEWER_BASE_URL=https://<viewer-host>:<port>
TDMS_VIEWER_ENABLED=false
```

client ID, callback client ID, shared secret 및 Windows/AIX 활성화 절차는 [viewer-integration.md](viewer-integration.md)를 따른다. shared secret은 DB나 이 파일에 저장하지 않는다.

### 5.2 외부연계 스케줄러

다음 세 스케줄러는 현재 `application.properties`에서 모두 `false`이고, 각 컴포넌트도 `matchIfMissing=false`라서 속성을 생략해도 생성되지 않는다. 운영 승인 없이 공통 설정이나 WAR 내부 기본값을 `true`로 바꾸지 않는다.

| 속성 | 외부 작업 | 기본값 | 활성화 시점 |
|---|---|---:|---|
| `update.distribution.scheduler.enabled` | 배포 부서정보 동기화 | `false` | 부서정보 연계 검증 및 담당자 승인 후 |
| `file.auto.insert.scheduler.enabled` | ADAP 파일 자동등록 | `false` | ADAP 연결·경로·중복처리 검증 후 |
| `send.mail.scheduler.enabled` | 외부 메일 병합·발송 | `false` | 메일 연계 및 수신자 검증 후 |

활성화는 배포 환경의 외부 설정에서 항목별로 명시적으로 수행한다. 세 값을 한 번에 켜지 말고 한 작업씩 승인·검증한다.

```properties
update.distribution.scheduler.enabled=false
file.auto.insert.scheduler.enabled=false
send.mail.scheduler.enabled=false
```

이 세 컴포넌트는 활성화되면 스케줄 주기뿐 아니라 애플리케이션 시작 시에도 실행되도록 구성되어 있다. 따라서 `true`로 바꾸기 전에 외부 endpoint, 계정, DB 설정, 저장경로, 재실행 안전성 및 장애 시 롤백 절차를 준비한다.

## 6. 빌드 및 실행

Windows 검증에서 생성한 동일 WAR를 AIX에 배포하고 양쪽 체크섬을 비교한다.

```powershell
.\mvnw.cmd clean verify
Get-FileHash .\target\TDMS-KT-1B.war -Algorithm SHA256
```

```sh
openssl dgst -sha256 /opt/kt1b/TDMS-KT-1B.war
$JAVA_HOME/bin/java -Xms1g -Xmx2g -jar /opt/kt1b/TDMS-KT-1B.war
```

메모리 값은 운영 서버 용량에 맞춰 조정한다. 장기 실행은 AIX 서비스 관리 방식으로 등록하고, 시작·중지·로그 경로를 서비스 계정 기준으로 고정한다.

## 7. Windows 사전 Smoke Checklist

- [ ] JDK 17에서 `mvnw clean verify`가 성공하고 WAR가 생성된다.
- [ ] PostgreSQL `SELECT version()` 결과가 17 계열이며 Mapper 로딩 오류 없이 기동된다.
- [ ] `KT1B_DB_PASSWORD`가 외부에서 주입되고 빈 값이나 WAR 내 기본 비밀번호가 사용되지 않는다.
- [ ] 세 외부연계 스케줄러 설정이 모두 `false`이며 시작 시 동기화·자동등록·메일 작업이 실행되지 않는다.
- [ ] `/login/loginPage` 접근, 정상 로그인, 로그아웃이 동작한다.
- [ ] 비로그인 사용자는 보호 URL에 접근하지 못하고, 관리자 ACL 화면은 지정 권한만 접근한다.
- [ ] 문서 등급별 허용 사용자와 차단 사용자를 각각 시험해 차단 요청이 403으로 종료된다.
- [ ] 업로드·조회·다운로드·출력 후 행위 로그에 사용자, 대상 문서, 성공 여부, 시각이 남는다.
- [ ] 한글·공백·대소문자가 섞인 파일명으로 업로드와 다운로드가 성공한다.
- [ ] `../`, 절대 경로, 역슬래시를 이용한 저장소 이탈 요청이 거부된다.
- [ ] 외부 인터페이스가 호출되지 않으며 `UnsatisfiedLinkError`나 ePDF JNI 로딩 로그가 없다.
- [ ] 외부연계 시험 시 전송·뷰어 URL이 HTTPS이고 인증서·호스트명 검증이 성공한다.
- [ ] 재시작 후 ACL, 이력, 파일이 그대로 조회된다.

## 8. 실제 AIX Smoke Checklist

- [ ] `oslevel -s`, `getconf KERNEL_BITMODE`, `java -version`, `locale`, `date`가 기준과 일치한다.
- [ ] `java -version` 결과가 Java 17 및 IBM Semeru/OpenJ9 AIX ppc64 승인 빌드를 표시한다.
- [ ] Windows 검증 WAR와 AIX 배포 WAR의 체크섬이 일치한다.
- [ ] `KT1B_DB_PASSWORD`와 절대경로 `KT1B_LOG_DIR`가 서비스 환경에 주입되며 실제 값을 로그나 점검 출력에 노출하지 않는다.
- [ ] 서비스 계정으로 파일·임시·로그 디렉터리에 생성, 읽기, 삭제 및 디렉터리 탐색이 가능하다.
- [ ] PostgreSQL 17 연결, DB 시간대, 한글 조회가 정상이다.
- [ ] 애플리케이션 시작 로그에 Mapper/JSP 오류, APR/native 로딩, `UnsatisfiedLinkError`가 없다.
- [ ] `curl -I http://127.0.0.1:3508/login/loginPage`가 200 또는 정상 리다이렉트를 반환한다.
- [ ] Windows와 동일한 로그인, 문서등급 ACL, 행위 이력 시나리오가 통과한다.
- [ ] 한글·공백·대소문자가 섞인 파일의 업로드, 조회, 다운로드, 출력이 성공한다.
- [ ] 생성된 모든 파일이 `KT1B_FILE_ROOT` 아래에 있으며 파일명에 `C:`, UNC, 역슬래시가 없다.
- [ ] 재시작과 동시 사용자 시험 후에도 세션, ACL, 파일, 감사 로그가 정상이다.
- [ ] 세 외부연계 스케줄러가 기본 `false`이고 외부 인터페이스 장애가 핵심 문서 기능에 영향을 주지 않는다.
- [ ] 레거시 SEED 연계를 켠 경우에만 정확히 16바이트인 `KT1B_LEGACY_CRYPTO_KEY`가 설정된다.
- [ ] 외부연계를 켠 경우 전송·뷰어 URL과 공개 reverse proxy URL이 HTTPS이고 AIX truststore 검증이 성공한다.

Windows 테스트는 AIX의 OpenJ9, 대소문자 구분, 파일 권한과 경로 동작을 대신할 수 없다. 최종 배포 승인은 실제 AIX 7.3 환경에서 위 체크리스트를 모두 통과한 결과로 결정한다.

## 9. 롤백 기준

- 이전 WAR, 환경변수 목록, `DOCS_SYSTEM_CONFIG` 경로값과 PostgreSQL 백업을 배포 전에 보관한다.
- 기동 실패, 저장소 이탈, ACL 우회, 감사 로그 유실이 발생하면 서비스를 중지하고 이전 WAR와 설정으로 복귀한다.
- 데이터베이스 마이그레이션 실패 시 신규 데이터베이스를 폐기하고, 배포 전에 보관한 백업으로 복구한다.
