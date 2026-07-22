package kr.esob.fdms.controller.inside.organizationmanage.auditlog;

import com.fasterxml.jackson.core.JsonProcessingException;
import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import net.sf.json.JSONArray;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/inside/organizationmanage/auditlog")
public class AuditLogController extends AbstractController {

    @Inject
    AuditLogService service;

    @RequestMapping(value = {"", "/"})
    public String home(Model model) throws JsonProcessingException {
        model.addAttribute("formInfo", JSONArray.fromObject(formService.selectFormInfo("formInsideAuditLog")));
        model.addAttribute("toolbarInfo", JSONArray.fromObject(toolbarService.selectToolbarInfo("toolbarInsideAuditLog")));
        model.addAttribute("gridInfo", JSONArray.fromObject(gridService.selectGridInfo("gridInsideAuditLogList")));

        return "inside/organizationmanage/auditlog/auditlogList";
    }

    @RequestMapping("/selectList")
    public @ResponseBody GridResultVO selectList(AuditLogListParam param) throws Exception {
        return commonSelectList(param, service);
    }

    @RequestMapping("/notifyLogoutOnLeave")
    public @ResponseBody void notifyLogoutOnLeave(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        service.markPendingLogoutOnLeave(session, request);
    }

    @RequestMapping("/clearPendingLogoutOnStay")
    public @ResponseBody void clearPendingLogoutOnStay(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        service.clearPendingLogoutOnStay(session);
    }
}
