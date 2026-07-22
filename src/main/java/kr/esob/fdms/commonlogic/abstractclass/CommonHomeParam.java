package kr.esob.fdms.commonlogic.abstractclass;

import org.springframework.security.core.context.SecurityContextHolder;

import kr.esob.fdms.controller.login.UserVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommonHomeParam {
    private UserVO sessionUser;

    public CommonHomeParam() {
        sessionUser = (UserVO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private String statusCd;
    private String startDt;
    private String requestType;
    private String endDt;
    private String requestUserCd;
    private String requestUserNm;
    private String destroyRequestUserCd;
    private String destroyRequestUserNm;
    private String destroyStatusCd;
    private String approvalUserCd;
    private String approvalUserNm;
    private String termLimit;
    private String purchaserUid;
}