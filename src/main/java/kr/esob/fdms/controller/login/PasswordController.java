package kr.esob.fdms.controller.login;

import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.controller.inside.distribution.doc_pdf_link_request.DocPdfLinkRequestDao;
import kr.esob.fdms.controller.inside.organizationmanage.auditlog.AuditLogService;
import kr.esob.fdms.util.seed.PasswordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/login/password")
public class PasswordController {

    @Autowired
    DocPdfLinkRequestDao systemConfigDao;

    @Inject
    LoginService loginService;

    @Inject
    AuditLogService auditLogService;

    @GetMapping("")
    public String getPasswordConfigPage(Model model, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserVO)) {
            return "redirect:/login/loginPage";
        }

        model.addAttribute("userVo", authentication.getPrincipal());
        return "login/passwordConfig";
    }

    @PostMapping("")
    @ResponseBody
    public ResultVO changeOwnPassword(@RequestParam("userPwd") String newPassword,
                                      Authentication authentication,
                                      HttpServletRequest request) {
        ResultVO result = new ResultVO();
        result.setSuccess(false);

        if (authentication == null || !(authentication.getPrincipal() instanceof UserVO)) {
            result.setMessage("msg.accessDenied");
            return result;
        }
        if (!PasswordUtils.isAcceptablePassword(newPassword)) {
            result.setMessage("msg.invalidPassword");
            return result;
        }

        String basicPassword = findBasicPassword(systemConfigDao.selectDbConfig());
        if (basicPassword == null || basicPassword.equals(newPassword)) {
            result.setMessage("msg.invalidPassword");
            return result;
        }

        UserVO userVo = (UserVO) authentication.getPrincipal();
        if (!loginService.changeOwnPassword(userVo.getUserCd(), newPassword)) {
            result.setMessage("msg.error");
            return result;
        }

        auditLogService.insertAuditLog("changePassword", userVo.getUserId(), userVo.getUserNm(), request);
        userVo.setUserPwd(null);
        result.setSuccess(true);

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return result;
    }

    private String findBasicPassword(List<Map<String, Object>> dbConfig) {
        if (dbConfig == null) {
            return null;
        }
        for (Map<String, Object> config : dbConfig) {
            if (config == null) {
                continue;
            }
            Object configCd = value(config, "SYSTEM_CONFIG_CD", "system_config_cd");
            if (!"BASIC_PASSWORD".equals(configCd)) {
                continue;
            }
            Object configValue = value(config, "SYSTEM_CONFIG_VALUE", "system_config_value");
            return configValue == null ? null : configValue.toString();
        }
        return null;
    }

    private Object value(Map<String, Object> config, String... keys) {
        for (String key : keys) {
            Object value = config.get(key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }
}
