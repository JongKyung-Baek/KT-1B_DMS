package kr.esob.fdms.commonlogic.updown;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Objects;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.value.Constant;
import kr.esob.fdms.util.FileUtil;
import kr.esob.fdms.util.seed.seed.Seed128Cipher;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;

// @MultipartConfig 꼭 필요! 매우 중요!!
@Slf4j
@Controller
@MultipartConfig
public class CommonFileReceiverController extends AbstractController {
	/**
	 * 파일 수신자.
	 * @param request
	 * @param response {result:true,msg:"결과메시지"}
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	@RequestMapping("/common/fileTransfer/receiver")
	public @ResponseBody String receiver(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
		String orgFileNm = Seed128Cipher.decrypt(request.getParameter("orgFileNm"), Constant.SEED_KEY.getBytes(), Constant.SEED_ENCODING);
		String dstFilePath = Seed128Cipher.decrypt(request.getParameter("dstFilePath"), Constant.SEED_KEY.getBytes(), Constant.SEED_ENCODING);
		String dstFileNm = Seed128Cipher.decrypt(request.getParameter("dstFileNm"), Constant.SEED_KEY.getBytes(), Constant.SEED_ENCODING);
		String ext = FilenameUtils.getExtension(dstFileNm);
		String uuid = FileUtil.getFileUuid() + ("".equals(ext) ? "" : "." + FilenameUtils.getExtension(dstFileNm));
		JSONObject resultObj = new JSONObject();
		boolean success = true;
		String message = null;

		InputStream fileContent = null;
		OutputStream outStream = null;

		log.info("orgFileNm : " + orgFileNm);
		log.info("dstFilePath : " + dstFilePath);
		log.info("dstFileNm : " + dstFileNm);

		try {
			Part filePart = request.getPart("binaryFile"); // Retrieves <input type="binaryFile" name="binaryFile">
			log.info(filePart.getSubmittedFileName());
			File file = new File(dstFilePath);
//			FileUtil.mkdir(file.getParent());			// 파일을 저장할 경로가 없다면 생성
			FileUtil.mkdir(file.getAbsolutePath());		// 파일을 저장할 경로가 없다면 생성

			fileContent = filePart.getInputStream();

			outStream = new FileOutputStream(dstFilePath + uuid);

			// 읽어들일 버퍼크기를 메모리에 생성
			byte[] buf = new byte[1024];

			int len = 0;

			// 끝까지 읽어들이면서 File 객체에 내용들을 쓴다
			while ((len = fileContent.read(buf)) > 0){
				outStream.write(buf, 0, len);
			}
			// Stream 객체를 모두 닫는다.
		}
		catch(Exception e) {
			e.printStackTrace();
			message = e.getMessage();
			success = false;
		}
		finally {
			try {
				if(outStream != null) outStream.close();
				if(fileContent != null) fileContent.close();
			} catch (IOException e) {
			}
		}

		resultObj.put("result", success);
		resultObj.put("msg", Objects.toString(message, ""));
		resultObj.put("fileNm", uuid);

		log.info(resultObj.toString());

		return resultObj.toString();
	}
}
