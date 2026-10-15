package com.healthcare.servlet;

import com.healthcare.dao.SettingsDAO;
import com.healthcare.model.Role;
import com.healthcare.model.User;
import com.healthcare.service.UserService;
import com.healthcare.util.DAOException;
import com.healthcare.util.ValidationException;
import java.io.IOException;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

/** Login, patient self-registration and logout. */
@WebServlet({"/login", "/register", "/logout"})
public class AuthServlet extends BaseServlet {
    private final UserService userService = new UserService();
    private final SettingsDAO settingsDAO = new SettingsDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        if ("/logout".equals(path)) {
            HttpSession s = req.getSession(false);
            if (s != null) s.invalidate();
            redirect(req, resp, "/login");
            return;
        }
        User u = currentUser(req);
        if (u != null) { redirect(req, resp, u.getRole().getHome()); return; }
        if ("/register".equals(path)) {
            try {
                req.setAttribute("registrationOpen", "true".equals(settingsDAO.getAll().get("allow_registration")));
            } catch (DAOException e) {
                req.setAttribute("registrationOpen", false);
            }
            render(req, resp, "register.jsp");
        } else {
            render(req, resp, "login.jsp");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        try {
            if ("/login".equals(path)) {
                User u = userService.authenticate(param(req, "email"), req.getParameter("password"));
                HttpSession old = req.getSession(false);
                if (old != null) old.invalidate();                 // prevent session fixation
                HttpSession s = req.getSession(true);
                u.setPasswordHash(null);                           // never keep the hash in the session
                s.setAttribute("user", u);
                s.setAttribute("csrf", UUID.randomUUID().toString());
                redirect(req, resp, u.getRole().getHome());
                return;
            }
            if ("/register".equals(path)) {
                if (!"true".equals(settingsDAO.getAll().get("allow_registration")))
                    throw new ValidationException("Registration is currently closed.");
                String pw = req.getParameter("password");
                if (pw == null || !pw.equals(req.getParameter("confirm")))
                    throw new ValidationException("Passwords do not match.");
                userService.createUser(param(req, "name"), param(req, "email"), pw, Role.PATIENT,
                        param(req, "phone"), param(req, "dob"));
                flash(req, "success", "Account created. You can now sign in.");
                redirect(req, resp, "/login");
                return;
            }
        } catch (ValidationException | DAOException e) {
            flash(req, "error", e.getMessage());
            redirect(req, resp, path);
            return;
        }
        redirect(req, resp, "/login");
    }
}
