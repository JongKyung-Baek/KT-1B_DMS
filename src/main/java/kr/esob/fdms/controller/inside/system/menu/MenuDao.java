package kr.esob.fdms.controller.inside.system.menu;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.commonlogic.message.Prop;
import kr.esob.fdms.commonlogic.tree.TreeVO;

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

	/**
	 * 메뉴 TREE 목
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<TreeVO> selectTree() {
		Map<String, Object> param = new HashMap<>();
		param.put("rootText", menuMessage.msg("label.allMenus"));

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
