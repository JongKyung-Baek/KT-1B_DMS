package kr.esob.fdms.commonlogic.grid;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;


@Controller
@RequestMapping("/gridInfo")
public class GridInfoController extends AbstractController{

	@Inject
	GridInfoService service;

	@RequestMapping(value="/selectGridColumn")
	public @ResponseBody List<GridInfoVO> selectGridColumn(HttpServletRequest request) {
		List<GridInfoVO> result = service.selectGridInfo(request.getParameter("gridId"));
		Map<String, Object> rtn = new HashMap<String, Object>();
		rtn.put("list", result);
		return result;
	}
}
