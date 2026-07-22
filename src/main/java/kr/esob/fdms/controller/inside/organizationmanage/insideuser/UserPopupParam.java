package kr.esob.fdms.controller.inside.organizationmanage.insideuser;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserPopupParam extends CommonParam {
    // 사용자 등록/수정
    private String userCd;

    private String companyCd;
    private String userId;  // 계정
    private String userPwd; // 비번
    private String userNm; // 사용자명( 닉네임 )
    private String email;

    private String businessAreaCd; // 사업장 코드

    private String authSite; // 내부/외부
    private String saveFlag;

    private String deptCd; // 부서
    private String deptNm;

    private String positionCd; // 직급
    private String positionNm;

    private String roleGroupCd; // 사용자 권한
    private String roleGroupNm;

    private String authApprovalYn;  // 배포 권한
    private int authApprovalLevel;  // 배포 권한

    private String useYn;
    private String delYn;
    private String protectYn;
    private String lockYn;

    private String crYn;

    private boolean newUser;

}
