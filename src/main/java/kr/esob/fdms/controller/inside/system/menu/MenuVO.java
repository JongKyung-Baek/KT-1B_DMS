package kr.esob.fdms.controller.inside.system.menu;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MenuVO {
	private String menuCd;				// 메뉴코드
	private String menuNm;				// 메뉴명
	private String parentMenuCd;		// 부모메뉴코드
	private String parentMenuNm;		// 부모메뉴명
	private String menuUrl;				// 메뉴 주소
	private String useYn;				// 사용여부
	private String popupYn;				// popup여부(menuType이 P이면 Y, 아니면 N
	private String roleCd;				// 권한코드
	private String authSite;			// 내외부
	private String menuLevel;			// 메뉴 레벨
	private String menuIcon;
}
