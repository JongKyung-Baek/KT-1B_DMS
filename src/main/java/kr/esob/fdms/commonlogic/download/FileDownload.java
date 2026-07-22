package kr.esob.fdms.commonlogic.download;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import kr.esob.fdms.commonlogic.value.Constant;
import kr.esob.fdms.util.seed.seed.Seed128Cipher;

@Controller
public class FileDownload {

	/**
	 * 파일(첨부파일, 이미지등) 다운로드.(업체견적서)
	 * @throws UnsupportedEncodingException
	 */
	@RequestMapping(value = "fileDownload")
	public void fileDownload4(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
		String path =  request.getSession().getServletContext().getRealPath("/");
		String filename = Seed128Cipher.decrypt(request.getParameter("fileName"), Constant.SEED_KEY.getBytes(), Constant.SEED_ENCODING);
		String downname = Seed128Cipher.decrypt(request.getParameter("fileName"), Constant.SEED_KEY.getBytes(), Constant.SEED_ENCODING);
		String realPath = "";
		System.out.println("downname: "+downname);
		if (filename == null || "".equals(filename)) {
			filename = downname;
		}
		filename = filename.replaceAll("[\\r\\n]", "");
		System.out.println("filename: " + filename);
		filename = filename.split("/")[1];
		System.out.println("new filename: " + filename);

		try {
			String browser = request.getHeader("User-Agent");
			//파일 인코딩
			if (browser.contains("MSIE") || browser.contains("Trident")
					|| browser.contains("Chrome")) {
				filename = URLEncoder.encode(filename, "UTF-8").replaceAll("\\+", "%20");
			} else {
				filename = new String(filename.getBytes("UTF-8"), "ISO-8859-1");
			}
		} catch (UnsupportedEncodingException ex) {
			System.out.println("UnsupportedEncodingException");
		}
		//realPath = path +"/" +downname.substring(0,4) + "/"+downname;
		realPath = path + downname;
		File file1 = new File(realPath);
		if (!file1.exists()) {
			return ;
		}

		// 파일명 지정
		response.setContentType("application/octer-stream");
		response.setHeader("Content-Transfer-Encoding", "binary;");
		response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
		try {
			OutputStream os = response.getOutputStream();
			FileInputStream fis = new FileInputStream(realPath);

			int ncount = 0;
			byte[] bytes = new byte[512];

			while ((ncount = fis.read(bytes)) != -1 ) {
				os.write(bytes, 0, ncount);
			}
			fis.close();
			os.close();
		} catch (FileNotFoundException ex) {
			System.out.println("FileNotFoundException");
		} catch (IOException ex) {
			System.out.println("IOException");
		}
	}
}