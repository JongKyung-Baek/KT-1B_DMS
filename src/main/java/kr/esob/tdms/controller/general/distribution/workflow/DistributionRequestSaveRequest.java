package kr.esob.tdms.controller.general.distribution.workflow;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DistributionRequestSaveRequest {
    private String title;
    private String purpose;
    private Long partnerCompanyId;
    private List<Long> recipientUserIds;
    private String approverUserCd;
    private String distributionStartDate;
    private String distributionEndDate;
    private List<DistributionRequestDocumentRef> documents;
}
