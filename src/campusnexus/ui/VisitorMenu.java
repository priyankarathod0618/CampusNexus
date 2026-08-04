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
import campusnexus.dao.CourseDAO;
import campusnexus.model.Course;
import campusnexus.dao.CourseDAO;
import campusnexus.model.Course;


public class VisitorMenu implements DashboardMenu {
    private final Scanner scanner;
    private final CollegeDAO collegeDAO = new CollegeDAO();
    private final CourseDAO courseDAO = new CourseDAO();

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
                case "5" -> collegeAdmissionPredictor();
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
        System.out.println("5. College Admission Predictor");
        System.out.println("6. Filter by Maximum Fee");
        System.out.println("0. Back");
        System.out.print("Choose an option: ");
    }

    private void searchByCity() {
        System.out.print("Enter city name: ");
        String city = scanner.nextLine().trim();

        List<College> colleges = collegeDAO.searchByCity(city);

        if (colleges.isEmpty()) {
            System.out.println("\nNo colleges found in \"" + city + "\".");
            return;
        }

        showColleges(colleges);
    }

    private void compareColleges() {

        List<College> allColleges = collegeDAO.findAll();
        showColleges(allColleges);

        System.out.print("\nEnter college IDs to compare (comma separated): ");

        String input = scanner.nextLine().trim();

        List<Integer> ids = new ArrayList<>();

        for (String s : input.split(",")) {
            try {
                ids.add(Integer.parseInt(s.trim()));
            } catch (NumberFormatException e) {
                System.out.println("'" + s + "' is not a valid ID.");
            }
        }

        if (ids.size() < 2) {
            System.out.println("Please enter at least two college IDs.");
            return;
        }

        List<College> colleges = collegeDAO.findByIds(ids);

        if (colleges.size() != ids.size()) {
            System.out.println("One or more college IDs do not exist.");
            return;
        }

        System.out.println("\n===================== COLLEGE COMPARISON =====================");

        System.out.printf("%-35s %-12s %-10s %-12s %-10s%n",
                "College", "Fees", "Rating", "Hostel", "City");

        System.out.println("--------------------------------------------------------------------------");

        College best = colleges.get(0);

        for (College c : colleges) {

            System.out.printf("%-35s ₹%-11.0f %-10s %-12s %-10s%n",
                    c.getName(),
                    c.getFees(),
                    formatRating(c.getAverageRating()),
                    c.isHostelAvailable() ? "Yes" : "No",
                    c.getCity());

            if (c.getAverageRating() > best.getAverageRating()) {
                best = c;
            } else if (c.getAverageRating() == best.getAverageRating()
                    && c.getFees() < best.getFees()) {
                best = c;
            }
        }

        System.out.println("--------------------------------------------------------------------------");

        System.out.println("\n=============== RECOMMENDATION ===============");

        System.out.println("🏆 Best Overall College : " + best.getName());
        System.out.println("📍 City                : " + best.getCity());
        System.out.println("⭐ Rating              : " + formatRating(best.getAverageRating()));
        System.out.println("💰 Fees                : ₹" + best.getFees());
        System.out.println("🏠 Hostel              : " + (best.isHostelAvailable() ? "Available" : "Not Available"));

        System.out.println("\nReason:");

        if (best.getAverageRating() >= 4.5)
            System.out.println("✔ Highest student rating.");

        if (best.getFees() <= 100000)
            System.out.println("✔ Affordable fee structure.");

        if (best.isHostelAvailable())
            System.out.println("✔ Hostel facility available.");

        System.out.println("✔ Recommended based on rating and fee.");
    }

    private void showCoursesInfo() {

        List<College> colleges = collegeDAO.findAll();

        System.out.println("\n============== Available Colleges ==============");

        for (College c : colleges) {
            System.out.printf("%-4d %-35s%n",
                    c.getId(),
                    c.getName());
        }

        System.out.print("\nEnter College ID: ");

        int collegeId;

        try {
            collegeId = Integer.parseInt(scanner.nextLine());

        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
            return;
        }

        List<Course> courses = courseDAO.findByCollegeId(collegeId);

        if (courses.isEmpty()) {
            System.out.println("\nNo courses found for this college.");
            return;
        }

        System.out.println("\n==================== COURSES ====================");

        System.out.printf("%-5s %-40s %-15s %-15s%n",
                "ID",
                "Course Name",
                "Duration",
                "Annual Fee");

        System.out.println("--------------------------------------------------------------------------");

        for (Course course : courses) {

            System.out.printf("%-5d %-40s %-15s ₹%-15.0f%n",
                    course.getId(),
                    course.getCourseName(),
                    course.getDuration(),
                    course.getAnnualFee());
        }

        System.out.println("--------------------------------------------------------------------------");
    }

    private void collegeAdmissionPredictor() {

        System.out.println("\n========== College Admission Predictor ==========");

        System.out.print("Preferred City : ");
        String city = scanner.nextLine().trim();

        double budget;

        while (true) {

            System.out.print("Maximum Annual Budget (₹) : ");

            try {
                budget = Double.parseDouble(scanner.nextLine());

                if (budget > 0)
                    break;

                System.out.println("Enter a valid budget.");

            } catch (NumberFormatException e) {

                System.out.println("Invalid amount.");
            }
        }

        int hostelChoice;

        while (true) {

            System.out.println("\nHostel Required?");
            System.out.println("1. Yes");
            System.out.println("2. No");
            System.out.print("Choice : ");

            String input = scanner.nextLine();

            if (input.equals("1") || input.equals("2")) {
                hostelChoice = Integer.parseInt(input);
                break;
            }

            System.out.println("Please enter 1 or 2.");
        }

        boolean hostelRequired = hostelChoice == 1;

        List<College> colleges = collegeDAO.findAll();

        List<College> matched = new ArrayList<>();

        for (College c : colleges) {

            if (c.getCity().equalsIgnoreCase(city)
                    && c.getFees() <= budget
                    && (!hostelRequired || c.isHostelAvailable())) {

                matched.add(c);
            }
        }

        if (matched.isEmpty()) {

            System.out.println("\nNo colleges found.");

            System.out.println("\nSuggestions:");
            System.out.println("• Increase your budget.");
            System.out.println("• Choose another city.");
            System.out.println("• Disable hostel requirement.");

            return;
        }

        matched.sort((a, b) ->
                Double.compare(b.getAverageRating(), a.getAverageRating()));

        System.out.println("\n================ Recommended Colleges ================");

        for (College c : matched) {

            System.out.println("------------------------------------------------");

            System.out.println("College : " + c.getName());
            System.out.println("City    : " + c.getCity());
            System.out.println("Fees    : ₹" + c.getFees());
            System.out.println("Rating  : " + formatRating(c.getAverageRating()));
            System.out.println("Hostel  : " +
                    (c.isHostelAvailable() ? "Available" : "Not Available"));

            System.out.println("\nReason");

            System.out.println("✔ Within your budget");

            if (c.isHostelAvailable())
                System.out.println("✔ Hostel available");

            if (c.getAverageRating() >= 4.5)
                System.out.println("✔ Highly rated college");

            System.out.println("✔ Matches your preferred city");
        }

        College best = matched.get(0);

        System.out.println("\n================================================");

        System.out.println("🏆 BEST RECOMMENDATION");

        System.out.println(best.getName());

        System.out.println("Rating : " + formatRating(best.getAverageRating()));

        System.out.println("Fees   : ₹" + best.getFees());

        System.out.println("================================================");
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
            if (filtered.isEmpty()) {
                System.out.println("No colleges found with fees below ₹" + maxFee);
                return;
            }
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

        System.out.println("------------------------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-4s %-35s %-15s %-12s %-10s %-12s %-10s%n",
                "ID", "College Name", "City", "Fees", "Hostel", "Rating", "Code");
        System.out.println("------------------------------------------------------------------------------------------------------------------------------");

        for (College c : colleges) {

            String rating;

            if (c.getAverageRating() == 0) {
                rating = "Not Rated";
            } else {
                rating = String.format("%.1f ★", c.getAverageRating());
            }

            System.out.printf("%-4d %-35s %-15s ₹%-11.0f %-10s %-12s %-10s%n",
                    c.getId(),
                    c.getName(),
                    c.getCity(),
                    c.getFees(),
                    c.isHostelAvailable() ? "Yes" : "No",
                    rating,
                    c.getCode());
        }

        System.out.println("------------------------------------------------------------------------------------------------------------------------------");
        System.out.println("Total Colleges : " + colleges.size());
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
    private College getHighestRatedCollege(List<College> colleges) {
        College best = colleges.get(0);

        for (College c : colleges) {
            if (c.getAverageRating() > best.getAverageRating()) {
                best = c;
            }
        }

        return best;
    }

    private College getLowestFeeCollege(List<College> colleges) {
        College cheapest = colleges.get(0);

        for (College c : colleges) {
            if (c.getFees() < cheapest.getFees()) {
                cheapest = c;
            }
        }

        return cheapest;
    }

    private College getBestValueCollege(List<College> colleges) {

        College best = colleges.get(0);

        double bestScore = best.getAverageRating() / best.getFees();

        for (College c : colleges) {

            double score = c.getAverageRating() / c.getFees();

            if (score > bestScore) {
                best = c;
                bestScore = score;
            }
        }

        return best;
    }
}
