# TDMS 모바일 접근 차단 정책

TDMS 업무 화면과 API는 휴대폰 및 태블릿에서 사용할 수 없다. 로그인
전·후에 동일한 서버 필터가 적용되며, 알려진 모바일 단말 신호가 확인되면
업무 로직에 도달하기 전에 HTTP 403으로 종료한다.

## 판정 신호와 응답

- `Sec-CH-UA-Mobile: ?1`, `Sec-CH-UA-Platform: Android/iOS`
- Android, iPhone, iPad, iPod, Mobile, Tablet, Silk, Kindle 등 알려진
  모바일/태블릿 `User-Agent` 토큰
- HTML 탐색은 현재 TDMS 오류 페이지 디자인의 한국어·영어·인도네시아어
  안내 화면, API·JSON·XHR 요청은 동일 언어의 403 JSON으로 응답한다.
- 차단 응답은 `no-store`이며 User-Agent와 Client Hints를 `Vary`에 남긴다.

정적 자원, 파비콘, 운영 health 경로와 정확히 일치하는 HMAC 서버 연계
요청만 필터에서 제외한다. 제외는 모바일 화면 사용을 허용하기 위한 것이
아니며, 각 연계 경로의 기존 서명 검증과 Spring Security 정책은 그대로
적용된다.

## 보안 한계

`User-Agent`와 Client Hints는 클라이언트가 임의로 변경할 수 있으므로 이
필터는 모바일 사용을 억제하는 애플리케이션 정책이지 단말 신뢰를 증명하는
보안 경계가 아니다. 특히 데스크톱 모드로 완전히 위장한 iPadOS나 수정된
브라우저 헤더는 서버에서 확실히 구분할 수 없다. 더 강한 통제가 필요하면
폐쇄망 게이트웨이의 관리 단말 인증, 클라이언트 인증서 또는 MDM 기반
접근정책을 함께 적용한다.
