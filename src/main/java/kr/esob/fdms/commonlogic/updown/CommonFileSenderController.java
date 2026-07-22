package kr.esob.fdms.commonlogic.updown;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;

@Slf4j
@Controller
public class CommonFileSenderController extends AbstractController {
	@RequestMapping("/common/fileTransfer/sender")
	public @ResponseBody String sender(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
		String dstUrl = request.getParameter("dstUrl");
		String srcFilePath = request.getParameter("srcFilePath");
		String dstFilePath = request.getParameter("dstFilePath");
		String dstFileNm = request.getParameter("dstFileNm");

		log.info("dstUrl : " +dstUrl);
		log.info("srcFilePath : " +srcFilePath);
		log.info("dstFilePath : " +dstFilePath);
		log.info("dstFileNm : " +dstFileNm);

		return JSONObject.fromObject(FileUtil.sendFile(dstUrl, srcFilePath, dstFilePath, dstFileNm)).toString();
	}
}
