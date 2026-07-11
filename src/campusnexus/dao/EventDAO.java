package campusnexus.dao;

import campusnexus.config.DatabaseConfig;
import campusnexus.model.Event;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.List;

public class EventDAO {

    public List<Event> findUpcoming() throws SQLException {
        String sql = "SELECT id, title, event_date, venue, created_by, description " +
                "FROM events WHERE event_date >= CURDATE() ORDER BY event_date";

        List<Event> events = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                events.add(new Event(
                        rs.getInt("id"), rs.getString("title"),
                        rs.getDate("event_date").toLocalDate(),
                        rs.getString("venue"), rs.getInt("created_by"), rs.getString("description")
                ));
            }
        }
        return events;
    }

    // Real transaction: insert registration + insert notification as one atomic unit,
    // with a savepoint so a partial failure rolls back cleanly. (Unit 8 topic demo)
    public void registerStudentForEvent(int eventId, int studentId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);
            Savepoint savepoint = conn.setSavepoint("beforeRegistration");

            try {
                String insertReg = "INSERT INTO event_registrations (event_id, student_id) VALUES (?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertReg)) {
                    ps.setInt(1, eventId);
                    ps.setInt(2, studentId);
                    ps.executeUpdate();
                }

                String insertNotif = "INSERT INTO notifications (user_id, message, is_read, created_at) " +
                        "VALUES (?, ?, FALSE, NOW())";
                try (PreparedStatement ps = conn.prepareStatement(insertNotif)) {
                    ps.setInt(1, studentId);
                    ps.setString(2, "You are registered for event #" + eventId + ".");
                    ps.executeUpdate();
                }

                conn.commit();

            } catch (SQLException e) {
                conn.rollback(savepoint);
                throw e;
            }

        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
}
