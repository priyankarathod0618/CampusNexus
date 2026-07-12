package campusnexus.dao;

import campusnexus.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsDAO {

    // COUNT, ROUND, AVG, MAX, MIN, SUM - all in one query (Unit 4 topic)
    public Map<String, String> getOverallStats() throws SQLException {
        String sql = """
                SELECT
                    (SELECT COUNT(*) FROM student_profiles) AS total_students,
                    (SELECT COUNT(*) FROM teacher_profiles) AS total_teachers,
                    (SELECT COUNT(*) FROM events) AS total_events,
                    (SELECT ROUND(AVG(fees), 2) FROM colleges) AS avg_college_fee,
                    (SELECT MAX(price) FROM marketplace_items) AS max_item_price,
                    (SELECT MIN(price) FROM marketplace_items WHERE status = 'AVAILABLE') AS min_available_price,
                    (SELECT COALESCE(SUM(price), 0) FROM marketplace_items WHERE status = 'SOLD') AS total_sales
                """;

        Map<String, String> stats = new LinkedHashMap<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                stats.put("Total Students", rs.getString("total_students"));
                stats.put("Total Teachers", rs.getString("total_teachers"));
                stats.put("Total Events", rs.getString("total_events"));
                stats.put("Average College Fee", rs.getString("avg_college_fee"));
                stats.put("Highest Marketplace Price Ever Listed", rs.getString("max_item_price"));
                stats.put("Cheapest Available Item", rs.getString("min_available_price"));
                stats.put("Total Marketplace Sales", rs.getString("total_sales"));
            }
        }
        return stats;
    }

    // LEFT JOIN + GROUP BY + HAVING (Unit 5/7 topics): events with at least one registration
    public List<String> getPopularEvents() throws SQLException {
        String sql = """
                SELECT e.title, COUNT(er.student_id) AS registrations
                FROM events e
                LEFT JOIN event_registrations er ON e.id = er.event_id
                GROUP BY e.id, e.title
                HAVING COUNT(er.student_id) >= 1
                ORDER BY registrations DESC
                """;
        return runSimpleQuery(sql, "title", "registrations", " registrations");
    }

    // RIGHT JOIN (Unit 5/7 topic): every college listed, even ones with zero students
    public List<String> getCollegesWithStudentCounts() throws SQLException {
        String sql = """
                SELECT c.name, COUNT(sp.user_id) AS student_count
                FROM student_profiles sp
                RIGHT JOIN colleges c ON sp.college_id = c.id
                GROUP BY c.id, c.name
                ORDER BY student_count DESC
                """;
        return runSimpleQuery(sql, "name", "student_count", " students");
    }

    // UNION (Unit 4 topic): students who need follow-up, from two different sources, deduplicated
    public List<String> getStudentsNeedingFollowUp() throws SQLException {
        String sql = """
                SELECT DISTINCT u.name, u.email, 'Unresolved complaint' AS reason
                FROM hostel_complaints hc
                JOIN users u ON hc.student_id = u.id
                WHERE hc.status <> 'RESOLVED'
                UNION
                SELECT DISTINCT u.name, u.email, 'Unanswered question' AS reason
                FROM questions q
                JOIN users u ON q.student_id = u.id
                WHERE q.status = 'OPEN'
                  AND NOT EXISTS (SELECT 1 FROM question_replies r WHERE r.question_id = q.id)
                ORDER BY name
                """;

        List<String> lines = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lines.add(rs.getString("name") + " (" + rs.getString("email") + ") - " + rs.getString("reason"));
            }
        }
        return lines;
    }

    // FULL OUTER JOIN emulation (Unit 7 topic) - MySQL has no native FULL JOIN, so it's built
    // as LEFT JOIN UNION RIGHT JOIN. Every college appears whether or not it has a teacher,
    // and (were it possible under this schema's NOT NULL FK) every teacher would appear even
    // without a matching college - the RIGHT-JOIN half is included for completeness/syntax
    // even though every teacher here always has one.
    public List<String> getCollegeTeacherFullOverview() throws SQLException {
        String sql = """
                SELECT c.name AS college_name, u.name AS teacher_name
                FROM colleges c
                LEFT JOIN teacher_profiles tp ON c.id = tp.college_id
                LEFT JOIN users u ON tp.user_id = u.id
                UNION
                SELECT c.name AS college_name, u.name AS teacher_name
                FROM teacher_profiles tp
                JOIN users u ON tp.user_id = u.id
                RIGHT JOIN colleges c ON tp.college_id = c.id
                ORDER BY college_name
                """;

        List<String> lines = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String teacher = rs.getString("teacher_name");
                lines.add(rs.getString("college_name") + " - " + (teacher == null ? "(no teacher yet)" : teacher));
            }
        }
        return lines;
    }

    private List<String> runSimpleQuery(String sql, String labelCol, String countCol, String suffix) throws SQLException {
        List<String> lines = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lines.add(rs.getString(labelCol) + ": " + rs.getInt(countCol) + suffix);
            }
        }
        return lines;
    }
}
