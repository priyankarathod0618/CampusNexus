package campusnexus.dao;

import campusnexus.config.DatabaseConfig;
import campusnexus.model.HostelComplaint;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class HostelComplaintDAO {

    public void submitComplaint(int studentId, String category, String description) throws SQLException {
        String sql = "INSERT INTO hostel_complaints (student_id, category, description, status, created_at) " +
                "VALUES (?, ?, ?, 'OPEN', NOW())";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setString(2, category);
            ps.setString(3, description);
            ps.executeUpdate();
        }
    }

    public List<HostelComplaint> findByStudent(int studentId) throws SQLException {
        String sql = """
                SELECT hc.id, hc.student_id, u.name AS student_name, hc.category, hc.description,
                       hc.status, hc.created_at, hc.resolved_at
                FROM hostel_complaints hc
                INNER JOIN users u ON hc.student_id = u.id
                WHERE hc.student_id = ?
                ORDER BY hc.created_at DESC
                """;
        return runQuery(sql, studentId);
    }

    public List<HostelComplaint> findOpen() throws SQLException {
        String sql = """
                SELECT hc.id, hc.student_id, u.name AS student_name, hc.category, hc.description,
                       hc.status, hc.created_at, hc.resolved_at
                FROM hostel_complaints hc
                INNER JOIN users u ON hc.student_id = u.id
                WHERE hc.status <> 'RESOLVED'
                ORDER BY hc.created_at
                """;
        return runQuery(sql, null);
    }

    // Flipping status to RESOLVED fires trg_complaint_resolved_notify automatically
    public void resolveComplaint(int complaintId) throws SQLException {
        String sql = "UPDATE hostel_complaints SET status = 'RESOLVED', resolved_at = NOW() WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, complaintId);
            ps.executeUpdate();
        }
    }

    private List<HostelComplaint> runQuery(String sql, Integer studentId) throws SQLException {
        List<HostelComplaint> complaints = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (studentId != null) {
                ps.setInt(1, studentId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp resolvedTs = rs.getTimestamp("resolved_at");
                    complaints.add(new HostelComplaint(
                            rs.getInt("id"), rs.getInt("student_id"), rs.getString("student_name"),
                            rs.getString("category"), rs.getString("description"), rs.getString("status"),
                            rs.getTimestamp("created_at").toLocalDateTime(),
                            resolvedTs != null ? resolvedTs.toLocalDateTime() : null
                    ));
                }
            }
        }
        return complaints;
    }
}
