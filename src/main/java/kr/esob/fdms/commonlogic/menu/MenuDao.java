package kr.esob.fdms.commonlogic.menu;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.commonlogic.combo.SearchComboInfoVO;
import kr.esob.fdms.controller.login.UserVO;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class MenuDao extends AbstractDao{
	public List<MenuVO> getMenuList(){
		return list("sql.Menu.getMenuList");
	}

	public List<MenuVO> getMenuTopList(UserVO param){
		return list("sql.Menu.getMenuTopList", param);
	}

	public List<MenuVO> getMenuSubList(UserVO param){
		List<MenuVO> list = list("sql.Menu.getMenuSubList", param);

		for(MenuVO vo : list) {
			if(null != vo.getMenuUrl()) {
				vo.setMenuUrl(vo.getMenuUrl().replaceAll("\\*", ""));
			}
		}

		return list;
	}

	public List<SearchComboInfoVO> getMenuCombo(String menuNm){
		return list("sql.Menu.getMenuCombo", menuNm);
	}

	public void insertMenu(MenuVO menuVo) {
		insert("sql.Menu.insertMenu", menuVo);
	}

	public List<MenuVO> getParentMenuInfo(MenuVO menuVo) {
		return list("sql.Menu.getParentMenuInfo", menuVo);
	}

	/**
	 * sRL로 메뉴명을 구한다.
	 * @param menuUrl
	 * @return
	 */
	public String getMenuNm(String menuUrl){
		return (String) obj("sql.Menu.getMenuNm", menuUrl);
	}

	/**
	 * 권한 있는지 체크.
	 * @param menuUrl
	 * @return 해당유저에게 메뉴 권한이 있으면 true, 없으면 false
	 */
	public boolean existRole(String menuUrl) {
		UserVO user = (UserVO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Map<String, Object> param = new HashMap<>();
		param.put("url", menuUrl);
		param.put("roleGroup", user.getRoleGroup());

		return (Integer) obj("sql.Menu.getUrlCount", menuUrl) > 0;
	}
}
