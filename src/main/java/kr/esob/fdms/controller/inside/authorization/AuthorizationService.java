package kr.esob.fdms.controller.inside.authorization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import kr.esob.fdms.commonlogic.combo.ComboInfoVO;
import kr.esob.fdms.commonlogic.combo.SearchComboInfoVO;
import kr.esob.fdms.commonlogic.combo.SearchComboParamVO;
import kr.esob.fdms.commonlogic.message.Prop;
import kr.esob.fdms.commonlogic.value.Constant;
import kr.esob.fdms.controller.inside.unregisted.request.DistributionRequestPopupParam;
import kr.esob.fdms.controller.login.UserVO;

@Service
public class AuthorizationService {

	@Inject
	AuthorizationDao dao;
	@Inject
	Prop prop;

	public Map<String, Object> selectSearchCombo(SearchComboParamVO vo) {
		int limit = 50;
		int page = ( (null == vo.getPage() || 0 == vo.getPage()) ? 1 : vo.getPage() );
		vo.setPosStart(limit * page - limit);
		vo.setCount(limit);

		Map<String, Object> result = new HashMap<String, Object>();

		List<SearchComboInfoVO> list = dao.selectSearchCombo(vo);
		List<SearchComboInfoVO> list2 = new ArrayList<SearchComboInfoVO>();

		SearchComboInfoVO plzSelect = new SearchComboInfoVO();

		if(null == vo.getDefaultText() || "".equals(vo.getDefaultText())) {
			plzSelect.setId("");
			plzSelect.setText("선택하세요");
			list2.add(plzSelect);
		}
		else if(Constant.DEFAULT_TEXT_NOTHING.equals(vo.getDefaultText())) {
			// nothing이면 아무것도 넣지 않는다.
		}
		else {
			plzSelect.setId("");
			plzSelect.setText(prop.msg(vo.getDefaultText()));
			list2.add(plzSelect);
		}


		list2.addAll(list);

		Map<String, Object> pagination = new HashMap<String, Object>();
		pagination.put("more", false);

		result.put("results", list2);
		result.put("pagination", pagination);

		return result;
	}

	public UserVO getPurchaseInfo(UserVO user) {
		return dao.getPurchaseInfo(user);
	}

	public List<ComboInfoVO> getTeamLeader(Object param) {
		return dao.getTeamLeader(param);
	}

	public List<ComboInfoVO> getTeamLeaderList(CommonParam param) {
		return dao.getTeamLeaderList(param);
	}

	public List<ComboInfoVO> getTeamLeaderListById(CommonParam param) {
		return dao.getTeamLeaderListById(param);
	}

	public String getDefenseTeamLeaderUid() {
		return dao.getDefenseTeamLeaderUid();
	}

	public List<ComboInfoVO> selectDefenseTeamLeaderList() {
		return dao.selectDefenseTeamLeaderList();
	}

//	public List<ComboInfoVO> getTeamLeader(DistributionRequestPopupParam param) {
//		return dao.getTeamLeader(param);
//	}

	public List<ComboInfoVO> selectDeptComboListByBusinessArea(SearchComboParamVO vo){
		return dao.selectDeptComboListByBusinessArea(vo);
	}

	public List<SearchComboInfoVO> selectInsideUserComboByDept(SearchComboParamVO vo){
		return dao.selectInsideUserComboByDept(vo);
	}

	public List<ComboInfoVO> selectProductProtectUserList(String businessAreaCd) {
		return dao.selectProductProtectUserList(businessAreaCd);
	}

	public List<ComboInfoVO> selectDevelopmentProtectUserList() {
		return dao.selectDevelopmentProtectUserList();
	}
	public List<ComboInfoVO> selectSecurityUserList(){
		return dao.selectSecurityUserList();
	}

	public List<String> selectSecurityUserByBusinessArea(Object obj){
		return dao.selectSecurityUserByBusinessArea(obj);
	}

	public UserVO selectPurchaserTeamLeader(Object obj) {
		return dao.selectPurchaserTeamLeader(obj);
	}

	public List<ComboInfoVO> selectPassTargetCombo(Object obj){
		return dao.selectPassTargetCombo(obj);
	}

	public List<ComboInfoVO> selectTeamUserList(CommonParam param) {
		return dao.selecTeamUserList(param);
	}

	public List<String> checkProtectAuth(ProtectObjectVO vo) {
		String [] objIds = vo.getObjectId().split(",");
		List<String> result = new ArrayList<String>();

		for(int i=0; i<objIds.length; i++) {
			vo.setObjectId(objIds[i]);

			// 권한이 없다면 result에 추가
			if(!dao.checkProtectAuth(vo)) {
				result.add(objIds[i]);
			}
		}

		return result;
	}

	public ComboInfoVO selectDept(Object obj) {
		return dao.selectDept(obj);
	}

}
