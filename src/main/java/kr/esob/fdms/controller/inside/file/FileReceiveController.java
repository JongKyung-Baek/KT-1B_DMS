package kr.esob.fdms.controller.inside.file;

import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import kr.esob.fdms.commonlogic.value.SessionValue;

@RestController
@RequestMapping("/FileReceiveController")
public class FileReceiveController {
	@Inject
    FileReceiveService service;

	@Autowired
	SessionValue sessionValue;




}
