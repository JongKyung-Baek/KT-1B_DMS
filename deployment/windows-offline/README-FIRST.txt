KT-1B 기술자료관리시스템 Windows 폐쇄망 배포본
================================================

이 패키지는 인터넷과 Docker Registry에 연결하지 않고 Windows Server에서
KT-1B 기술자료관리시스템 데모를 설치하고 실행하기 위한 배포본입니다.

애플리케이션 이미지, Java 17 런타임, PostgreSQL 17.10 이미지, 샘플 DB와
샘플 PDF가 모두 포함되어 있습니다. 대상 서버에 Java, Maven, PostgreSQL을
별도로 설치할 필요가 없습니다.

단, Docker 및 Docker Compose V2 자체는 이 패키지에 포함되지 않습니다.
폐쇄망 반입 전에 Windows Server에 설치하고 Linux 컨테이너를 실행할 수
있는 상태로 준비해야 합니다.

1. 지원 환경
------------

- Windows Server x64
- Linux/AMD64 컨테이너를 실행하는 Docker
- Docker Compose V2
- 최소 5GB 이상의 여유 디스크 권장
- 최소 4GB 이상의 사용 가능 메모리 권장

Windows 컨테이너 모드에서는 실행할 수 없습니다.
원격 Docker daemon은 패키지의 로컬 경로를 마운트할 수 없으므로 지원하지
않습니다. Docker daemon은 이 BAT를 실행하는 Windows Server에 있어야 합니다.

2. 설치 전 확인
--------------

1) ZIP 파일을 서버의 로컬 NTFS 폴더에 완전히 압축 해제합니다.
2) ZIP 안에서 BAT를 직접 실행하지 마십시오.
3) Docker daemon을 시작합니다.
4) 실행 계정이 Docker daemon에 접근할 수 있어야 합니다.
5) 먼저 VERIFY_PACKAGE.BAT를 실행하면 모든 파일의 SHA-256을 확인할 수 있습니다.

Docker 접근 시 Access Denied가 표시되면 관리자 권한으로 실행하거나 실행 계정을
docker-users 그룹에 추가한 뒤 Windows에 다시 로그인하십시오.

3. 원스톱 설치 및 실행
----------------------

INSTALL_AND_RUN.BAT를 실행합니다.

설치기는 다음 작업을 자동으로 수행합니다.

- Docker, Compose V2, Linux 컨테이너 모드 확인
- 배포 파일 SHA-256 검증
- 동봉된 애플리케이션/PostgreSQL 이미지 로드
- 이미지 ID 및 Linux/AMD64 플랫폼 검증
- 샘플 PostgreSQL DB 복원
- 애플리케이션 실행 및 상태 확인
- 로그인 페이지 열기

설치 과정에서는 docker pull, docker build 또는 외부 Registry 접속을 사용하지
않습니다.

기본 주소:

  http://127.0.0.1:3508/login/loginPage

기본 관리자 계정:

  아이디: admin
  비밀번호: esob!

기본 포트가 사용 중이면 다른 포트를 지정할 수 있습니다.

  INSTALL_AND_RUN.BAT 3510

브라우저를 자동으로 열지 않으려면 다음과 같이 실행합니다.

  INSTALL_AND_RUN.BAT --no-open
  INSTALL_AND_RUN.BAT 3510 --no-open

자동화 실행에서 실패 시 일시정지를 하지 않으려면 --no-pause를 추가합니다.

  INSTALL_AND_RUN.BAT 3510 --no-open --no-pause

4. 운영 BAT
-----------

- START_DEMO.BAT
  저장된 포트로 시스템을 시작합니다. 최초 실행에도 사용할 수 있습니다.

- STOP_DEMO.BAT
  컨테이너를 종료합니다. DB 데이터는 보존됩니다.

- STATUS_DEMO.BAT
  컨테이너 상태와 로그인 페이지 응답을 확인합니다.

- VIEW_LOGS.BAT
  앱과 DB 컨테이너 로그를 표시합니다. 파일 로그는 logs 폴더에 있습니다.

- RESET_DEMO_DATA.BAT
  명시적으로 RESET을 입력한 경우에만 이 패키지의 DB 볼륨을 삭제하고
  동봉된 샘플 DB로 복원합니다.

- VERIFY_PACKAGE.BAT
  checksums.sha256 기준으로 전체 배포 파일을 다시 검증합니다.

5. 데이터 위치
--------------

- PostgreSQL 데이터:
  Docker 볼륨 kt1b-dms-offline-db-data

- 샘플 및 등록 파일:
  storage 폴더

- 애플리케이션 파일 로그:
  logs 폴더

STOP_DEMO.BAT 실행 또는 서버 재시작 후에도 DB 데이터는 유지됩니다.
초기 샘플 상태로 되돌릴 때만 RESET_DEMO_DATA.BAT를 사용하십시오.

6. 샘플 데이터
--------------

- 관리자 1명과 샘플 사용자 5명
- 샘플 부서 6개
- 기술자료 16건
- 각 기술자료에 주파일 1개와 보조파일 1개

샘플 DB는 외부 시스템 주소, 메일 설정, 실운영 파일 경로와 개인 실행 이력을
제거한 데모 전용 데이터입니다. 원본 덤프 파일은 포함되어 있지 않습니다.

7. 네트워크 및 보안 범위
------------------------

- 웹 포트는 기본적으로 서버 자체의 127.0.0.1에만 열립니다.
- PostgreSQL 포트는 호스트에 공개하지 않습니다.
- 메일과 외부 인터페이스 스케줄러는 비활성화되어 있습니다.
- 다른 PC에서 접속하려면 별도의 내부망 바인딩, 방화벽 및 HTTPS 설계가
  필요합니다. 본 패키지는 기본적으로 서버 로컬 데모용입니다.

8. 문제 해결
------------

- Docker CLI was not found
  폐쇄망 반입 전에 Docker와 Compose V2를 설치해야 합니다.

- Docker is not running or this account cannot access it
  Docker daemon 상태와 실행 계정의 docker-users 권한을 확인합니다.

- Docker is not using Linux containers
  Docker daemon을 Linux 컨테이너 모드로 전환합니다.

- Checksum mismatch
  파일이 손상되었으므로 설치하지 말고 원본 ZIP을 다시 반입합니다.

- Loaded image IDs do not match
  images 폴더 또는 runtime\offline.env가 손상된 상태입니다.
  VERIFY_PACKAGE.BAT를 실행하고 원본 ZIP을 다시 확인합니다.

- Port 3508 is already in use
  해당 프로그램을 종료하거나 INSTALL_AND_RUN.BAT 3510처럼 다른 포트를
  지정합니다.

- 애플리케이션이 준비되지 않음
  STATUS_DEMO.BAT와 VIEW_LOGS.BAT를 실행하고 logs 폴더도 확인합니다.

배포 이미지의 태그, 이미지 ID, 플랫폼, 내장 WAR SHA-256은
IMAGE-MANIFEST.txt에서 확인할 수 있습니다.
