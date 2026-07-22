package kr.esob.fdms.commonlogic.excel;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.abstractclass.CommonParam;
import kr.esob.fdms.util.RequestUtil;

/**
 * 엑셀 생성
 * @author younjh
 *
 */
@Controller
@RequestMapping("/common/createExcel")
public class CreateExcelController extends AbstractController {

	@Inject
	CreateExcelService service;

	RequestUtil requestUtil;

	@RequestMapping("/createExcel")
	public @ResponseBody CreateExcelVO CreateExcel(HttpServletRequest request) throws Exception {
//		if(!"".equals(request.getParameter("dynamicType"))) {
//			Map<String, String> paramMap = requestUtil.getRequestParameterToMap(request);
//			List<String> dynamicList = null;
//			List<String> columnNameList = new ArrayList<>();
//			String dynamicType = request.getParameter("dynamicType");
//
//			if("date".equals(dynamicType)) {
//				dynamicList = DateUtil.getDateList(paramMap);
//
//				for(String dt : dynamicList) {
//					columnNameList.add(DateUtil.getAddDelimiter(dt, "-"));
//				}
//			}
//			else if("code".equals(dynamicType)) {
//				List<ComboVO> comboList = commonDao.getComboList(request.getParameter("dynamicColumnType"));
//				dynamicList = ComboUtil.toValueList(comboList);
//				columnNameList = ComboUtil.toLabelList(comboList);
//			}
//
//			return service.createExcelDynamicColumn(request, dynamicList, columnNameList);
//		}
//		else {
			return service.createExcel(request);
//		}
	}

	@RequestMapping("/createExcelFromLocalGrid")
	public @ResponseBody CreateExcelVO createExcelFromLocalGrid(HttpServletRequest request, LocalParam param) throws Exception {
		System.out.println(param.getGridId());
//		return null;
		return service.createExcelFromLocalGrid(request, param);
	}
	
	@RequestMapping("/createExcelDuanzongPdm")
	public @ResponseBody CreateExcelVO createExcelDuanzongPdm(HttpServletRequest request, CommonParam param) throws Exception {
		return service.createExcelDuanzongPdm(request, param);
	}
}
