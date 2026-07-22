package kr.esob.fdms.commonlogic.updown;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartRequest;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import net.sf.json.JSONObject;


@Controller
@RequestMapping("/common/updown")
public class CommonUpdownController extends AbstractController {

	@Inject
	CommonUpdownService service;

	@RequestMapping(value="/downloadData")
	public @ResponseBody Map<String, Object> downloadData(@RequestBody CommonUpdownParam param) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();

		map.put("config", service.getUploadConfig());

		if("NORMAL".equals(param.getDistributeTypeCd())) {
			map.put("downloadVolume", "R");
		}else {
			map.put("downloadVolume", "S");
		}

		map.put("list", service.selectList(param));

		map.put("userNm",param.getSessionUser().getUsername());


//		model.addAttribute("gridId", param.getGridId());
//		model.addAttribute("reqType", param.getReqType());
		return map;
	}

	@RequestMapping(value="/openFileDownPopup")
	public String openFileListPopup(CommonUpdownParam param, Model model) throws JsonProcessingException {
		model.addAttribute("config", service.getUploadConfig());
		if("NORMAL".equals(param.getDistributeTypeCd())) {
			model.addAttribute("downloadVolume", "R");
		}else {
			model.addAttribute("downloadVolume", "S");
		}
		model.addAttribute("gridId", param.getGridId());
		model.addAttribute("reqType", param.getReqType());
		//		List<CommonUpdownParam> list = JSONArray.toList(JSONArray.fromObject(request.getParameter("list")), CommonUpdownParam.class);
		//		param.setList(list);
		//		List<CommonUpdownFileVO> result = service.selectFileInfo(param);
		//		model.addAttribute("arrFileInfo", result);
		return "updown/commonFileDownPopup";
	}

	@RequestMapping("/selectList")
	public @ResponseBody List selectList(@RequestBody CommonUpdownParam param) throws Exception {
		//		public @ResponseBody List<CommonUpdownFileVO> selectList(CommonUpdownParam param) throws Exception {
		return service.selectList(param);
	}

	@RequestMapping(value="/downloadProgressPopup")
	public String downloadProgressPopup(CommonUpdownParam param, Model model) {
		model.addAttribute("gridId", param.getGridId());
		model.addAttribute("reqType", param.getReqType());
		model.addAttribute("objectType", param.getObjectType());
		model.addAttribute("distributeTypeCd", param.getDistributeTypeCd());
		model.addAttribute("selectedDataJson", param.getSelectedDataJson());
		return "updown/downloadProgressPopup";
	}





//	public static void main(String [] args) throws IOException {
//		String url = "http://localhost:8080/common/upload/receiver";
//		String charset = "UTF-8";
//		//		File textFile = new File("/path/to/file.txt");
//		File binaryFile = new File("C:\\Users\\younjh\\Downloads\\HD_PLM구축_DOCS_화면정의서_내부사용자_v2.0_20191002.pptx");
//		String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
//		String CRLF = "\r\n"; // Line separator required by multipart/form-data.
//
//		URLConnection connection = new URL(url).openConnection();
//		connection.setDoOutput(true);
//		connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
//
//		try (
//				OutputStream output = connection.getOutputStream();
//				PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);
//				) {
//			// Send normal param.
//			// 여러개의 Content-Disposition 동시 전송 가능.
//			writer.append("--" + boundary).append(CRLF);
//			writer.append("Content-Disposition: form-data; name=\"filename\"").append(CRLF);
//			writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
//			writer.append(CRLF).append(binaryFile.getName()).append(CRLF).flush();
//
//			writer.append("--" + boundary).append(CRLF);
//			writer.append("Content-Disposition: form-data; name=\"destination\"").append(CRLF);
//			writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
//			writer.append(CRLF).append("out").append(CRLF).flush();
//
//			// Send text file.
//			//		    writer.append("--" + boundary).append(CRLF);
//			//		    writer.append("Content-Disposition: form-data; name=\"textFile\"; filename=\"" + textFile.getName() + "\"").append(CRLF);
//			//		    writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF); // Text file itself must be saved in this charset!
//			//		    writer.append(CRLF).flush();
//			//		    Files.copy(textFile.toPath(), output);
//			//		    output.flush(); // Important before continuing with writer!
//			//		    writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.
//
//			// Send binary file.
//			writer.append("--" + boundary).append(CRLF);
//			// filename 파라메터는 꼭 있어야 함.
//			// 없으면 receiver에서 filePart.getSubmittedFileName()이 null임
//			writer.append("Content-Disposition: form-data; name=\"binaryFile\"; filename=\"" + binaryFile.getName() + "\"").append(CRLF);
//			writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(binaryFile.getName())).append(CRLF);
//			writer.append("Content-Transfer-Encoding: binary").append(CRLF);
//			writer.append(CRLF).flush();
//			Files.copy(binaryFile.toPath(), output);
//			output.flush(); // Important before continuing with writer!
//			writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.
//
//			// End of multipart/form-data.
//			writer.append("--" + boundary + "--").append(CRLF).flush();
//		}
//
//		// Request is lazily fired whenever you need to obtain information about response.
//		int responseCode = ((HttpURLConnection) connection).getResponseCode();
//
//		StringBuffer sbResponse = new StringBuffer();
//		BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//
//		String line = null;
//
//		while ((line = br.readLine()) != null) {
//			sbResponse.append(line);
//		}
//
//		System.out.println(responseCode); // Should be 200
//		System.out.println(sbResponse.toString());
//	}
}
