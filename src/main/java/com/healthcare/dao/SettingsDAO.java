package com.healthcare.dao;

import com.healthcare.util.DAOException;
import com.healthcare.util.DBConnection;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class SettingsDAO {
    public static final Map<String, String> DEFAULTS = new LinkedHashMap<>();
    static {
        DEFAULTS.put("site_name", "HealthConnect");
        DEFAULTS.put("slot_minutes", "30");
        DEFAULTS.put("max_advance_days", "60");
        DEFAULTS.put("allow_registration", "true");
    }

    public void ensureDefaults() throws DAOException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("INSERT IGNORE INTO settings(setting_key,setting_value) VALUES(?,?)")) {
            for (Map.Entry<String, String> e : DEFAULTS.entrySet()) {
                ps.setString(1, e.getKey());
                ps.setString(2, e.getValue());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) { throw new DAOException("Could not initialise settings", e); }
    }

    public Map<String, String> getAll() throws DAOException {
        Map<String, String> m = new LinkedHashMap<>(DEFAULTS);
        try (Connection c = DBConnection.get(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT setting_key,setting_value FROM settings")) {
            while (rs.next()) m.put(rs.getString(1), rs.getString(2));
        } catch (SQLException e) { throw new DAOException("Could not load settings", e); }
        return m;
    }

    public int getInt(String key, int fallback) {
        try { return Integer.parseInt(getAll().get(key)); } catch (Exception e) { return fallback; }
    }

    /** Saves all settings atomically. */
    public void saveAll(Map<String, String> values) throws DAOException {
        String sql = "INSERT INTO settings(setting_key,setting_value) VALUES(?,?) ON DUPLICATE KEY UPDATE setting_value=VALUES(setting_value)";
        try (Connection c = DBConnection.get()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                for (Map.Entry<String, String> e : values.entrySet()) {
                    ps.setString(1, e.getKey());
                    ps.setString(2, e.getValue());
                    ps.addBatch();
                }
                ps.executeBatch();
                c.commit();
            } catch (SQLException e) { c.rollback(); throw e; }
        } catch (SQLException e) { throw new DAOException("Could not save settings", e); }
    }
}
