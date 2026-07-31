package kr.esob.fdms.controller.inside.system.menu;

import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.commonlogic.toolbar.ToolbarInfoDao;
import kr.esob.fdms.commonlogic.tree.TreeVO;
import kr.esob.fdms.commonlogic.value.Constant;
import kr.esob.fdms.controller.inside.system.roleassign.RoleAssignDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author younjh
 *
 */
@Service
public class MenuService {
	public static final String PORTAL_ROOT_CD = "ROOT";

	@Inject
	MenuDao dao;
	@Inject
	RoleAssignDao roleAssignDao;
	@Inject
	ToolbarInfoDao toolbarDao;

	/**
	 * menu tree
	 * @param auth - I:내부사용자/E:외부사용자
	 * @return
	 */
	public List<TreeVO> selectTree() {
		return dao.selectTree();
	}

	/**
	 * GET 메뉴정보
	 * @param param
	 * @return
	 */
	public MenuVO selectMenuInfo(MenuSaveRequestParam param) {
		return dao.selectMenuInfo(param);
	}

	@Transactional
	public ResultVO saveMenu(MenuSaveRequestParam param) {
		ResultVO result = new ResultVO();
		result.setSuccess(false);

		if(param == null || param.getSaveFlag() == null) {
			result.setFailReason("Invalid menu request.");
			return result;
		}

		if("I".equals(param.getSaveFlag())) {
			if(Constant.ROOT_MENU_CD.equals(param.getParentMenuCd())) {
				param.setParentMenuCd(PORTAL_ROOT_CD);
			}
			// 메뉴 추가
			// URL이 변경되면 ROLE_CD가 변경되기 때문에 ROLE_CD를 사용할 수 없음.
			//param.setRoleCd("ROLE_"+Integer.toHexString(param.getMenuUrl().hashCode()).toUpperCase());
			param.setMenuLevel(getMenuLevel(param));
			param.setMenuType(getMenuType(param));
			param.setTreeType(getTreeType(param));

			dao.insertMenu(param);


			// 관리자는 모든 메뉴에 권한이 있음
			insertRelRoleGroup(Constant.GROUP_CD_ADMIN, "ROLE_" + param.getMenuCd());
		}
		else if("U".equals(param.getSaveFlag())) {
			// 메뉴 수정
			MenuVO orgMenu = dao.selectMenuInfo(param);
			param.setRoleCd(orgMenu.getRoleCd());
			param.setMenuType(getMenuType(param));

			dao.updateMenu(param);

			if("Y".equals(param.getUseYn())) {
				insertRelRoleGroup(Constant.GROUP_CD_ADMIN,
						"ROLE_" + param.getMenuCd());
			}
			else {
				roleAssignDao.deleteRelRoleGroup("ROLE_" + param.getMenuCd());
			}

			// 버튼형 메뉴 사용 여부 업데이트
			if("Y".equals(param.getPopupYn()) && param.getMenuCd() != null){
				Map<String, Object> map = new HashMap<>();
				map.put("systemClassGroup", param.getMenuCd());
				map.put("useYn", param.getUseYn());
				toolbarDao.updateToolbar(map);
			}

		}
		else if("D".equals(param.getSaveFlag())) {
			// 메뉴 삭제
			dao.deleteMenu(param.getMenuCd());
			// 해당 메뉴 권한 모두 삭제
			roleAssignDao.deleteRelRoleGroup("ROLE_" + param.getMenuCd());

			if(null != param.getChildren() && param.getChildren().size() > 0) {
				for(String childrenMenu : param.getChildren()) {
					dao.deleteMenu(childrenMenu);
					roleAssignDao.deleteRelRoleGroup("ROLE_" + childrenMenu);
				}
			}

			// 버튼형 메뉴 사용 여부 업데이트
			if("P".equals(param.getMenuType()) && param.getMenuCd() != null){
				Map<String, Object> map = new HashMap<>();
				map.put("systemClassGroup", param.getMenuCd());
				map.put("useYn", "N");
				toolbarDao.updateToolbar(map);
			}
		}
		else {
			result.setFailReason("Invalid menu save mode.");
			return result;
		}

		result.setSuccess(true);
		return result;
	}

	/**
	 * 권한, 메뉴 관계 INSERT
	 * @param groupCd
	 * @param roleCd
	 */
	private void insertRelRoleGroup(String groupCd, String roleCd) {
		roleAssignDao.insertRelRoleGroup(groupCd, roleCd);
	}

	private String getTreeType(MenuSaveRequestParam param) {
		if("1".equals(param.getMenuLevel())) {
			return "root";
		}
		else {
			return "leaf";
		}
	}

	/**
	 * 메뉴유형을 구한다. P:popup, T:1 DEPTH메뉴, M: 메뉴,
	 * @param param
	 * @return
	 */
	private String getMenuType(MenuSaveRequestParam param) {
		if("Y".equals(param.getPopupYn())) {
			return "P";
		}
		else if("1".equals(param.getMenuLevel())) {
			return "T";
		}
		else {
			return "M";
		}
	}

	/**
	 * 부모 메뉴의 ID로 insert될 메뉴의 level을 구한다.
	 * @param param
	 * @return
	 */
	private String getMenuLevel(MenuSaveRequestParam param) {

		if(PORTAL_ROOT_CD.equals(param.getParentMenuCd())
				|| Constant.ROOT_MENU_CD.equals(param.getParentMenuCd())) {
			return "1";
		}

		MenuSaveRequestParam tmp = new MenuSaveRequestParam();
		tmp.setMenuCd(param.getParentMenuCd());
		MenuVO menu = dao.selectMenuInfo(tmp);

		return String.valueOf(Integer.parseInt(menu.getMenuLevel()) + 1);
	}

	@Transactional
	public ResultVO saveMenuSort(SortRequestParam param) {
		ResultVO result = new ResultVO();

		if(param == null || param.getList() == null || param.getList().isEmpty()) {
			result.setSuccess(false);
			result.setFailReason("데이터가 존재하지 않습니다.");
			return result;
		}

		for(int i=0; i<param.getList().size(); i++) {
			SortRequestParam vo = param.getList().get(i);

			if(vo.getId().equals(Constant.ROOT_MENU_CD)) {
				continue;
			}

			vo.setSortSeq(i);
			vo.setMenuType(vo.getMenuLevel().equals("1") ? "T" : "");
			vo.setParent(getParent(vo));
			vo.setTreeType(vo.getMenuLevel().equals("1") ? "root" : "");						// menu level이 1일 경우 root임

			dao.updateMenuSort(vo);
		}

		result.setSuccess(true);

		return result;
	}

	/**
	 * Top-level menus always use the neutral portal root.
	 * @param vo
	 * @return
	 */
	private String getParent(SortRequestParam vo) {
		if(vo.getMenuLevel().equals("1")) {
			return PORTAL_ROOT_CD;
		}

		return vo.getParent();
	}

}
