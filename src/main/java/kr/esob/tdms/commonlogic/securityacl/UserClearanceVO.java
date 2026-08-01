package kr.esob.tdms.commonlogic.securityacl;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserClearanceVO {
    private String userCd;
    private String userId;
    private String userNm;
    private String deptCd;
    private String deptNm;
    private String companyNm;
    private String gradeCd;
    private String gradeNm;
    private Integer gradeLevel;
    private String validFrom;
    private String validTo;
    private String grantReason;
    private String listYn;
    private String detailYn;
    private String viewYn;
    private String downloadOriginalYn;
    private String printYn;
    private String manageAclYn;
    private Map<String, String> permissions = new LinkedHashMap<String, String>();

    public void buildPermissions() {
        permissions.clear();
        permissions.put("LIST", yn(listYn));
        permissions.put("DETAIL", yn(detailYn));
        permissions.put("VIEW", yn(viewYn));
        permissions.put("DOWNLOAD_ORIGINAL", yn(downloadOriginalYn));
        permissions.put("PRINT", yn(printYn));
        permissions.put("MANAGE_ACL", yn(manageAclYn));
    }

    private String yn(String value) {
        return "Y".equalsIgnoreCase(value) ? "Y" : "N";
    }
}
