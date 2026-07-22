package kr.esob.fdms.commonlogic.abstractclass;

import javax.inject.Inject;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.ui.Model;

import kr.esob.fdms.commonlogic.form.FormInfoService;
import kr.esob.fdms.commonlogic.grid.GridInfoService;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.commonlogic.message.Prop;
import kr.esob.fdms.commonlogic.toolbar.ToolbarInfoService;

public class AbstractController {
	@Inject
	protected GridInfoService gridService;

	@Inject
	protected FormInfoService formService;

	@Inject
	protected ToolbarInfoService toolbarService;

	@Inject
	protected Prop prop;

	/**
	 * 공통 list 함수. 첫번째 인자가 Object인 이유는, parameter가 VO인데, 화면마다 VO가 다 다르기 때문
	 * @param obj
	 * @param service
	 * @return
	 * @throws Exception
	 */
	public GridResultVO commonSelectList(Object obj, CommonService service) throws Exception {
		GridResultVO result = new GridResultVO();
//		result.setGridInfo(dao.selectGridInfoList(obj));
		result.setContents(service.selectList(obj));
		result.setRecords(service.selectListCount(obj));
		BeanUtils.setProperty(result, "page", BeanUtils.getProperty(obj, "page"));
		BeanUtils.setProperty(result, "size", BeanUtils.getProperty(obj, "size"));

		return result;
	}

	public void setHomeParam(Model model, CommonHomeParam param) {
		model.addAttribute("statusCd", param.getStatusCd());
		model.addAttribute("startDt", param.getStartDt());
		model.addAttribute("endDt", param.getEndDt());
		model.addAttribute("requestType", param.getRequestType());
		model.addAttribute("requestUserCd", param.getRequestUserCd());
		model.addAttribute("requestUserNm", param.getRequestUserNm());
		model.addAttribute("approvalUserCd", param.getApprovalUserCd());
		model.addAttribute("approvalUserNm", param.getApprovalUserNm());
		model.addAttribute("destroyRequestUserCd", param.getDestroyRequestUserCd());
		model.addAttribute("destroyRequestUserNm", param.getDestroyRequestUserNm());
		model.addAttribute("destroyStatusCd", param.getDestroyStatusCd());
		model.addAttribute("termLimit", param.getTermLimit());
		model.addAttribute("purchaserUid", param.getPurchaserUid());

	}


}
