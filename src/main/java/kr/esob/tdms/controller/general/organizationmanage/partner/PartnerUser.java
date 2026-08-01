package kr.esob.tdms.controller.general.organizationmanage.partner;

import lombok.Getter;
import lombok.Setter;

/** A non-authenticating recipient/contact belonging to one partner company. */
@Getter
@Setter
public class PartnerUser {
    private Long partnerUserId;
    private Long partnerCompanyId;
    private String userName;
    private String email;
    private String phone;
    private String positionName;
    private String representativeYn;
    private String useYn;
}
