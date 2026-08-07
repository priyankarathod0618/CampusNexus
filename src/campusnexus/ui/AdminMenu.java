package campusnexus.ui;

import campusnexus.dao.CollegeDAO;
import campusnexus.dao.UserDAO;
import campusnexus.exception.DuplicateEmailException;
import campusnexus.exception.DuplicateRollNumberException;
import campusnexus.exception.InvalidDataException;
import campusnexus.model.College;
import campusnexus.model.Student;
import campusnexus.model.Teacher;
import campusnexus.service.AdminService;
import campusnexus.service.InputValidator;
import campusnexus.util.ActivityLogger;
import campusnexus.config.AppConfig;
import campusnexus.dao.CollegeAdminDAO;
import campusnexus.model.CollegeAdmin;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Set;

public class AdminMenu implements DashboardMenu {
    //private static final String ADMIN_CODE = "ADMIN@123";

    private final Scanner scanner;
    private final AdminService adminService = new AdminService();
    private final UserDAO userDAO = new UserDAO();
    private final CollegeDAO collegeDAO = new CollegeDAO();
    private final CollegeAdminDAO adminDAO = new CollegeAdminDAO();
    private CollegeAdmin loggedInAdmin;

    public AdminMenu(Scanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public void show() {

        System.out.println();
        System.out.println("===== College Admin Login =====");

        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        loggedInAdmin = adminDAO.login(email, password);

        if (loggedInAdmin == null) {
            System.out.println("Invalid email or password.");
            return;
        }

        System.out.println("\nWelcome " + loggedInAdmin.getName() + "!");

        boolean inAdmin = true;

        while (inAdmin) {

            printAdminMenu();

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> addStudent();
                case "2" -> addTeacher();
                case "3" -> viewCampusMembers();
                case "4" -> ActivityLogger.printByCollege(loggedInAdmin.getCollegeId());
                case "0" -> inAdmin = false;
                default -> System.out.println("Invalid option.");
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

            int collegeId = loggedInAdmin.getCollegeId();

            System.out.print("Enter name: ");
            String name = scanner.nextLine().trim();

            while (!InputValidator.isValidName(name)) {
                System.out.print("Invalid name. Enter again: ");
                name = scanner.nextLine().trim();
            }
            College college = collegeDAO.findById(collegeId);
            String domain = college.getEmailDomain().trim();

            System.out.print("Enter college email: ");
            String email = scanner.nextLine().trim();

            while (!InputValidator.isValidEmail(email)
                    || !email.toLowerCase().endsWith("@" + domain.toLowerCase())) {

                System.out.println("Email must end with @" + domain);
                System.out.print("Enter college email: ");
                email = scanner.nextLine().trim();
            }
            String phone;
            while (true) {
                System.out.print("Enter phone number: ");
                phone = scanner.nextLine().trim();

                if (!InputValidator.isValidPhone(phone)) {
                    System.out.println("Phone number must be exactly 10 digits and start with 6, 7, 8 or 9 (no spaces).");
                    continue;
                }

                break;
            }

            System.out.print("Enter roll number (digits only): ");
            String rollNumber = scanner.nextLine().trim();

            while (!InputValidator.isValidRollNumber(rollNumber)) {
                System.out.print("Invalid roll number. Enter again: ");
                rollNumber = scanner.nextLine().trim();
            }

            System.out.print("Enter branch: ");
            String branch="";
            while(true){
                System.out.println("Select Branch");
                System.out.println("1. CSE");
                System.out.println("2. IT");
                System.out.println("3. CE");
                System.out.println("4. Mechanical");
                System.out.println("5. Civil");
                System.out.print("Choice: ");

                switch(scanner.nextLine().trim()){
                    case "1" -> { branch="CSE"; break; }
                    case "2" -> { branch="IT"; break; }
                    case "3" -> { branch="CE"; break; }
                    case "4" -> { branch="Mechanical"; break; }
                    case "5" -> { branch="Civil"; break; }
                    default -> {
                        System.out.println("Invalid choice.");
                        continue;
                    }
                }
                break;
            }

            System.out.print("Enter year: ");
            String yearStr = scanner.nextLine().trim();
            while (!InputValidator.isValidYear(yearStr)) {
                System.out.print("Invalid year (1-4). Enter year: ");
                yearStr = scanner.nextLine().trim();
            }
            int year = Integer.parseInt(yearStr);

            String hostelBlock="";

            System.out.print("Do you stay in hostel? (Y/N): ");
            String stay=scanner.nextLine().trim();

            if(stay.equalsIgnoreCase("Y")){
                System.out.println("1. College Hostel");
                System.out.println("2. Private Hostel");
                System.out.print("Choice: ");
                String type=scanner.nextLine();

                if(type.equals("1")){
                    System.out.println("Select Block");
                    System.out.println("1. A");
                    System.out.println("2. B");
                    System.out.println("3. C");
                    System.out.println("4. D");
                    System.out.print("Choice: ");

                    switch(scanner.nextLine()){
                        case "1" -> hostelBlock="A";
                        case "2" -> hostelBlock="B";
                        case "3" -> hostelBlock="C";
                        case "4" -> hostelBlock="D";
                        default -> hostelBlock="A";
                    }
                }else{
                    hostelBlock="Private Hostel";
                }
            }else{
                hostelBlock="Non-Hosteller";
            }

            adminService.addStudent(name, email, phone, collegeId, rollNumber, branch, year, hostelBlock);

            System.out.println();
            System.out.println("Student account created successfully.");
            System.out.println("First-time password is the registered phone number.");

            ActivityLogger.log(loggedInAdmin.getCollegeId() + "|Admin added student: " + email);

        } catch (DuplicateEmailException | DuplicateRollNumberException | InvalidDataException e) {
            System.out.println(e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database error while adding student: " + e.getMessage());
        }
    }

    private void addTeacher() {
        try {
            System.out.println();
            System.out.println("===== Add Teacher Account =====");

            int collegeId = loggedInAdmin.getCollegeId();

            System.out.print("Enter name: ");
            String name = scanner.nextLine().trim();

            while (!InputValidator.isValidName(name)) {
                System.out.print("Invalid name. Enter again: ");
                name = scanner.nextLine().trim();
            }

            // BUGFIX: addStudent() already restricted the email to the admin's own
            // college domain; addTeacher() was missing the same check, which let a
            // teacher be registered under any email domain regardless of college.
            College college = collegeDAO.findById(collegeId);
            String domain = college.getEmailDomain().trim();

            System.out.print("Enter college email: ");
            String email = scanner.nextLine().trim();

            while (!InputValidator.isValidEmail(email)
                    || !email.toLowerCase().endsWith("@" + domain.toLowerCase())) {

                System.out.println("Email must end with @" + domain);
                System.out.print("Enter college email: ");
                email = scanner.nextLine().trim();
            }

            String phone;

            while (true) {
                System.out.print("Enter phone number: ");
                phone = scanner.nextLine().trim();

                if (!InputValidator.isValidPhone(phone)) {
                    System.out.println("Phone number must be exactly 10 digits and start with 6, 7, 8 or 9 (no spaces).");
                    continue;
                }

                break;
            }

            System.out.print("Enter employee ID: ");
            String employeeId = scanner.nextLine().trim();

            while (!InputValidator.isValidEmployeeId(employeeId)) {
                System.out.print("Invalid employee ID. Enter again: ");
                employeeId = scanner.nextLine().trim();
            }


            String department = "";

            while (true) {
                System.out.println("Select Department");
                System.out.println("1. CSE");
                System.out.println("2. IT");
                System.out.println("3. CE");
                System.out.println("4. Mechanical");
                System.out.println("5. Civil");
                System.out.print("Choice: ");

                switch (scanner.nextLine().trim()) {
                    case "1" -> department = "CSE";
                    case "2" -> department = "IT";
                    case "3" -> department = "CE";
                    case "4" -> department = "Mechanical";
                    case "5" -> department = "Civil";
                    default -> {
                        System.out.println("Invalid choice.");
                        continue;
                    }
                }
                break;
            }

            String subject = "";

            while (true) {

                System.out.println("Select Subject");

                switch (department) {
                    case "CSE" -> {
                        System.out.println("1. Java Programming");
                        System.out.println("2. Data Structures");
                        System.out.println("3. DBMS");
                        System.out.println("4. Operating Systems");
                        System.out.println("Enter your choice");

                        switch (scanner.nextLine().trim()) {
                            case "1" -> subject = "Java Programming";
                            case "2" -> subject = "Data Structures";
                            case "3" -> subject = "DBMS";
                            case "4" -> subject = "Operating Systems";
                            default -> {
                                System.out.println("Invalid choice.");
                                continue;
                            }
                        }
                    }

                    case "IT" -> {
                        System.out.println("1. Python");
                        System.out.println("2. Web Technology");
                        System.out.println("3. Computer Networks");

                        switch (scanner.nextLine().trim()) {
                            case "1" -> subject = "Python";
                            case "2" -> subject = "Web Technology";
                            case "3" -> subject = "Computer Networks";
                            default -> {
                                System.out.println("Invalid choice.");
                                continue;
                            }
                        }
                    }

                    case "CE" -> {
                        System.out.println("1. Structural Engineering");
                        System.out.println("2. Surveying");
                        switch (scanner.nextLine().trim()) {
                            case "1" -> subject = "Structural Engineering";
                            case "2" -> subject = "Surveying";
                            default -> {
                                System.out.println("Invalid choice.");
                                continue;
                            }
                        }
                    }

                    case "Mechanical" -> {
                        System.out.println("1. Thermodynamics");
                        System.out.println("2. Machine Design");
                        switch (scanner.nextLine().trim()) {
                            case "1" -> subject = "Thermodynamics";
                            case "2" -> subject = "Machine Design";
                            default -> {
                                System.out.println("Invalid choice.");
                                continue;
                            }
                        }
                    }

                    case "Civil" -> {
                        System.out.println("1. Structural Analysis");
                        System.out.println("2. Transportation Engineering");
                        switch (scanner.nextLine().trim()) {
                            case "1" -> subject = "Structural Analysis";
                            case "2" -> subject = "Transportation Engineering";
                            default -> {
                                System.out.println("Invalid choice.");
                                continue;
                            }
                        }
                    }
                }

                break;
            }
            adminService.addTeacher(name, email, phone, collegeId, employeeId, department, subject);

            System.out.println();
            System.out.println("Teacher account created successfully.");
            System.out.println("First-time password is the registered phone number.");

            ActivityLogger.log(loggedInAdmin.getCollegeId() + "|Admin added teacher: " + email);

        } catch (DuplicateEmailException | InvalidDataException e) {
            System.out.println(e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database error while adding teacher: " + e.getMessage());
        }
    }



    private void viewCampusMembers() {
        try {
            List<Student> students =
                    userDAO.findStudentsByCollege(loggedInAdmin.getCollegeId());

            List<Teacher> teachers =
                    userDAO.findTeachersByCollege(loggedInAdmin.getCollegeId());

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
