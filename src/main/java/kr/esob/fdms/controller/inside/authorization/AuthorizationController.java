package kr.esob.fdms.controller.inside.authorization;


import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.combo.ComboInfoVO;
import kr.esob.fdms.commonlogic.combo.SearchComboParamVO;
import kr.esob.fdms.controller.login.UserVO;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/inside/authorization")
public class AuthorizationController extends AbstractController {

	@Inject
	AuthorizationService service;


	/**
	 * 구매 팀장 대결자 콤보
	 * @param vo
	 * @return
	 */
	@RequestMapping("/selectInstaedCombo")
	public @ResponseBody String selectInstaedCombo(SearchComboParamVO vo) {
		vo.setQueryId("sql.Authorization.selectInstaedCombo");
		return JSONObject.fromObject(service.selectSearchCombo(vo)).toString();
	}

	/**
	 * 구매팀
	 * @param vo
	 * @return
	 */
	@RequestMapping(value= "/selectSearchComboList")
	public @ResponseBody Map<String, Object> selectSearchComboList(SearchComboParamVO vo) {
		vo.setQueryId("sql.OrganizationmanageInsideuser.selectUserCombo");
		return service.selectSearchCombo(vo);
	}

	@RequestMapping(value= "/selectUserCdComboList")
	public @ResponseBody Map<String, Object> selectUserCdComboList(SearchComboParamVO vo) {
		vo.setQueryId("sql.OrganizationmanageInsideuser.selectUserComboUserCd");
		return service.selectSearchCombo(vo);
	}

	@RequestMapping(value="/selectApprovalUser")
	public @ResponseBody String selectApprovalUser(SearchComboParamVO vo) {
		UserVO sessionUser = (UserVO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		vo.setCompanyCd(sessionUser.getCompanyCd());
		vo.setQueryId("sql.Authorization.selectApprovalUserCombo");
		return JSONObject.fromObject(service.selectSearchCombo(vo)).toString();
	}


	@RequestMapping("/selectOutsideUserCombo")
	public @ResponseBody String selectOutsideUserCombo(SearchComboParamVO vo) {
		UserVO sessionUser = (UserVO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		vo.setCompanyCd(sessionUser.getCompanyCd());
		vo.setQueryId("sql.Authorization.selectOutsideUserCombo");
		return JSONObject.fromObject(service.selectSearchCombo(vo)).toString();
	}

	@RequestMapping("/selectInsideUserCombo")
	public @ResponseBody String selectInsideUserCombo(SearchComboParamVO vo) {
		UserVO sessionUser = (UserVO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		vo.setCompanyCd(sessionUser.getCompanyCd());
		vo.setQueryId("sql.Authorization.selectInsideUserCombo");
		return JSONObject.fromObject(service.selectSearchCombo(vo)).toString();
	}

//	@RequestMapping(value= "/selectSearchComboList", produces="application/json;charset=UTF-8")
//	public @ResponseBody String selectSearchComboList(SearchComboParamVO vo) {
//		vo.setQueryId("sql.OrganizationmanageInsideuser.selectUserCombo");
//		return JSONObject.fromObject(comboService.selectSearchComboList(vo)).toString();
//	}


	/**
	 * 업체 리스트 콤보
	 * @param vo
	 * @return
	 */
	@RequestMapping("/selectCompanyCombo")
	public @ResponseBody String selectCompanyCombo(SearchComboParamVO vo) {
		vo.setQueryId("sql.Authorization.selectCompanyCombo");
		return JSONObject.fromObject(service.selectSearchCombo(vo)).toString();
	}

	/**
	 * 업체 사용자 콤보
	 * @param vo
	 * @return
	 */
	@RequestMapping("/selectCompanyUserCombo")
	public @ResponseBody String selectCompanyUserCombo(SearchComboParamVO vo) {
		vo.setQueryId("sql.Authorization.selectCompanyUserCombo");
		return JSONObject.fromObject(service.selectSearchCombo(vo)).toString();
	}

	/**
	 * 업체 사용자 콤보 - 배포용
	 * @param vo
	 * @return
	 */
	@RequestMapping("/selectDistributionCompanyUserCombo")
	public @ResponseBody String selectDistributionCompanyUserCombo(SearchComboParamVO vo) {
		vo.setQueryId("sql.Authorization.selectDistributionCompanyUserCombo");
		return JSONObject.fromObject(service.selectSearchCombo(vo)).toString();
	}


	/**
	 * 현재 로그인한 사용자의 구매팀원 리스트 콤보
	 * @param vo
	 * @return
	 */
	@RequestMapping("/selectPurchaseTeamCombo")
	public @ResponseBody String selectPurchaseTeamCombo(SearchComboParamVO vo) {
		vo.setQueryId("sql.Authorization.selectPurchaseTeamCombo");
		UserVO user = (UserVO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		vo.setDeptCd(user.getDeptCd());
		return JSONObject.fromObject(service.selectSearchCombo(vo)).toString();
	}

	/**
	 * 부서조회 콤보
	 * @param vo
	 * @return
	 */
	@RequestMapping("/selectSearchDeptList")
	public @ResponseBody String selectSearchDeptList(SearchComboParamVO vo) {
		vo.setQueryId("sql.Authorization.selectSearchDeptList");
		UserVO user = (UserVO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		vo.setDeptCd(user.getDeptCd());
		return JSONObject.fromObject(service.selectSearchCombo(vo)).toString();
	}



	/**
	 * 업체 구매 담당자 조회
	 * @param vo
	 * @return
	 */
	@RequestMapping("/getPurchaseInfo")
	public @ResponseBody Map<String, String> getPurchaseInfo(HttpServletRequest req) {
		UserVO user = new UserVO();
		Map<String, String> rtn = new HashMap<String, String>();
		user.setCompanyCd(req.getParameter("companyCd"));
		// businessAreaCd가 안넘어오는 이유는 이후에 파악해야함 23.06.21 by koo
		if(req.getParameter("businessAreaCd").equals("")){
			user.setBusinessAreaCd("1310");
		}
		else {
			user.setBusinessAreaCd(req.getParameter("businessAreaCd"));
		}
		user = service.getPurchaseInfo(user);
		if(user == null)user = new UserVO();
		rtn.put("userCd", user.getUserCd());
		rtn.put("userNm", user.getUserNm());
		System.out.println("userNm -> " + rtn.get("userNm"));
		return rtn;
	}

	@RequestMapping(value="/selectDeptComboListByBusinessArea")
	public @ResponseBody List<ComboInfoVO> selectDeptComboListByBusinessArea(@RequestBody SearchComboParamVO vo){
		return service.selectDeptComboListByBusinessArea(vo);
	}

	@RequestMapping(value="/selectInsideUserComboByDept")
	public @ResponseBody String selectInsideUserComboByDept(@RequestBody SearchComboParamVO vo){
		vo.setQueryId("sql.Authorization.selectInsideUserComboByDept");
		return JSONObject.fromObject(service.selectSearchCombo(vo)).toString();
	}

	@RequestMapping("/selectSearchProductUserComboList")
	public @ResponseBody String selectSearchProductUserComboList(SearchComboParamVO vo) {
		vo.setQueryId("sql.Authorization.selectSearchProductUserComboList");
		return JSONObject.fromObject(service.selectSearchCombo(vo)).toString();
	}

	@RequestMapping("/selectPurchaserNotSelectedCombo")
	public @ResponseBody String selectPurchaserNotSelectedCombo(SearchComboParamVO vo) {
		vo.setQueryId("sql.Authorization.selectPurchaserNotSelectedCombo");
		return JSONObject.fromObject(service.selectSearchCombo(vo)).toString();
	}

	/**
	 * 방산기술요청권한이 있는지 체크
	 * @param vo
	 * @return
	 */
	@RequestMapping("/checkProtectAuth")
	public @ResponseBody String checkProtectAuth(@RequestBody ProtectObjectVO vo) {
		return JSONArray.fromObject(service.checkProtectAuth(vo)).toString();
	}

	/**
	 * 외부사용자 - 방산기술요청권한이 있는지 여부
	 * @param vo
	 * @return
	 */
	@RequestMapping("/checkOutsideProtectAuth")
	public @ResponseBody String checkOutsideProtectAuth(@RequestBody ProtectObjectVO vo) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("protectYn", vo.getSessionUser().getProtectYn());

		return JSONObject.fromObject(map).toString();
	}

	/**
	 * 외부사용자 - 이용가능한 업체인지 확인
	 * @param vo
	 * @return
	 */
	@RequestMapping("/getOutsideCompanyAuthYn")
	public @ResponseBody String getOutsideCompanyAuthYn(@RequestBody ProtectObjectVO vo) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("delYn", vo.getSessionUser().getCompanyDelYn());

		/*
		 * 오픈때문에 급하게 수정(Viewer 또는 파일 다운로드시 "거래중단 업체" 메시지 출력됨)
		 * 차후 검토 필요 2020.01.06 yongjulee
		 * */
		if(null == map.get("delYn")){
			map.put("delYn", "N");
		}

		return JSONObject.fromObject(map).toString();
	}

	@RequestMapping("/selectUserCombo")
	public @ResponseBody String selectUserCombo(SearchComboParamVO vo) {
		vo.setQueryId("sql.Authorization.selectUserCombo");
		return JSONObject.fromObject(service.selectSearchCombo(vo)).toString();
	}

	/**
	 * 모든 업체담당자 - ID 기준
	 * @param vo
	 * @return
	 */
	@RequestMapping("/selectAllOutsideUserIdCombo")
	public @ResponseBody String selectAllOutsideUserIdCombo(SearchComboParamVO vo) {
		vo.setQueryId("sql.Authorization.selectAllOutsideUserIdCombo");
		return JSONObject.fromObject(service.selectSearchCombo(vo)).toString();
	}

	/**
	 * 모든 내부사용자 -ID기준
	 * @param vo
	 * @return
	 */
	@RequestMapping("/selectAllInsideUserIdCombo")
	public @ResponseBody String selectAllInsideUserIdCombo(SearchComboParamVO vo) {
		vo.setQueryId("sql.Authorization.selectAllInsideUserIdCombo");
		return JSONObject.fromObject(service.selectSearchCombo(vo)).toString();
	}

	@RequestMapping("/selectProdSearchCombo")
	public @ResponseBody String selectProdSearchCombo(SearchComboParamVO vo) {
		vo.setQueryId("sql.docsProd.selectProdSearchCombo");
		return JSONObject.fromObject(service.selectSearchCombo(vo)).toString();
	}

	/**
	 * '직' 사용자 목록
	 * @param vo
	 * @return
	 */
	@RequestMapping(value="/selectJikUserComboList")
	public @ResponseBody String selectJikUserComboList(SearchComboParamVO vo){
		System.out.println("selectJikUserComboList");

		UserVO user = (UserVO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		vo.setQueryId("sql.Authorization.selectJikUserComboList");
		// 내 사업장의 사용자만 표시
		vo.setBusinessAreaCd(user.getBusinessAreaCd());
		return JSONObject.fromObject(service.selectSearchCombo(vo)).toString();
	}

	@RequestMapping(value="/selectDept")
	public @ResponseBody String selectDeptNm(@RequestBody SearchComboParamVO vo){
		return JSONObject.fromObject(service.selectDept(vo)).toString();
	}

	/**
	 * 전체 부서 목록
	 * @param vo
	 * @return
	 */
	@RequestMapping("/selectDeptCombo")
	public @ResponseBody String selectDeptCombo(SearchComboParamVO vo) {
		vo.setQueryId("sql.Authorization.selectDeptCombo");
		return JSONObject.fromObject(service.selectSearchCombo(vo)).toString();
	}

	/**
	 * 전체 부서 목록
	 * @param vo
	 * @return
	 */
	@RequestMapping("/selectDistributionPointCombo")
	public @ResponseBody String selectDistributionPointCombo(SearchComboParamVO vo) {
		vo.setQueryId("sql.Authorization.selectDistributionPointCombo");
		return JSONObject.fromObject(service.selectSearchCombo(vo)).toString();
	}


	/**
	 * 부서 코드 조회
	 * @param vo
	 * @return
	 */
	@RequestMapping("/selectDeptCdCombo")
	public @ResponseBody String selectDeptCdCombo(SearchComboParamVO vo) {
		vo.setQueryId("sql.Authorization.selectDeptCdCombo");
		return JSONObject.fromObject(service.selectSearchCombo(vo)).toString();
	}

	/**
	 * 전체 직급 목록
	 * @param vo
	 * @return
	 */
	@RequestMapping("/selectPositionCombo")
	public @ResponseBody String selectPositionCombo(SearchComboParamVO vo) {
		vo.setQueryId("sql.Authorization.selectPositionCombo");
		return JSONObject.fromObject(service.selectSearchCombo(vo)).toString();
	}

	/**
	 * 전체 권한 목록
	 * @param vo
	 * @return
	 */
	@RequestMapping("/selectRoleGroupCombo")
	public @ResponseBody String selectRoleGroupCombo(SearchComboParamVO vo) {
		vo.setQueryId("sql.Authorization.selectRoleGroupCombo");
		return JSONObject.fromObject(service.selectSearchCombo(vo)).toString();
	}

	/**
	 * 배포 권한 목록
	 * @param vo
	 * @return
	 */
	@RequestMapping("/selectDistributionAuthCombo")
	public @ResponseBody String selectDistributionAuthCombo(SearchComboParamVO vo) {
		vo.setQueryId("sql.Authorization.selectDistributionAuthCombo");
//		vo.setDefaultText(Constant.DEFAULT_TEXT_NOTHING);
		return JSONObject.fromObject(service.selectSearchCombo(vo)).toString();
	}
	/**
	 * 내부 사용자 전체 목록
	 * @param vo
	 * @return
	 */
	@RequestMapping("/selectAllInsideUser")
	public @ResponseBody String selectAllInsideUser(SearchComboParamVO vo) {
		vo.setQueryId("sql.Authorization.selectAllInsideUserCdCombo");
		return JSONObject.fromObject(service.selectSearchCombo(vo)).toString();
	}
}
