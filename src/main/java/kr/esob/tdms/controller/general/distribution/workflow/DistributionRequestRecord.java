package kr.esob.tdms.controller.general.distribution.workflow;

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
    private Long partnerCompanyId;
    private String partnerCompanyCode;
    private String partnerCompanyName;
    private String approverUserCd;
    private String approverUserId;
    private String approverUserNm;
    private String distributionStartDate;
    private String distributionEndDate;
    private String submittedAt;
    private String decidedAt;
    private String decidedByUserCd;
    private String decidedByUserId;
    private String decidedByUserNm;
    private String decisionComment;
    private String createdAt;
    private String updatedAt;
    private int recipientCount;
    private int documentCount;
    private int itemCount;
    private int remainingDays;
}
