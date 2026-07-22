package kr.esob.fdms.controller.inside.organizationmanage.insideuser;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InsideuserListParam extends CommonParam {
	private String userId;		// 사용자계정
	private String userNm;		// 사용자성명
	private String deptCd;		// 부서코드
	private String useYn;		// 제직구분







}
