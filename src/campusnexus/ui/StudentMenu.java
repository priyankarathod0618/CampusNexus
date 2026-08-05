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

                case "2" -> viewUpcomingEvents();

                case "3" -> viewNotificationsAndAnnouncements();

                case "4" -> academicResourcesMenu();

                case "5" -> clubsMenu();

                case "6" -> seniorInteractionMenu();

                case "7" -> skillSwapMenu();

                case "8" -> marketplaceMenu();

                case "9" -> careerPlacementMenu();

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
        System.out.println("2. Upcoming Events");
        System.out.println("3. Notifications & Announcements");
        System.out.println("4. Academic Resources");
        System.out.println("5. Clubs");
        System.out.println("6. Senior Interaction (Q&A)");
        System.out.println("7. Skill Swap");
        System.out.println("8. Marketplace");
        System.out.println("9. Career & Placements");
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
        } catch (campusnexus.exception.AlreadyRegisteredException e) {
        System.out.println(e.getMessage());
    }
        catch (SQLException e) {
            System.out.println("Could not register (rolled back): " + e.getMessage());
        }
    }

    private void viewNotificationsAndAnnouncements() {
        try {
            List<Notification> notifications =
                    notificationDAO.findRecentByUser(student.getId(), 5);

            List<Announcement> announcements =
                    announcementDAO.findRelevantForStudent(
                            student.getBranch(), student.getYear());

            System.out.println();
            System.out.println("=================================================");
            System.out.println("      Notifications & Announcements");
            System.out.println("=================================================");
            System.out.println();

            // Notifications
            System.out.println("🔔 Notifications");
            System.out.println("-------------------------------------------------");

            if (notifications.isEmpty()) {
                System.out.println("No new notifications.");
            } else {
                for (Notification n : notifications) {
                    System.out.println("[" + n.getCreatedAt() + "]");
                    System.out.println("✔ " + n.getMessage());
                    System.out.println();
                }
            }

            // Announcements
            System.out.println("📢 Announcements");
            System.out.println("-------------------------------------------------");

            if (announcements.isEmpty()) {
                System.out.println("No announcements available for");
                System.out.println(student.getBranch() + " - Year " + student.getYear() + ".");
            } else {
                for (Announcement a : announcements) {
                    System.out.println("[" + a.getCreatedAt() + "]");
                    System.out.println(a.getTitle());
                    System.out.println(a.getMessage());
                    System.out.println("By: " + a.getTeacherName());
                    System.out.println();
                }
            }

            System.out.println("=================================================");
            System.out.print("Press Enter to go back...");
            scanner.nextLine();

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

    private void printClubs(List<Club> clubs) throws SQLException {

        if (clubs.isEmpty()) {
            System.out.println("\nNo clubs found.");
            return;
        }

        while (true) {

            System.out.println();
            System.out.println("==============================================================");
            System.out.println("                         Clubs");
            System.out.println("==============================================================");
            System.out.printf("%-4s %-30s %-18s%n",
                    "ID", "Club Name", "Category");
            System.out.println("--------------------------------------------------------------");

            for (Club c : clubs) {
                System.out.printf("%-4d %-30s %-18s%n",
                        c.getId(),
                        c.getName(),
                        c.getCategory());
            }

            System.out.println("--------------------------------------------------------------");
            System.out.println("Enter Club ID to view details");
            System.out.println("0. Back");
            System.out.print("Choose: ");

            try {
                int id = Integer.parseInt(scanner.nextLine());

                if (id == 0)
                    return;

                Club selected = null;

                for (Club c : clubs) {
                    if (c.getId() == id) {
                        selected = c;
                        break;
                    }
                }

                if (selected == null) {
                    System.out.println("Invalid Club ID.");
                } else {
                    clubDetails(selected);
                }

            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
            }
        }
    }
    private void clubDetails(Club club) throws SQLException {

        boolean back = false;

        while (!back) {

            System.out.println();
            System.out.println("==================================================");
            System.out.println("                 Club Details");
            System.out.println("==================================================");
            System.out.println("Club Name   : " + club.getName());
            System.out.println("Category    : " + club.getCategory());
            System.out.println("Description : " + club.getDescription());

            System.out.println("==================================================");
            System.out.println("1. Join Club");
            System.out.println("0. Back");
            System.out.print("Choose: ");

            String choice = scanner.nextLine();

            switch (choice) {

                case "1":
                    try {
                        clubDAO.joinClub(club.getId(), student.getId());
                        System.out.println("You've joined the club!");
                    } catch (DuplicateClubMembershipException e) {
                        System.out.println(e.getMessage());
                    }
                    break;

                case "0":
                    back = true;
                    break;

                default:
                    System.out.println("Invalid option.");
            }
        }
    }
    private void joinClub() throws SQLException {

        List<Club> clubs = clubDAO.findAll();

        if (clubs.isEmpty()) {
            System.out.println("No clubs available.");
            return;
        }

        System.out.println();
        System.out.println("==============================================================");
        System.out.println("                     Join a Club");
        System.out.println("==============================================================");
        System.out.printf("%-4s %-30s %-18s%n",
                "ID", "Club Name", "Category");
        System.out.println("--------------------------------------------------------------");

        for (Club c : clubs) {
            System.out.printf("%-4d %-30s %-18s%n",
                    c.getId(),
                    c.getName(),
                    c.getCategory());
        }

        System.out.println("--------------------------------------------------------------");
        System.out.print("Enter Club ID to join (0 to cancel): ");

        try {
            int id = Integer.parseInt(scanner.nextLine());

            if (id == 0)
                return;

            clubDAO.joinClub(id, student.getId());

            System.out.println("✓ You've successfully joined the club!");

        } catch (NumberFormatException e) {
            System.out.println("Invalid Club ID.");
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

        while (true) {

            List<Question> questions = questionDAO.findAll();

            if (questions.isEmpty()) {
                System.out.println("\nNo questions available.");
                return;
            }

            System.out.println();
            System.out.println("==============================================================");
            System.out.println("                Senior Interaction (Q&A)");
            System.out.println("==============================================================");
            System.out.printf("%-4s %-12s %-35s %-20s%n",
                    "ID", "Status", "Title", "Asked By");
            System.out.println("--------------------------------------------------------------------------");

            for (Question q : questions) {
                System.out.printf("%-4d %-12s %-35s %-20s%n",
                        q.getId(),
                        q.getStatus(),
                        q.getTitle(),
                        q.getStudentName());
            }

            System.out.println("--------------------------------------------------------------------------");
            System.out.println("Enter Question ID to view details");
            System.out.println("0. Back");
            System.out.print("Choose: ");

            try {

                int id = Integer.parseInt(scanner.nextLine());

                if (id == 0)
                    return;

                Question selected = null;

                for (Question q : questions) {
                    if (q.getId() == id) {
                        selected = q;
                        break;
                    }
                }

                if (selected == null) {
                    System.out.println("Invalid Question ID.");
                } else {
                    Question fullQuestion = questionDAO.findById(selected.getId());

                    if (fullQuestion != null) {
                        questionDetails(fullQuestion);
                    }
                }

            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
            }
        }
    }
    private void questionDetails(Question question) throws SQLException {

        while (true) {

            System.out.println();
            System.out.println("==================================================");
            System.out.println("               Question Details");
            System.out.println("==================================================");

            System.out.println("Title      : " + question.getTitle());
            System.out.println("Asked By   : " + question.getStudentName());
            System.out.println("Status     : " + question.getStatus());
            System.out.println();

            System.out.println("Question");
            System.out.println("--------------------------------------------------");
            System.out.println(question.getDescription());

            System.out.println();
            System.out.println("Replies");
            System.out.println("--------------------------------------------------");

            List<QuestionReply> replies =
                    questionDAO.findRepliesByQuestion(question.getId());

            if (replies.isEmpty()) {

                System.out.println("No replies yet.");

            } else {

                int count = 1;

                for (QuestionReply reply : replies) {

                    System.out.println(count + ". " + reply.getAuthorName());
                    System.out.println(reply.getReplyText());
                    System.out.println();

                    count++;
                }
            }

            System.out.println("==================================================");
            System.out.println("0. Back");
            System.out.print("Choose: ");

            String choice = scanner.nextLine();

            if (choice.equals("0"))
                return;

            System.out.println("Invalid option.");
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
            System.out.println("4. My Skills");
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

                    case "4" -> printSkills(skillDAO.findByStudent(student.getId()));

                    case "0" -> back = true;

                    default -> System.out.println("Invalid option.");
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

    private void printSkills(List<Skill> skills) throws SQLException {

        if (skills.isEmpty()) {
            System.out.println("\nNo skills found.");
            return;
        }

        while (true) {

            System.out.println();
            System.out.println("==============================================================");
            System.out.println("                    Skill Exchange");
            System.out.println("==============================================================");
            System.out.printf("%-4s %-20s %-20s %-30s%n",
                    "ID", "Skill", "Student", "Description");
            System.out.println("--------------------------------------------------------------------------");

            for (Skill s : skills) {

                String desc = s.getDescription();

                if (desc.length() > 28)
                    desc = desc.substring(0, 28) + "...";

                System.out.printf("%-4d %-20s %-20s %-30s%n",
                        s.getId(),
                        s.getSkillName(),
                        s.getStudentName(),
                        desc);
            }

            System.out.println("--------------------------------------------------------------------------");
            System.out.println("Enter Skill ID to view details");
            System.out.println("0. Back");
            System.out.print("Choose: ");

            try {

                int id = Integer.parseInt(scanner.nextLine());

                if (id == 0)
                    return;

                Skill selected = null;

                for (Skill s : skills) {

                    if (s.getId() == id) {
                        selected = s;
                        break;
                    }
                }

                if (selected == null) {
                    System.out.println("Invalid Skill ID.");
                } else {
                    skillDetails(selected);
                }

            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
            }
        }
    }
    private void skillDetails(Skill skill) {

        while (true) {

            System.out.println();
            System.out.println("==================================================");
            System.out.println("                 Skill Details");
            System.out.println("==================================================");

            System.out.println("Skill       : " + skill.getSkillName());
            System.out.println("Offered By  : " + skill.getStudentName());

            System.out.println();
            System.out.println("Description");
            System.out.println("--------------------------------------------------");
            System.out.println(skill.getDescription());

            System.out.println("==================================================");
            System.out.println("1. Contact Student");
            System.out.println("0. Back");
            System.out.print("Choose: ");

            String choice = scanner.nextLine();

            switch (choice) {

                case "1":

                    System.out.println();
                    System.out.println("Student : " + skill.getStudentName());
                    System.out.println("Contact through CampusNexus messaging.");
                    System.out.println("(Messaging module can be added later.)");

                    break;

                case "0":
                    return;

                default:
                    System.out.println("Invalid option.");
            }
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

        double price;

        while (true) {

            System.out.print("Enter price: ");

            String input = scanner.nextLine().trim();

            try {

                price = Double.parseDouble(input);

                if (price <= 0) {

                    System.out.println("Price must be greater than zero.");
                    continue;
                }

                break;

            } catch (NumberFormatException e) {

                System.out.println("Please enter a valid price.");

            }
        }

        marketplaceDAO.listItem(student.getId(), title, description, price);

        System.out.println("Item listed successfully!");
    }

    private void myListings() throws SQLException {

        List<MarketplaceItem> items = marketplaceDAO.findBySeller(student.getId());

        if (items.isEmpty()) {
            System.out.println("\nYou haven't listed any items yet.");
            return;
        }

        printItems(items);
    }

    private void printItems(List<MarketplaceItem> items) throws SQLException {

        if (items.isEmpty()) {
            System.out.println("\nNo items found.");
            return;
        }

        while (true) {

            System.out.println();
            System.out.println("==============================================================");
            System.out.println("                     Marketplace");
            System.out.println("==============================================================");
            System.out.printf("%-4s %-30s %-10s %-12s %-20s%n",
                    "ID", "Item", "Price", "Status", "Seller");
            System.out.println("--------------------------------------------------------------------------");

            for (MarketplaceItem item : items) {

                System.out.printf("%-4d %-30s Rs.%-7.2f %-12s %-20s%n",
                        item.getId(),
                        item.getTitle(),
                        item.getPrice(),
                        item.getStatus(),
                        item.getSellerName());
            }

            System.out.println("--------------------------------------------------------------------------");
            System.out.println("Enter Item ID to view details");
            System.out.println("0. Back");
            System.out.print("Choose: ");

            try {

                int id = Integer.parseInt(scanner.nextLine());

                if (id == 0)
                    return;

                MarketplaceItem selected = null;

                for (MarketplaceItem item : items) {

                    if (item.getId() == id) {
                        selected = item;
                        break;
                    }
                }

                if (selected == null) {
                    System.out.println("Invalid Item ID.");
                } else {
                    marketplaceDetails(selected);
                }

            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
            }
        }
    }
    private void marketplaceDetails(MarketplaceItem item) throws SQLException {

        while (true) {

            System.out.println();
            System.out.println("==================================================");
            System.out.println("                Item Details");
            System.out.println("==================================================");

            System.out.println("Title       : " + item.getTitle());
            System.out.println("Price       : Rs." + item.getPrice());
            System.out.println("Seller      : " + item.getSellerName());
            System.out.println("Status      : " + item.getStatus());

            System.out.println();
            System.out.println("Description");
            System.out.println("--------------------------------------------------");
            System.out.println(item.getDescription());

            System.out.println("==================================================");

            if (item.getSellerId() == student.getId()) {

                System.out.println("1. Mark as Sold");

            } else {

                System.out.println("1. Contact Seller");

            }

            System.out.println("0. Back");
            System.out.print("Choose: ");

            String choice = scanner.nextLine();

            switch (choice) {

                case "1":

                    if (item.getSellerId() == student.getId()) {

                        marketplaceDAO.markSold(item.getId(), student.getId());

                        System.out.println("Item marked as SOLD.");

                    } else {

                        System.out.println("Seller : " + item.getSellerName());
                        System.out.println("Contact through CampusNexus.");

                    }

                    break;

                case "0":
                    return;

                default:
                    System.out.println("Invalid option.");
            }
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
    private void printResources(List<AcademicResource> resources) throws SQLException {

        if (resources.isEmpty()) {
            System.out.println("\nNo resources found.");
            return;
        }

        while (true) {

            System.out.println();
            System.out.println("==============================================================");
            System.out.println("                    Academic Resources");
            System.out.println("==============================================================");
            System.out.printf("%-4s %-30s %-6s %-15s %-5s%n",
                    "ID", "Title", "Type", "Subject", "Year");
            System.out.println("--------------------------------------------------------------");

            for (AcademicResource r : resources) {
                System.out.printf("%-4d %-30s %-6s %-15s %-5d%n",
                        r.getId(),
                        r.getTitle(),
                        r.getType(),
                        r.getSubject(),
                        r.getYear());
            }

            System.out.println("--------------------------------------------------------------");
            System.out.println("Enter Resource ID to view details");
            System.out.println("0. Back");
            System.out.print("Choose: ");

            try {
                int id = Integer.parseInt(scanner.nextLine());

                if (id == 0)
                    return;

                AcademicResource selected = null;

                for (AcademicResource r : resources) {
                    if (r.getId() == id) {
                        selected = r;
                        break;
                    }
                }

                if (selected == null) {
                    System.out.println("Invalid Resource ID.");
                } else {
                    resourceDetails(selected);
                }

            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
            }
        }
    }
    private void resourceDetails(AcademicResource resource) {

        boolean back = false;

        while (!back) {

            System.out.println();
            System.out.println("==================================================");
            System.out.println("               Resource Details");
            System.out.println("==================================================");
            System.out.println("Title       : " + resource.getTitle());
            System.out.println("Subject     : " + resource.getSubject());
            System.out.println("Year        : " + resource.getYear());
            System.out.println("Type        : " + resource.getType());
            System.out.println("Uploaded By : " + resource.getUploaderName());

            // Uncomment if your model has these fields
            // System.out.println("Description : " + resource.getDescription());
            // System.out.println("Uploaded On : " + resource.getUploadedDate());

            System.out.println("==================================================");
            System.out.println("1. Download");
            System.out.println("2. View Information");
            System.out.println("0. Back");
            System.out.print("Choose: ");

            String choice = scanner.nextLine();

            switch (choice) {

                case "1":
                    System.out.println();
                    System.out.println("Downloading \"" + resource.getTitle() + "\"...");
                    System.out.println("(Demo only - file download not implemented)");
                    break;

                case "2":
                    System.out.println();
                    System.out.println("Resource Information");
                    System.out.println("----------------------------------");
                    System.out.println("Title       : " + resource.getTitle());
                    System.out.println("Subject     : " + resource.getSubject());
                    System.out.println("Year        : " + resource.getYear());
                    System.out.println("Type        : " + resource.getType());
                    System.out.println("Uploaded By : " + resource.getUploaderName());
                    break;

                case "0":
                    back = true;
                    break;

                default:
                    System.out.println("Invalid option.");
            }
        }
    }
    private void careerPlacementMenu() {

        boolean back = false;

        while (!back) {

            System.out.println();
            System.out.println("===== Career & Placements =====");
            System.out.println("1. Internship Opportunities");
            System.out.println("2. Placement Drives");
            System.out.println("3. Career Resources");
            System.out.println("0. Back");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1":
                    System.out.println();
                    System.out.println("No internship opportunities available.");
                    break;

                case "2":
                    System.out.println();
                    System.out.println("No placement drives available.");
                    break;

                case "3":
                    System.out.println();
                    System.out.println("Career Resources");
                    System.out.println("--------------------------------");
                    System.out.println("- Resume Writing Guide");
                    System.out.println("- Interview Preparation");
                    System.out.println("- Aptitude Practice");
                    System.out.println("- Group Discussion Tips");
                    break;

                case "0":
                    back = true;
                    break;

                default:
                    System.out.println("Invalid option.");
            }
        }
    }
}
