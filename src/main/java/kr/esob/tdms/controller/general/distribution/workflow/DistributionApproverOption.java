package kr.esob.tdms.controller.general.distribution.workflow;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DistributionApproverOption {
    private String approverUserCd;
    private String userId;
    private String userName;
    private String deptCd;
    private String deptNm;
}
