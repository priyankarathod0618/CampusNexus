package campusnexus.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
    // EDIT THESE to match your local MySQL setup
    private static final String URL = "jdbc:mysql://localhost:3306/campusnexus";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";

    private DatabaseConfig() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}
