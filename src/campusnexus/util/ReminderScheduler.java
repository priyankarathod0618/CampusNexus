package campusnexus.util;

import campusnexus.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Multithreading demo (Java syllabus topic 4).
 * Runs on a background thread, independent of the console input thread,
 * and pushes a reminder notification for any event happening tomorrow.
 */
public class ReminderScheduler {
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public void start() {
        // First run 5 seconds after startup, then every 6 hours
        executor.scheduleAtFixedRate(this::sendUpcomingEventReminders, 5, 6 * 60 * 60, TimeUnit.SECONDS);
    }

    public void stop() {
        executor.shutdownNow();
    }

    private void sendUpcomingEventReminders() {
        String findEvents = "SELECT id, title FROM events WHERE event_date = ?";
        String findStudents = "SELECT student_id FROM event_registrations WHERE event_id = ?";
        String insertNotif = "INSERT INTO notifications (user_id, message, is_read, created_at) VALUES (?, ?, FALSE, NOW())";

        LocalDate tomorrow = LocalDate.now().plusDays(1);

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement eventStmt = conn.prepareStatement(findEvents)) {

            eventStmt.setDate(1, Date.valueOf(tomorrow));

            try (ResultSet events = eventStmt.executeQuery()) {
                while (events.next()) {
                    int eventId = events.getInt("id");
                    String title = events.getString("title");

                    try (PreparedStatement studentStmt = conn.prepareStatement(findStudents)) {
                        studentStmt.setInt(1, eventId);

                        try (ResultSet students = studentStmt.executeQuery()) {
                            while (students.next()) {
                                int studentId = students.getInt("student_id");

                                try (PreparedStatement notifStmt = conn.prepareStatement(insertNotif)) {
                                    notifStmt.setInt(1, studentId);
                                    notifStmt.setString(2, "Reminder: \"" + title + "\" is happening tomorrow.");
                                    notifStmt.executeUpdate();
                                }
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Reminder scheduler error: " + e.getMessage());
        }
    }
}
