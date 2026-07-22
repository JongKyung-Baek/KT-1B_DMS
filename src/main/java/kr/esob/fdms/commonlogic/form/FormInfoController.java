package kr.esob.fdms.commonlogic.form;

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
import kr.esob.fdms.commonlogic.combo.ComboDao;
import kr.esob.fdms.commonlogic.combo.ComboInfoVO;
import kr.esob.fdms.commonlogic.combo.ComboParamVO;
import kr.esob.fdms.commonlogic.combo.ComboService;

@Controller
@RequestMapping("/formInfo")
public class FormInfoController extends AbstractController {

	@Inject
	FormInfoService service;
	@Inject
	ComboDao comboDao;

	@RequestMapping(value = "/getSelectList")
	public @ResponseBody List<ComboInfoVO> getSelectList(HttpServletRequest request) {
		String queryId = request.getParameter("queryId");
		String comboCd = request.getParameter("comboCd");

		ComboParamVO comboParamVO = new ComboParamVO();
		comboParamVO.setQueryId(queryId);
		comboParamVO.setComboCd(comboCd);

		List<ComboInfoVO> systemClassList = new ArrayList<ComboInfoVO>();

		// queryId가 존재한다면 특정 queryId를 찾아가서 조회하고,
		// 존재하지 않는다면 DOCS_COMBO 테이블에서 조회한다.
		if (!"".equals(queryId)) {
			systemClassList = service.selectQueryList(comboParamVO);
		} else { // systemClass에서 조회(combo)
			systemClassList = comboDao.selectComboList(comboParamVO);

		}
		return systemClassList;
	}

	@RequestMapping(value = "/getCombo")
	public @ResponseBody List<ComboInfoVO> getRadioCombo(HttpServletRequest request) {
		String comboCd = request.getParameter("comboCd");
		List<ComboInfoVO> systemClassList = new ArrayList<ComboInfoVO>();
		ComboInfoVO comboInfo = new ComboInfoVO();
		comboInfo.setComboCd(comboCd);
		systemClassList = comboDao.selectComboList(comboInfo);
		return systemClassList;
	}

}
