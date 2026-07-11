package campusnexus.dao;

import campusnexus.config.DatabaseConfig;
import campusnexus.model.Notification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class NotificationDAO {

    public void insert(int userId, String message) throws SQLException {
        String sql = "INSERT INTO notifications (user_id, message, is_read, created_at) VALUES (?, ?, FALSE, NOW())";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, message);
            ps.executeUpdate();
        }
    }

    // Deque used as an in-memory "recent notifications" buffer (DS topic demo)
    public List<Notification> findRecentByUser(int userId, int limit) throws SQLException {
        String sql = "SELECT id, user_id, message, is_read, created_at FROM notifications " +
                "WHERE user_id = ? ORDER BY created_at DESC LIMIT ?";

        Deque<Notification> buffer = new ArrayDeque<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    buffer.addLast(new Notification(
                            rs.getInt("id"), rs.getInt("user_id"), rs.getString("message"),
                            rs.getBoolean("is_read"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    ));
                }
            }
        }
        return new ArrayList<>(buffer);
    }
}
