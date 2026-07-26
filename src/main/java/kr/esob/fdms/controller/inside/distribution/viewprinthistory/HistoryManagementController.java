package kr.esob.fdms.controller.inside.distribution.viewprinthistory;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.abstractclass.CommonHomeParam;
import kr.esob.fdms.commonlogic.securityacl.SecurityAclService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.List;

@Controller
@RequestMapping("/inside/history")
public class HistoryManagementController extends AbstractController {

    private static final String TECHNICAL_DATA_SCOPE = "TECHNICAL_DATA";
    private static final String LEGACY_TECHNICAL_DATA_SCOPE = "기술자료관리";

    @Inject
    HistoryService service;

    @Inject
    SecurityAclService securityAclService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String home() {
        securityAclService.requireCurrentUser();
        return "redirect:/inside/distribution/viewPrintHistory/";
    }

    @RequestMapping(value = "/view/", method = RequestMethod.GET)
    public String viewHistory(Model model, CommonHomeParam param) {
        securityAclService.requireCurrentUser();
        setHomeParam(model, param);
        addPageModel(
                model,
                "view",
                "feature.history.view.title",
                "feature.history.view.kicker",
                "feature.history.view.description",
                "/inside/history/view/events");
        return "inside/distribution/viewPrintHistory/recordHistory";
    }

    @RequestMapping(value = "/view/events", method = RequestMethod.GET)
    public @ResponseBody List<HistoryEventVO> viewEvents(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "scope", required = false) String distributionType) {
        securityAclService.requireCurrentUser();
        String scope = TECHNICAL_DATA_SCOPE.equals(distributionType)
                ? LEGACY_TECHNICAL_DATA_SCOPE
                : distributionType;
        return service.selectViewEvents(keyword, scope);
    }

    @RequestMapping(value = "/print/", method = RequestMethod.GET)
    public String printHistory(Model model, CommonHomeParam param) {
        securityAclService.requireCurrentUser();
        setHomeParam(model, param);
        addPageModel(
                model,
                "print",
                "feature.history.print.title",
                "feature.history.print.kicker",
                "feature.history.print.description",
                "/inside/history/print/events");
        return "inside/distribution/viewPrintHistory/recordHistory";
    }

    @RequestMapping(value = "/print/events", method = RequestMethod.GET)
    public @ResponseBody List<HistoryEventVO> printEvents(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "scope", required = false) String statusCd) {
        securityAclService.requireCurrentUser();
        return service.selectPrintEvents(keyword, statusCd);
    }

    private void addPageModel(
            Model model,
            String mode,
            String titleCode,
            String kickerCode,
            String descriptionCode,
            String endpoint) {
        model.addAttribute("historyMode", mode);
        model.addAttribute("historyTitleCode", titleCode);
        model.addAttribute("historyKickerCode", kickerCode);
        model.addAttribute("historyDescriptionCode", descriptionCode);
        model.addAttribute("historyEndpoint", endpoint);
    }
}
