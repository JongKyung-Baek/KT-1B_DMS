package kr.esob.fdms.controller.inside.system.session;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import net.sf.json.JSONArray;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/inside/system/session")
public class SessionManagementController extends AbstractController {

    @RequestMapping("")
    public String getSessionMangementPage(Model model){
        model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarSystemRoleAssign")));
        return "inside/system/session/sessionManagement";
    }

}
