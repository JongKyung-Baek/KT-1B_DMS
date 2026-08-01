package kr.esob.tdms.commonlogic.securityacl;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccessAuditEventVO {
    private Long eventId;
    private String occurredAt;
    private String eventType;
    private String actionType;
    private String resultCd;
    private String reasonCd;
    private String resultMessage;
    private String actorUserCd;
    private String actorUserId;
    private String actorUserNm;
    private String objectType;
    private String objectId;
    private String fileNo;
    private String requestNo;
    private String gradeCd;
    private String menuCd;
    private String menuNm;
    private String menuUrl;
    private String actionNm;
    private String requestUri;
    private String httpMethod;
    private Integer httpStatus;
    private Long durationMs;
    private String clientIp;
    private String sessionId;
    private String correlationId;
    private String detailJson;
}
