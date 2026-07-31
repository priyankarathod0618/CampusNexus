package campusnexus.ui;

import campusnexus.exception.AccountNotFoundException;
import campusnexus.exception.InvalidCredentialsException;
import campusnexus.model.Person;
import campusnexus.model.Student;
import campusnexus.model.Teacher;
import campusnexus.service.AuthService;

import java.sql.SQLException;
import java.util.Scanner;

public class MainMenu {
    private final Scanner scanner = new Scanner(System.in);
    private final VisitorMenu visitorMenu = new VisitorMenu(scanner);
    private final AuthService authService = new AuthService();

    public void start() {
        boolean running = true;

        while (running) {
            printMainMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> visitorMenu.show();
                case "2" -> handleLogin();
                case "3" -> new AdminMenu(scanner).show();
                case "4" -> showAbout();
                case "0" -> {
                    System.out.println("Thank you for using CampusNexus. Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void handleLogin() {
        System.out.println();
        System.out.println("===== Login =====");
        System.out.print("Enter email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();

        try {
            Person person = authService.login(email, password);
            System.out.println();
            System.out.println("Login successful!");

            // Runtime polymorphism: same DashboardMenu reference, different actual class underneath
            DashboardMenu dashboard = (person instanceof Student student)
                    ? new StudentMenu(scanner, student)
                    : new TeacherMenu(scanner, (Teacher) person);
            dashboard.show();

        } catch (AccountNotFoundException | InvalidCredentialsException e) {
            System.out.println();
            System.out.println(e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database error during login: " + e.getMessage());
        }
    }

    private void printMainMenu() {
        System.out.println();
        System.out.println("===== Welcome to CampusNexus =====");
        System.out.println("1. Explore Colleges");
        System.out.println("2. Login as Campus Member");
        System.out.println("3. College Admin Setup");
        System.out.println("4. About CampusNexus");
        System.out.println("0. Exit");
        System.out.print("Choose an option: ");
    }

    private void showAbout() {
        System.out.println();
        System.out.println("========== About CampusNexus ==========");
        System.out.println();

        System.out.println("CampusNexus is a smart campus management and exploration system");
        System.out.println("designed to connect students, teachers, college administrators,");
        System.out.println("and visitors through a centralized digital platform.");
        System.out.println();

        System.out.println("The system provides college discovery features for visitors,");
        System.out.println("secure account management for students and teachers,");
        System.out.println("and administrative tools for managing campus members.");
        System.out.println();

        System.out.println("========== Key Features ==========");
        System.out.println();

        System.out.println("1. Visitor Module:");
        System.out.println("   - Explore available colleges and their details.");
        System.out.println("   - View college information before registration.");
        System.out.println();

        System.out.println("2. Student Module:");
        System.out.println("   - Secure student login system.");
        System.out.println("   - Access personal profile and campus services.");
        System.out.println("   - Manage academic-related information.");
        System.out.println();

        System.out.println("3. Teacher Module:");
        System.out.println("   - Teacher account management.");
        System.out.println("   - Access academic activities and assigned information.");
        System.out.println();

        System.out.println("4. College Admin Module:");
        System.out.println("   - Add and manage student accounts.");
        System.out.println("   - Add and manage teacher accounts.");
        System.out.println("   - View complete campus member information.");
        System.out.println("   - Monitor admin activities through activity logs.");
        System.out.println();

        System.out.println("========== Technical Implementation ==========");
        System.out.println();

        System.out.println("CampusNexus is developed using Java with JDBC integration");
        System.out.println("for database connectivity and MySQL for structured data storage.");
        System.out.println("The project follows a layered architecture:");
        System.out.println("- Model Layer: Represents system entities like Student, Teacher, College.");
        System.out.println("- DAO Layer: Handles database operations.");
        System.out.println("- Service Layer: Contains business logic.");
        System.out.println("- UI Layer: Provides user interaction.");
        System.out.println();

        System.out.println("The project also demonstrates Java concepts including:");
        System.out.println("- Object-Oriented Programming");
        System.out.println("- Exception Handling");
        System.out.println("- Collections Framework (HashMap, HashSet, PriorityQueue)");
        System.out.println("- Database Management using SQL and JDBC");
        System.out.println();

        System.out.println("========== Project Goal ==========");
        System.out.println();

        System.out.println("The goal of CampusNexus is to create a simple, efficient,");
        System.out.println("and scalable digital campus ecosystem that reduces manual");
        System.out.println("management efforts and provides easy access to campus services.");
        System.out.println();

        System.out.println("Developed as a Java + DBMS + Data Structures based project.");
        System.out.println("=========================================");
    }
}
