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
        System.out.println("===== About CampusNexus =====");
        System.out.println("CampusNexus is a console-based smart campus companion system.");
        System.out.println("It helps visitors explore colleges, students access campus services,");
        System.out.println("and teachers manage academic activities.");
    }
}
