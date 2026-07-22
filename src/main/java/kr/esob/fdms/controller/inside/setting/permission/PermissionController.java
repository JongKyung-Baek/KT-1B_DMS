package kr.esob.fdms.controller.inside.setting.permission;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;

@Controller
@RequestMapping("/inside/setting/permission")
public class PermissionController extends AbstractController {

	@RequestMapping("")
	public ModelAndView home() {
		ModelAndView mav = new ModelAndView("/inside/setting/permission/permissionList");
		return mav;
	}

}
