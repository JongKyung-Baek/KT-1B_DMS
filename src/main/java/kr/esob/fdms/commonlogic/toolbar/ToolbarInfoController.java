package kr.esob.fdms.commonlogic.toolbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.combo.ComboInfoVO;

@Controller
@RequestMapping("/formInfo")
public class ToolbarInfoController extends AbstractController {

	@Inject
	ToolbarInfoService service;

}
