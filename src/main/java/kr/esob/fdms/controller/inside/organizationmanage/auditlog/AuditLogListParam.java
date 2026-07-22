package kr.esob.fdms.controller.inside.organizationmanage.auditlog;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuditLogListParam extends CommonParam {
    private String userId;
    private String userName;
    private String userNm;
    private String menuNm;
    private String actionType;
    private String accessIp;
    private String resultYn;
    private String startDt;
    private String endDt;
    private String logNo;
}
