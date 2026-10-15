package com.healthcare.dao;

import com.healthcare.model.HealthRecord;
import com.healthcare.util.DAOException;
import com.healthcare.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HealthRecordDAO implements CrudDAO<HealthRecord> {
    private static final String SELECT = "SELECT id,patient_id,record_type,description,record_date FROM health_records ";

    private HealthRecord map(ResultSet rs) throws SQLException {
        HealthRecord r = new HealthRecord();
        r.setId(rs.getInt("id"));
        r.setPatientId(rs.getInt("patient_id"));
        r.setRecordType(rs.getString("record_type"));
        r.setDescription(rs.getString("description"));
        r.setRecordDate(rs.getDate("record_date").toLocalDate());
        return r;
    }

    @Override
    public int insert(HealthRecord r) throws DAOException {
        String sql = "INSERT INTO health_records(patient_id,record_type,description,record_date) VALUES(?,?,?,?)";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getPatientId());
            ps.setString(2, r.getRecordType());
            ps.setString(3, r.getDescription());
            ps.setDate(4, Date.valueOf(r.getRecordDate()));
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { return k.next() ? k.getInt(1) : 0; }
        } catch (SQLException e) { throw new DAOException("Could not save record", e); }
    }

    @Override
    public HealthRecord findById(int id) throws DAOException {
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(SELECT + "WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        } catch (SQLException e) { throw new DAOException("Could not load record", e); }
    }

    @Override public List<HealthRecord> findAll() throws DAOException { return load("", -1); }
    public List<HealthRecord> findByPatient(int patientId) throws DAOException { return load("WHERE patient_id=? ", patientId); }

    private List<HealthRecord> load(String where, int arg) throws DAOException {
        List<HealthRecord> list = new ArrayList<>();
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(SELECT + where + "ORDER BY record_date DESC, id DESC")) {
            if (arg >= 0) ps.setInt(1, arg);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        } catch (SQLException e) { throw new DAOException("Could not load records", e); }
        return list;
    }

    /** Only updates when the record belongs to r.getPatientId() (ownership check in SQL). */
    @Override
    public boolean update(HealthRecord r) throws DAOException {
        String sql = "UPDATE health_records SET record_type=?,description=?,record_date=? WHERE id=? AND patient_id=?";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, r.getRecordType());
            ps.setString(2, r.getDescription());
            ps.setDate(3, Date.valueOf(r.getRecordDate()));
            ps.setInt(4, r.getId());
            ps.setInt(5, r.getPatientId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { throw new DAOException("Could not update record", e); }
    }

    @Override
    public boolean delete(int id) throws DAOException {
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement("DELETE FROM health_records WHERE id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { throw new DAOException("Could not delete record", e); }
    }

    public boolean deleteForPatient(int id, int patientId) throws DAOException {
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement("DELETE FROM health_records WHERE id=? AND patient_id=?")) {
            ps.setInt(1, id);
            ps.setInt(2, patientId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { throw new DAOException("Could not delete record", e); }
    }
}
