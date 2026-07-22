package kr.esob.fdms.controller.outside.duanzong.docs;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.abstractclass.CommonHomeParam;
import kr.esob.fdms.commonlogic.combo.ComboDao;
import kr.esob.fdms.commonlogic.combo.ComboService;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.controller.bbs.notice.BbsNoticeAddParam;
import net.sf.json.JSONArray;

@Controller
@RequestMapping("/outside/duanzong/fdms")
public class DocsController extends AbstractController {
	
	@Inject
	DocsService service;
	
	@Inject
	ComboService comboService;
	
	@Inject
	ComboDao comboDao;
	
	@RequestMapping(value="/")
	public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
		setHomeParam(model, param);
		
		model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formOutsideDuanzongDocs")));
		model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarOutsideDuanzongDocs")));
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridOutsideDuanzongDocsList")));
		return "/outside/duanzong/fdms/fdmsList";
	}
	
	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(DocsListParam param) throws Exception {
		GridResultVO result = commonSelectList(param, service);
		return result;
	}
	
	/*
	 * 등록 팝업
	 */
	@RequestMapping(value="/fdmsAddPopup")
	public String addPopup(BbsNoticeAddParam param, Model model) throws JsonProcessingException {
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridAddDuanzongDocs")));
		model.addAttribute("vendor", param.getSessionUser()); 
		model.addAttribute("defensePurs", service.getDefensePurs());
		model.addAttribute("businessAreaCd", comboDao.selectComboListByCd("businessAreaCd"));
		return "/outside/duanzong/fdms/fdmsAddPopup";
	}
	
	/*
	 * 삽입
	 * */
	@RequestMapping(value="/duanzongDocs")
	public @ResponseBody ResultVO duanzongDocs(MultipartHttpServletRequest request) throws Exception {
		return service.insertDocsDuanzong(request);
	}
	
	/*
	 * 상세보기 팝업
	 */
	@RequestMapping(value="/fdmsStatusPopup")
	public String docsStatusPopup(@RequestParam String managementNo, Model model) throws JsonProcessingException {
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridStatusDuanzongDocs")));
		model.addAttribute("duanzongDocsInfo", service.getDuanzongDocs(managementNo));
		return "/outside/duanzong/fdms/fdmsStatusPopup";
	}
	
	@RequestMapping(value="/fdmsFileDownload")
	public void docsFileDownload(DocsStatusPopupVO param, HttpServletResponse response) throws Exception {
		service.docsFileDownload(service.getDuanzongDocs(param.getManagementNo()), response);
	}
}