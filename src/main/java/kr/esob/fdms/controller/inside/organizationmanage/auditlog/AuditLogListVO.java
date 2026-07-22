package kr.esob.fdms.controller.inside.organizationmanage.auditlog;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuditLogListVO {
    private String logNo;
    private String userId;
    private String userName;
    private String userNm;
    private String actionType;
    private String accessIp;
    private String date;
    private String logDt;
}
