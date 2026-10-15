package com.healthcare.dao;

import com.healthcare.model.*;
import com.healthcare.util.DAOException;
import com.healthcare.util.DBConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.sql.Date;

public class UserDAO implements CrudDAO<User> {

    private static final String SELECT = "SELECT id,name,email,password_hash,role,specialization,phone,dob,active,created_at FROM users ";

    private User map(ResultSet rs) throws SQLException {
        User u = UserFactory.create(Role.valueOf(rs.getString("role")));
        u.setId(rs.getInt("id"));
        u.setName(rs.getString("name"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setPhone(rs.getString("phone"));
        u.setActive(rs.getBoolean("active"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) u.setCreatedAt(ts.toLocalDateTime());
        if (u instanceof Doctor) ((Doctor) u).setSpecialization(rs.getString("specialization"));
        if (u instanceof Patient) {
            Date d = rs.getDate("dob");
            if (d != null) ((Patient) u).setDateOfBirth(d.toLocalDate());
        }
        return u;
    }

    private String specializationOf(User u) { return u instanceof Doctor ? ((Doctor) u).getSpecialization() : null; }
    private LocalDate dobOf(User u) { return u instanceof Patient ? ((Patient) u).getDateOfBirth() : null; }

    private void bindProfile(PreparedStatement ps, int startIdx, User u) throws SQLException {
        ps.setString(startIdx, specializationOf(u));
        ps.setString(startIdx + 1, u.getPhone());
        LocalDate dob = dobOf(u);
        ps.setDate(startIdx + 2, dob == null ? null : Date.valueOf(dob));
    }

    @Override
    public int insert(User u) throws DAOException {
        String sql = "INSERT INTO users(name,email,password_hash,role,specialization,phone,dob,active) VALUES(?,?,?,?,?,?,?,?)";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getName());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getPasswordHash());
            ps.setString(4, u.getRoleName());
            bindProfile(ps, 5, u);
            ps.setBoolean(8, u.isActive());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { return keys.next() ? keys.getInt(1) : 0; }
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new DAOException("That email address is already registered.", e);
        } catch (SQLException e) {
            throw new DAOException("Could not create user: " + e.getMessage(), e);
        }
    }

    @Override
    public User findById(int id) throws DAOException {
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(SELECT + "WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        } catch (SQLException e) { throw new DAOException("Could not load user", e); }
    }

    public User findByEmail(String email) throws DAOException {
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(SELECT + "WHERE email=?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        } catch (SQLException e) { throw new DAOException("Could not load user", e); }
    }

    private List<User> query(String sql, Object... args) throws DAOException {
        List<User> list = new ArrayList<>();
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < args.length; i++) ps.setObject(i + 1, args[i]);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        } catch (SQLException e) { throw new DAOException("Could not load users", e); }
        return list;
    }

    @Override public List<User> findAll() throws DAOException { return query(SELECT + "ORDER BY role, name"); }

    public List<User> findByRole(Role role) throws DAOException {
        return query(SELECT + "WHERE role=? AND active=TRUE ORDER BY name", role.name());
    }

    /** Distinct patients who have (or had) an appointment with the given doctor. */
    public List<User> findPatientsOfDoctor(int doctorId) throws DAOException {
        return query("SELECT DISTINCT u.id,u.name,u.email,u.password_hash,u.role,u.specialization,u.phone,u.dob,u.active,u.created_at "
                + "FROM users u JOIN appointments a ON a.patient_id=u.id WHERE a.doctor_id=? ORDER BY u.name", doctorId);
    }

    @Override
    public boolean update(User u) throws DAOException {
        String sql = "UPDATE users SET name=?,email=?,role=?,specialization=?,phone=?,dob=?,active=? WHERE id=?";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getName());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getRoleName());
            bindProfile(ps, 4, u);
            ps.setBoolean(7, u.isActive());
            ps.setInt(8, u.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new DAOException("That email address is already registered.", e);
        } catch (SQLException e) { throw new DAOException("Could not update user", e); }
    }

    public boolean updatePassword(int id, String hash) throws DAOException {
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement("UPDATE users SET password_hash=? WHERE id=?")) {
            ps.setString(1, hash);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { throw new DAOException("Could not change password", e); }
    }

    @Override
    public boolean delete(int id) throws DAOException {
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement("DELETE FROM users WHERE id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { throw new DAOException("Could not delete user", e); }
    }

    public boolean existsWithRole(Role role) throws DAOException {
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement("SELECT 1 FROM users WHERE role=? LIMIT 1")) {
            ps.setString(1, role.name());
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { throw new DAOException("Query failed", e); }
    }

    /** Analytics: number of users per role. */
    public Map<String, Integer> countByRole() throws DAOException {
        Map<String, Integer> m = new LinkedHashMap<>();
        for (Role r : Role.values()) m.put(r.name(), 0);
        try (Connection c = DBConnection.get(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT role, COUNT(*) n FROM users GROUP BY role")) {
            while (rs.next()) m.put(rs.getString("role"), rs.getInt("n"));
        } catch (SQLException e) { throw new DAOException("Analytics failed", e); }
        return m;
    }
}
