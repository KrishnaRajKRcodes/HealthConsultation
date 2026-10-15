package com.healthcare.servlet;

import com.healthcare.dao.*;
import com.healthcare.model.*;
import com.healthcare.service.AppointmentService;
import com.healthcare.service.UserService;
import com.healthcare.util.*;
import java.io.IOException;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

/** Admin dashboard: users, appointments, system settings and analytics. */
@WebServlet("/admin")
public class AdminServlet extends BaseServlet {
    private final UserDAO userDAO = new UserDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final SettingsDAO settingsDAO = new SettingsDAO();
    private final UserService userService = new UserService();
    private final AppointmentService appointmentService = new AppointmentService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String section = param(req, "section") == null ? "analytics" : param(req, "section");
        try {
            switch (section) {
                case "users":
                    req.setAttribute("users", userDAO.findAll());
                    break;
                case "appointments":
                    req.setAttribute("appointments", appointmentDAO.findAll());
                    req.setAttribute("doctors", userDAO.findByRole(Role.DOCTOR));
                    req.setAttribute("patients", userDAO.findByRole(Role.PATIENT));
                    break;
                case "settings":
                    req.setAttribute("settings", settingsDAO.getAll());
                    break;
                default:
                    section = "analytics";
                    Map<String, Integer> byRole = userDAO.countByRole();
                    Map<String, Integer> byStatus = appointmentDAO.countByStatus();
                    req.setAttribute("usersByRole", byRole);
                    req.setAttribute("apptByStatus", byStatus);
                    req.setAttribute("apptPerDoctor", appointmentDAO.countPerDoctor());
                    req.setAttribute("totalUsers", byRole.values().stream().mapToInt(Integer::intValue).sum());
                    req.setAttribute("totalAppointments", byStatus.values().stream().mapToInt(Integer::intValue).sum());
            }
        } catch (DAOException e) {
            req.setAttribute("error", e.getMessage());
        }
        req.setAttribute("section", section);
        render(req, resp, "admin.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = param(req, "action") == null ? "" : param(req, "action");
        String section = "users";
        try {
            User me = currentUser(req);
            switch (action) {
                case "createUser":
                    userService.createUser(param(req, "name"), param(req, "email"), req.getParameter("password"),
                            roleParam(req, "role"), param(req, "phone"), param(req, "extra"));
                    flash(req, "success", "User created successfully.");
                    break;
                case "updateUser": {
                    int id = intParam(req, "id");
                    Role role = roleParam(req, "role");
                    boolean active = req.getParameter("active") != null;
                    if (id == me.getId() && (!active || role != Role.ADMIN))
                        throw new ValidationException("You cannot disable or demote your own account.");
                    userService.updateUser(id, param(req, "name"), param(req, "email"), role,
                            param(req, "phone"), param(req, "extra"), active);
                    flash(req, "success", "User updated successfully.");
                    break;
                }
                case "deleteUser": {
                    int id = intParam(req, "id");
                    if (id == me.getId()) throw new ValidationException("You cannot delete your own account.");
                    boolean deleted = userDAO.delete(id);
                    flash(req, deleted ? "success" : "error", deleted ? "User deleted." : "User not found.");
                    break;
                }
                case "bookAppointment":
                    section = "appointments";
                    appointmentService.book(intParam(req, "patientId"), intParam(req, "doctorId"),
                            ValidationUtil.date(param(req, "date"), "Date"),
                            ValidationUtil.time(param(req, "time"), "Time"), param(req, "reason"));
                    flash(req, "success", "Appointment scheduled successfully.");
                    break;
                case "setStatus":
                    section = "appointments";
                    try {
                        appointmentDAO.updateStatus(intParam(req, "id"), AppointmentStatus.valueOf(param(req, "status")));
                    } catch (IllegalArgumentException e) {
                        throw new ValidationException("Invalid status.");
                    }
                    flash(req, "success", "Appointment status updated.");
                    break;
                case "deleteAppointment":
                    section = "appointments";
                    appointmentDAO.delete(intParam(req, "id"));
                    flash(req, "success", "Appointment deleted.");
                    break;
                case "saveSettings":
                    section = "settings";
                    saveSettings(req);
                    flash(req, "success", "System settings updated successfully.");
                    break;
                default:
                    throw new ValidationException("Unknown action.");
            }
        } catch (ValidationException | DAOException e) {
            flash(req, "error", e.getMessage());
            if (action.contains("ppointment") || "setStatus".equals(action)) section = "appointments";
            if ("saveSettings".equals(action)) section = "settings";
        }
        redirect(req, resp, "/admin?section=" + section);
    }

    private void saveSettings(HttpServletRequest req) throws ValidationException, DAOException {
        String site = ValidationUtil.required(param(req, "site_name"), "Site name");
        int slot = ValidationUtil.integer(param(req, "slot_minutes"), "Slot length");
        int days = ValidationUtil.integer(param(req, "max_advance_days"), "Booking window");
        if (slot < 10 || slot > 120) throw new ValidationException("Slot length must be between 10 and 120 minutes.");
        if (days < 1 || days > 365) throw new ValidationException("Booking window must be between 1 and 365 days.");
        Map<String, String> m = new LinkedHashMap<>();
        m.put("site_name", site);
        m.put("slot_minutes", String.valueOf(slot));
        m.put("max_advance_days", String.valueOf(days));
        m.put("allow_registration", req.getParameter("allow_registration") != null ? "true" : "false");
        settingsDAO.saveAll(m);
        getServletContext().setAttribute("siteName", site);
    }
}
