package com.healthcare.servlet;

import com.healthcare.dao.*;
import com.healthcare.model.*;
import com.healthcare.service.MessageService;
import com.healthcare.service.ScheduleService;
import com.healthcare.util.*;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

/** Healthcare-professional dashboard: schedule, consultations, patient records, messages. */
@WebServlet("/doctor")
public class DoctorServlet extends BaseServlet {
    private final ScheduleDAO scheduleDAO = new ScheduleDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final ConsultationDAO consultationDAO = new ConsultationDAO();
    private final HealthRecordDAO recordDAO = new HealthRecordDAO();
    private final MessageDAO messageDAO = new MessageDAO();
    private final UserDAO userDAO = new UserDAO();
    private final ScheduleService scheduleService = new ScheduleService();
    private final MessageService messageService = new MessageService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User me = currentUser(req);
        String section = param(req, "section") == null ? "schedule" : param(req, "section");
        try {
            switch (section) {
                case "consultations": {
                    req.setAttribute("appointments", appointmentDAO.findByDoctor(me.getId()));
                    Map<Integer, Consultation> byAppt = consultationDAO.findByDoctor(me.getId()).stream()
                            .collect(Collectors.toMap(Consultation::getAppointmentId, c -> c, (a, b) -> a));
                    req.setAttribute("consultationMap", byAppt);
                    break;
                }
                case "records": {
                    req.setAttribute("patients", userDAO.findPatientsOfDoctor(me.getId()));
                    String pid = param(req, "patientId");
                    if (pid != null && !pid.isEmpty()) {
                        int patientId = intParam(req, "patientId");
                        // authorisation: only patients this doctor has treated/booked
                        if (!appointmentDAO.hasRelationship(me.getId(), patientId))
                            throw new ValidationException("You do not have access to this patient's records.");
                        req.setAttribute("patient", userDAO.findById(patientId));
                        req.setAttribute("records", recordDAO.findByPatient(patientId));
                        req.setAttribute("history", consultationDAO.findByPatient(patientId));
                    }
                    break;
                }
                case "messages":
                    req.setAttribute("messages", messageDAO.findInvolving(me.getId()));
                    req.setAttribute("recipients", userDAO.findPatientsOfDoctor(me.getId()));
                    break;
                default:
                    section = "schedule";
                    req.setAttribute("slots", scheduleDAO.findByDoctor(me.getId()));
            }
        } catch (ValidationException | DAOException e) {
            req.setAttribute("error", e.getMessage());
        }
        req.setAttribute("section", section);
        render(req, resp, "doctor.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User me = currentUser(req);
        String action = param(req, "action") == null ? "" : param(req, "action");
        String section = "schedule";
        String extra = "";
        try {
            switch (action) {
                case "addSlot":
                    scheduleService.addSlot(me.getId(), param(req, "day"), param(req, "start"), param(req, "end"));
                    flash(req, "success", "Schedule updated successfully.");
                    break;
                case "deleteSlot":
                    scheduleDAO.delete(intParam(req, "id"), me.getId());
                    flash(req, "success", "Working period removed.");
                    break;
                case "saveConsultation": {
                    section = "consultations";
                    Appointment a = appointmentDAO.findById(intParam(req, "appointmentId"));
                    if (a == null || a.getDoctorId() != me.getId()) throw new ValidationException("Appointment not found.");
                    if (a.getStatus() == AppointmentStatus.CANCELLED) throw new ValidationException("This appointment was cancelled.");
                    String advice = ValidationUtil.required(param(req, "advice"), "Medical advice");
                    String rx = param(req, "prescription");
                    consultationDAO.saveAndComplete(a.getId(), advice, rx == null || rx.isEmpty() ? null : rx);
                    flash(req, "success", "Consultation saved and appointment marked completed.");
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
            if ("saveConsultation".equals(action)) section = "consultations";
            if ("sendMessage".equals(action)) section = "messages";
        }
        redirect(req, resp, "/doctor?section=" + section + extra);
    }
}
