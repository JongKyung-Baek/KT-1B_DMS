package kr.esob.tdms.commonlogic.excel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.esob.tdms.commonlogic.abstractclass.AbstractController;
import kr.esob.tdms.commonlogic.abstractclass.CommonParam;
import kr.esob.tdms.util.RequestUtil;

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

	@PostMapping("/createExcel")
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

	@PostMapping("/createExcelFromLocalGrid")
	public @ResponseBody CreateExcelVO createExcelFromLocalGrid(HttpServletRequest request, LocalParam param) throws Exception {
		
//		return null;
		return service.createExcelFromLocalGrid(request, param);
	}
	
	@PostMapping("/createExcelDuanzongPdm")
	public @ResponseBody CreateExcelVO createExcelDuanzongPdm(HttpServletRequest request, CommonParam param) throws Exception {
		return service.createExcelDuanzongPdm(request, param);
	}

	@GetMapping("/download/{fileName:.+}")
	public ResponseEntity<Resource> download(@PathVariable String fileName) throws IOException {
		Path generatedFile = service.resolveGeneratedExcelFile(fileName);
		if (generatedFile == null || !Files.isRegularFile(generatedFile)) {
			return ResponseEntity.notFound().build();
		}

		Resource resource = new FileSystemResource(generatedFile.toFile());
		return ResponseEntity.ok()
				.cacheControl(CacheControl.noStore())
				.contentType(MediaType.parseMediaType(
						"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.contentLength(Files.size(generatedFile))
				.header(HttpHeaders.CONTENT_DISPOSITION,
						"attachment; filename=\"" + fileName + "\"")
				.body(resource);
	}
}
