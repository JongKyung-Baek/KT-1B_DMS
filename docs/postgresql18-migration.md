# PostgreSQL 18 병행 전환 가이드

> 상태: 전환 계획 및 병행 검증 절차. 현재 Windows/Docker 기능 시험과 AIX 7.3 최초 배포의 승인 기준은 PostgreSQL 17입니다.

이 구성은 현재 기준인 PostgreSQL 17 컨테이너와 데이터를 그대로 둔 채 PostgreSQL 18을 별도로 검증하기 위한 것입니다. PostgreSQL 18 검증 성공 전에는 운영 연결 문자열이나 PostgreSQL 17 볼륨을 변경하지 않습니다.

- 기존 PostgreSQL 17: `127.0.0.1:5432`
- 신규 PostgreSQL 18: `127.0.0.1:5433`
- 신규 컨테이너: `kt1b-postgres18`
- 신규 전용 볼륨: `kt1b_postgres18_data`

PostgreSQL 18 공식 Docker 이미지는 데이터 디렉터리 구조가 변경되었습니다. 따라서 볼륨은 `/var/lib/postgresql/data`가 아니라 `/var/lib/postgresql`에 연결합니다. 자세한 내용은 [Docker Official Image 문서](https://hub.docker.com/_/postgres#pgdata)를 참고하십시오.

## 1. 사전 확인

아래 명령은 프로젝트 루트에서 실행합니다. 운영 중인 `kt1b-postgres` 컨테이너를 중지하거나 제거하지 마십시오.

```powershell
$env:KT1B_PG18_DATABASE = 'kt1b'
$env:KT1B_PG18_USERNAME = '<pg18-service-user>'
$env:KT1B_PG18_PASSWORD = '<pg18-password>'
$env:KT1B_PG18_TIMEZONE = 'Etc/UTC'
docker compose -f docker-compose.postgres18.yml config --quiet
docker compose -f docker-compose.postgres18.yml up -d
docker compose -f docker-compose.postgres18.yml ps
```

`<...>` 값은 실제 계정이나 비밀번호가 아닌 placeholder입니다. PostgreSQL 18 비밀번호에는 기본값이 없으며 `KT1B_PG18_PASSWORD`를 반드시 먼저 설정해야 합니다. 실제 비밀번호를 문서, Git, 셸 기록 또는 명령 출력에 남기지 않습니다. `config --quiet`는 설정 유효성만 검사하고 비밀번호가 포함된 전체 구성을 화면에 출력하지 않습니다. 초기화 값들은 볼륨이 처음 생성될 때만 적용됩니다. 이미 생성된 named volume에서 계정이나 초기화 옵션을 바꾸려면 새 검증용 볼륨을 사용해야 합니다.

## 2. 백업 복원

먼저 신규 컨테이너가 PostgreSQL 18인지 확인합니다.

```powershell
docker exec kt1b-postgres18 psql -U $env:KT1B_PG18_USERNAME -d $env:KT1B_PG18_DATABASE -Atc "select version();"
docker cp .\db\260701dumpdb3.backup kt1b-postgres18:/tmp/260701dumpdb3.backup
```

신규 DB가 비어 있는 최초 1회에만 복원합니다.

```powershell
docker exec kt1b-postgres18 sh -c 'PGPASSWORD="$POSTGRES_PASSWORD" exec pg_restore -U "$POSTGRES_USER" -d "$POSTGRES_DB" --no-owner --no-privileges --exit-on-error --verbose /tmp/260701dumpdb3.backup'
```

계정이나 DB 이름을 변경했다면 위 명령의 `PGPASSWORD`, `-U`, `-d`도 같은 값으로 바꿉니다. 복원을 다시 실행하기 위해 기존 데이터를 정리하는 작업은 PostgreSQL 18 전용 컨테이너와 볼륨인지 재확인한 뒤 수행해야 합니다.

## 3. 복원 검증

버전, 인코딩, locale, 시간대를 확인합니다.

```powershell
docker exec kt1b-postgres18 psql -U $env:KT1B_PG18_USERNAME -d $env:KT1B_PG18_DATABASE -P pager=off -c "select version(); show server_encoding; show timezone;"
docker exec kt1b-postgres18 psql -U $env:KT1B_PG18_USERNAME -d $env:KT1B_PG18_DATABASE -P pager=off -c "select datname, datcollate, datctype, datcollversion, pg_database_collation_actual_version(oid) as actual_collversion from pg_database where datname=current_database();"
```

확장, 인덱스, 핵심 데이터 건수를 확인합니다.

```powershell
docker exec kt1b-postgres18 psql -U $env:KT1B_PG18_USERNAME -d $env:KT1B_PG18_DATABASE -P pager=off -c "select extname, extversion from pg_extension order by extname;"
docker exec kt1b-postgres18 psql -U $env:KT1B_PG18_USERNAME -d $env:KT1B_PG18_DATABASE -P pager=off -c "select count(*) filter (where not indisvalid) as invalid_indexes, count(*) filter (where not indisready) as not_ready_indexes from pg_index;"
docker exec kt1b-postgres18 psql -U $env:KT1B_PG18_USERNAME -d $env:KT1B_PG18_DATABASE -P pager=off -c "select 'docs_user' as table_name, count(*) from docs_user union all select 'docs_sw', count(*) from docs_sw union all select 'docs_sw_file', count(*) from docs_sw_file union all select 'docs_sw_sub_file', count(*) from docs_sw_sub_file;"
```

현재 전달받은 dump의 기준 건수는 다음과 같습니다.

| 테이블 | 기준 건수 |
|---|---:|
| `docs_user` | 8 |
| `docs_sw` | 15 |
| `docs_sw_file` | 15 |
| `docs_sw_sub_file` | 13 |

복원 후 다음 항목도 확인합니다.

1. `uuid-ossp`와 애플리케이션 함수가 정상 복원되었는지 확인
2. `datcollversion`과 `actual_collversion`이 다르면 문자열 인덱스 재생성 여부 검토
3. 시퀀스의 현재값이 각 대상 테이블의 최대 키보다 큰지 확인
4. `ANALYZE` 실행 후 로그인, 목록, 등록, 뷰어 주요 기능 점검
5. PostgreSQL 17과 18의 핵심 테이블 건수 및 샘플 조회 결과 비교

## 4. 애플리케이션을 PostgreSQL 18에 연결

PostgreSQL 17에서 동일 WAR의 회귀 시험과 데이터 기준선 비교가 끝난 뒤에만 PostgreSQL 18 연결 시험을 시작합니다. `application.properties`의 기존 기본값은 계속 `5432`를 사용합니다. 아래 환경변수를 설정한 검증 프로세스만 PostgreSQL 18로 연결됩니다.

```powershell
$env:KT1B_DB_URL = 'jdbc:postgresql://localhost:5433/kt1b'
$env:KT1B_DB_USERNAME = $env:KT1B_PG18_USERNAME
$env:KT1B_DB_PASSWORD = $env:KT1B_PG18_PASSWORD
.\mvnw.cmd spring-boot:run
```

검증이 끝난 뒤 환경변수를 제거하면 다시 기존 기본값인 PostgreSQL 17을 사용합니다.

```powershell
Remove-Item Env:KT1B_DB_URL -ErrorAction SilentlyContinue
Remove-Item Env:KT1B_DB_USERNAME -ErrorAction SilentlyContinue
Remove-Item Env:KT1B_DB_PASSWORD -ErrorAction SilentlyContinue
```

## 5. 전환 승인과 롤백 계획

PostgreSQL 18 전환은 다음 gate를 모두 통과한 별도 변경으로 수행합니다.

1. PostgreSQL 17 기준 백업과 핵심 테이블 건수·시퀀스·샘플 결과를 보관
2. PostgreSQL 18 전용 컨테이너와 전용 볼륨에 동일 백업 복원
3. DDL, 제약조건, 함수, 확장, 인덱스, 인코딩·collation 차이 검증
4. 동일 WAR로 로그인, ACL, 등록, 조회, 업로드, 다운로드, 뷰어, 출력, 이력 회귀 시험
5. 성능 및 동시성 시험과 운영 승인 완료
6. 변경 창에서 최종 백업 후 `KT1B_DB_URL`을 PostgreSQL 18 endpoint로 전환

PostgreSQL 17 데이터와 볼륨은 롤백 기간 동안 변경 없이 보존합니다. 전환 후 중대한 오류가 발생하면 애플리케이션을 중지하고 PostgreSQL 17 연결값으로 복귀한 뒤, PostgreSQL 18에서 생성된 데이터의 처리 방안을 별도 승인받습니다. PostgreSQL 17 볼륨을 PostgreSQL 18 컨테이너에 직접 연결하거나 같은 볼륨을 두 버전이 공유하게 해서는 안 됩니다.

## 6. PostgreSQL 18 컨테이너 중지

다음 명령은 PostgreSQL 18 컨테이너만 중지하며 named volume은 보존합니다.

```powershell
docker compose -f docker-compose.postgres18.yml stop
```

`docker compose ... down -v`는 `kt1b_postgres18_data`의 복원 데이터를 삭제하므로 단순 중지 목적으로 사용하지 마십시오.
