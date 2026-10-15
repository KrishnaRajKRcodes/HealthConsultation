package com.healthcare.filter;

import com.healthcare.model.Role;
import com.healthcare.model.User;
import java.io.IOException;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;

/** Role-based access control for /admin, /doctor and /patient. */
@WebFilter(urlPatterns = {"/admin", "/doctor", "/patient"})
public class AuthFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        Role required = Role.valueOf(request.getServletPath().substring(1).toUpperCase());
        if (user.getRole() != required) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "You do not have access to this area.");
            return;
        }
        response.setHeader("Cache-Control", "no-store");
        chain.doFilter(req, res);
    }
}
