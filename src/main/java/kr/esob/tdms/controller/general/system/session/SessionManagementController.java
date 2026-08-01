package kr.esob.tdms.controller.general.system.session;

import kr.esob.tdms.commonlogic.abstractclass.AbstractController;
import net.sf.json.JSONArray;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/general/system/session")
public class SessionManagementController extends AbstractController {

    @RequestMapping("")
    public String getSessionMangementPage(Model model){
        model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarSystemRoleAssign")));
        return "general/system/session/sessionManagement";
    }

}
