package kr.esob.tdms.controller.general.distribution.workflow;

import lombok.Getter;
import lombok.Setter;

/** Immutable-at-save recipient details copied from the partner directory. */
@Getter
@Setter
public class DistributionRequestRecipientSnapshot {
    private Long recipientId;
    private Long requestId;
    private int lineNo;
    private Long partnerCompanyId;
    private Long partnerUserId;
    private String userName;
    private String email;
    private String phone;
    private String representativeYn;
}
