package kr.esob.fdms.controller.inside.system.role;

import java.util.List;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestParam extends CommonParam {
	private String authSite;
	private String groupCd;
	private String groupNm;

	private String deptNm;		// 검색 시 사용
	private String userNm;		// 검색 시 사용

	private String menuCd;
	private String roleCd;
	private String selectedYn;

	private String saveFlag;
	private List<RequestParam> list;		// assign 저장 시 사용
}
