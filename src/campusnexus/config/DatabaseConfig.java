package campusnexus.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
    // Reads CAMPUSNEXUS_DB_URL / _USER / _PASSWORD env vars if set, otherwise
    // falls back to the old hardcoded local defaults.
    private static final String URL =
            System.getenv().getOrDefault("CAMPUSNEXUS_DB_URL", "jdbc:mysql://localhost:3306/campusnexus");
    private static final String USERNAME =
            System.getenv().getOrDefault("CAMPUSNEXUS_DB_USER", "root");
    private static final String PASSWORD =
            System.getenv().getOrDefault("CAMPUSNEXUS_DB_PASSWORD", "");

    private DatabaseConfig() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}