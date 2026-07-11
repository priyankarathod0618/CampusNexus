package campusnexus.dao;

import campusnexus.config.DatabaseConfig;
import campusnexus.model.Announcement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class AnnouncementDAO {

    public void post(int teacherId, String title, String message, String targetBranch, Integer targetYear)
            throws SQLException {
        String sql = "INSERT INTO announcements (teacher_id, title, message, target_branch, target_year, created_at) " +
                "VALUES (?, ?, ?, ?, ?, NOW())";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teacherId);
            ps.setString(2, title);
            ps.setString(3, message);

            if (targetBranch == null || targetBranch.isBlank()) {
                ps.setNull(4, Types.VARCHAR);
            } else {
                ps.setString(4, targetBranch);
            }

            if (targetYear == null) {
                ps.setNull(5, Types.INTEGER);
            } else {
                ps.setInt(5, targetYear);
            }

            ps.executeUpdate();
        }
    }

    // AND/OR/IS NULL operators: matches announcements aimed at everyone OR this student's branch/year
    public List<Announcement> findRelevantForStudent(String branch, int year) throws SQLException {
        String sql = """
                SELECT a.id, u.name AS teacher_name, a.title, a.message, a.target_branch, a.target_year, a.created_at
                FROM announcements a
                INNER JOIN users u ON a.teacher_id = u.id
                WHERE (a.target_branch IS NULL OR a.target_branch = ?)
                  AND (a.target_year IS NULL OR a.target_year = ?)
                ORDER BY a.created_at DESC
                LIMIT 5
                """;

        List<Announcement> announcements = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, branch);
            ps.setInt(2, year);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int targetYearValue = rs.getInt("target_year");
                    announcements.add(new Announcement(
                            rs.getInt("id"), rs.getString("teacher_name"), rs.getString("title"),
                            rs.getString("message"), rs.getString("target_branch"),
                            rs.wasNull() ? null : targetYearValue,
                            rs.getTimestamp("created_at").toLocalDateTime()
                    ));
                }
            }
        }
        return announcements;
    }
}
