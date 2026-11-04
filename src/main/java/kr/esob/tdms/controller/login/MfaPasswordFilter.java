package kr.esob.tdms.controller.login;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/** Password verification only. Never publish the first factor to the security context. */
public class MfaPasswordFilter extends UsernamePasswordAuthenticationFilter {
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain, Authentication result) throws IOException, ServletException {
        getSuccessHandler().onAuthenticationSuccess(request, response, result);
    }
}
