package campusnexus.view;

import campusnexus.dao.*;
import campusnexus.exception.DuplicateClubMembershipException;
import campusnexus.model.*;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.List;

public class StudentDashboardView {
    private static final EventDAO eventDAO = new EventDAO();
    private static final NotificationDAO notificationDAO = new NotificationDAO();
    private static final AnnouncementDAO announcementDAO = new AnnouncementDAO();
    private static final AcademicResourceDAO academicResourceDAO = new AcademicResourceDAO();
    private static final ClubDAO clubDAO = new ClubDAO();
    private static final QuestionDAO questionDAO = new QuestionDAO();
    private static final SkillDAO skillDAO = new SkillDAO();
    private static final MarketplaceDAO marketplaceDAO = new MarketplaceDAO();
    private static final HostelComplaintDAO hostelComplaintDAO = new HostelComplaintDAO();

    private static Student student;
    private static BorderPane root;

    public static void show(Student loggedInStudent) {
        student = loggedInStudent;
        root = new BorderPane();
        root.setStyle("-fx-background-color: " + Theme.PAGE_BG + ";");
        root.setLeft(buildSidebar());
        showProfile();

        Session.showScene(root, "Student Dashboard", 1000, 640);
    }

    private static VBox buildSidebar() {
        VBox sidebar = new VBox(4);
        sidebar.setPrefWidth(210);
        sidebar.setPadding(new Insets(20, 10, 20, 10));
        sidebar.setStyle("-fx-background-color: " + Theme.NAVY + ";");

        Label name = new Label(student.getName());
        name.setStyle("-fx-text-fill: " + Theme.LIGHT_TEXT + "; -fx-font-weight: bold; -fx-font-size: 15px;");
        Label role = new Label("Student");
        role.setStyle("-fx-text-fill: " + Theme.AMBER + "; -fx-font-size: 11px;");
        VBox header = new VBox(2, name, role);
        header.setPadding(new Insets(0, 10, 20, 10));

        var profileBtn = Theme.sidebarButton("Profile");
        var eventsBtn = Theme.sidebarButton("Upcoming Events");
        var notifBtn = Theme.sidebarButton("Notifications");
        var resourcesBtn = Theme.sidebarButton("Academic Resources");
        var clubsBtn = Theme.sidebarButton("Clubs");
        var qaBtn = Theme.sidebarButton("Senior Interaction");
        var skillBtn = Theme.sidebarButton("Skill Swap");
        var marketBtn = Theme.sidebarButton("Marketplace");
        var hostelBtn = Theme.sidebarButton("Hostel Help");
        var exploreBtn = Theme.sidebarButton("Explore Colleges");
        var logoutBtn = Theme.sidebarButton("Logout");

        profileBtn.setOnAction(e -> showProfile());
        eventsBtn.setOnAction(e -> showEvents());
        notifBtn.setOnAction(e -> showNotifications());
        resourcesBtn.setOnAction(e -> showResources());
        clubsBtn.setOnAction(e -> showClubs());
        qaBtn.setOnAction(e -> showQuestions());
        skillBtn.setOnAction(e -> showSkills());
        marketBtn.setOnAction(e -> showMarketplace());
        hostelBtn.setOnAction(e -> showHostel());
        exploreBtn.setOnAction(e -> ExploreCollegesView.show());
        logoutBtn.setOnAction(e -> WelcomeView.show());

        sidebar.getChildren().addAll(header, profileBtn, eventsBtn, notifBtn, resourcesBtn, clubsBtn,
                qaBtn, skillBtn, marketBtn, hostelBtn, exploreBtn, new Separator(), logoutBtn);
        return sidebar;
    }

    private static VBox contentWrapper(String heading) {
        VBox content = new VBox(14);
        content.setPadding(new Insets(24));
        content.getChildren().add(Theme.title(heading));
        return content;
    }

    private static void setContent(VBox content) {
        root.setCenter(content);
    }

    // ---------------- Profile ----------------
    private static void showProfile() {
        VBox content = contentWrapper("My Profile");
        Label details = new Label(student.getProfileDetails());
        details.setStyle("-fx-text-fill: " + Theme.DARK_TEXT + "; -fx-font-size: 13px;");
        VBox card = Theme.card();
        card.getChildren().add(details);
        content.getChildren().add(card);
        setContent(content);
    }

    // ---------------- Events ----------------
    private static void showEvents() {
        VBox content = contentWrapper("Upcoming Events");
        ListView<String> list = new ListView<>();
        List<Event> events;
        try {
            events = eventDAO.findUpcoming();
            for (Event ev : events) {
                list.getItems().add(ev.getId() + " | " + ev.getTitle() + " - " + ev.getEventDate() + " @ " + ev.getVenue());
            }
        } catch (SQLException e) {
            list.getItems().add("Error loading events: " + e.getMessage());
        }

        TextField eventIdField = new TextField();
        eventIdField.setPromptText("Event ID to register");
        Label status = Theme.errorLabel();
        var registerBtn = Theme.primaryButton("Register");

        registerBtn.setOnAction(e -> {
            try {
                int id = Integer.parseInt(eventIdField.getText().trim());
                eventDAO.registerStudentForEvent(id, student.getId());
                status.setStyle("-fx-text-fill: green;");
                status.setText("Registered! A confirmation notification was sent.");
            } catch (NumberFormatException ex) {
                status.setText("Enter a valid event ID.");
            } catch (SQLException ex) {
                status.setText("Could not register (rolled back): " + ex.getMessage());
            }
        });

        VBox.setVgrow(list, Priority.ALWAYS);
        content.getChildren().addAll(list, new HBox(10, eventIdField, registerBtn), status);
        setContent(content);
    }

    // ---------------- Notifications & Announcements ----------------
    private static void showNotifications() {
        VBox content = contentWrapper("Notifications & Announcements");
        ListView<String> list = new ListView<>();
        try {
            List<Notification> notifications = notificationDAO.findRecentByUser(student.getId(), 5);
            for (Notification n : notifications) {
                list.getItems().add("[" + n.getCreatedAt() + "] " + n.getMessage());
            }
            List<Announcement> announcements = announcementDAO.findRelevantForStudent(student.getBranch(), student.getYear());
            for (Announcement a : announcements) {
                list.getItems().add("[ANNOUNCEMENT] " + a.getTitle() + " (" + a.getTeacherName() + "): " + a.getMessage());
            }
        } catch (SQLException e) {
            list.getItems().add("Error loading notifications: " + e.getMessage());
        }
        VBox.setVgrow(list, Priority.ALWAYS);
        content.getChildren().add(list);
        setContent(content);
    }

    // ---------------- Academic Resources ----------------
    private static void showResources() {
        VBox content = contentWrapper("Academic Resources");
        ListView<String> list = new ListView<>();
        TextField searchField = new TextField();
        searchField.setPromptText("Search by subject...");
        var searchBtn = Theme.primaryButton("Search");
        var allBtn = Theme.secondaryButton("Show All");

        Runnable loadAll = () -> {
            list.getItems().clear();
            try {
                for (AcademicResource r : academicResourceDAO.findAll()) {
                    list.getItems().add(r.getTitle() + " [" + r.getType() + "] " + r.getSubject()
                            + " Year " + r.getYear() + " | " + r.getUploaderName());
                }
            } catch (SQLException e) {
                list.getItems().add("Error: " + e.getMessage());
            }
        };
        loadAll.run();

        searchBtn.setOnAction(e -> {
            list.getItems().clear();
            try {
                for (AcademicResource r : academicResourceDAO.searchBySubject(searchField.getText().trim())) {
                    list.getItems().add(r.getTitle() + " [" + r.getType() + "] " + r.getSubject()
                            + " Year " + r.getYear() + " | " + r.getUploaderName());
                }
            } catch (SQLException ex) {
                list.getItems().add("Error: " + ex.getMessage());
            }
        });
        allBtn.setOnAction(e -> loadAll.run());

        VBox.setVgrow(list, Priority.ALWAYS);
        content.getChildren().addAll(new HBox(10, searchField, searchBtn, allBtn), list);
        setContent(content);
    }

    // ---------------- Clubs ----------------
    private static void showClubs() {
        VBox content = contentWrapper("Clubs");
        ListView<Club> list = new ListView<>();
        try {
            list.setItems(FXCollections.observableArrayList(clubDAO.findAll()));
        } catch (SQLException e) {
            content.getChildren().add(new Label("Error: " + e.getMessage()));
        }
        list.setCellFactory(v -> new ListCell<>() {
            @Override
            protected void updateItem(Club c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null : c.getId() + ". " + c.getName() + " [" + c.getCategory() + "] - " + c.getDescription());
            }
        });

        Label status = Theme.errorLabel();
        var joinBtn = Theme.primaryButton("Join Selected Club");
        joinBtn.setOnAction(e -> {
            Club selected = list.getSelectionModel().getSelectedItem();
            if (selected == null) {
                status.setText("Select a club first.");
                return;
            }
            try {
                clubDAO.joinClub(selected.getId(), student.getId());
                status.setStyle("-fx-text-fill: green;");
                status.setText("Joined " + selected.getName() + "!");
            } catch (DuplicateClubMembershipException ex) {
                status.setStyle("-fx-text-fill: #D64545;");
                status.setText(ex.getMessage());
            } catch (SQLException ex) {
                status.setText("Database error: " + ex.getMessage());
            }
        });

        VBox.setVgrow(list, Priority.ALWAYS);
        content.getChildren().addAll(list, joinBtn, status);
        setContent(content);
    }

    // ---------------- Senior Interaction (Q&A) ----------------
    private static void showQuestions() {
        VBox content = contentWrapper("Senior Interaction (Q&A)");
        ListView<String> list = new ListView<>();
        try {
            for (Question q : questionDAO.findAll()) {
                list.getItems().add(q.getId() + ". [" + q.getStatus() + "] " + q.getTitle() + " - by " + q.getStudentName());
            }
        } catch (SQLException e) {
            list.getItems().add("Error: " + e.getMessage());
        }

        TextField titleField = new TextField();
        titleField.setPromptText("Question title");
        TextField descField = new TextField();
        descField.setPromptText("Details");
        Label status = Theme.errorLabel();
        var postBtn = Theme.primaryButton("Post Question");

        postBtn.setOnAction(e -> {
            try {
                questionDAO.postQuestion(student.getId(), titleField.getText().trim(), descField.getText().trim());
                status.setStyle("-fx-text-fill: green;");
                status.setText("Question posted!");
                titleField.clear();
                descField.clear();
            } catch (SQLException ex) {
                status.setText("Database error: " + ex.getMessage());
            }
        });

        VBox.setVgrow(list, Priority.ALWAYS);
        content.getChildren().addAll(list, titleField, descField, postBtn, status);
        setContent(content);
    }

    // ---------------- Skill Swap ----------------
    private static void showSkills() {
        VBox content = contentWrapper("Skill Swap");
        ListView<String> list = new ListView<>();
        try {
            for (Skill s : skillDAO.findAll()) {
                list.getItems().add(s.getSkillName() + " (" + s.getStudentName() + "): " + s.getDescription());
            }
        } catch (SQLException e) {
            list.getItems().add("Error: " + e.getMessage());
        }

        TextField skillField = new TextField();
        skillField.setPromptText("Skill name");
        TextField descField = new TextField();
        descField.setPromptText("Description");
        Label status = Theme.errorLabel();
        var offerBtn = Theme.primaryButton("Offer Skill");

        offerBtn.setOnAction(e -> {
            try {
                skillDAO.offerSkill(student.getId(), skillField.getText().trim(), descField.getText().trim());
                status.setStyle("-fx-text-fill: green;");
                status.setText("Skill listed!");
                skillField.clear();
                descField.clear();
            } catch (SQLException ex) {
                status.setText("Database error: " + ex.getMessage());
            }
        });

        VBox.setVgrow(list, Priority.ALWAYS);
        content.getChildren().addAll(list, skillField, descField, offerBtn, status);
        setContent(content);
    }

    // ---------------- Marketplace ----------------
    private static void showMarketplace() {
        VBox content = contentWrapper("Marketplace");
        ListView<String> list = new ListView<>();
        try {
            for (MarketplaceItem m : marketplaceDAO.findAvailable()) {
                list.getItems().add(m.getId() + ". " + m.getTitle() + " - Rs." + m.getPrice() + " by " + m.getSellerName());
            }
        } catch (SQLException e) {
            list.getItems().add("Error: " + e.getMessage());
        }

        TextField titleField = new TextField();
        titleField.setPromptText("Item title");
        TextField descField = new TextField();
        descField.setPromptText("Description");
        TextField priceField = new TextField();
        priceField.setPromptText("Price");
        Label status = Theme.errorLabel();
        var listBtn = Theme.primaryButton("List Item");

        listBtn.setOnAction(e -> {
            try {
                double price = Double.parseDouble(priceField.getText().trim());
                marketplaceDAO.listItem(student.getId(), titleField.getText().trim(), descField.getText().trim(), price);
                status.setStyle("-fx-text-fill: green;");
                status.setText("Item listed!");
                titleField.clear(); descField.clear(); priceField.clear();
            } catch (NumberFormatException ex) {
                status.setText("Enter a valid price.");
            } catch (SQLException ex) {
                status.setText("Database error: " + ex.getMessage());
            }
        });

        VBox.setVgrow(list, Priority.ALWAYS);
        content.getChildren().addAll(list, titleField, descField, priceField, listBtn, status);
        setContent(content);
    }

    // ---------------- Hostel Help ----------------
    private static void showHostel() {
        VBox content = contentWrapper("Hostel Help");
        ListView<String> list = new ListView<>();
        try {
            for (HostelComplaint c : hostelComplaintDAO.findByStudent(student.getId())) {
                list.getItems().add("[" + c.getStatus() + "] " + c.getCategory() + ": " + c.getDescription());
            }
        } catch (SQLException e) {
            list.getItems().add("Error: " + e.getMessage());
        }

        TextField categoryField = new TextField();
        categoryField.setPromptText("Category (Electrical, Plumbing, ...)");
        TextField descField = new TextField();
        descField.setPromptText("Description");
        Label status = Theme.errorLabel();
        var submitBtn = Theme.primaryButton("Submit Complaint");

        submitBtn.setOnAction(e -> {
            try {
                hostelComplaintDAO.submitComplaint(student.getId(), categoryField.getText().trim(), descField.getText().trim());
                status.setStyle("-fx-text-fill: green;");
                status.setText("Complaint submitted. You'll be notified when resolved.");
                categoryField.clear();
                descField.clear();
            } catch (SQLException ex) {
                status.setText("Database error: " + ex.getMessage());
            }
        });

        VBox.setVgrow(list, Priority.ALWAYS);
        content.getChildren().addAll(list, categoryField, descField, submitBtn, status);
        setContent(content);
    }
}
