package com.healthcare.service;

import com.healthcare.dao.*;
import com.healthcare.model.*;
import com.healthcare.util.*;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

/** Scheduling rules: working hours, slot length, no double-booking, booking window. */
public class AppointmentService {
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final ScheduleDAO scheduleDAO = new ScheduleDAO();
    private final UserDAO userDAO = new UserDAO();
    private final SettingsDAO settingsDAO = new SettingsDAO();

    public int book(int patientId, int doctorId, LocalDate date, LocalTime time, String reason)
            throws DAOException, ValidationException {
        User doctor = userDAO.findById(doctorId);
        User patient = userDAO.findById(patientId);
        if (doctor == null || doctor.getRole() != Role.DOCTOR || !doctor.isActive())
            throw new ValidationException("The selected doctor is not available.");
        if (patient == null || patient.getRole() != Role.PATIENT)
            throw new ValidationException("The selected patient is invalid.");

        Map<String, String> cfg = settingsDAO.getAll();
        int slot = Integer.parseInt(cfg.get("slot_minutes"));
        int maxDays = Integer.parseInt(cfg.get("max_advance_days"));

        if (LocalDateTime.of(date, time).isBefore(LocalDateTime.now()))
            throw new ValidationException("You cannot book an appointment in the past.");
        if (date.isAfter(LocalDate.now().plusDays(maxDays)))
            throw new ValidationException("Appointments can only be booked up to " + maxDays + " days ahead.");

        boolean withinShift = scheduleDAO.findByDoctorAndDay(doctorId, date.getDayOfWeek()).stream()
                .anyMatch(s -> !time.isBefore(s.getStartTime()) && !time.plusMinutes(slot).isAfter(s.getEndTime())
                        && time.plusMinutes(slot).isAfter(time));
        if (!withinShift) throw new ValidationException("The doctor does not work at that time.");
        if (conflicts(appointmentDAO.bookedTimes(doctorId, date), time, slot))
            throw new ValidationException("That time slot is already booked.");

        Appointment a = new Appointment();
        a.setPatientId(patientId);
        a.setDoctorId(doctorId);
        a.setDate(date);
        a.setTime(time);
        a.setReason(reason == null || reason.trim().isEmpty() ? null : reason.trim());
        a.setStatus(AppointmentStatus.BOOKED);
        return appointmentDAO.insert(a);
    }

    private boolean conflicts(List<LocalTime> booked, LocalTime t, int slotMinutes) {
        return booked.stream().anyMatch(b -> Math.abs(Duration.between(b, t).toMinutes()) < slotMinutes);
    }

    /** All still-free start times for a doctor on a date. */
    public List<LocalTime> freeSlots(int doctorId, LocalDate date) throws DAOException {
        int slot = settingsDAO.getInt("slot_minutes", 30);
        List<LocalTime> booked = appointmentDAO.bookedTimes(doctorId, date);
        LocalTime now = LocalTime.now();
        List<LocalTime> out = new ArrayList<>();
        for (Schedule s : scheduleDAO.findByDoctorAndDay(doctorId, date.getDayOfWeek())) {
            for (LocalTime t = s.getStartTime();
                 t.plusMinutes(slot).isAfter(t) && !t.plusMinutes(slot).isAfter(s.getEndTime());
                 t = t.plusMinutes(slot)) {
                out.add(t);
            }
        }
        return out.stream()
                .filter(t -> !date.equals(LocalDate.now()) || t.isAfter(now))
                .filter(t -> !conflicts(booked, t, slot))
                .distinct().sorted().collect(Collectors.toList());
    }

    /** A patient may cancel only their own, still-booked appointments. */
    public void cancelByPatient(int appointmentId, int patientId) throws DAOException, ValidationException {
        Appointment a = appointmentDAO.findById(appointmentId);
        if (a == null || a.getPatientId() != patientId) throw new ValidationException("Appointment not found.");
        if (a.getStatus() != AppointmentStatus.BOOKED) throw new ValidationException("Only booked appointments can be cancelled.");
        appointmentDAO.updateStatus(appointmentId, AppointmentStatus.CANCELLED);
    }
}
