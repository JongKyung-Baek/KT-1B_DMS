package kr.esob.tdms.controller.main;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/main")
public class MainController {

	@RequestMapping("")
	public String home() {
		return "redirect:/general/distribution/swRequest/dashboard";
	}
}
