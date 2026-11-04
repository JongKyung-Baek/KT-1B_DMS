package kr.esob.tdms.controller.login;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.inject.Inject;
import javax.servlet.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import kr.esob.tdms.commonlogic.message.LocaleUtil;

public class MfaStart implements AuthenticationSuccessHandler {
    static final String PENDING = MfaStart.class.getName();
    record Challenge(String userId, String userCd, byte[] passwordStamp, long expires, long generation, byte[] enrollment)
            implements Serializable {}

    @Inject MfaStore store;
    @Inject LoginSuccess login;

    static byte[] stamp(UserVO user) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(user.getPassword().getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }

    public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse res, Authentication result)
            throws IOException {
        UserVO user = (UserVO) result.getPrincipal();
        HttpSession old = req.getSession();
        String lang = login.resolveLoginLanguage(req, old);
        SecurityContextHolder.clearContext();
        old.invalidate();
        HttpSession session = req.getSession(true);
        session.setAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME, LocaleUtil.getLocale(lang));
        MfaStore.Enrollment enrollment = store.enrollment(user.getUserCd());
        session.setAttribute(PENDING, new Challenge(user.getUserId(), user.getUserCd(), stamp(user),
                System.currentTimeMillis() + 300_000L, enrollment.generation(), enrollment.setupKey()));
        res.sendRedirect(req.getContextPath() + "/login/mfa");
    }
}
