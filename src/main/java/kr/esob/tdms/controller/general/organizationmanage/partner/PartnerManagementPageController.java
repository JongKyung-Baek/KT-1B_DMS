package kr.esob.tdms.controller.general.organizationmanage.partner;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import kr.esob.tdms.commonlogic.securityacl.SecurityAclService;

@Controller
@RequestMapping("/general/organizationmanage/partner")
public class PartnerManagementPageController {
    private final SecurityAclService aclService;

    public PartnerManagementPageController(SecurityAclService aclService) {
        this.aclService = aclService;
    }

    @GetMapping({"", "/"})
    public String home() {
        aclService.requireCurrentUser();
        return "/general/organizationmanage/partner/partnerManagement";
    }
}
