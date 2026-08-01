package kr.esob.tdms.controller.general.distribution.workflow;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DistributionRecipientOption {
    private Long partnerCompanyId;
    private Long partnerUserId;
    private String userName;
    private String email;
    private String phone;
    private String representativeYn;
}
