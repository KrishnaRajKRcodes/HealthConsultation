package com.healthcare.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Central JDBC connection factory; configuration comes from db.properties. */
public final class DBConnection {
    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            PROPS.load(in);
            Class.forName(PROPS.getProperty("driver"));
        } catch (Exception e) {
            throw new ExceptionInInitializerError("Cannot load db.properties / JDBC driver: " + e);
        }
    }

    private DBConnection() { }

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(PROPS.getProperty("url"),
                PROPS.getProperty("username"), PROPS.getProperty("password"));
    }
}
