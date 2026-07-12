package campusnexus.ui;

import campusnexus.dao.CollegeDAO;
import campusnexus.dao.UserDAO;
import campusnexus.exception.DuplicateEmailException;
import campusnexus.exception.DuplicateRollNumberException;
import campusnexus.model.College;
import campusnexus.model.Student;
import campusnexus.model.Teacher;
import campusnexus.service.AdminService;
import campusnexus.service.InputValidator;
import campusnexus.util.ActivityLogger;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Set;

public class AdminMenu implements DashboardMenu {
    private static final String ADMIN_CODE = "ADMIN@123";

    private final Scanner scanner;
    private final AdminService adminService = new AdminService();
    private final UserDAO userDAO = new UserDAO();
    private final CollegeDAO collegeDAO = new CollegeDAO();

    public AdminMenu(Scanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public void show() {
        System.out.println();
        System.out.println("===== Admin Login =====");
        System.out.print("Enter admin code: ");
        String code = scanner.nextLine().trim();

        if (!ADMIN_CODE.equals(code)) {
            System.out.println("Incorrect admin code.");
            return;
        }

        boolean inAdmin = true;
        while (inAdmin) {
            printAdminMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> addStudent();
                case "2" -> addTeacher();
                case "3" -> viewCampusMembers();
                case "4" -> ActivityLogger.printAll();
                case "0" -> inAdmin = false;
                default -> System.out.println("Invalid option. Try again.");
            }
        }
    }

    private void printAdminMenu() {
        System.out.println();
        System.out.println("===== College Admin Setup =====");
        System.out.println("1. Add Student Account");
        System.out.println("2. Add Teacher Account");
        System.out.println("3. View Campus Members");
        System.out.println("4. View Admin Activity Log");
        System.out.println("0. Back");
        System.out.print("Choose an option: ");
    }

    private void addStudent() {
        try {
            System.out.println();
            System.out.println("===== Add Student Account =====");

            int collegeId = pickCollege();
            if (collegeId == -1) return;

            System.out.print("Enter name: ");
            String name = scanner.nextLine().trim();

            System.out.print("Enter college email: ");
            String email = scanner.nextLine().trim();
            while (!InputValidator.isValidEmail(email)) {
                System.out.print("Invalid email. Enter college email: ");
                email = scanner.nextLine().trim();
            }

            System.out.print("Enter phone number: ");
            String phone = scanner.nextLine().trim();
            while (!InputValidator.isValidPhone(phone)) {
                System.out.print("Invalid phone number (10 digits). Enter phone number: ");
                phone = scanner.nextLine().trim();
            }

            System.out.print("Enter roll number: ");
            String rollNumber = scanner.nextLine().trim();

            System.out.print("Enter branch: ");
            String branch = scanner.nextLine().trim();

            System.out.print("Enter year: ");
            String yearStr = scanner.nextLine().trim();
            while (!InputValidator.isValidYear(yearStr)) {
                System.out.print("Invalid year (1-4). Enter year: ");
                yearStr = scanner.nextLine().trim();
            }
            int year = Integer.parseInt(yearStr);

            System.out.print("Enter hostel block: ");
            String hostelBlock = scanner.nextLine().trim();

            adminService.addStudent(name, email, phone, collegeId, rollNumber, branch, year, hostelBlock);

            System.out.println();
            System.out.println("Student account created successfully.");
            System.out.println("First-time password is the registered phone number.");

            ActivityLogger.log("Admin added student: " + email);

        } catch (DuplicateEmailException | DuplicateRollNumberException e) {
            System.out.println(e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database error while adding student: " + e.getMessage());
        }
    }

    private void addTeacher() {
        try {
            System.out.println();
            System.out.println("===== Add Teacher Account =====");

            int collegeId = pickCollege();
            if (collegeId == -1) return;

            System.out.print("Enter name: ");
            String name = scanner.nextLine().trim();

            System.out.print("Enter college email: ");
            String email = scanner.nextLine().trim();
            while (!InputValidator.isValidEmail(email)) {
                System.out.print("Invalid email. Enter college email: ");
                email = scanner.nextLine().trim();
            }

            System.out.print("Enter phone number: ");
            String phone = scanner.nextLine().trim();
            while (!InputValidator.isValidPhone(phone)) {
                System.out.print("Invalid phone number (10 digits). Enter phone number: ");
                phone = scanner.nextLine().trim();
            }

            System.out.print("Enter employee ID: ");
            String employeeId = scanner.nextLine().trim();

            System.out.print("Enter department: ");
            String department = scanner.nextLine().trim();

            System.out.print("Enter subject: ");
            String subject = scanner.nextLine().trim();

            adminService.addTeacher(name, email, phone, collegeId, employeeId, department, subject);

            System.out.println();
            System.out.println("Teacher account created successfully.");
            System.out.println("First-time password is the registered phone number.");

            ActivityLogger.log("Admin added teacher: " + email);

        } catch (DuplicateEmailException e) {
            System.out.println(e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database error while adding teacher: " + e.getMessage());
        }
    }

    private int pickCollege() {
        try {
            List<College> colleges = collegeDAO.findAll();
            if (colleges.isEmpty()) {
                System.out.println("No colleges found. Please run the seed data script first.");
                return -1;
            }

            System.out.println();
            System.out.println("Select college:");
            for (College c : colleges) {
                System.out.println(c.getId() + ". " + c.getName() + " (" + c.getCity() + ")");
            }
            System.out.print("Enter college ID: ");
            String input = scanner.nextLine().trim();
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid college ID.");
            return -1;
        }
    }

    private void viewCampusMembers() {
        try {
            List<Student> students = userDAO.findAllStudents();
            List<Teacher> teachers = userDAO.findAllTeachers();

            // Collections demo: HashSet for distinct branches, HashMap tallying students per
            // branch, PriorityQueue to rank by year
            Set<String> distinctBranches = new HashSet<>();
            Map<String, Integer> branchCounts = new HashMap<>();
            PriorityQueue<Student> byYearDesc = new PriorityQueue<>(
                    (a, b) -> Integer.compare(b.getYear(), a.getYear())
            );
            for (Student s : students) {
                distinctBranches.add(s.getBranch());
                branchCounts.merge(s.getBranch(), 1, Integer::sum);
                byYearDesc.add(s);
            }

            System.out.println();
            System.out.println("===== Campus Members =====");
            System.out.println("Total students: " + students.size());
            System.out.println("Total teachers: " + teachers.size());
            System.out.println("Branches represented: " + distinctBranches);
            System.out.println("Students per branch: " + branchCounts);

            System.out.println();
            System.out.println("-- Students (senior year first) --");
            while (!byYearDesc.isEmpty()) {
                Student s = byYearDesc.poll();
                System.out.println("- " + s.getName() + " | " + s.getBranch() + " Year " + s.getYear()
                        + " | " + s.getEmail());
            }

            System.out.println();
            System.out.println("-- Teachers --");
            for (Teacher t : teachers) {
                System.out.println("- " + t.getName() + " | " + t.getDepartment() + " | " + t.getSubject());
            }

        } catch (SQLException e) {
            System.out.println("Database error while loading campus members: " + e.getMessage());
        }
    }
}
