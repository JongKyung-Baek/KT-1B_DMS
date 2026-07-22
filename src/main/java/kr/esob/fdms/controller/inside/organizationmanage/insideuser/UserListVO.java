package kr.esob.fdms.controller.inside.organizationmanage.insideuser;

import kr.esob.fdms.util.DateUtil;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class UserListVO {
    private String userCd;
    private String userNm;
    private String email;
    private String userPwd;
    private String userId;
    private String lastLoginDt;
    private String pwdUpdateDt;
    private String loginCount;
    private String lockYn;
    private String companyCd;
    private String companyNm;
    private String protectYn;
    private String crYn;
    private String distributionYn;
    private String businessAreaCd;

    private String deptCd;
    private String deptNm;

    private String positionCd;
    private String positionNm;

    private String roleGroupCd;
    private String roleGroupNm;

    private String distributionAuthCd;
    private String distributionAuthNm;


    public String getLastLoginDt() {
        return DateUtil.getYmd(lastLoginDt);
    }

    public String getPwdUpdateDt() {
        return DateUtil.getYmd(pwdUpdateDt);
    }
}
