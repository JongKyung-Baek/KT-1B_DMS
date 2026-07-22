package kr.esob.fdms.controller.inside.pdm.deployapprove;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/PdmDeployApprove")
public class PdmDeployApproveController {

	@Inject
	PdmDeployApproveService service;

	@RequestMapping("/selectList")
	public String selecList(PdmDeployApproveParam param) {
		return "";
	}

}
