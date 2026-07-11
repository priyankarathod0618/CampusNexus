package campusnexus.ui;


import campusnexus.config.DatabaseConfig;
import campusnexus.dao.CollegeDAO;
import campusnexus.model.College;
import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.function.Predicate;

public class VisitorMenu implements DashboardMenu {
    private final Scanner scanner;
    private final CollegeDAO collegeDAO = new CollegeDAO();

    public VisitorMenu(Scanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public void show() {
        showExploreCollegesMenu();
    }

    public void showExploreCollegesMenu() {
        boolean exploring = true;

        while (exploring) {
            printExploreMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> showColleges(collegeDAO.findAll());
                case "2" -> searchByCity();
                case "3" -> compareColleges();
                case "4" -> showCoursesInfo();
                case "5" -> showFeesAndFacilities();
                case "6" -> filterByMaxFee();
                case "0" -> exploring = false;
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void printExploreMenu() {
        System.out.println();
        System.out.println("===== Explore Colleges =====");
        System.out.println("1. View All Colleges");
        System.out.println("2. Search College by City");
        System.out.println("3. Compare Colleges");
        System.out.println("4. View Courses");
        System.out.println("5. View Fees and Facilities");
        System.out.println("6. Filter by Maximum Fee");
        System.out.println("0. Back");
        System.out.print("Choose an option: ");
    }

    private void searchByCity() {
        System.out.print("Enter city name: ");
        String city = scanner.nextLine().trim();
        showColleges(collegeDAO.searchByCity(city));
    }

    private void compareColleges() {
        List<College> allColleges = collegeDAO.findAll();
        showColleges(allColleges);

        System.out.println();
        System.out.print("Enter college IDs to compare, separated by comma: ");
        String input = scanner.nextLine().trim();

        List<Integer> ids = new ArrayList<>();
        for (String value : input.split(",")) {
            try {
                ids.add(Integer.parseInt(value.trim()));
            } catch (NumberFormatException e) {
                System.out.println("Skipping invalid college ID: " + value.trim());
            }
        }

        showColleges(collegeDAO.findByIds(ids));
    }

    private void showCoursesInfo() {
        System.out.println();
        System.out.println("Courses shown for demo:");
        System.out.println("- Computer Engineering");
        System.out.println("- Information Technology");
        System.out.println("- Computer Science");
        System.out.println("- Electronics and Communication");
    }

    private void showFeesAndFacilities() {
        showColleges(collegeDAO.findAll());
    }

    // Lambda + Predicate demo (Java syllabus topic 1a), sorted with a Comparator lambda
    private void filterByMaxFee() {
        System.out.print("Enter maximum fee: ");
        String input = scanner.nextLine().trim();

        try {
            double maxFee = Double.parseDouble(input);
            Predicate<College> withinBudget = college -> college.getFees() <= maxFee;

            List<College> filtered = collegeDAO.findAll().stream()
                    .filter(withinBudget)
                    .sorted(Comparator.comparingDouble(College::getFees))
                    .toList();

            showColleges(filtered);
        } catch (NumberFormatException e) {
            System.out.println("Invalid fee amount.");
        }
    }

    private void showColleges(List<College> colleges) {
        System.out.println();

        if (colleges.isEmpty()) {
            System.out.println("No colleges found.");
            return;
        }

        for (College college : colleges) {
            System.out.println("----------------------------------------");
            System.out.println("ID: " + college.getId());
            System.out.println("Name: " + college.getName());
            System.out.println("City: " + college.getCity());
            System.out.println("Code: " + college.getCode());
            System.out.println("Email Domain: " + college.getEmailDomain());
            System.out.println("Fees: " + college.getFees());
            System.out.println("Hostel Available: " + (college.isHostelAvailable() ? "Yes" : "No"));
            System.out.println("Average Rating: " + formatRating(college.getAverageRating()));
            System.out.println("Facilities: " + college.getFacilities());
        }

        System.out.println("----------------------------------------");
    }

    private String formatRating(double rating) {
        if (rating == 0) {
            return "Not rated yet";
        }

        return String.format("%.1f/5", rating);
    }
    private void showAdvancedSearch() {
        System.out.println("\n--- Advanced Search (Subquery + BETWEEN + JOIN) ---");
        String sql = """
        SELECT c.name, c.city, COUNT(e.id) as event_count 
        FROM colleges c 
        LEFT JOIN events e ON c.id = e.college_id 
        WHERE c.fees BETWEEN ? AND ? 
        GROUP BY c.id
        HAVING event_count > 0
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, 50000);
            ps.setDouble(2, 100000);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println(rs.getString("name") + " (" + rs.getString("city") +
                            ") - Events: " + rs.getInt("event_count"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Query error: " + e.getMessage());
        }
    }
}
