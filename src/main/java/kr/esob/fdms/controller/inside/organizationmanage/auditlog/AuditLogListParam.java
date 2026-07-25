package kr.esob.fdms.controller.inside.organizationmanage.auditlog;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuditLogListParam extends CommonParam {
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
    private String reasonCd;
    private String resultMessage;
    private String objectType;
    private String objectId;
    private String fileNo;
    private String requestNo;
    private String gradeCd;
    private String targetKeyword;
    private String accessIp;
    private String sessionId;
    private String correlationId;
    private String requestUri;
    private String httpMethod;
    private Integer httpStatus;
    private Long durationMs;
    private String detailJson;
    private String startDt;
    private String endDt;
    private String logNo;
}
