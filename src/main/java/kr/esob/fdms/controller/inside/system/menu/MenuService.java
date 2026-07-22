package kr.esob.fdms.controller.inside.system.menu;

import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.commonlogic.toolbar.ToolbarInfoDao;
import kr.esob.fdms.commonlogic.tree.TreeVO;
import kr.esob.fdms.commonlogic.value.Constant;
import kr.esob.fdms.controller.inside.system.roleassign.RoleAssignDao;
import org.springframework.stereotype.Service;

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
	public List<TreeVO> selectTree(String auth) {
		return dao.selectTree(auth);
	}

	/**
	 * GET 메뉴정보
	 * @param param
	 * @return
	 */
	public MenuVO selectMenuInfo(MenuSaveRequestParam param) {
		return dao.selectMenuInfo(param);
	}

	public ResultVO saveMenu(MenuSaveRequestParam param) {
		ResultVO result = new ResultVO();

		if("I".equals(param.getSaveFlag())) {
			// 메뉴 추가(내/외부 공통)
			// URL이 변경되면 ROLE_CD가 변경되기 때문에 ROLE_CD를 사용할 수 없음.
			//param.setRoleCd("ROLE_"+Integer.toHexString(param.getMenuUrl().hashCode()).toUpperCase());
			param.setMenuLevel(getMenuLevel(param));
			param.setMenuType(getMenuType(param));
			param.setTreeType(getTreeType(param));

			dao.insertMenu(param);

			if(param.getAuthSite().equals("E") || param.getAuthSite().equals("B")) {
				// 외부사용자 메뉴의 경우 rel_role_group에 insert해야 함.
				insertRelRoleGroup(Constant.GROUP_CD_OUTSIDE, "ROLE_" + param.getMenuCd());
			}

			// 관리자는 모든 메뉴에 권한이 있음
			insertRelRoleGroup(Constant.GROUP_CD_ADMIN, "ROLE_" + param.getMenuCd());
		}
		else if("U".equals(param.getSaveFlag())) {
			// 메뉴 수정
			MenuVO orgMenu = dao.selectMenuInfo(param);
			param.setRoleCd(orgMenu.getRoleCd());
			param.setMenuType(getMenuType(param));

			dao.updateMenu(param);

			// 버튼형 메뉴 사용 여부 업데이트
			if(param.getPopupYn().equals("Y") && param.getMenuCd() != null){
				Map<String, Object> map = new HashMap<>();
				map.put("systemClassGroup", param.getMenuCd());
				map.put("useYn", param.getUseYn());
				toolbarDao.updateToolbar(map);
			}

			if(param.getAuthSite().equals("E") || param.getAuthSite().equals("B")) {
				// 외부사용자의 경우 사용 사용하지 않는 메뉴는 권한에서 삭제
				//groupCd, roleCd;

				if("N".equals(param.getUseYn())) {
					roleAssignDao.deleteRelRoleGroup(Constant.GROUP_CD_OUTSIDE, "ROLE_" + param.getMenuCd());
				}
				else {
					insertRelRoleGroup(Constant.GROUP_CD_OUTSIDE, "ROLE_" + param.getMenuCd());
				}

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
			if(param.getMenuType().equals("P") && param.getMenuCd() != null){
				Map<String, Object> map = new HashMap<>();
				map.put("systemClassGroup", param.getMenuCd());
				map.put("useYn", "N");
				toolbarDao.updateToolbar(map);
			}
		}

		return result;
	}

	/**
	 * 권한, 메뉴 관계 INSERT
	 * @param groupCd
	 * @param roleCd
	 */
	private void insertRelRoleGroup(String groupCd, String roleCd) {
		RequestParam tmp = new RequestParam();
		tmp.setGroupCd(groupCd);
		tmp.setRoleCd(roleCd);

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

		if(Constant.ROOT_MENU_CD.equals(param.getParentMenuCd())) {
			return "1";
		}

		MenuSaveRequestParam tmp = new MenuSaveRequestParam();
		tmp.setMenuCd(param.getParentMenuCd());
		MenuVO menu = dao.selectMenuInfo(tmp);

		return String.valueOf(Integer.parseInt(menu.getMenuLevel()) + 1);
	}

	public ResultVO saveMenuSort(SortRequestParam param) {
		ResultVO result = new ResultVO();

		if(0 == param.getList().size()) {
			result.setSuccess(false);
			result.setFailReason("데이터가 존재하지 않습니다.");
		}

		for(int i=0; i<param.getList().size(); i++) {
			SortRequestParam vo = param.getList().get(i);

			if(vo.getId().equals(Constant.ROOT_MENU_CD)) {
				continue;
			}

			vo.setSortSeq(i);
			vo.setMenuType(vo.getMenuLevel().equals("1") ? "T" : "");
			vo.setParent(getParent(vo));	// menu level이 1일 경우 내/외부 문자가 parent에 들어감
			vo.setTreeType(vo.getMenuLevel().equals("1") ? "root" : "");						// menu level이 1일 경우 root임

			dao.updateMenuSort(vo);
		}

		result.setSuccess(true);

		return result;
	}

	/**
	 * parent코드를 구한다. level이 1일 경우 authsite와 같은 값이 들어가야 함.
	 * @param vo
	 * @return
	 */
	private String getParent(SortRequestParam vo) {
		if(vo.getMenuLevel().equals("1")) {
			MenuSaveRequestParam param = new MenuSaveRequestParam();
			param.setMenuCd(vo.getId());
			MenuVO menu = dao.selectMenuInfo(param);

			return menu.getAuthSite();
		}

		return vo.getParent();
	}



























//	public ResultVO saveOutsideMenu(RequestParam param) {
//		ResultVO result = new ResultVO();
//
//		if(0 == param.getList().size()) {
//			result.setSuccess(false);
//			result.setFailReason("데이터가 존재하지 않습니다.");
//		}
//
//		// groupCd
//		RequestParam tmp = new RequestParam();
//		tmp.setGroupCd(Constant.GROUP_CD_OUTSIDE);
//		//dao.deleteRelRoleGroup(tmp);
//		insertMainPage(tmp);
//
//		for(int i=0; i<param.getList().size(); i++) {
//			RequestParam vo = param.getList().get(i);
//
//			// 외부사용자용 메뉴는 1000부터
//			vo.setSortSeq(1000 + i);
//			vo.setGroupCd(Constant.GROUP_CD_OUTSIDE);
//
//			//dao.updateMenu(vo);
//
//			if("Y".equals(vo.getUseYn())) {
//				dao.insertRelRoleGroup(vo);
//			}
//		}
//
//		result.setSuccess(true);
//
//		return result;
//	}

	public ResultVO saveInsideMenu(RequestParam param) {
		ResultVO result = new ResultVO();

		if(0 == param.getList().size()) {
			result.setSuccess(false);
			result.setFailReason("데이터가 존재하지 않습니다.");
		}

		for(int i=0; i<param.getList().size(); i++) {
			RequestParam vo = param.getList().get(i);

			vo.setSortSeq(i);

			//dao.updateMenu(vo);
		}

		result.setSuccess(true);

		return result;
	}

//	/**
//	 * insert 메인 페이지
//	 * @param tmp
//	 */
//	public void insertMainPage(RequestParam tmp) {
//		List<TreeVO> list = dao.selectMainMenu();
//
//		for(TreeVO vo : list) {
//			tmp.setRoleCd(vo.getRoleCd());
//
//			dao.insertRelRoleGroup(tmp);
//		}
//	}
}
