package campusnexus.ui;

import campusnexus.dao.*;
import campusnexus.exception.WeakPasswordException;
import campusnexus.model.*;
import campusnexus.service.AuthService;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;

public class TeacherMenu implements DashboardMenu {
    private final Scanner scanner;
    private final Teacher teacher;
    private final UserDAO userDAO = new UserDAO();
    private final ReportDAO reportDAO = new ReportDAO();
    private final AcademicResourceDAO academicResourceDAO = new AcademicResourceDAO();
    private final AnnouncementDAO announcementDAO = new AnnouncementDAO();
    private final QuestionDAO questionDAO = new QuestionDAO();
    private final HostelComplaintDAO hostelComplaintDAO = new HostelComplaintDAO();
    private final AnalyticsDAO analyticsDAO = new AnalyticsDAO();
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
                case "4" -> uploadResource();
                case "5" -> postAnnouncement();
                case "6" -> answerQuestions();
                case "7" -> resolveComplaint();
                case "8" -> basicAnalytics();
                case "0" -> {
                    System.out.println("Logging out...");
                    logout = true;
                }
                default -> System.out.println("Invalid option. Try again.");
            }
        }
    }

    private void forcePasswordChange() {
        System.out.println();
        System.out.println("This is your first login. You must set a new password.");

        boolean updated = false;
        while (!updated) {
            System.out.print("Enter new password: ");
            String newPassword = scanner.nextLine().trim();
            try {
                authService.changePassword(teacher.getId(), newPassword);
                teacher.setPassword(newPassword);
                teacher.setMustChangePassword(false);
                System.out.println("Password updated. Welcome, " + teacher.getName() + "!");
                updated = true;
            } catch (WeakPasswordException e) {
                System.out.println(e.getMessage());
            } catch (SQLException e) {
                System.out.println("Could not update password: " + e.getMessage());
                updated = true;
            }
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
        System.out.println("7. Resolve Hostel Complaint");
        System.out.println("8. Basic Analytics");
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

    private void uploadResource() {
        try {
            System.out.print("Enter title: ");
            String title = scanner.nextLine().trim();
            System.out.print("Enter type (PAPER/ASSIGNMENT/NOTES): ");
            String type = scanner.nextLine().trim();
            System.out.print("Enter subject: ");
            String subject = scanner.nextLine().trim();
            System.out.print("Enter target year (1-4): ");
            int year = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Enter description: ");
            String description = scanner.nextLine().trim();

            academicResourceDAO.upload(title, type, subject, year, teacher.getId(), description);
            System.out.println("Resource uploaded!");
        } catch (NumberFormatException e) {
            System.out.println("Invalid year.");
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    private void postAnnouncement() {
        try {
            System.out.print("Enter title: ");
            String title = scanner.nextLine().trim();
            System.out.print("Enter message: ");
            String message = scanner.nextLine().trim();
            System.out.print("Target branch (leave blank for all): ");
            String branch = scanner.nextLine().trim();
            System.out.print("Target year (leave blank for all): ");
            String yearInput = scanner.nextLine().trim();

            Integer year = yearInput.isBlank() ? null : Integer.parseInt(yearInput);
            String targetBranch = branch.isBlank() ? null : branch;

            announcementDAO.post(teacher.getId(), title, message, targetBranch, year);
            System.out.println("Announcement posted!");
        } catch (NumberFormatException e) {
            System.out.println("Invalid year.");
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    private void answerQuestions() {
        try {
            // Queue demo (Java syllabus topic 6a): process unanswered questions strictly FIFO,
            // oldest first - findUnanswered() already returns them oldest-first, so a Queue
            // makes that ordering guarantee explicit in the type itself.
            Queue<Question> pending = new LinkedList<>(questionDAO.findUnanswered());

            System.out.println();
            System.out.println("----- Unanswered Questions (oldest first) -----");
            if (pending.isEmpty()) {
                System.out.println("No unanswered questions. All caught up!");
                return;
            }
            for (Question q : pending) {
                System.out.println(q.getId() + ". " + q.getTitle() + " - by " + q.getStudentName()
                        + " : " + q.getDescription());
            }

            System.out.print("Enter question ID to answer (0 to cancel): ");
            int id = Integer.parseInt(scanner.nextLine().trim());
            if (id == 0) return;

            System.out.print("Enter your reply: ");
            String reply = scanner.nextLine().trim();
            questionDAO.addReply(id, teacher.getId(), reply);
            pending.poll();
            System.out.println("Reply posted!");

        } catch (NumberFormatException e) {
            System.out.println("Invalid question ID.");
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    private void resolveComplaint() {
        try {
            List<HostelComplaint> open = hostelComplaintDAO.findOpen();
            System.out.println();
            System.out.println("----- Open Hostel Complaints -----");
            if (open.isEmpty()) {
                System.out.println("No open complaints.");
                return;
            }
            for (HostelComplaint c : open) {
                System.out.println(c.getId() + ". [" + c.getStatus() + "] " + c.getCategory()
                        + " - " + c.getDescription() + " (" + c.getStudentName() + ")");
            }

            System.out.print("Enter complaint ID to resolve (0 to cancel): ");
            int id = Integer.parseInt(scanner.nextLine().trim());
            if (id == 0) return;

            hostelComplaintDAO.resolveComplaint(id);
            System.out.println("Marked resolved - the student has been notified automatically.");

        } catch (NumberFormatException e) {
            System.out.println("Invalid complaint ID.");
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    private void basicAnalytics() {
        try {
            System.out.println();
            System.out.println("===== Basic Analytics =====");

            System.out.println();
            System.out.println("-- Overall Stats (COUNT/AVG/MAX/MIN/SUM) --");
            analyticsDAO.getOverallStats().forEach((label, value) -> System.out.println(label + ": " + value));

            System.out.println();
            System.out.println("-- Popular Events (GROUP BY + HAVING) --");
            List<String> popularEvents = analyticsDAO.getPopularEvents();
            if (popularEvents.isEmpty()) {
                System.out.println("No events have registrations yet.");
            } else {
                popularEvents.forEach(System.out::println);
            }

            System.out.println();
            System.out.println("-- Colleges & Student Counts (RIGHT JOIN) --");
            analyticsDAO.getCollegesWithStudentCounts().forEach(System.out::println);

            System.out.println();
            System.out.println("-- Students Needing Follow-up (UNION) --");
            List<String> followUp = analyticsDAO.getStudentsNeedingFollowUp();
            if (followUp.isEmpty()) {
                System.out.println("Nobody needs follow-up right now.");
            } else {
                followUp.forEach(System.out::println);
            }

            System.out.println();
            System.out.println("-- College/Teacher Full Overview (FULL JOIN emulation) --");
            analyticsDAO.getCollegeTeacherFullOverview().forEach(System.out::println);

        } catch (SQLException e) {
            System.out.println("Could not generate analytics: " + e.getMessage());
        }
    }
}
