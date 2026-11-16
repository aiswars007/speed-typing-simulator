package com.speedtyping.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DatabaseConfig {

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/speedtyping_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";

    private static String url = DEFAULT_URL;
    private static String user = DEFAULT_USER;
    private static String password = DEFAULT_PASSWORD;
    private static boolean initialized = false;

    static {
        loadProperties();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
        }
    }

    private static void loadProperties() {
        Properties props = new Properties();
        try {
            File propFile = new File("db.properties");
            if (propFile.exists()) {
                try (InputStream in = new FileInputStream(propFile)) {
                    props.load(in);
                }
            } else {
                try (InputStream in = DatabaseConfig.class.getResourceAsStream("/db.properties")) {
                    if (in != null) {
                        props.load(in);
                    }
                }
            }
            if (props.containsKey("db.url")) {
                url = props.getProperty("db.url");
            }
            if (props.containsKey("db.user")) {
                user = props.getProperty("db.user");
            }
            if (props.containsKey("db.password")) {
                password = props.getProperty("db.password");
            }
        } catch (Exception e) {
            System.err.println("Notice: Using default database connection settings: " + e.getMessage());
        }

        // Environment variables override properties if defined
        String envUrl = System.getenv("DB_URL");
        if (envUrl != null && !envUrl.trim().isEmpty()) {
            url = envUrl.trim();
        }
        String envUser = System.getenv("DB_USER");
        if (envUser != null && !envUser.trim().isEmpty()) {
            user = envUser.trim();
        }
        String envPass = System.getenv("DB_PASSWORD");
        if (envPass != null) {
            password = envPass;
        }
    }

    public static Connection getConnection() throws SQLException {
        ensureDatabaseInitialized();
        return DriverManager.getConnection(url, user, password);
    }

    private static synchronized void ensureDatabaseInitialized() {
        if (initialized) return;

        // Try connecting directly or initialize DB if needed
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            migrateUserTableIfNeeded(conn);
            initialized = true;
        } catch (SQLException ex) {
            // Might need to create the database if it doesn't exist
            String rootUrl = "jdbc:mysql://localhost:3306/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            try (Connection rootConn = DriverManager.getConnection(rootUrl, user, password);
                 Statement stmt = rootConn.createStatement()) {
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS speedtyping_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;");
                stmt.executeUpdate("USE speedtyping_db;");
                createTables(stmt);
                initialized = true;
            } catch (SQLException e) {
                System.err.println("Warning during auto-database initialization: " + e.getMessage());
            }
        }
    }

    private static void migrateUserTableIfNeeded(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            // Expand password column to accommodate secure PBKDF2 hashes
            try {
                stmt.executeUpdate("ALTER TABLE users MODIFY COLUMN password VARCHAR(255) NOT NULL");
            } catch (SQLException ignored) {}
            try {
                stmt.executeUpdate("ALTER TABLE typing_texts MODIFY COLUMN content MEDIUMTEXT NOT NULL");
            } catch (SQLException ignored) {}

            // Auto-upgrade any legacy plaintext passwords to PBKDF2 hashes
            upgradeLegacyPasswords(conn);
        } catch (SQLException ignored) {}
    }

    private static void upgradeLegacyPasswords(Connection conn) {
        String selectSql = "SELECT id, password FROM users";
        try (Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(selectSql)) {
            java.util.Map<Integer, String> toUpdate = new java.util.HashMap<>();
            while (rs.next()) {
                int id = rs.getInt("id");
                String pass = rs.getString("password");
                if (pass != null && !com.speedtyping.service.SecurityUtils.isHashed(pass)) {
                    toUpdate.put(id, com.speedtyping.service.SecurityUtils.hashPassword(pass));
                }
            }
            if (!toUpdate.isEmpty()) {
                String updateSql = "UPDATE users SET password = ? WHERE id = ?";
                try (java.sql.PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    for (java.util.Map.Entry<Integer, String> entry : toUpdate.entrySet()) {
                        ps.setString(1, entry.getValue());
                        ps.setInt(2, entry.getKey());
                        ps.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Notice: Could not automatically migrate legacy passwords: " + e.getMessage());
        }
    }

    private static void createTables(Statement stmt) throws SQLException {
        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS users (" +
            "  id INT AUTO_INCREMENT PRIMARY KEY," +
            "  username VARCHAR(50) NOT NULL UNIQUE," +
            "  password VARCHAR(255) NOT NULL," +
            "  full_name VARCHAR(100) DEFAULT ''," +
            "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ") ENGINE=InnoDB;"
        );

        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS typing_texts (" +
            "  id INT AUTO_INCREMENT PRIMARY KEY," +
            "  title VARCHAR(100) NOT NULL," +
            "  difficulty VARCHAR(20) DEFAULT 'Medium'," +
            "  content MEDIUMTEXT NOT NULL" +
            ") ENGINE=InnoDB;"
        );

        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS typing_results (" +
            "  id INT AUTO_INCREMENT PRIMARY KEY," +
            "  user_id INT NOT NULL," +
            "  wpm INT NOT NULL," +
            "  raw_wpm INT NOT NULL," +
            "  accuracy DECIMAL(5,2) NOT NULL," +
            "  correct_chars INT NOT NULL," +
            "  incorrect_chars INT NOT NULL," +
            "  total_chars INT NOT NULL," +
            "  duration_seconds INT NOT NULL," +
            "  text_title VARCHAR(100) DEFAULT 'Standard Test'," +
            "  test_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "  CONSTRAINT fk_user_results FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
            ") ENGINE=InnoDB;"
        );
    }

    public static String getUrl() {
        return url;
    }

    public static String getUser() {
        return user;
    }
}
