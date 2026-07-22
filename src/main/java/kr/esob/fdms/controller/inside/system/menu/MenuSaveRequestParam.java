package kr.esob.fdms.controller.inside.system.menu;

import java.util.List;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MenuSaveRequestParam extends CommonParam {
	private String menuCd;
	private String parentMenuCd;
	private String menuNm;
	private String menuLevel;
	private String menuType;
	private String menuUrl;
	private Integer sortSeq;
	private String treeType;
	private String useYn;
	private String popupYn;
	private String roleCd;
	private String authSite;
	private String saveFlag;
	private String menuIcon;
	private List<String> children;		// 삭제할 때 사용. 선택한 메뉴의 하위메뉴들

	public String getUseYn() {
		if(null == useYn) { return "N"; }
		if("".equals(useYn)) { return "N"; }

		return useYn;
	}
}
