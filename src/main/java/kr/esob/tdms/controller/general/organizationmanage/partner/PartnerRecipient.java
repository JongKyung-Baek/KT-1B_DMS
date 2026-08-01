package kr.esob.tdms.controller.general.organizationmanage.partner;

import lombok.Getter;
import lombok.Setter;

/** Stable read model consumed by the technical-data distribution workflow. */
@Getter
@Setter
public class PartnerRecipient {
    private Long partnerCompanyId;
    private String companyCode;
    private String companyName;
    private Long partnerUserId;
    private String userName;
    private String email;
    private String phone;
    private String positionName;
    private String representativeYn;
}
