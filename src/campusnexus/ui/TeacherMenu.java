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
                case "3" -> uploadResource();
                case "4" -> postAnnouncement();
                case "5" -> answerQuestions();
                case "6" -> exportReports();
                case "7" -> {
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
        System.out.println("3. Upload Academic Resources");
        System.out.println("4. Post Announcements");
        System.out.println("5. Answer Student Questions");
        System.out.println("6. Export Reports to File");
        System.out.println("7. Logout");
        System.out.print("Enter your choice (1-7): ");
    }

    private void viewProfile() {
        System.out.println();
        System.out.println("----- My Profile -----");
        System.out.println(teacher.getProfileDetails());
    }

    private void viewStudents() {

        try {

            List<String> directory = userDAO.findStudentDirectoryFromView(
                    teacher.getCollegeName(),
                    teacher.getDepartment()
            );

            System.out.println();
            System.out.println("------------ Student Directory ------------");
            System.out.println("College    : " + teacher.getCollegeName());
            System.out.println("Department : " + teacher.getDepartment());
            System.out.println();

            if (directory.isEmpty()) {
                System.out.println("No students found.");
                return;
            }

            directory.forEach(System.out::println);

        } catch (SQLException e) {
            System.out.println("Could not load students: " + e.getMessage());
        }
    }


    private void uploadResource() {
        try {
            String title;

            while (true) {
                System.out.print("Enter title: ");
                title = scanner.nextLine().trim();

                if (!title.isEmpty())
                    break;

                System.out.println("Title cannot be empty.");
            }
            String type;

            while (true) {

                System.out.print("Enter type (PAPER/ASSIGNMENT/NOTES): ");

                type = scanner.nextLine().trim().toUpperCase();

                if (type.equals("PAPER")
                        || type.equals("ASSIGNMENT")
                        || type.equals("NOTES")) {
                    break;
                }

                System.out.println("Invalid type. Enter PAPER, ASSIGNMENT or NOTES.");
            }
            String subject;

            while (true) {

                System.out.print("Enter subject: ");

                subject = scanner.nextLine().trim();

                if (!subject.isEmpty())
                    break;

                System.out.println("Subject cannot be empty.");
            }
            int targetYear;

            while (true) {

                System.out.print("Enter target year (1-4): ");

                try {

                    targetYear = Integer.parseInt(scanner.nextLine());

                    if (targetYear >= 1 && targetYear <= 4)
                        break;

                    System.out.println("Year must be between 1 and 4.");

                } catch (NumberFormatException e) {

                    System.out.println("Please enter a valid number.");
                }
            }
            String description;

            while (true) {

                System.out.print("Enter description: ");

                description = scanner.nextLine().trim();

                if (!description.isEmpty())
                    break;

                System.out.println("Description cannot be empty.");
            }

            academicResourceDAO.upload(
                    title,
                    type,
                    subject,
                    targetYear,
                    teacher.getId(),
                    description
            );

            System.out.println("Resource uploaded!");

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    private void postAnnouncement() {
        try {
            String title;

            while (true) {
                System.out.print("Enter title: ");
                title = scanner.nextLine().trim();

                if (!title.isEmpty())
                    break;

                System.out.println("Title cannot be empty.");
            }

            String message;

            while (true) {
                System.out.print("Enter message: ");
                message = scanner.nextLine().trim();

                if (!message.isEmpty())
                    break;

                System.out.println("Message cannot be empty.");
            }
            Integer year = null;

            while (true) {

                System.out.print("Target year (leave blank for all): ");
                String yearInput = scanner.nextLine().trim();

                if (yearInput.isBlank()) {
                    break;
                }

                try {

                    int y = Integer.parseInt(yearInput);

                    if (y >= 1 && y <= 4) {
                        year = y;
                        break;
                    }

                    System.out.println("Year must be between 1 and 4.");

                } catch (NumberFormatException e) {

                    System.out.println("Please enter a valid number.");
                }
            }
            String targetBranch = null;

            while (true) {

                System.out.print("Target branch (leave blank for all): ");
                String branch = scanner.nextLine().trim().toUpperCase();

                if (branch.isBlank()) {
                    break;
                }

                if (branch.equals("CSE") ||
                        branch.equals("IT") ||
                        branch.equals("CE") ||
                        branch.equals("ICT") ||
                        branch.equals("CIVIL") ||
                        branch.equals("MECHANICAL")) {

                    targetBranch = branch;
                    break;
                }

                System.out.println("Invalid branch.");
            }

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

            Queue<Question> pending = new LinkedList<>(
                    questionDAO.findUnanswered(
                            teacher.getCollegeId(),
                            teacher.getDepartment()
                    )
            );

            System.out.println();
            System.out.println("----- Unanswered Questions (oldest first) -----");

            if (pending.isEmpty()) {
                System.out.println("No unanswered questions. All caught up!");
                return;
            }

            for (Question q : pending) {
                System.out.println(q.getId() + ". " + q.getTitle()
                        + " - by " + q.getStudentName()
                        + " : " + q.getDescription());
            }

            System.out.print("Enter question ID to answer (0 to cancel): ");
            int id = Integer.parseInt(scanner.nextLine().trim());

            if (id == 0)
                return;

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


    private void basicAnalytics() {
        try {
            System.out.println();
            System.out.println("===== Basic Analytics =====");

            System.out.println();
            System.out.println("-- Overall Stats (COUNT/AVG/MAX/MIN/SUM) --");
            analyticsDAO.getOverallStats(teacher.getCollegeId()).forEach((label, value) -> System.out.println(label + ": " + value));

            System.out.println();
            System.out.println("-- Popular Events (GROUP BY + HAVING) --");
            List<String> popularEvents = analyticsDAO.getPopularEvents(teacher.getCollegeId());
            if (popularEvents.isEmpty()) {
                System.out.println("No events have registrations yet.");
            } else {
                popularEvents.forEach(System.out::println);
            }

            System.out.println();
            System.out.println("-- Colleges & Student Counts (RIGHT JOIN) --");
            analyticsDAO.getCollegesWithStudentCounts(teacher.getCollegeId()).forEach(System.out::println);

            System.out.println();
            System.out.println("-- Students Needing Follow-up (UNION) --");
            List<String> followUp = analyticsDAO.getStudentsNeedingFollowUp(teacher.getCollegeId());
            if (followUp.isEmpty()) {
                System.out.println("Nobody needs follow-up right now.");
            } else {
                followUp.forEach(System.out::println);
            }

            System.out.println();
            System.out.println("-- College/Teacher Full Overview (FULL JOIN emulation) --");
            analyticsDAO.getCollegeTeacherFullOverview(teacher.getCollegeId()).forEach(System.out::println);

        } catch (SQLException e) {
            System.out.println("Could not generate analytics: " + e.getMessage());
        }
    }

    private void exportReports() {
        try {
            String path1 = campusnexus.util.ReportExportService.exportComplaintReport(
                    reportDAO.unresolvedComplaintsByHostelBlock());
            String path2 = campusnexus.util.ReportExportService.exportStudentDirectory(
                    userDAO.findStudentDirectoryFromView(
                            teacher.getCollegeName(),
                            teacher.getDepartment()
                    ));
            String path3 = campusnexus.util.ReportExportService.exportFollowUpList(
                    analyticsDAO.getStudentsNeedingFollowUp(teacher.getCollegeId()));

            System.out.println();
            System.out.println("Reports exported:");
            System.out.println("- " + path1);
            System.out.println("- " + path2);
            System.out.println("- " + path3);

        } catch (java.io.IOException e) {
            System.out.println("Could not write report file: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
}
