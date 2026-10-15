package com.healthcare.dao;

import com.healthcare.model.Appointment;
import com.healthcare.model.AppointmentStatus;
import com.healthcare.util.DAOException;
import com.healthcare.util.DBConnection;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.sql.Date;

public class AppointmentDAO implements CrudDAO<Appointment> {

    private static final String BASE =
            "SELECT a.id,a.patient_id,a.doctor_id,a.appt_date,a.appt_time,a.status,a.reason,"
          + "p.name AS patient_name,d.name AS doctor_name FROM appointments a "
          + "JOIN users p ON a.patient_id=p.id JOIN users d ON a.doctor_id=d.id ";
    private static final String ORDER = " ORDER BY a.appt_date DESC, a.appt_time DESC";

    private Appointment map(ResultSet rs) throws SQLException {
        Appointment a = new Appointment();
        a.setId(rs.getInt("id"));
        a.setPatientId(rs.getInt("patient_id"));
        a.setDoctorId(rs.getInt("doctor_id"));
        a.setDate(rs.getDate("appt_date").toLocalDate());
        a.setTime(rs.getTime("appt_time").toLocalTime());
        a.setStatus(AppointmentStatus.valueOf(rs.getString("status")));
        a.setReason(rs.getString("reason"));
        a.setPatientName(rs.getString("patient_name"));
        a.setDoctorName(rs.getString("doctor_name"));
        return a;
    }

    private List<Appointment> query(String where, Object... args) throws DAOException {
        List<Appointment> list = new ArrayList<>();
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(BASE + where + ORDER)) {
            for (int i = 0; i < args.length; i++) ps.setObject(i + 1, args[i]);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        } catch (SQLException e) { throw new DAOException("Could not load appointments", e); }
        return list;
    }

    @Override
    public int insert(Appointment a) throws DAOException {
        String sql = "INSERT INTO appointments(patient_id,doctor_id,appt_date,appt_time,status,reason) VALUES(?,?,?,?,?,?)";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, a.getPatientId());
            ps.setInt(2, a.getDoctorId());
            ps.setDate(3, Date.valueOf(a.getDate()));
            ps.setTime(4, Time.valueOf(a.getTime()));
            ps.setString(5, a.getStatusName());
            ps.setString(6, a.getReason());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { return k.next() ? k.getInt(1) : 0; }
        } catch (SQLException e) { throw new DAOException("Could not book appointment: " + e.getMessage(), e); }
    }

    @Override
    public Appointment findById(int id) throws DAOException {
        List<Appointment> l = query("WHERE a.id=?", id);
        return l.isEmpty() ? null : l.get(0);
    }

    @Override public List<Appointment> findAll() throws DAOException { return query(""); }
    public List<Appointment> findByPatient(int patientId) throws DAOException { return query("WHERE a.patient_id=?", patientId); }
    public List<Appointment> findByDoctor(int doctorId) throws DAOException { return query("WHERE a.doctor_id=?", doctorId); }

    @Override
    public boolean update(Appointment a) throws DAOException {
        String sql = "UPDATE appointments SET appt_date=?,appt_time=?,status=?,reason=? WHERE id=?";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(a.getDate()));
            ps.setTime(2, Time.valueOf(a.getTime()));
            ps.setString(3, a.getStatusName());
            ps.setString(4, a.getReason());
            ps.setInt(5, a.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { throw new DAOException("Could not update appointment", e); }
    }

    public boolean updateStatus(int id, AppointmentStatus status) throws DAOException {
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement("UPDATE appointments SET status=? WHERE id=?")) {
            ps.setString(1, status.name());
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { throw new DAOException("Could not update status", e); }
    }

    @Override
    public boolean delete(int id) throws DAOException {
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement("DELETE FROM appointments WHERE id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { throw new DAOException("Could not delete appointment", e); }
    }

    /** Times already taken (status BOOKED) for a doctor on a date. */
    public List<LocalTime> bookedTimes(int doctorId, LocalDate date) throws DAOException {
        List<LocalTime> times = new ArrayList<>();
        String sql = "SELECT appt_time FROM appointments WHERE doctor_id=? AND appt_date=? AND status='BOOKED'";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ps.setDate(2, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) times.add(rs.getTime(1).toLocalTime()); }
        } catch (SQLException e) { throw new DAOException("Could not check availability", e); }
        return times;
    }

    /** Authorisation helper: has this doctor ever had an appointment with this patient? */
    public boolean hasRelationship(int doctorId, int patientId) throws DAOException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("SELECT 1 FROM appointments WHERE doctor_id=? AND patient_id=? LIMIT 1")) {
            ps.setInt(1, doctorId);
            ps.setInt(2, patientId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { throw new DAOException("Query failed", e); }
    }

    private Map<String, Integer> groupCount(String sql, List<String> seedKeys) throws DAOException {
        Map<String, Integer> m = new LinkedHashMap<>();
        for (String k : seedKeys) m.put(k, 0);
        try (Connection c = DBConnection.get(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) m.put(rs.getString(1), rs.getInt(2));
        } catch (SQLException e) { throw new DAOException("Analytics failed", e); }
        return m;
    }

    public Map<String, Integer> countByStatus() throws DAOException {
        List<String> keys = new ArrayList<>();
        for (AppointmentStatus s : AppointmentStatus.values()) keys.add(s.name());
        return groupCount("SELECT status, COUNT(*) FROM appointments GROUP BY status", keys);
    }

    public Map<String, Integer> countPerDoctor() throws DAOException {
        return groupCount("SELECT d.name, COUNT(*) FROM appointments a JOIN users d ON a.doctor_id=d.id "
                + "GROUP BY d.name ORDER BY COUNT(*) DESC", Collections.emptyList());
    }
}
