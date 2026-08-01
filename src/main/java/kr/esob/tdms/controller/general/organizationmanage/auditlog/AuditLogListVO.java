package kr.esob.tdms.controller.general.organizationmanage.auditlog;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuditLogListVO {
    private Long eventId;
    private String logNo;
    private String occurredAt;
    private String logDt;
    private String date;
    private String sourceType;
    private String eventType;
    private String userCd;
    private String userId;
    private String userName;
    private String userNm;
    private String menuCd;
    private String menuNm;
    private String menuUrl;
    private String actionType;
    private String actionNm;
    private String resultCd;
    private String objectType;
    private String objectId;
    private String fileNo;
    private String requestNo;
    private String gradeCd;
    private String targetSummary;
    private String reasonCd;
    private String resultMessage;
    private String accessIp;
    private String requestUri;
    private String httpMethod;
    private Integer httpStatus;
    private Long durationMs;
    private String correlationId;
    private String detailJson;
}
