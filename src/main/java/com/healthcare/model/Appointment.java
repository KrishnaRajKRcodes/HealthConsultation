package com.healthcare.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Appointment {
    private int id, patientId, doctorId;
    private LocalDate date;
    private LocalTime time;
    private AppointmentStatus status = AppointmentStatus.BOOKED;
    private String reason, patientName, doctorName;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }
    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public LocalTime getTime() { return time; }
    public void setTime(LocalTime time) { this.time = time; }
    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }
    public String getStatusName() { return status.name(); }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
}
