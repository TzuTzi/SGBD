package org.example.demo11.DAO;

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

    public static Connection getConnection() throws SQLException {
        Properties props = loadConfig();
        String url = props.getProperty("db.url");
        String username = props.getProperty("db.username");
        String password = props.getProperty("db.password");
        return DriverManager.getConnection(url, username, password);
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

