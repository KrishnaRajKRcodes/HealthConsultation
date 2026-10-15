package com.healthcare.dao;

import com.healthcare.model.Schedule;
import com.healthcare.util.DAOException;
import com.healthcare.util.DBConnection;
import java.sql.*;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

public class ScheduleDAO {
    private static final String SELECT = "SELECT id,doctor_id,day_of_week,start_time,end_time FROM schedules WHERE doctor_id=? ";

    private Schedule map(ResultSet rs) throws SQLException {
        Schedule s = new Schedule();
        s.setId(rs.getInt("id"));
        s.setDoctorId(rs.getInt("doctor_id"));
        s.setDay(DayOfWeek.valueOf(rs.getString("day_of_week")));
        s.setStartTime(rs.getTime("start_time").toLocalTime());
        s.setEndTime(rs.getTime("end_time").toLocalTime());
        return s;
    }

    public int insert(Schedule s) throws DAOException {
        String sql = "INSERT INTO schedules(doctor_id,day_of_week,start_time,end_time) VALUES(?,?,?,?)";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, s.getDoctorId());
            ps.setString(2, s.getDay().name());
            ps.setTime(3, Time.valueOf(s.getStartTime()));
            ps.setTime(4, Time.valueOf(s.getEndTime()));
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { return k.next() ? k.getInt(1) : 0; }
        } catch (SQLException e) { throw new DAOException("Could not save schedule", e); }
    }

    public List<Schedule> findByDoctor(int doctorId) throws DAOException {
        return query(SELECT + "ORDER BY FIELD(day_of_week,'MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY'), start_time", doctorId);
    }

    public List<Schedule> findByDoctorAndDay(int doctorId, DayOfWeek day) throws DAOException {
        return query(SELECT + "AND day_of_week=? ORDER BY start_time", doctorId, day.name());
    }

    private List<Schedule> query(String sql, Object... args) throws DAOException {
        List<Schedule> list = new ArrayList<>();
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < args.length; i++) ps.setObject(i + 1, args[i]);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        } catch (SQLException e) { throw new DAOException("Could not load schedule", e); }
        return list;
    }

    public boolean delete(int id, int doctorId) throws DAOException {
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement("DELETE FROM schedules WHERE id=? AND doctor_id=?")) {
            ps.setInt(1, id);
            ps.setInt(2, doctorId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { throw new DAOException("Could not delete schedule", e); }
    }
}
