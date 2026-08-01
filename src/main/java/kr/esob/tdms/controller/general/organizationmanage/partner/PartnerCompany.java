package kr.esob.tdms.controller.general.organizationmanage.partner;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * A distribution partner. Partner contacts deliberately are not TDMS login
 * accounts and therefore have no password, role group or clearance fields.
 */
@Getter
@Setter
public class PartnerCompany {
    private Long partnerCompanyId;
    private String companyCode;
    private String companyName;
    private String businessNo;
    private String contactEmail;
    private String contactPhone;
    private String address;
    private String useYn;
    private String representativeUserName;
    private Integer userCount;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private List<PartnerUser> users = new ArrayList<PartnerUser>();
}
