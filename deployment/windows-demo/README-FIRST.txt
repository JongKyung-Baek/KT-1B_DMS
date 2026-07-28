KT-1B DMS Windows Demo
======================

이 배포본은 Windows Server에서 Docker의 Linux 컨테이너 모드로 실행하는
로컬 데모 패키지입니다. 호스트에 Java, Maven, PostgreSQL을 별도로 설치할
필요가 없습니다.

1. 사전 준비
------------

- Windows Server에 Docker와 Docker Compose V2를 설치합니다.
- Docker를 Linux 컨테이너 모드로 실행합니다.
- 최초 실행 시 공식 Java/PostgreSQL 이미지를 내려받으므로 인터넷 연결이
  필요합니다.
- 배포 ZIP을 원하는 로컬 폴더에 완전히 압축 해제합니다.

2. 원스톱 설치 및 실행
----------------------

INSTALL_AND_RUN.BAT를 더블클릭합니다.

스크립트가 다음 작업을 자동으로 수행합니다.

- Docker, Docker Compose, Linux 컨테이너 모드 확인
- 로컬 포트 사용 여부 확인
- PostgreSQL 17 데이터베이스 생성 및 데모 데이터 복원
- Java 17 애플리케이션 이미지 생성 및 실행
- 로그인 화면 준비 상태 확인
- 기본 브라우저 열기

기본 주소:

  http://127.0.0.1:3508/login/loginPage

기본 관리자 계정:

  아이디: admin
  비밀번호: esob!

최초 샘플 DB에는 관리자 1명, 샘플 사용자 5명, 부서 6개, 기술자료
16건이 들어 있습니다. 각 기술자료에는 주파일 1개와 보조파일 1개가
연결되어 있습니다.

기본 포트 3508이 사용 중이면 명령 프롬프트에서 다른 포트를 지정할 수
있습니다.

  INSTALL_AND_RUN.BAT 3510

브라우저를 자동으로 열지 않으려면 다음과 같이 실행합니다.

  INSTALL_AND_RUN.BAT --no-open
  INSTALL_AND_RUN.BAT 3510 --no-open

한 번 지정한 포트는 runtime\.demo-port에 저장되어 다음 실행에 다시
사용됩니다.

3. 운영 스크립트
----------------

- START_DEMO.BAT
  저장된 포트로 데모를 시작합니다. 최초 실행에도 사용할 수 있습니다.

- STOP_DEMO.BAT
  컨테이너를 종료합니다. 데이터베이스 볼륨은 보존됩니다.

- STATUS_DEMO.BAT
  컨테이너 상태와 로그인 화면 응답을 확인합니다.

- VIEW_LOGS.BAT
  애플리케이션과 데이터베이스 로그를 실시간으로 표시합니다.
  종료하려면 Ctrl+C를 누릅니다.

- RESET_DEMO_DATA.BAT
  데모 DB를 최초 샘플 상태로 되돌립니다. 확인란에 정확히 RESET을
  입력해야 실행됩니다. 다른 Docker 데이터나 볼륨은 삭제하지 않습니다.

4. 데이터와 로그
----------------

- PostgreSQL 데이터:
  Docker 볼륨 kt1b-dms-demo-db-data

- 문서 파일:
  storage 폴더

- 애플리케이션 로그:
  logs 폴더

STOP_DEMO.BAT 실행 또는 서버 재부팅 후 다시 시작해도 DB 데이터는
유지됩니다. DB 변경 내용을 모두 버리고 최초 상태로 되돌릴 때만
RESET_DEMO_DATA.BAT를 사용하십시오.

5. 문제 해결
------------

- "Docker is not running"
  Docker를 시작한 후 준비가 완료될 때까지 기다렸다가 다시 실행합니다.

- "Docker is not using Linux containers"
  Docker를 Linux 컨테이너 모드로 전환합니다.

- "Port 3508 is already in use"
  해당 프로그램을 종료하거나 INSTALL_AND_RUN.BAT 3510처럼 다른 포트를
  지정합니다.

- 180초 안에 시작되지 않음
  VIEW_LOGS.BAT를 실행하여 앱/DB 로그를 확인합니다. 최초 이미지 다운로드
  속도에 따라 더 오래 걸린 경우 START_DEMO.BAT를 다시 실행해도 됩니다.

6. 보안 범위
------------

- 웹 포트는 서버 자체의 127.0.0.1에만 연결됩니다.
- PostgreSQL 포트는 호스트에 공개하지 않습니다.
- 이 구성과 내장 계정은 로컬 데모 전용이며 운영 배포에 사용하면 안 됩니다.
- 다른 PC에서 접속하려면 별도의 네트워크/방화벽/HTTPS 설계가 필요합니다.
