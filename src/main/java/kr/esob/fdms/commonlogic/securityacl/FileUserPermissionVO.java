package kr.esob.fdms.commonlogic.securityacl;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileUserPermissionVO {
    private String userCd;
    private String userId;
    private String userNm;
    private String deptNm;
    private String userGradeCd;
    private String userGradeNm;
    private Integer userGradeLevel;
    private String fileGradeCd;
    private String fileGradeNm;
    private Integer fileGradeLevel;
    private String accountActiveYn;
    private String gradeEligibleYn;
    private String globalViewYn;
    private String globalDownloadOriginalYn;
    private String globalPrintYn;
    private String viewYn;
    private String downloadOriginalYn;
    private String printYn;
}
