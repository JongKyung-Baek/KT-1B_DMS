package kr.esob.tdms.commonlogic.viewer;




import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.esob.tdms.commonlogic.abstractclass.AbstractController;
import kr.esob.tdms.commonlogic.grid.GridResultVO;
import kr.esob.tdms.commonlogic.systemconfig.SystemConfig;
import net.sf.json.JSONArray;

@Controller
@RequestMapping("/common/viewer")
public class CommonViewerController extends AbstractController {

	@Inject
	CommonViewerService service;

	@Inject
	PrintAuditService printAuditService;

	@Inject
	ViewerTicketService viewerTicketService;

	@PostMapping("/openFileListPopup")
	public String openFileListPopup(CommonViewerParam param, Model model) throws JsonProcessingException {
		model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridCommonViewerPopup")));
		model.addAttribute("requestNo", param.getRequestNo());
		model.addAttribute("objectId", param.getObjectId());
		model.addAttribute("objectType", param.getObjectType());
		model.addAttribute("actionCd", param.getActionCd());
		return "viewer/commonViewer";
	}


	@PostMapping("/selectList")
	public @ResponseBody GridResultVO selectList(CommonViewerParam param) throws Exception {
		GridResultVO result = new GridResultVO();
		result.setContents(service.selectList(param));
		return result;
	}

	@PostMapping("/getDestroyStatus")
	public @ResponseBody boolean getDestroyStatus(@RequestBody CommonViewerParam param) throws Exception {
		return service.getDestroyStatus(param);
	}

	@PostMapping("/getDestroyStatus_printHistory")
	public @ResponseBody boolean getDestroyStatus_printHistory(@RequestBody CommonViewerParam param) throws Exception {
		return service.getDestroyStatus_printHistory(param);
	}

	@PostMapping("/openViewer")
	public void openViewer(HttpServletResponse response) {
		response.setStatus(HttpServletResponse.SC_GONE);
	}

	@PostMapping("/getPrintInfo")
	public @ResponseBody CommonViewerVO getPrintInfo(@RequestBody CommonViewerParam param) throws Exception {
		return service.getPrintInfo(param);
	}


	@PostMapping("/getMergePrintInfo")
	public @ResponseBody CommonViewerVO getMergePrintInfo(@RequestBody CommonViewerParam param) throws Exception {
	//public @ResponseBody void getPrintInfo(@RequestBody CommonViewerParam param) throws Exception {
		//System.out.println("====" );
		//System.out.println("param = " +param.getObjectId());
		//System.out.println("====" );
		
		return service.getMergePrintInfo(param);
	}

	@RequestMapping(value = "/print-result", method = RequestMethod.POST)
	public @ResponseBody PrintJobVO printResult(@RequestBody PrintResultParam param) {
		return printAuditService.complete(param);
	}


	@GetMapping("/pdf-cache/{ticketKey:[0-9a-fA-F]{32}}")
	public void pdfCache(@PathVariable String ticketKey, HttpServletResponse response) throws IOException {
		String fileName = viewerTicketService.resolve(ticketKey);
		String basePath = SystemConfig.getSystemConfigValue("ADAP_PDF_PATH");
		if (basePath == null || basePath.trim().isEmpty()) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		File baseDir = new File(basePath.replace("$", "").trim()).getCanonicalFile();
		File file = new File(baseDir, fileName).getCanonicalFile();
		if (!file.toPath().startsWith(baseDir.toPath())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
		if (!file.isFile()) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		response.setContentType("application/pdf");
		response.setContentLengthLong(file.length());
		response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");
		response.setHeader("Cache-Control", "no-store, private");
		response.setHeader("X-Content-Type-Options", "nosniff");
		Files.copy(file.toPath(), response.getOutputStream());
	}

}
