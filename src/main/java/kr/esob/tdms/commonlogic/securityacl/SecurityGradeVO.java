package kr.esob.tdms.commonlogic.securityacl;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SecurityGradeVO {
    private String gradeCd;
    private String gradeNm;
    private Integer gradeLevel;
    private String description;
    private String defaultYn;
    private String useYn;
}
