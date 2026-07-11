package campusnexus.dao;

import campusnexus.config.DatabaseConfig;
import campusnexus.model.Question;
import campusnexus.model.QuestionReply;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QuestionDAO {

    public void postQuestion(int studentId, String title, String description) throws SQLException {
        String sql = "INSERT INTO questions (student_id, title, description, status, created_at) " +
                "VALUES (?, ?, ?, 'OPEN', NOW())";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setString(2, title);
            ps.setString(3, description);
            ps.executeUpdate();
        }
    }

    public List<Question> findAll() throws SQLException {
        String sql = """
                SELECT q.id, q.student_id, u.name AS student_name, q.title, q.description, q.status, q.created_at
                FROM questions q
                INNER JOIN users u ON q.student_id = u.id
                ORDER BY q.created_at DESC
                """;
        return runQuery(sql);
    }

    // Subquery + NOT EXISTS (Unit 7 topic demo): questions nobody has replied to yet
    public List<Question> findUnanswered() throws SQLException {
        String sql = """
                SELECT q.id, q.student_id, u.name AS student_name, q.title, q.description, q.status, q.created_at
                FROM questions q
                INNER JOIN users u ON q.student_id = u.id
                WHERE NOT EXISTS (
                    SELECT 1 FROM question_replies r WHERE r.question_id = q.id
                )
                ORDER BY q.created_at
                """;
        return runQuery(sql);
    }

    private List<Question> runQuery(String sql) throws SQLException {
        List<Question> questions = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                questions.add(new Question(
                        rs.getInt("id"), rs.getInt("student_id"), rs.getString("student_name"),
                        rs.getString("title"), rs.getString("description"), rs.getString("status"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                ));
            }
        }
        return questions;
    }

    public List<QuestionReply> findRepliesByQuestion(int questionId) throws SQLException {
        String sql = """
                SELECT r.id, r.question_id, u.name AS author_name, r.reply_text, r.created_at
                FROM question_replies r
                INNER JOIN users u ON r.user_id = u.id
                WHERE r.question_id = ?
                ORDER BY r.created_at
                """;
        List<QuestionReply> replies = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, questionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    replies.add(new QuestionReply(
                            rs.getInt("id"), rs.getInt("question_id"), rs.getString("author_name"),
                            rs.getString("reply_text"), rs.getTimestamp("created_at").toLocalDateTime()
                    ));
                }
            }
        }
        return replies;
    }

    public void addReply(int questionId, int userId, String replyText) throws SQLException {
        String insertReply = "INSERT INTO question_replies (question_id, user_id, reply_text, created_at) " +
                "VALUES (?, ?, ?, NOW())";
        String updateStatus = "UPDATE questions SET status = 'ANSWERED' WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(insertReply)) {
                ps.setInt(1, questionId);
                ps.setInt(2, userId);
                ps.setString(3, replyText);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(updateStatus)) {
                ps.setInt(1, questionId);
                ps.executeUpdate();
            }
        }
    }
}
