package kr.esob.fdms.commonlogic.viewer;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrintJobVO {
    private String printJobId;
    private String statusCd;
    private String actorUserCd;
    private String objectType;
    private String objectId;
    private String fileNo;
    private String requestNo;
    private String countAppliedYn;
}
