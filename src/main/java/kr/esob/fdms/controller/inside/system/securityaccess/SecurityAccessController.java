package kr.esob.fdms.controller.inside.system.securityaccess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.abstractclass.CommonHomeParam;
import kr.esob.fdms.commonlogic.securityacl.FileAccessDecisionVO;
import kr.esob.fdms.commonlogic.securityacl.FileAccessRequest;
import kr.esob.fdms.commonlogic.securityacl.FileSecurityLabelVO;
import kr.esob.fdms.commonlogic.securityacl.FileUserPermissionSaveRequest;
import kr.esob.fdms.commonlogic.securityacl.FileUserPermissionVO;
import kr.esob.fdms.commonlogic.securityacl.SecurityAclService;
import kr.esob.fdms.commonlogic.securityacl.SecurityGradeVO;
import kr.esob.fdms.commonlogic.securityacl.UserClearanceVO;

@Controller
@RequestMapping("/inside/system/securityaccess")
public class SecurityAccessController extends AbstractController {
    private final SecurityAclService service;

    public SecurityAccessController(SecurityAclService service) {
        this.service = service;
    }

    @RequestMapping("/")
    public String page(Model model, CommonHomeParam param) {
        service.requireManageAcl();
        setHomeParam(model, param);
        return "inside/system/securityaccess/securityAccess";
    }

    @RequestMapping(value = "/api/grades", method = RequestMethod.GET)
    public @ResponseBody List<SecurityGradeVO> grades() {
        return service.selectGradesForManagement();
    }

    @RequestMapping(value = "/api/grades", method = RequestMethod.POST)
    public @ResponseBody Map<String, Object> saveGrade(@RequestBody SecurityGradeVO grade) {
        service.saveGrade(grade);
        return success();
    }

    @RequestMapping(value = "/api/users", method = RequestMethod.GET)
    public @ResponseBody List<UserClearanceVO> users(
        @RequestParam(value = "keyword", required = false) String keyword) {
        return service.selectUsersForManagement(keyword);
    }

    @RequestMapping(value = "/api/users/clearance", method = RequestMethod.POST)
    public @ResponseBody Map<String, Object> saveClearance(@RequestBody UserClearanceVO clearance) {
        service.saveUserClearance(clearance);
        return success();
    }

    @RequestMapping(value = "/api/files", method = RequestMethod.GET)
    public @ResponseBody List<FileSecurityLabelVO> files(
        @RequestParam("objectType") String objectType,
        @RequestParam(value = "keyword", required = false) String keyword) {
        return service.selectFilesForManagement(objectType, keyword);
    }

    @RequestMapping(value = "/api/files/label", method = RequestMethod.POST)
    public @ResponseBody Map<String, Object> saveLabel(@RequestBody FileSecurityLabelVO label) {
        service.saveFileLabel(label);
        return success();
    }

    @RequestMapping(value = "/api/files/permissions", method = RequestMethod.GET)
    public @ResponseBody List<FileUserPermissionVO> filePermissions(
        @RequestParam("objectType") String objectType,
        @RequestParam("objectId") String objectId,
        @RequestParam(value = "fileNo", required = false) String fileNo) {
        return service.selectFileUserPermissionsForManagement(objectType, objectId, fileNo);
    }

    @RequestMapping(value = "/api/files/permissions", method = RequestMethod.POST)
    public @ResponseBody Map<String, Object> saveFilePermissions(
        @RequestBody FileUserPermissionSaveRequest request) {
        service.saveFileUserPermissions(request);
        return success();
    }

    @RequestMapping(value = "/api/check", method = RequestMethod.POST)
    public @ResponseBody FileAccessDecisionVO check(@RequestBody FileAccessRequest request) {
        service.requireManageAcl();
        return service.checkAccess(request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public @ResponseBody Map<String, Object> denied(AccessDeniedException exception, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        return failure("접근이 거부되었습니다.");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public @ResponseBody Map<String, Object> invalid(IllegalArgumentException exception, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return failure("요청값이 올바르지 않습니다.");
    }

    private Map<String, Object> success() {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("success", true);
        return result;
    }

    private Map<String, Object> failure(String message) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("success", false);
        result.put("message", message);
        return result;
    }
}
