package kr.esob.fdms.controller.login;

import java.lang.reflect.InvocationTargetException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.esob.fdms.commonlogic.grid.GridInfoService;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.commonlogic.value.SessionValue;
import kr.esob.fdms.config.Property;
import net.sf.json.JSONArray;

@Controller
@RequestMapping("/login")
public class LoginController {

	@Inject
	GridInfoService gridService;

	@Inject
	LoginService service;

	@RequestMapping(value="/loginPage")
	public String home(UserVO userVo, HttpServletRequest request) throws Exception{
		String loginUrl = "404"; //실운영시에 사용
		//		String loginUrl = "/login/login";

		Property property = new Property();
		String location = property.getProperty("location");

		if("I".equals(request.getParameter("url_type"))) {
			loginUrl = "/login/login";
		}else if("E".equals(request.getParameter("url_type"))) {
			loginUrl = "/login/login_out";
		}
		else if(request.getParameter("url_type") == null || "".equals(request.getParameter("url_type"))) {
			loginUrl = "/login/login";
		}
		else if("I".equals(location)) {
			loginUrl = "/login/login";
		}
		else if("E".equals(location)) {
			loginUrl = "/login/login_out";
		}
		return loginUrl;
	}

	@RequestMapping(value="/duplication")
	public String duplication(HttpServletRequest request, Model model) throws Exception{
		return "/login/duplication";
	}

	@RequestMapping(value="/userChangePopup")
	public String userChangePopup(Model model) {
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridUserChangePopup")));
		return "/login/userChangePopup";
	}

	@RequestMapping(value="/selectList")
	public @ResponseBody GridResultVO selectList(UserChangePopupVO param) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		GridResultVO result = new GridResultVO();
		result.setContents(service.selectList(param));
		result.setRecords(service.selectListCount(param));
		BeanUtils.setProperty(result, "page", BeanUtils.getProperty(param, "page"));
		BeanUtils.setProperty(result, "size", BeanUtils.getProperty(param, "size"));
		return result;
	}
	
	@RequestMapping(value="/kess")
	public String installKess(Model model) {
		// model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridUserChangePopup")));
		return "/login/kess_install";
	}
}
