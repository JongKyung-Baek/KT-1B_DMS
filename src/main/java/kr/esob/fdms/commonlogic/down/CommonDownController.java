package kr.esob.fdms.commonlogic.down;

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
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

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

@Controller
@RequestMapping("/common/down")
public class CommonDownController extends AbstractController {

	@Inject
	CommonDownService service;

	@RequestMapping(value="/openFileDownPopup")
	public String openFileDownPopup(CommonDownParam param, Model model) throws JsonProcessingException {
		model.addAttribute("config", service.getUploadConfig());
		model.addAttribute("gridId", param.getGridId());
		return "down/commonFileDownPopup";
	}

	@RequestMapping("/selectList")
	public @ResponseBody List selectList(@RequestBody CommonDownParam param) throws Exception {
		return service.selectList(param);
	}
}
