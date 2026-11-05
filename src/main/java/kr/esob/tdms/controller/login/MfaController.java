package kr.esob.tdms.controller.login;

import java.security.MessageDigest;
import javax.servlet.http.*;
import org.apache.commons.codec.binary.Base32;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/login/mfa")
public class MfaController {
    private final MfaStore store;
    private final LoginService users;
    private final LoginSuccess success;

    public MfaController(MfaStore store, LoginService users, LoginSuccess success) {
        this.store = store;
        this.users = users;
        this.success = success;
    }

    private MfaStart.Challenge pending(HttpSession session) {
        if (session == null) return null;
        MfaStart.Challenge challenge = (MfaStart.Challenge) session.getAttribute(MfaStart.PENDING);
        if (challenge != null && challenge.expires() <= System.currentTimeMillis()) {
            session.removeAttribute(MfaStart.PENDING);
            return null;
        }
        return challenge;
    }

    @GetMapping
    public String challenge(HttpServletRequest req, HttpServletResponse res, Model model) {
        res.setHeader("Cache-Control", "no-store");
        res.setHeader("Pragma", "no-cache");
        res.setHeader("Referrer-Policy", "no-referrer");
        MfaStart.Challenge challenge = pending(req.getSession(false));
        if (challenge == null) return "redirect:/login/loginPage";
        if (challenge.enrollment() != null) {
            String setupKey = new Base32().encodeToString(challenge.enrollment());
            model.addAttribute("setupKey", setupKey);
            model.addAttribute("account", challenge.userId());
            try {
                model.addAttribute("setupQr", MfaSetupQr.pngDataUrl(challenge.userId(), setupKey));
            } catch (IllegalStateException e) {
                // Manual enrollment remains available if the image encoder is unavailable.
                model.addAttribute("setupQrUnavailable", true);
            }
        }
        return "/login/mfa";
    }

    @PostMapping
    public void verify(@RequestParam(defaultValue="") String otp, HttpServletRequest req, HttpServletResponse res) throws Exception {
        HttpSession session = req.getSession(false);
        MfaStart.Challenge challenge = pending(session);
        if (challenge == null) {
            res.sendRedirect(req.getContextPath()+"/login/loginPage");
            return;
        }
        UserVO user = users.getInUser(challenge.userId());
        if (user == null || !user.isEnabled() || !user.isAccountNonLocked() || user.getRoleGroup() == null
                || !challenge.userCd().equals(user.getUserCd()) || !MessageDigest.isEqual(challenge.passwordStamp(), MfaStart.stamp(user))) {
            session.removeAttribute(MfaStart.PENDING);
            res.sendRedirect(req.getContextPath()+"/login/loginPage");
            return;
        }
        if (!store.verify(challenge, otp)) {
            res.sendRedirect(req.getContextPath()+"/login/mfa?retry");
            return;
        }
        session.removeAttribute(MfaStart.PENDING);
        req.changeSessionId();
        new HttpSessionCsrfTokenRepository().saveToken(null, req, res);
        users.setAuthority(user);
        var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        try {
            success.onAuthenticationSuccess(req, res, auth);
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            session.invalidate();
            throw e;
        }
    }
}
