package kr.esob.tdms.controller.general.system.menu;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Repository;

import kr.esob.tdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.tdms.commonlogic.message.Prop;
import kr.esob.tdms.commonlogic.tree.TreeVO;
import kr.esob.tdms.commonlogic.value.SessionValue;

/**
 * 메뉴관리
 * @author younjh
 *
 */
@Repository
public class MenuDao extends AbstractDao {
	private String prefix = "sql.Menu.";

	@Inject
	Prop menuMessage;
	@Inject
	SessionValue sessionValue;

	/**
	 * 메뉴 TREE 목
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<TreeVO> selectTree() {
		return selectTree(false);
	}

	@SuppressWarnings("unchecked")
	public List<TreeVO> selectAdminTree() {
		return selectTree(true);
	}

	@SuppressWarnings("unchecked")
	private List<TreeVO> selectTree(boolean includeInactive) {
		Map<String, Object> param = new HashMap<>();
		param.put("rootText", menuMessage.msg("label.allMenus"));
		param.put("sessionLang", sessionValue.getSessionLang());
		param.put("includeInactive", includeInactive);

		return list(prefix + "selectTree", param);
	}

	/**
	 * 메뉴정보
	 * @param param
	 * @return
	 */
	public MenuVO selectMenuInfo(MenuSaveRequestParam param) {
		return (MenuVO) obj(prefix + "selectMenuInfo", param);
	}

	/**
	 * 메뉴 등록
	 * @param param
	 */
	public void insertMenu(MenuSaveRequestParam param) {
		insert(prefix + "insertMenu", param);
	}

	/**
	 * 메뉴 update
	 * @param param
	 */
	public void updateMenu(MenuSaveRequestParam param) {
		update(prefix + "updateMenu", param);
	}

	/**
	 * 메뉴삭제
	 * @param menuCd
	 */
	public void deleteMenu(String menuCd) {
		Map<String, String> param = new HashMap<>();
		param.put("menuCd", menuCd);

		update(prefix + "deleteMenu", param);
	}

	/**
	 * 메뉴 순서 정렬
	 * @param param
	 */
	public void updateMenuSort(SortRequestParam param) {
		update(prefix + "updateMenuSort", param);
	}
}
