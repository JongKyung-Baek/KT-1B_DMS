package kr.esob.fdms.controller.login;

import java.lang.reflect.InvocationTargetException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.esob.fdms.commonlogic.grid.GridInfoService;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import net.sf.json.JSONArray;

@Controller
@RequestMapping("/login")
public class LoginController {

	@Inject
	GridInfoService gridService;

	@Inject
	LoginService service;

	@GetMapping("/loginPage")
	public String home(UserVO userVo, HttpServletRequest request) throws Exception{
		HttpSession session = request.getSession(false);
		if (session != null) {
			Object loginError = session.getAttribute(LoginFailure.LOGIN_ERROR_SESSION_KEY);
			if (loginError != null) {
				request.setAttribute("errorMsg", loginError);
				session.removeAttribute(LoginFailure.LOGIN_ERROR_SESSION_KEY);
			}
		}
		String loginUrl = "404"; //실운영시에 사용
		//		String loginUrl = "/login/login";

		return "/login/login";
	}

	@GetMapping("/duplication")
	public String duplication(HttpServletRequest request, Model model) throws Exception{
		return "/login/duplication";
	}

	@GetMapping("/userChangePopup")
	public String userChangePopup(Model model) {
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridUserChangePopup")));
		return "/login/userChangePopup";
	}

	@PostMapping("/selectList")
	public @ResponseBody GridResultVO selectList(UserChangePopupVO param) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		GridResultVO result = new GridResultVO();
		result.setContents(service.selectList(param));
		result.setRecords(service.selectListCount(param));
		BeanUtils.setProperty(result, "page", BeanUtils.getProperty(param, "page"));
		BeanUtils.setProperty(result, "size", BeanUtils.getProperty(param, "size"));
		return result;
	}
}
