package kr.esob.fdms.controller.inside.distribution.workflow;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DistributionRequestRecord {
    private Long requestId;
    private String requestNo;
    private String title;
    private String purpose;
    private String status;
    private String requestedByUserCd;
    private String requestedByUserId;
    private String requestedByUserNm;
    private String requestedDeptCd;
    private String requestedDeptNm;
    private String submittedAt;
    private String decidedAt;
    private String decidedByUserCd;
    private String decidedByUserId;
    private String decidedByUserNm;
    private String decisionComment;
    private String createdAt;
    private String updatedAt;
    private int itemCount;
}
