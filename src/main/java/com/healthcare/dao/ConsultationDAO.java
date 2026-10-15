package com.healthcare.dao;

import com.healthcare.model.Consultation;
import com.healthcare.util.DAOException;
import com.healthcare.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConsultationDAO {
    private static final String BASE =
            "SELECT c.id,c.appointment_id,c.advice,c.prescription,c.created_at,a.appt_date,"
          + "d.name AS doctor_name,p.name AS patient_name FROM consultations c "
          + "JOIN appointments a ON c.appointment_id=a.id JOIN users d ON a.doctor_id=d.id JOIN users p ON a.patient_id=p.id ";

    private Consultation map(ResultSet rs) throws SQLException {
        Consultation c = new Consultation();
        c.setId(rs.getInt("id"));
        c.setAppointmentId(rs.getInt("appointment_id"));
        c.setAdvice(rs.getString("advice"));
        c.setPrescription(rs.getString("prescription"));
        c.setAppointmentDate(rs.getDate("appt_date").toLocalDate());
        c.setDoctorName(rs.getString("doctor_name"));
        c.setPatientName(rs.getString("patient_name"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) c.setCreatedAt(ts.toLocalDateTime());
        return c;
    }

    /**
     * Saves advice and marks the appointment COMPLETED in ONE transaction
     * (commit on success, rollback on failure).
     */
    public void saveAndComplete(int appointmentId, String advice, String prescription) throws DAOException {
        String upsert = "INSERT INTO consultations(appointment_id,advice,prescription) VALUES(?,?,?) "
                + "ON DUPLICATE KEY UPDATE advice=VALUES(advice), prescription=VALUES(prescription)";
        try (Connection con = DBConnection.get()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps1 = con.prepareStatement(upsert);
                 PreparedStatement ps2 = con.prepareStatement("UPDATE appointments SET status='COMPLETED' WHERE id=?")) {
                ps1.setInt(1, appointmentId);
                ps1.setString(2, advice);
                ps1.setString(3, prescription);
                ps1.executeUpdate();
                ps2.setInt(1, appointmentId);
                ps2.executeUpdate();
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) { throw new DAOException("Could not save consultation", e); }
    }

    private List<Consultation> query(String where, int id) throws DAOException {
        List<Consultation> list = new ArrayList<>();
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(BASE + where + " ORDER BY a.appt_date DESC, c.created_at DESC")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        } catch (SQLException e) { throw new DAOException("Could not load consultations", e); }
        return list;
    }

    public List<Consultation> findByPatient(int patientId) throws DAOException { return query("WHERE a.patient_id=?", patientId); }
    public List<Consultation> findByDoctor(int doctorId) throws DAOException { return query("WHERE a.doctor_id=?", doctorId); }
}
