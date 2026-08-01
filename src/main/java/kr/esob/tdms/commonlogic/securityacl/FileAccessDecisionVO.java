package kr.esob.tdms.commonlogic.securityacl;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileAccessDecisionVO {
    private boolean allowed;
    private String reasonCd;
    private String actorUserCd;
    private String actionCd;
    private String objectType;
    private String objectId;
    private String fileNo;
    private String userGradeCd;
    private String fileGradeCd;
    private Integer userGradeLevel;
    private Integer fileGradeLevel;
    private String actionAllowedYn;
    private String documentPermissionAllowedYn;
}
