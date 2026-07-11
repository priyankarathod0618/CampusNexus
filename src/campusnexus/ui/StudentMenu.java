package campusnexus.ui;

import campusnexus.dao.EventDAO;
import campusnexus.dao.NotificationDAO;
import campusnexus.model.Event;
import campusnexus.model.Notification;
import campusnexus.model.Student;
import campusnexus.service.AuthService;
import campusnexus.service.InputValidator;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class StudentMenu implements DashboardMenu {
    private final Scanner scanner;
    private final Student student;
    private final VisitorMenu visitorMenu;
    private final EventDAO eventDAO = new EventDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final AuthService authService = new AuthService();

    public StudentMenu(Scanner scanner, Student student) {
        this.scanner = scanner;
        this.student = student;
        this.visitorMenu = new VisitorMenu(scanner);
    }

    @Override
    public void show() {
        if (student.isMustChangePassword()) {
            forcePasswordChange();
        }

        boolean logout = false;
        while (!logout) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> viewProfile();
                case "2" -> visitorMenu.show();
                case "3" -> viewUpcomingEvents();
                case "4" -> viewNotifications();
                case "0" -> {
                    System.out.println("Logging out...");
                    logout = true;
                }
                case "5", "6", "7", "8", "9", "10" ->
                        System.out.println("This feature is coming in a future version.");
                default -> System.out.println("Invalid option. Try again.");
            }
        }
    }

    private void forcePasswordChange() {
        System.out.println();
        System.out.println("This is your first login. You must set a new password.");

        String newPassword;
        do {
            System.out.print("Enter new password (min 4 characters): ");
            newPassword = scanner.nextLine().trim();
        } while (!InputValidator.isNotEmpty(newPassword) || newPassword.length() < 4);

        try {
            authService.changePassword(student.getId(), newPassword);
            student.setPassword(newPassword);
            student.setMustChangePassword(false);
            System.out.println("Password updated. Welcome, " + student.getName() + "!");
        } catch (SQLException e) {
            System.out.println("Could not update password: " + e.getMessage());
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("====================================");
        System.out.println("        Student Dashboard");
        System.out.println("====================================");
        System.out.println("Welcome, " + student.getName() + "!");
        System.out.println();
        System.out.println("1. View Profile");
        System.out.println("2. Explore Colleges");
        System.out.println("3. Upcoming Events");
        System.out.println("4. Notifications");
        System.out.println("5. Academic Resources");
        System.out.println("6. Clubs");
        System.out.println("7. Senior Interaction");
        System.out.println("8. Skill Swap");
        System.out.println("9. Marketplace");
        System.out.println("10. Hostel Help");
        System.out.println("0. Logout");
        System.out.print("Choose an option: ");
    }

    private void viewProfile() {
        System.out.println();
        System.out.println("----- My Profile -----");
        System.out.println(student.getProfileDetails());
    }

    private void viewUpcomingEvents() {
        try {
            List<Event> events = eventDAO.findUpcoming();
            if (events.isEmpty()) {
                System.out.println("No upcoming events.");
                return;
            }

            System.out.println();
            System.out.println("----- Upcoming Events -----");
            for (Event e : events) {
                System.out.println(e.getId() + ". " + e.getTitle() + " - " + e.getEventDate() + " @ " + e.getVenue());
            }

            System.out.print("Enter event ID to register (0 to cancel): ");
            String input = scanner.nextLine().trim();
            int eventId = Integer.parseInt(input);
            if (eventId == 0) return;

            eventDAO.registerStudentForEvent(eventId, student.getId());
            System.out.println("Registered successfully! A confirmation notification was sent.");

        } catch (NumberFormatException e) {
            System.out.println("Invalid event ID.");
        } catch (SQLException e) {
            System.out.println("Could not complete registration, changes were rolled back: " + e.getMessage());
        }
    }

    private void viewNotifications() {
        try {
            List<Notification> recent = notificationDAO.findRecentByUser(student.getId(), 5);
            System.out.println();
            System.out.println("----- Recent Notifications -----");
            if (recent.isEmpty()) {
                System.out.println("No notifications yet.");
                return;
            }
            for (Notification n : recent) {
                System.out.println("[" + n.getCreatedAt() + "] " + n.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Could not load notifications: " + e.getMessage());
        }
    }
}
