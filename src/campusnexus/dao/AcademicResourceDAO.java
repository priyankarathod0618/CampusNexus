package campusnexus.dao;

import campusnexus.config.DatabaseConfig;
import campusnexus.model.AcademicResource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AcademicResourceDAO {

    public void upload(String title, String type, String subject, int year, int uploadedBy, String description)
            throws SQLException {
        String sql = "INSERT INTO academic_resources (title, type, subject, year, uploaded_by, description, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, NOW())";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, type);
            ps.setString(3, subject);
            ps.setInt(4, year);
            ps.setInt(5, uploadedBy);
            ps.setString(6, description);
            ps.executeUpdate();
        }
    }

    public List<AcademicResource> findAll() throws SQLException {
        String sql = """
                SELECT r.id, r.title, r.type, r.subject, r.year, u.name AS uploader_name, r.description, r.created_at
                FROM academic_resources r
                INNER JOIN users u ON r.uploaded_by = u.id
                ORDER BY r.created_at DESC
                """;
        return runQuery(sql, null);
    }

    public List<AcademicResource> searchBySubject(String keyword) throws SQLException {
        String sql = """
                SELECT r.id, r.title, r.type, r.subject, r.year, u.name AS uploader_name, r.description, r.created_at
                FROM academic_resources r
                INNER JOIN users u ON r.uploaded_by = u.id
                WHERE LOWER(r.subject) LIKE LOWER(CONCAT('%', ?, '%'))
                ORDER BY r.created_at DESC
                """;
        return runQuery(sql, keyword);
    }

    private List<AcademicResource> runQuery(String sql, String keyword) throws SQLException {
        List<AcademicResource> resources = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (keyword != null) {
                ps.setString(1, keyword);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resources.add(new AcademicResource(
                            rs.getInt("id"), rs.getString("title"), rs.getString("type"), rs.getString("subject"),
                            rs.getInt("year"), rs.getString("uploader_name"), rs.getString("description"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    ));
                }
            }
        }
        return resources;
    }
}
