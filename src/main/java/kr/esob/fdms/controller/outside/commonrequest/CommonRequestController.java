package kr.esob.fdms.controller.outside.commonrequest;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.combo.ComboInfoVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.controller.inside.authorization.AuthorizationDao;
import kr.esob.fdms.controller.login.UserVO;
import net.sf.json.JSONObject;

@Controller
@RequestMapping("/outside/commonRequest")
public class CommonRequestController extends AbstractController {

	@Inject
	CommonRequestService service;

	@Inject
	AuthorizationDao authorizationDao;


	@RequestMapping(value="/selectSimilarInspectionInfo", method=RequestMethod.POST)
	public @ResponseBody List<ObjectInfoVO> selectSimilarInspectionInfo(@RequestBody ObjectInfoVO param) {
		return service.selectSimilarInspectionInfo(param);
	}

	@RequestMapping(value="/selectInspectionInfo", method=RequestMethod.POST)
	public @ResponseBody List<ObjectInfoVO> selectInspectionInfo(@RequestBody ObjectInfoVO param) {
		return service.selectInspectionInfo(param);
	}

	@RequestMapping(value="/selectUserInfo")
	public @ResponseBody String selectUserInfo(@RequestBody UserVO userVo) {
		return JSONObject.fromObject(service.selectUserInfo(userVo)).toString();
	}
	
	@RequestMapping(value="/selectUserEmail")
	public @ResponseBody UserVO selectUserEmail(@RequestBody UserVO userVo) {
		return service.selectUserEmail(userVo);
	}

	@RequestMapping("/selectAcceptanceUser")
	public @ResponseBody List<ComboInfoVO> selectAcceptanceUser(@RequestBody RequestParam param){
		return authorizationDao.comboList("sql.Authorization.selectPurchaserCombo", param);
	}

	@RequestMapping(value="/vendorAccept")
	public @ResponseBody ResultVO vendorAccept(@RequestBody RequestParam param) throws Exception {
		return service.updateVendorAccept(param);
	}
}
