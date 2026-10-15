package com.healthcare.servlet;

import com.healthcare.model.Role;
import com.healthcare.model.User;
import com.healthcare.util.ValidationException;
import java.io.IOException;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.http.*;

/** Shared helpers: session user, flash messages (Post/Redirect/Get), CSRF token, view rendering. */
public abstract class BaseServlet extends HttpServlet {

    protected User currentUser(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        return s == null ? null : (User) s.getAttribute("user");
    }

    protected void flash(HttpServletRequest req, String type, String message) {
        req.getSession().setAttribute("flash", new String[]{type, message});
    }

    protected void render(HttpServletRequest req, HttpServletResponse resp, String view) throws ServletException, IOException {
        HttpSession s = req.getSession();
        if (s.getAttribute("csrf") == null) s.setAttribute("csrf", UUID.randomUUID().toString());
        Object f = s.getAttribute("flash");
        if (f != null) {
            req.setAttribute("flash", f);
            s.removeAttribute("flash");
        }
        req.getRequestDispatcher("/WEB-INF/views/" + view).forward(req, resp);
    }

    protected void redirect(HttpServletRequest req, HttpServletResponse resp, String path) throws IOException {
        resp.sendRedirect(req.getContextPath() + path);
    }

    protected String param(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return v == null ? null : v.trim();
    }

    protected int intParam(HttpServletRequest req, String name) throws ValidationException {
        try { return Integer.parseInt(param(req, name)); }
        catch (NumberFormatException e) { throw new ValidationException("Invalid request parameter: " + name); }
    }

    protected Role roleParam(HttpServletRequest req, String name) throws ValidationException {
        try { return Role.valueOf(param(req, name)); }
        catch (Exception e) { throw new ValidationException("Invalid role."); }
    }
}
