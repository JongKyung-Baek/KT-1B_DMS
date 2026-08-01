package kr.esob.tdms.controller.general.system.distributionaccountrequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import kr.esob.tdms.commonlogic.securityacl.SecurityAclService;

/**
 * Administrator page for account requests received from an integrated
 * technical-data distribution system.
 */
@Controller
@RequestMapping("/general/distribution/account-requests")
public class DistributionAccountRequestPageController {
    private final SecurityAclService aclService;

    public DistributionAccountRequestPageController(SecurityAclService aclService) {
        this.aclService = aclService;
    }

    @GetMapping({"", "/"})
    public String home() {
        aclService.requireCurrentUser();
        return "/general/system/distributionaccountrequest/distributionAccountRequestList";
    }
}
