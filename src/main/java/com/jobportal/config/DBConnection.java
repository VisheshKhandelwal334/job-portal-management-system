package com.jobportal.config;

import com.jobportal.exception.PortalException;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Creates JDBC connections using settings from db.properties (file handling). */
public final class DBConnection {

    private static final String CONFIG_FILE = "db.properties";
    private static Properties props;

    private DBConnection() { }

    private static synchronized Properties load() throws PortalException {
        if (props == null) {
            Properties p = new Properties();
            try (FileInputStream in = new FileInputStream(CONFIG_FILE)) {
                p.load(in);
            } catch (IOException e) {
                throw new PortalException("Cannot read '" + CONFIG_FILE
                        + "'. Copy db.properties.example to db.properties and edit it.", e);
            }
            props = p;
        }
        return props;
    }

    public static Connection getConnection() throws PortalException {
        Properties p = load();
        try {
            return DriverManager.getConnection(
                    p.getProperty("db.url"), p.getProperty("db.user"), p.getProperty("db.password"));
        } catch (SQLException e) {
            throw new PortalException("Database connection failed: " + e.getMessage(), e);
        }
    }
}
