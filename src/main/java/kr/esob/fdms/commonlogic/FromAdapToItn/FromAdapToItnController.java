package kr.esob.fdms.commonlogic.FromAdapToItn;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.core.JsonProcessingException;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.abstractclass.CommonHomeParam;


@Controller
public class FromAdapToItnController{

	@RequestMapping("/commonlogic/FromAdapToItn")
	public String home(Model model, CommonHomeParam param) throws JsonProcessingException {
		
		return "aaa";
	}
}