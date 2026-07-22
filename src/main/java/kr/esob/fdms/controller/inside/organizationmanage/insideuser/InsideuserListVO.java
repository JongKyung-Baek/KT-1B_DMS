package kr.esob.fdms.controller.inside.organizationmanage.insideuser;

import kr.esob.fdms.util.DateUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InsideuserListVO {
	private String userId;			    // 사용자계정

	private String userCd;
	private String userNm;  		    // 사용자성명
	private String deptNm; 			    // 부서
	private String positionNm;		    // 직위
	private String lastLoginDt ;      	// 최종로그인
	private String accountLockYn;	// 계정잠금여부
	private String loginCount;	   	    // 비밀번호틀린횟수
	private String roleGroup;		    // 사용자권한

	public String getLastLoginDt() {
		return DateUtil.getYmd(lastLoginDt);
	}

}
