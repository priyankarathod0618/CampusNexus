package campusnexus.ui;

import campusnexus.dao.*;
import campusnexus.exception.DuplicateClubMembershipException;
import campusnexus.exception.WeakPasswordException;
import campusnexus.model.*;
import campusnexus.service.AuthService;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class StudentMenu implements DashboardMenu {
    private final Scanner scanner;
    private final Student student;
    private final VisitorMenu visitorMenu;
    private final EventDAO eventDAO = new EventDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final AnnouncementDAO announcementDAO = new AnnouncementDAO();
    private final AcademicResourceDAO academicResourceDAO = new AcademicResourceDAO();
    private final ClubDAO clubDAO = new ClubDAO();
    private final QuestionDAO questionDAO = new QuestionDAO();
    private final SkillDAO skillDAO = new SkillDAO();
    private final MarketplaceDAO marketplaceDAO = new MarketplaceDAO();
    private final HostelComplaintDAO hostelComplaintDAO = new HostelComplaintDAO();
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
                case "4" -> viewNotificationsAndAnnouncements();
                case "5" -> academicResourcesMenu();
                case "6" -> clubsMenu();
                case "7" -> seniorInteractionMenu();
                case "8" -> skillSwapMenu();
                case "9" -> marketplaceMenu();
                case "10" -> hostelHelpMenu();
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
                authService.changePassword(student.getId(), newPassword);
                student.setPassword(newPassword);
                student.setMustChangePassword(false);
                System.out.println("Password updated. Welcome, " + student.getName() + "!");
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
        System.out.println("        Student Dashboard");
        System.out.println("====================================");
        System.out.println("Welcome, " + student.getName() + "!");
        System.out.println();
        System.out.println("1. View Profile");
        System.out.println("2. Explore Colleges");
        System.out.println("3. Upcoming Events");
        System.out.println("4. Notifications & Announcements");
        System.out.println("5. Academic Resources");
        System.out.println("6. Clubs");
        System.out.println("7. Senior Interaction (Q&A)");
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
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("===== Upcoming Events =====");
            System.out.println("1. View All Upcoming");
            System.out.println("2. Search Between Two Dates");
            System.out.println("0. Back");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> listAndRegister(eventDAO.findUpcoming());
                    case "2" -> searchEventsBetweenDates();
                    case "0" -> back = true;
                    default -> System.out.println("Invalid option. Try again.");
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
        }
    }

    private void searchEventsBetweenDates() throws SQLException {
        try {
            System.out.print("Start date (YYYY-MM-DD): ");
            java.time.LocalDate start = java.time.LocalDate.parse(scanner.nextLine().trim());
            System.out.print("End date (YYYY-MM-DD): ");
            java.time.LocalDate end = java.time.LocalDate.parse(scanner.nextLine().trim());
            listAndRegister(eventDAO.findBetweenDates(start, end));
        } catch (java.time.format.DateTimeParseException e) {
            System.out.println("Invalid date format. Use YYYY-MM-DD.");
        }
    }

    private void listAndRegister(List<Event> events) throws SQLException {
        if (events.isEmpty()) {
            System.out.println("No events found.");
            return;
        }

        System.out.println();
        for (Event e : events) {
            System.out.println(e.getId() + ". " + e.getTitle() + " - " + e.getEventDate() + " @ " + e.getVenue()
                    + " (" + e.getRegisteredCount() + "/" + e.getCapacity() + " seats)"
                    + (e.isFull() ? " [FULL]" : ""));
        }

        System.out.print("Enter event ID to register (0 to cancel): ");
        try {
            int eventId = Integer.parseInt(scanner.nextLine().trim());
            if (eventId == 0) return;

            eventDAO.registerStudentForEvent(eventId, student.getId());
            System.out.println("Registered successfully! A confirmation notification was sent.");

        } catch (NumberFormatException e) {
            System.out.println("Invalid event ID.");
        } catch (campusnexus.exception.EventFullException e) {
            System.out.println(e.getMessage());
        }
    }

    private void viewNotificationsAndAnnouncements() {
        try {
            List<Notification> recent = notificationDAO.findRecentByUser(student.getId(), 5);
            System.out.println();
            System.out.println("----- Recent Notifications -----");
            if (recent.isEmpty()) {
                System.out.println("No notifications yet.");
            } else {
                for (Notification n : recent) {
                    System.out.println("[" + n.getCreatedAt() + "] " + n.getMessage());
                }
            }

            List<Announcement> announcements = announcementDAO.findRelevantForStudent(student.getBranch(), student.getYear());
            System.out.println();
            System.out.println("----- Announcements for you -----");
            if (announcements.isEmpty()) {
                System.out.println("No announcements yet.");
            } else {
                for (Announcement a : announcements) {
                    System.out.println("[" + a.getCreatedAt() + "] " + a.getTitle() + " (" + a.getTeacherName() + "): " + a.getMessage());
                }
            }
        } catch (SQLException e) {
            System.out.println("Could not load notifications: " + e.getMessage());
        }
    }

    private void academicResourcesMenu() {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("===== Academic Resources =====");
            System.out.println("1. View All Resources");
            System.out.println("2. Search by Subject");
            System.out.println("0. Back");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> printResources(academicResourceDAO.findAll());
                    case "2" -> {
                        System.out.print("Enter subject keyword: ");
                        String keyword = scanner.nextLine().trim();
                        printResources(academicResourceDAO.searchBySubject(keyword));
                    }
                    case "0" -> back = true;
                    default -> System.out.println("Invalid option. Try again.");
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
        }
    }

    private void printResources(List<AcademicResource> resources) {
        System.out.println();
        if (resources.isEmpty()) {
            System.out.println("No resources found.");
            return;
        }
        for (AcademicResource r : resources) {
            System.out.println("- " + r.getTitle() + " [" + r.getType() + "] " + r.getSubject()
                    + " Year " + r.getYear() + " | uploaded by " + r.getUploaderName());
        }
    }

    private void clubsMenu() {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("===== Clubs =====");
            System.out.println("1. View All Clubs");
            System.out.println("2. Join a Club");
            System.out.println("3. My Clubs");
            System.out.println("0. Back");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> printClubs(clubDAO.findAll());
                    case "2" -> joinClub();
                    case "3" -> printClubs(clubDAO.findByStudent(student.getId()));
                    case "0" -> back = true;
                    default -> System.out.println("Invalid option. Try again.");
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
        }
    }

    private void printClubs(List<Club> clubs) {
        System.out.println();
        if (clubs.isEmpty()) {
            System.out.println("No clubs found.");
            return;
        }
        for (Club c : clubs) {
            System.out.println(c.getId() + ". " + c.getName() + " [" + c.getCategory() + "] - " + c.getDescription());
        }
    }

    private void joinClub() throws SQLException {
        printClubs(clubDAO.findAll());
        System.out.print("Enter club ID to join: ");
        try {
            int clubId = Integer.parseInt(scanner.nextLine().trim());
            clubDAO.joinClub(clubId, student.getId());
            System.out.println("You've joined the club!");
        } catch (NumberFormatException e) {
            System.out.println("Invalid club ID.");
        } catch (DuplicateClubMembershipException e) {
            System.out.println(e.getMessage());
        }
    }

    private void seniorInteractionMenu() {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("===== Senior Interaction (Q&A) =====");
            System.out.println("1. Post a Question");
            System.out.println("2. View Questions & Replies");
            System.out.println("0. Back");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> postQuestion();
                    case "2" -> viewQuestions();
                    case "0" -> back = true;
                    default -> System.out.println("Invalid option. Try again.");
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
        }
    }

    private void postQuestion() throws SQLException {
        System.out.print("Enter question title: ");
        String title = scanner.nextLine().trim();
        System.out.print("Enter details: ");
        String description = scanner.nextLine().trim();
        questionDAO.postQuestion(student.getId(), title, description);
        System.out.println("Question posted!");
    }

    private void viewQuestions() throws SQLException {
        List<Question> questions = questionDAO.findAll();
        System.out.println();
        if (questions.isEmpty()) {
            System.out.println("No questions yet.");
            return;
        }
        for (Question q : questions) {
            System.out.println(q.getId() + ". [" + q.getStatus() + "] " + q.getTitle() + " - by " + q.getStudentName());
        }

        System.out.print("Enter question ID to view replies (0 to skip): ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            if (id == 0) return;
            List<QuestionReply> replies = questionDAO.findRepliesByQuestion(id);
            if (replies.isEmpty()) {
                System.out.println("No replies yet.");
            } else {
                for (QuestionReply r : replies) {
                    System.out.println("  " + r.getAuthorName() + ": " + r.getReplyText());
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid question ID.");
        }
    }

    private void skillSwapMenu() {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("===== Skill Swap =====");
            System.out.println("1. Offer a Skill");
            System.out.println("2. Browse All Skills");
            System.out.println("3. Search Skill");
            System.out.println("0. Back");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> offerSkill();
                    case "2" -> printSkills(skillDAO.findAll());
                    case "3" -> {
                        System.out.print("Enter skill keyword: ");
                        String keyword = scanner.nextLine().trim();
                        printSkills(skillDAO.searchBySkillName(keyword));
                    }
                    case "0" -> back = true;
                    default -> System.out.println("Invalid option. Try again.");
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
        }
    }

    private void offerSkill() throws SQLException {
        System.out.print("Enter skill name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter description: ");
        String description = scanner.nextLine().trim();
        skillDAO.offerSkill(student.getId(), name, description);
        System.out.println("Skill listed!");
    }

    private void printSkills(List<Skill> skills) {
        System.out.println();
        if (skills.isEmpty()) {
            System.out.println("No skills found.");
            return;
        }
        for (Skill s : skills) {
            System.out.println("- " + s.getSkillName() + " (" + s.getStudentName() + "): " + s.getDescription());
        }
    }

    private void marketplaceMenu() {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("===== Marketplace =====");
            System.out.println("1. Browse Available Items");
            System.out.println("2. List an Item");
            System.out.println("3. My Listings (mark as sold)");
            System.out.println("0. Back");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> printItems(marketplaceDAO.findAvailable());
                    case "2" -> listItem();
                    case "3" -> myListings();
                    case "0" -> back = true;
                    default -> System.out.println("Invalid option. Try again.");
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
        }
    }

    private void listItem() throws SQLException {
        System.out.print("Enter item title: ");
        String title = scanner.nextLine().trim();
        System.out.print("Enter description: ");
        String description = scanner.nextLine().trim();
        System.out.print("Enter price: ");
        try {
            double price = Double.parseDouble(scanner.nextLine().trim());
            marketplaceDAO.listItem(student.getId(), title, description, price);
            System.out.println("Item listed!");
        } catch (NumberFormatException e) {
            System.out.println("Invalid price.");
        }
    }

    private void myListings() throws SQLException {
        List<MarketplaceItem> items = marketplaceDAO.findBySeller(student.getId());
        printItems(items);
        System.out.print("Enter item ID to mark as sold (0 to skip): ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            if (id == 0) return;
            marketplaceDAO.markSold(id, student.getId());
            System.out.println("Marked as sold.");
        } catch (NumberFormatException e) {
            System.out.println("Invalid item ID.");
        }
    }

    private void printItems(List<MarketplaceItem> items) {
        System.out.println();
        if (items.isEmpty()) {
            System.out.println("No items found.");
            return;
        }
        for (MarketplaceItem m : items) {
            System.out.println(m.getId() + ". " + m.getTitle() + " - Rs." + m.getPrice()
                    + " [" + m.getStatus() + "] by " + m.getSellerName());
        }
    }

    private void hostelHelpMenu() {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("===== Hostel Help =====");
            System.out.println("1. Submit a Complaint");
            System.out.println("2. View My Complaints");
            System.out.println("0. Back");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> submitComplaint();
                    case "2" -> viewMyComplaints();
                    case "0" -> back = true;
                    default -> System.out.println("Invalid option. Try again.");
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
        }
    }

    private void submitComplaint() throws SQLException {
        System.out.print("Enter category (e.g. Electrical, Plumbing, Cleanliness): ");
        String category = scanner.nextLine().trim();
        System.out.print("Enter description: ");
        String description = scanner.nextLine().trim();
        hostelComplaintDAO.submitComplaint(student.getId(), category, description);
        System.out.println("Complaint submitted. You'll be notified when it's resolved.");
    }

    private void viewMyComplaints() throws SQLException {
        List<HostelComplaint> complaints = hostelComplaintDAO.findByStudent(student.getId());
        System.out.println();
        if (complaints.isEmpty()) {
            System.out.println("No complaints submitted yet.");
            return;
        }
        for (HostelComplaint c : complaints) {
            System.out.println("[" + c.getStatus() + "] " + c.getCategory() + ": " + c.getDescription()
                    + " (submitted " + c.getCreatedAt() + ")");
        }
    }
}
