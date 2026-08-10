package kr.esob.tdms.commonlogic.branding;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Makes the port-selected brand available to MVC, SiteMesh and error JSPs. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class TdmsBrandFilter extends OncePerRequestFilter {
    private final TdmsBrandResolver resolver;

    public TdmsBrandFilter(TdmsBrandResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        if (request.getAttribute(TdmsBrandResolver.REQUEST_ATTRIBUTE) == null) {
            request.setAttribute(TdmsBrandResolver.REQUEST_ATTRIBUTE,
                    resolver.resolve(request));
        }
        filterChain.doFilter(request, response);
    }
}
