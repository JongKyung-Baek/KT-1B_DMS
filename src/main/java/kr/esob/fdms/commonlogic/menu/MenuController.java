package kr.esob.fdms.commonlogic.menu;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.controller.inside.distribution.acceptance.AcceptanceParam;
import kr.esob.fdms.util.RequestUtil;
import net.sf.json.JSONObject;

@Controller
@RequestMapping("/menu")
public class MenuController extends AbstractController {

	@Inject
	MenuService service;

	@Inject
	RequestUtil requestUtil;

	@RequestMapping("")
	public String home() {
		return "/menu/menuAdd";
	}

	@RequestMapping("/getMenuCombo")
	public @ResponseBody String getMenuCombo(String q){
		System.out.println(q);
		Map<String, Object> rtn = new HashMap<String, Object>();
		rtn.put("results", service.getMenuCombo(q));

		return JSONObject.fromObject(rtn).toString();
	}

	@RequestMapping("/insertMenu")
	public String insertMenu(MenuVO menuVo) {
		service.insertMenu(menuVo);
		return "/menu/menuAdd";
	}

	@RequestMapping("/getMenuNm")
	public @ResponseBody String getMenuNm(@RequestBody MenuVO param){
//		Map<String, String> map = requestUtil.getRequestParameterToMap(request);
		//System.out.println("menuUrl : " + menuUrl);

		Map<String, Object> rtn = new HashMap<String, Object>();
		System.out.println("##########################");System.out.println("##########################");
		rtn.put("results", service.getMenuNm(param.getMenuUrl()));
		System.out.println("##########################");System.out.println("##########################");
		
		return JSONObject.fromObject(rtn).toString();
	}



}
