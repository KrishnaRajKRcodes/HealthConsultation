package com.healthcare.servlet;

import com.healthcare.dao.*;
import com.healthcare.model.*;
import com.healthcare.service.AppointmentService;
import com.healthcare.service.MessageService;
import com.healthcare.service.UserService;
import com.healthcare.util.*;
import java.io.IOException;
import java.time.LocalDate;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

/** Patient dashboard: booking, medical advice, health records, profile, messages. */
@WebServlet("/patient")
public class PatientServlet extends BaseServlet {
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final ConsultationDAO consultationDAO = new ConsultationDAO();
    private final HealthRecordDAO recordDAO = new HealthRecordDAO();
    private final MessageDAO messageDAO = new MessageDAO();
    private final ScheduleDAO scheduleDAO = new ScheduleDAO();
    private final UserDAO userDAO = new UserDAO();
    private final AppointmentService appointmentService = new AppointmentService();
    private final MessageService messageService = new MessageService();
    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User me = currentUser(req);
        String section = param(req, "section") == null ? "book" : param(req, "section");
        try {
            switch (section) {
                case "advice":
                    req.setAttribute("consultations", consultationDAO.findByPatient(me.getId()));
                    break;
                case "records":
                    req.setAttribute("records", recordDAO.findByPatient(me.getId()));
                    break;
                case "profile":
                    req.setAttribute("profile", userDAO.findById(me.getId()));
                    break;
                case "messages":
                    req.setAttribute("messages", messageDAO.findInvolving(me.getId()));
                    req.setAttribute("recipients", userDAO.findByRole(Role.DOCTOR));
                    break;
                default:
                    section = "book";
                    req.setAttribute("appointments", appointmentDAO.findByPatient(me.getId()));
                    req.setAttribute("doctors", userDAO.findByRole(Role.DOCTOR));
                    req.setAttribute("today", LocalDate.now());
                    String did = param(req, "doctorId");
                    String d = param(req, "date");
                    if (did != null && !did.isEmpty()) {
                        int doctorId = intParam(req, "doctorId");
                        req.setAttribute("selectedDoctor", userDAO.findById(doctorId));
                        req.setAttribute("selectedDoctorId", doctorId);
                        req.setAttribute("doctorSchedule", scheduleDAO.findByDoctor(doctorId));
                        if (d != null && !d.isEmpty()) {
                            LocalDate date = ValidationUtil.date(d, "Date");
                            req.setAttribute("selectedDate", date);
                            req.setAttribute("slots", appointmentService.freeSlots(doctorId, date));
                        }
                    }
            }
        } catch (ValidationException | DAOException e) {
            req.setAttribute("error", e.getMessage());
        }
        req.setAttribute("section", section);
        render(req, resp, "patient.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User me = currentUser(req);
        String action = param(req, "action") == null ? "" : param(req, "action");
        String section = "book";
        try {
            switch (action) {
                case "bookAppointment":
                    appointmentService.book(me.getId(), intParam(req, "doctorId"),
                            ValidationUtil.date(param(req, "date"), "Date"),
                            ValidationUtil.time(param(req, "time"), "Time"), param(req, "reason"));
                    flash(req, "success", "Booking confirmed! Your appointment has been scheduled.");
                    break;
                case "cancelAppointment":
                    appointmentService.cancelByPatient(intParam(req, "id"), me.getId());
                    flash(req, "success", "Appointment cancelled.");
                    break;
                case "addRecord":
                    section = "records";
                    recordDAO.insert(readRecord(req, me.getId(), false));
                    flash(req, "success", "Health record added.");
                    break;
                case "updateRecord":
                    section = "records";
                    if (!recordDAO.update(readRecord(req, me.getId(), true))) throw new ValidationException("Record not found.");
                    flash(req, "success", "Health record updated.");
                    break;
                case "deleteRecord":
                    section = "records";
                    if (!recordDAO.deleteForPatient(intParam(req, "id"), me.getId())) throw new ValidationException("Record not found.");
                    flash(req, "success", "Health record deleted.");
                    break;
                case "updateProfile": {
                    section = "profile";
                    User updated = userService.updateProfile(me, param(req, "name"), param(req, "phone"),
                            param(req, "dob"), req.getParameter("newPassword"));
                    updated.setPasswordHash(null);
                    req.getSession().setAttribute("user", updated);
                    flash(req, "success", "Profile updated successfully.");
                    break;
                }
                case "sendMessage":
                    section = "messages";
                    messageService.send(me, intParam(req, "receiverId"), req.getParameter("body"));
                    flash(req, "success", "Message sent.");
                    break;
                default:
                    throw new ValidationException("Unknown action.");
            }
        } catch (ValidationException | DAOException e) {
            flash(req, "error", e.getMessage());
            if (action.endsWith("Record")) section = "records";
            if ("updateProfile".equals(action)) section = "profile";
            if ("sendMessage".equals(action)) section = "messages";
        }
        redirect(req, resp, "/patient?section=" + section);
    }

    private HealthRecord readRecord(HttpServletRequest req, int patientId, boolean withId) throws ValidationException {
        HealthRecord r = new HealthRecord();
        if (withId) r.setId(intParam(req, "id"));
        r.setPatientId(patientId);
        r.setRecordType(ValidationUtil.required(param(req, "recordType"), "Record type"));
        r.setDescription(ValidationUtil.required(param(req, "description"), "Description"));
        r.setRecordDate(ValidationUtil.date(param(req, "recordDate"), "Record date"));
        return r;
    }
}
