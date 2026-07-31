package campusnexus.dao;

import campusnexus.config.DatabaseConfig;
import campusnexus.exception.AlreadyRegisteredException;
import campusnexus.exception.EventFullException;
import campusnexus.model.Event;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EventDAO {

    public List<Event> findUpcoming() throws SQLException {
        String sql = """
                SELECT e.id, e.title, e.event_date, e.venue, e.created_by, e.description, e.capacity,
                       COUNT(er.student_id) AS registered_count
                FROM events e
                LEFT JOIN event_registrations er ON e.id = er.event_id
                WHERE e.event_date >= CURDATE()
                GROUP BY e.id, e.title, e.event_date, e.venue, e.created_by, e.description, e.capacity
                ORDER BY e.event_date
                """;
        return runQuery(sql, null, null);
    }

    // BETWEEN operator demo (Unit 7 topic)
    public List<Event> findBetweenDates(LocalDate start, LocalDate end) throws SQLException {
        String sql = """
                SELECT e.id, e.title, e.event_date, e.venue, e.created_by, e.description, e.capacity,
                       COUNT(er.student_id) AS registered_count
                FROM events e
                LEFT JOIN event_registrations er ON e.id = er.event_id
                WHERE e.event_date BETWEEN ? AND ?
                GROUP BY e.id, e.title, e.event_date, e.venue, e.created_by, e.description, e.capacity
                ORDER BY e.event_date
                """;
        return runQuery(sql, start, end);
    }

    private List<Event> runQuery(String sql, LocalDate start, LocalDate end) throws SQLException {
        List<Event> events = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (start != null) {
                ps.setDate(1, Date.valueOf(start));
                ps.setDate(2, Date.valueOf(end));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    events.add(new Event(
                            rs.getInt("id"), rs.getString("title"),
                            rs.getDate("event_date").toLocalDate(),
                            rs.getString("venue"), rs.getInt("created_by"), rs.getString("description"),
                            rs.getInt("capacity"), rs.getInt("registered_count")
                    ));
                }
            }
        }
        return events;
    }

    // Real transaction: lock the event row, check capacity, insert registration +
    // notification atomically, with a savepoint so a partial failure rolls back
    // cleanly. (Unit 8 topic demo)
    //
    // CHANGED: capacity check now uses "SELECT ... FOR UPDATE" so the event row is
    // locked for the transaction - closes a race where two students registering
    // for the last seat at the same instant could both pass the capacity check.
    //
    // CHANGED: a duplicate registration (same student, same event) used to bubble
    // up as a raw SQLIntegrityConstraintViolationException; it's now caught and
    // turned into AlreadyRegisteredException.
    public void registerStudentForEvent(int eventId, int studentId)
            throws EventFullException, AlreadyRegisteredException, SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);
            Savepoint savepoint = conn.setSavepoint("beforeRegistration");

            try {
                int capacity;
                String capacityLock = "SELECT capacity FROM events WHERE id = ? FOR UPDATE";
                try (PreparedStatement ps = conn.prepareStatement(capacityLock)) {
                    ps.setInt(1, eventId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Event not found.");
                        }
                        capacity = rs.getInt("capacity");
                    }
                }

                int registered;
                String countCheck = "SELECT COUNT(*) AS registered_count FROM event_registrations WHERE event_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(countCheck)) {
                    ps.setInt(1, eventId);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        registered = rs.getInt("registered_count");
                    }
                }

                if (registered >= capacity) {
                    conn.rollback(savepoint);
                    throw new EventFullException("This event is full (" + capacity + " seats taken).");
                }

                String insertReg = "INSERT INTO event_registrations (event_id, student_id) VALUES (?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertReg)) {
                    ps.setInt(1, eventId);
                    ps.setInt(2, studentId);
                    ps.executeUpdate();
                } catch (SQLIntegrityConstraintViolationException dup) {
                    conn.rollback(savepoint);
                    throw new AlreadyRegisteredException("You're already registered for this event.");
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