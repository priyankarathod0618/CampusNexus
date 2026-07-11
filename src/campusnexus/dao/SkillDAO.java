package campusnexus.dao;

import campusnexus.config.DatabaseConfig;
import campusnexus.model.Skill;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SkillDAO {

    public void offerSkill(int studentId, String skillName, String description) throws SQLException {
        String sql = "INSERT INTO skills (student_id, skill_name, description, created_at) VALUES (?, ?, ?, NOW())";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setString(2, skillName);
            ps.setString(3, description);
            ps.executeUpdate();
        }
    }

    public List<Skill> findAll() throws SQLException {
        String sql = """
                SELECT s.id, s.student_id, u.name AS student_name, s.skill_name, s.description, s.created_at
                FROM skills s
                INNER JOIN users u ON s.student_id = u.id
                ORDER BY s.created_at DESC
                """;
        return runQuery(sql, null);
    }

    public List<Skill> searchBySkillName(String keyword) throws SQLException {
        String sql = """
                SELECT s.id, s.student_id, u.name AS student_name, s.skill_name, s.description, s.created_at
                FROM skills s
                INNER JOIN users u ON s.student_id = u.id
                WHERE LOWER(s.skill_name) LIKE LOWER(CONCAT('%', ?, '%'))
                ORDER BY s.created_at DESC
                """;
        return runQuery(sql, keyword);
    }

    private List<Skill> runQuery(String sql, String keyword) throws SQLException {
        List<Skill> skills = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (keyword != null) {
                ps.setString(1, keyword);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    skills.add(new Skill(
                            rs.getInt("id"), rs.getInt("student_id"), rs.getString("student_name"),
                            rs.getString("skill_name"), rs.getString("description"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    ));
                }
            }
        }
        return skills;
    }
}
