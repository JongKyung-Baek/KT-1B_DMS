package kr.esob.fdms.commonlogic.viewer;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrintJobItemVO {
    private String printJobId;
    private Integer itemSeq;
    private String objectType;
    private String objectId;
    private String fileNo;
    private String requestNo;
    private String requestType;
    private String countRequiredYn;
}
