package campusnexus.ui;

import campusnexus.dao.ReportDAO;
import campusnexus.dao.UserDAO;
import campusnexus.model.Teacher;
import campusnexus.service.AuthService;
import campusnexus.service.InputValidator;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class TeacherMenu implements DashboardMenu {
    private final Scanner scanner;
    private final Teacher teacher;
    private final UserDAO userDAO = new UserDAO();
    private final ReportDAO reportDAO = new ReportDAO();
    private final AuthService authService = new AuthService();

    public TeacherMenu(Scanner scanner, Teacher teacher) {
        this.scanner = scanner;
        this.teacher = teacher;
    }

    @Override
    public void show() {
        if (teacher.isMustChangePassword()) {
            forcePasswordChange();
        }

        boolean logout = false;
        while (!logout) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> viewProfile();
                case "2" -> viewStudents();
                case "3" -> viewComplaintReport();
                case "0" -> {
                    System.out.println("Logging out...");
                    logout = true;
                }
                case "4", "5", "6" -> System.out.println("This feature is coming in a future version.");
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
            authService.changePassword(teacher.getId(), newPassword);
            teacher.setPassword(newPassword);
            teacher.setMustChangePassword(false);
            System.out.println("Password updated. Welcome, " + teacher.getName() + "!");
        } catch (SQLException e) {
            System.out.println("Could not update password: " + e.getMessage());
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("====================================");
        System.out.println("        Teacher Dashboard");
        System.out.println("====================================");
        System.out.println("Welcome, " + teacher.getName() + "!");
        System.out.println();
        System.out.println("1. View Profile");
        System.out.println("2. View Students");
        System.out.println("3. Hostel Complaint Report (by block)");
        System.out.println("4. Upload Academic Resources");
        System.out.println("5. Post Announcements");
        System.out.println("6. Answer Student Questions");
        System.out.println("0. Logout");
        System.out.print("Choose an option: ");
    }

    private void viewProfile() {
        System.out.println();
        System.out.println("----- My Profile -----");
        System.out.println(teacher.getProfileDetails());
    }

    private void viewStudents() {
        try {
            List<String> directory = userDAO.findStudentDirectoryFromView();
            System.out.println();
            System.out.println("----- Student Directory (via vw_student_directory) -----");
            if (directory.isEmpty()) {
                System.out.println("No students found.");
                return;
            }
            directory.forEach(System.out::println);
        } catch (SQLException e) {
            System.out.println("Could not load students: " + e.getMessage());
        }
    }

    private void viewComplaintReport() {
        try {
            Map<String, Integer> summary = reportDAO.unresolvedComplaintsByHostelBlock();
            System.out.println();
            System.out.println("----- Unresolved Complaints by Hostel Block -----");
            if (summary.isEmpty()) {
                System.out.println("No unresolved complaints. All clear!");
                return;
            }
            summary.forEach((block, count) -> System.out.println(block + ": " + count + " open"));
        } catch (SQLException e) {
            System.out.println("Could not generate report: " + e.getMessage());
        }
    }
}
