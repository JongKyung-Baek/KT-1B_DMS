package kr.esob.tdms.controller.general.setting.permission;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import kr.esob.tdms.commonlogic.abstractclass.AbstractController;

@Controller
@RequestMapping("/general/setting/permission")
public class PermissionController extends AbstractController {

	@RequestMapping("")
	public ModelAndView home() {
		ModelAndView mav = new ModelAndView("/general/setting/permission/permissionList");
		return mav;
	}

}
