package kr.esob.fdms.controller.inside.distribution.viewprinthistory;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.securityacl.SecurityAclService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;

/**
 * Compatibility redirect for the retired access-only history screen.
 *
 * <p>All legacy child endpoints were removed with the duplicate screen. The
 * canonical access and audit ledger is exposed by AuditLogController.</p>
 */
@Controller
@RequestMapping("/inside/distribution/viewPrintHistory")
public class ViewPrintHistoryController extends AbstractController {

    @Inject
    SecurityAclService securityAclService;

    @RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
    public String home() {
        securityAclService.requireCurrentUser();
        return "redirect:/inside/organizationmanage/auditlog/";
    }
}
