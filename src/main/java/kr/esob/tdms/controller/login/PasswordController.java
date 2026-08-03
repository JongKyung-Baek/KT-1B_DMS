package kr.esob.tdms.controller.login;

import kr.esob.tdms.commonlogic.result.ResultVO;
import kr.esob.tdms.commonlogic.value.Constant;
import kr.esob.tdms.controller.general.organizationmanage.auditlog.AuditLogService;
import kr.esob.tdms.util.seed.PasswordUtils;
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

@Controller
@RequestMapping("/login/password")
public class PasswordController {

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
            result.setMessage("feature.password.error.sessionExpired");
            return result;
        }
        if (!PasswordUtils.isAcceptablePassword(newPassword)) {
            result.setMessage("feature.password.error.invalidPolicy");
            return result;
        }

        if (Constant.INITIAL_PASSWORD.equals(newPassword)) {
            result.setMessage("feature.password.error.invalidPolicy");
            return result;
        }

        UserVO userVo = (UserVO) authentication.getPrincipal();
        if (!loginService.changeOwnPassword(userVo.getUserCd(), newPassword)) {
            result.setMessage("feature.password.error.save");
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
}
