package com.healthcare.filter;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;

/** Rejects any POST whose _csrf parameter does not match the token stored in the session. */
@WebFilter("/*")
public class CsrfFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            HttpSession s = request.getSession(false);
            String expected = s == null ? null : (String) s.getAttribute("csrf");
            String actual = request.getParameter("_csrf");
            if (expected == null || !expected.equals(actual)) {
                ((HttpServletResponse) res).sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing security token. Reload the page and try again.");
                return;
            }
        }
        chain.doFilter(req, res);
    }
}
