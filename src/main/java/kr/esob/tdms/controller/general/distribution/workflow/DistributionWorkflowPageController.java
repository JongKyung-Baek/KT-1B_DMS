package kr.esob.tdms.controller.general.distribution.workflow;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import kr.esob.tdms.commonlogic.securityacl.SecurityAclService;
import kr.esob.tdms.commonlogic.value.Constant;
import kr.esob.tdms.controller.general.distribution.swrequest.SwRequestService;
import kr.esob.tdms.controller.login.UserVO;

/**
 * MVC entry points for the distribution-request workflow.
 *
 * <p>The JSON lifecycle API remains isolated under {@code /api}. Keeping the
 * page routes in a separate controller prevents view resolution from being
 * coupled to the REST response contract.</p>
 */
@Controller
@RequestMapping("/general/distribution/workflow")
public class DistributionWorkflowPageController {
    private static final int MAX_INITIAL_ITEMS = 200;

    private final SecurityAclService aclService;
    private final SwRequestService swRequestService;
    private final DistributionWorkflowService workflowService;

    public DistributionWorkflowPageController(SecurityAclService aclService,
            SwRequestService swRequestService,
            DistributionWorkflowService workflowService) {
        this.aclService = aclService;
        this.swRequestService = swRequestService;
        this.workflowService = workflowService;
    }

    @GetMapping({"", "/"})
    public String home() {
        aclService.requireCurrentUser();
        return "redirect:/general/distribution/workflow/requests/";
    }

    @GetMapping({"/requests", "/requests/"})
    public String requests(
            @RequestParam(name = "objectType", required = false) List<String> objectTypes,
            @RequestParam(name = "objectId", required = false) List<String> objectIds,
            @RequestParam(name = "fileNo", required = false) List<String> fileNos,
            Model model) {
        return requestsPage(objectTypes, objectIds, fileNos, false, model);
    }

    @GetMapping({"/requests/new", "/requests/new/"})
    public String newRequest(
            @RequestParam(name = "objectType", required = false) List<String> objectTypes,
            @RequestParam(name = "objectId", required = false) List<String> objectIds,
            @RequestParam(name = "fileNo", required = false) List<String> fileNos,
            Model model) {
        return requestsPage(objectTypes, objectIds, fileNos, true, model);
    }

    private String requestsPage(List<String> objectTypes, List<String> objectIds,
            List<String> fileNos, boolean openCreate, Model model) {
        UserVO actor = aclService.requireCurrentUser();
        addPageModel(model, "mine", actor);
        List<DistributionRequestItemRef> selectedItems = initialItems(objectTypes, objectIds, fileNos);
        model.addAttribute("initialItems", selectedItems.isEmpty()
            ? selectedItems : workflowService.selectionPreview(selectedItems));
        model.addAttribute("workflowCategoryParents", swRequestService.selectLevelParentOptions(null));
        model.addAttribute("workflowCategoryChildren", swRequestService.selectLevelOptions(null));
        model.addAttribute("workflowOpenCreate", openCreate);
        return "/general/distribution/workflow/myRequests";
    }

    @GetMapping({"/approval", "/approval/"})
    public String approval(Model model) {
        UserVO actor = requireAdministrator();
        addPageModel(model, "approval", actor);
        return "/general/distribution/workflow/approvalQueue";
    }

    @GetMapping({"/approved", "/approved/"})
    public String approved(Model model) {
        UserVO actor = aclService.requireCurrentUser();
        addPageModel(model, "approved", actor);
        return "/general/distribution/workflow/approvedList";
    }

    private void addPageModel(Model model, String mode, UserVO actor) {
        model.addAttribute("workflowMode", mode);
        model.addAttribute("workflowAdministrator", isAdministrator(actor));
    }

    private UserVO requireAdministrator() {
        UserVO actor = aclService.requireCurrentUser();
        if (!isAdministrator(actor)) {
            throw new AccessDeniedException("Distribution approval requires an administrator role.");
        }
        return actor;
    }

    private boolean isAdministrator(UserVO actor) {
        return actor != null && Constant.GROUP_CD_ADMIN.equals(actor.getRoleGroup());
    }

    private List<DistributionRequestItemRef> initialItems(
            List<String> objectTypes, List<String> objectIds, List<String> fileNos) {
        List<DistributionRequestItemRef> result = new ArrayList<DistributionRequestItemRef>();
        if (objectTypes == null || objectIds == null || fileNos == null) {
            return result;
        }

        int count = Math.min(MAX_INITIAL_ITEMS,
            Math.min(objectTypes.size(), Math.min(objectIds.size(), fileNos.size())));
        Set<String> seen = new HashSet<String>();
        for (int index = 0; index < count; index++) {
            String objectType = normalizedObjectType(objectTypes.get(index));
            String objectId = safeIdentifier(objectIds.get(index), 128);
            String fileNo = safeIdentifier(fileNos.get(index), 50);
            if (objectType == null || objectId == null || fileNo == null) {
                continue;
            }
            String key = objectType + '\u0000' + objectId + '\u0000' + fileNo;
            if (!seen.add(key)) {
                continue;
            }

            DistributionRequestItemRef item = new DistributionRequestItemRef();
            item.setObjectType(objectType);
            item.setObjectId(objectId);
            item.setFileNo(fileNo);
            result.add(item);
        }
        return result;
    }

    private String normalizedObjectType(String value) {
        String normalized = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        return "SW".equals(normalized) || "SW_SUB".equals(normalized) ? normalized : null;
    }

    private String safeIdentifier(String value, int maxLength) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty() || normalized.length() > maxLength) {
            return null;
        }
        for (int index = 0; index < normalized.length(); index++) {
            char character = normalized.charAt(index);
            if (!(Character.isLetterOrDigit(character)
                    || character == '-' || character == '_' || character == '.')) {
                return null;
            }
        }
        return normalized;
    }
}
