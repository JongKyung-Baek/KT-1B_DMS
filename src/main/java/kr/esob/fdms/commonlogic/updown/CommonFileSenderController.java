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
		// External file transfer is intentionally disabled until the final
		// integration phase. The old endpoint trusted arbitrary source paths and
		// destination URLs, allowing server-side file reads or SSRF.
		response.setStatus(HttpServletResponse.SC_GONE);
		return "{\"result\":false,\"msg\":\"legacy file transfer is disabled\"}";
	}
}
