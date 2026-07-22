package kr.esob.fdms.controller.inside.system.roleassign;

import java.util.List;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestParam extends CommonParam {
	private String authSite;
	private String groupCd;

	private String menuCd;
	private String roleCd;
	private String selectedYn;
	private List<RequestParam> list;		// assign 저장 시 사용
}
