package kr.esob.fdms.commonlogic.viewer;




import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.commonlogic.systemconfig.SystemConfig;
import net.sf.json.JSONArray;

@Controller
@RequestMapping("/common/viewer")
public class CommonViewerController extends AbstractController {

	@Inject
	CommonViewerService service;

	@RequestMapping(value="/openFileListPopup")
	public String openFileListPopup(CommonViewerParam param, Model model) throws JsonProcessingException {
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridCommonViewerPopup")));
		model.addAttribute("requestNo", param.getRequestNo());
		model.addAttribute("objectId", param.getObjectId());
		model.addAttribute("objectType", param.getObjectType());
		model.addAttribute("actionCd", param.getActionCd());
		return "viewer/commonViewer";
	}


	@RequestMapping("/selectList")
	public @ResponseBody GridResultVO selectList(CommonViewerParam param) throws Exception {
		GridResultVO result = new GridResultVO();
		result.setContents(service.selectList(param));
		return result;
	}

	@RequestMapping("/getDestroyStatus")
	public @ResponseBody boolean getDestroyStatus(@RequestBody CommonViewerParam param) throws Exception {
		return service.getDestroyStatus(param);
	}

	@RequestMapping("/getDestroyStatus_printHistory")
	public @ResponseBody boolean getDestroyStatus_printHistory(@RequestBody CommonViewerParam param) throws Exception {
		return service.getDestroyStatus_printHistory(param);
	}

	@RequestMapping("/openViewer")
	public @ResponseBody CommonViewerVO openViewer(@RequestBody CommonViewerParam param) throws Exception {
		return service.getViewFilePath(param);
	}

	@RequestMapping("/getPrintInfo")
	public @ResponseBody CommonViewerVO getPrintInfo(@RequestBody CommonViewerParam param) throws Exception {
		return service.getPrintInfo(param);
	}


	@RequestMapping("/getMergePrintInfo")
	public @ResponseBody CommonViewerVO getMergePrintInfo(@RequestBody CommonViewerParam param) throws Exception {
	//public @ResponseBody void getPrintInfo(@RequestBody CommonViewerParam param) throws Exception {
		//System.out.println("====" );
		//System.out.println("param = " +param.getObjectId());
		//System.out.println("====" );
		
		return service.getMergePrintInfo(param);
	}

	@GetMapping("/collabInstallPage")
	public String collabInstallPage(){
		// 폴더 경로 views 생략
		return "viewer/collabViewInstallPage";
	}

	@RequestMapping(value = "/pdf-cache/{fileName:.+}", method = {RequestMethod.GET, RequestMethod.OPTIONS})
	public void pdfCache(@PathVariable String fileName, HttpServletRequest request, HttpServletResponse response) throws IOException {
		setPdfCacheCorsHeaders(response);
		if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
			response.setStatus(HttpServletResponse.SC_OK);
			return;
		}
		if (fileName.contains("/") || fileName.contains("\\") || !fileName.toLowerCase().endsWith(".pdf")) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		String basePath = SystemConfig.getSystemConfigValue("ADAP_PDF_PATH");
		File file = new File(basePath, fileName);
		if (!file.isFile()) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		response.setContentType("application/pdf");
		response.setContentLengthLong(file.length());
		response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");
		Files.copy(file.toPath(), response.getOutputStream());
	}

	private void setPdfCacheCorsHeaders(HttpServletResponse response) {
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
		response.setHeader("Access-Control-Allow-Headers", "Range, Content-Type");
		response.setHeader("Access-Control-Expose-Headers", "Accept-Ranges, Content-Encoding, Content-Length, Content-Range");
		response.setHeader("Accept-Ranges", "bytes");
	}

}
