package kr.esob.fdms.commonlogic.combo;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.controller.inside.authorization.AuthorizationService;
import net.sf.json.JSONObject;

@Controller
@RequestMapping("/combo")
public class ComboController extends AbstractController {

	@Inject
	ComboService service;

	@RequestMapping("/selectComboList")
	public @ResponseBody List<ComboInfoVO> selectComboList(@RequestBody ComboInfoVO param){
		return service.selectComboList(param);
	}
	
	/**
	 * 업체 리스트 콤보
	 * @param vo
	 * @return
	 */
	@RequestMapping("/selectProdList")
	public @ResponseBody String selectProdList(SearchComboParamVO vo) {
		vo.setQueryId("sql.Combo.selectProdList");
		return JSONObject.fromObject(service.selectSearchComboList(vo)).toString();
	}


}