package org.example.sgbd_lab1.DAO;

import org.example.sgbd_lab1.DbException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class Database {
    private static final String CONFIG_PATH = "/config.properties";

    private Database() {
    }

    public static Properties loadConfig() {
        Properties props = new Properties();
        try (InputStream input = Database.class.getResourceAsStream(CONFIG_PATH)) {
            if (input == null) {
                throw new IllegalStateException("Missing " + CONFIG_PATH + " on classpath");
            }
            props.load(input);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load database config", ex);
        }
        validate(props);
        return props;
    }

    public static Connection getConnection() {
        Properties props;
        try {
            props = loadConfig();
        } catch (RuntimeException ex) {
            throw new DbException("Setările bazei de date sunt invalide. Verificați `src/main/resources/config.properties`.", ex);
        }

        String url = props.getProperty("db.url");
        String username = props.getProperty("db.username");
        String password = props.getProperty("db.password");

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException ex) {
            throw new DbException("Driverul PostgreSQL nu este disponibil. Asigurați-vă că dependența este inclusă.", ex);
        }

        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException ex) {
            throw DbException.fromSQLException("conectare la baza de date", ex);
        }
    }

    private static void validate(Properties props) {
        String url = props.getProperty("db.url");
        String username = props.getProperty("db.username");
        String password = props.getProperty("db.password");
        if (url == null || url.isBlank() || username == null || username.isBlank() || password == null) {
            throw new IllegalStateException("Database config must include db.url, db.username, db.password");
        }
    }
}
