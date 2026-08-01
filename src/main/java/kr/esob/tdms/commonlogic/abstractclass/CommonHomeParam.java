package kr.esob.tdms.commonlogic.abstractclass;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.context.SecurityContextHolder;

import kr.esob.tdms.controller.login.UserVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommonHomeParam {
    @JsonIgnore
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
