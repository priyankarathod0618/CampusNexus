package campusnexus.dao;

import campusnexus.config.DatabaseConfig;
import campusnexus.model.CollegeAdmin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CollegeAdminDAO {

    public CollegeAdmin login(String email, String password) {

        String sql = """
                SELECT *
                FROM college_admins
                WHERE email = ?
                AND password = ?
                """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    return new CollegeAdmin(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getInt("college_id")
                    );
                }
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }

        return null;
    }
}