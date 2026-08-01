package kr.esob.tdms.commonlogic.securityacl;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileSecurityLabelVO {
    private String objectType;
    private String objectId;
    private String fileNo;
    private String objectNo;
    private String objectNm;
    private String orgFileNm;
    private String gradeCd;
    private String gradeNm;
    private Integer gradeLevel;
    private String labelReason;
    private String assignedYn;
}
