package kr.esob.fdms.commonlogic.updown.runtime;

public enum DownloadRuntimeStatus {
    QUEUED,         // 요청 등록
    DOWNLOADING,    // REST API 파일 수신 중
    SENT_TO_WS,     // 웹소켓 전송 완료
    COMPLETED,      // 결과코드 00
    FAILED          // 결과코드 00 이외
}
