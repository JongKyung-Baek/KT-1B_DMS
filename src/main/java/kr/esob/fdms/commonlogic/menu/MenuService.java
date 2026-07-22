package kr.esob.fdms.commonlogic.menu;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import kr.esob.fdms.commonlogic.combo.SearchComboInfoVO;

@Service
public class MenuService {

	@Inject
	MenuDao dao;

	public List<SearchComboInfoVO> getMenuCombo(String menuNm){
		return dao.getMenuCombo(menuNm);
	}

	public boolean existRole(String menuUrl) {
		return dao.existRole(menuUrl);
	}

	public void insertMenu(MenuVO menuVo) {
		List<MenuVO> menuList = dao.getParentMenuInfo(menuVo);
		int roleCode = 0;
		int menuLevel = 0;
		for(MenuVO menu : menuList) {
			if(menuVo.getParentMenuCd().equals(menu.getMenuCd())) {
				roleCode = Integer.parseInt(menu.getRoleCd().substring(5));
				menuLevel = menu.getMenuLevel() + 1;
			}
		}
		if("D".equals(menuVo.getMenuType()) || "R".equals(menuVo.getMenuType()) || "E".equals(menuVo.getMenuType())) {
			roleCode = roleCode + menuList.size();
		}
		menuVo.setRoleCd("ROLE_" + String.format("%06d", roleCode));
		menuVo.setMenuLevel(menuLevel);
		dao.insertMenu(menuVo);
	}

	/**
	 * URL로 메뉴명을 구한다.
	 * @param menuUrl
	 * @return
	 */
	public String getMenuNm(String menuUrl){
		return dao.getMenuNm(menuUrl);
	}
}
