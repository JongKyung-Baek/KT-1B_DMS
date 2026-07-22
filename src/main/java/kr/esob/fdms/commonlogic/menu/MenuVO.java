package kr.esob.fdms.commonlogic.menu;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MenuVO {
	private String menuCd;
	private String parentMenuCd;
	private String menuNm;
	private String messageCd;
	private int menuLevel;
	private String menuType;
	private String menuUrl;
	private int sortSeq;
	private String treeType;
	private String delYn;
	private String useYn;
	private String tooltip;
	private String menuDesc;
	private String roleCd;
	private String menuPath;
	private String mainCd;
	private String menuPathCd;
	private String menuIcon;

}
