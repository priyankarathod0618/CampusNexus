package campusnexus.view;

import campusnexus.dao.*;
import campusnexus.model.HostelComplaint;
import campusnexus.model.Question;
import campusnexus.model.Teacher;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class TeacherDashboardView {
    private static final UserDAO userDAO = new UserDAO();
    private static final ReportDAO reportDAO = new ReportDAO();
    private static final AcademicResourceDAO academicResourceDAO = new AcademicResourceDAO();
    private static final AnnouncementDAO announcementDAO = new AnnouncementDAO();
    private static final QuestionDAO questionDAO = new QuestionDAO();
    private static final HostelComplaintDAO hostelComplaintDAO = new HostelComplaintDAO();
    private static final AnalyticsDAO analyticsDAO = new AnalyticsDAO();

    private static Teacher teacher;
    private static BorderPane root;

    public static void show(Teacher loggedInTeacher) {
        teacher = loggedInTeacher;
        root = new BorderPane();
        root.setStyle("-fx-background-color: " + Theme.PAGE_BG + ";");
        root.setLeft(buildSidebar());
        showProfile();

        Session.showScene(root, "Teacher Dashboard", 1000, 640);
    }

    private static VBox buildSidebar() {
        VBox sidebar = new VBox(4);
        sidebar.setPrefWidth(210);
        sidebar.setPadding(new Insets(20, 10, 20, 10));
        sidebar.setStyle("-fx-background-color: " + Theme.NAVY + ";");

        Label name = new Label(teacher.getName());
        name.setStyle("-fx-text-fill: " + Theme.LIGHT_TEXT + "; -fx-font-weight: bold; -fx-font-size: 15px;");
        Label role = new Label("Teacher");
        role.setStyle("-fx-text-fill: " + Theme.AMBER + "; -fx-font-size: 11px;");
        VBox header = new VBox(2, name, role);
        header.setPadding(new Insets(0, 10, 20, 10));

        var profileBtn = Theme.sidebarButton("Profile");
        var studentsBtn = Theme.sidebarButton("View Students");
        var reportBtn = Theme.sidebarButton("Complaint Report");
        var resolveBtn = Theme.sidebarButton("Resolve Complaint");
        var uploadBtn = Theme.sidebarButton("Upload Resource");
        var announceBtn = Theme.sidebarButton("Post Announcement");
        var questionsBtn = Theme.sidebarButton("Answer Questions");
        var analyticsBtn = Theme.sidebarButton("Basic Analytics");
        var logoutBtn = Theme.sidebarButton("Logout");

        profileBtn.setOnAction(e -> showProfile());
        studentsBtn.setOnAction(e -> showStudents());
        reportBtn.setOnAction(e -> showComplaintReport());
        resolveBtn.setOnAction(e -> showResolveComplaint());
        uploadBtn.setOnAction(e -> showUploadResource());
        announceBtn.setOnAction(e -> showPostAnnouncement());
        questionsBtn.setOnAction(e -> showAnswerQuestions());
        analyticsBtn.setOnAction(e -> showAnalytics());
        logoutBtn.setOnAction(e -> WelcomeView.show());

        sidebar.getChildren().addAll(header, profileBtn, studentsBtn, reportBtn, resolveBtn,
                uploadBtn, announceBtn, questionsBtn, analyticsBtn, new Separator(), logoutBtn);
        return sidebar;
    }

    private static void showAnalytics() {
        VBox content = contentWrapper("Basic Analytics");
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");

        VBox sections = new VBox(20);
        sections.setPadding(new Insets(4));

        try {
            VBox statsCard = Theme.card();
            statsCard.getChildren().add(Theme.sectionHeading("Overall Stats"));
            analyticsDAO.getOverallStats().forEach((label, value) ->
                    statsCard.getChildren().add(new Label(label + ": " + value)));
            sections.getChildren().add(statsCard);

            sections.getChildren().add(analyticsListCard("Popular Events (GROUP BY + HAVING)",
                    analyticsDAO.getPopularEvents()));
            sections.getChildren().add(analyticsListCard("Colleges & Student Counts (RIGHT JOIN)",
                    analyticsDAO.getCollegesWithStudentCounts()));
            sections.getChildren().add(analyticsListCard("Students Needing Follow-up (UNION)",
                    analyticsDAO.getStudentsNeedingFollowUp()));
            sections.getChildren().add(analyticsListCard("College/Teacher Full Overview (FULL JOIN emulation)",
                    analyticsDAO.getCollegeTeacherFullOverview()));

        } catch (SQLException e) {
            sections.getChildren().add(new Label("Error loading analytics: " + e.getMessage()));
        }

        scroll.setContent(sections);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        content.getChildren().add(scroll);
        setContent(content);
    }

    private static VBox analyticsListCard(String heading, List<String> lines) {
        VBox card = Theme.card();
        card.getChildren().add(Theme.sectionHeading(heading));
        if (lines.isEmpty()) {
            card.getChildren().add(new Label("(nothing to show)"));
        } else {
            for (String line : lines) {
                card.getChildren().add(new Label(line));
            }
        }
        return card;
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

    private static void showProfile() {
        VBox content = contentWrapper("My Profile");
        Label details = new Label(teacher.getProfileDetails());
        details.setStyle("-fx-text-fill: " + Theme.DARK_TEXT + "; -fx-font-size: 13px;");
        VBox card = Theme.card();
        card.getChildren().add(details);
        content.getChildren().add(card);
        setContent(content);
    }

    private static void showStudents() {
        VBox content = contentWrapper("Student Directory (via vw_student_directory)");
        ListView<String> list = new ListView<>();
        try {
            for (String line : userDAO.findStudentDirectoryFromView()) {
                list.getItems().add(line);
            }
        } catch (SQLException e) {
            list.getItems().add("Error: " + e.getMessage());
        }
        VBox.setVgrow(list, Priority.ALWAYS);
        content.getChildren().add(list);
        setContent(content);
    }

    private static void showComplaintReport() {
        VBox content = contentWrapper("Unresolved Complaints by Hostel Block");
        Label subtitle = Theme.subtitle("Generated via the cursor-based sp_unresolved_complaints_report() stored procedure");
        ListView<String> list = new ListView<>();
        try {
            Map<String, Integer> summary = reportDAO.unresolvedComplaintsByHostelBlock();
            if (summary.isEmpty()) {
                list.getItems().add("No unresolved complaints. All clear!");
            } else {
                summary.forEach((block, count) -> list.getItems().add(block + ": " + count + " open"));
            }
        } catch (SQLException e) {
            list.getItems().add("Error: " + e.getMessage());
        }
        VBox.setVgrow(list, Priority.ALWAYS);
        content.getChildren().addAll(subtitle, list);
        setContent(content);
    }

    private static void showResolveComplaint() {
        VBox content = contentWrapper("Resolve Hostel Complaint");
        Label subtitle = Theme.subtitle("Marking a complaint resolved fires a database trigger that notifies the student automatically.");
        subtitle.setWrapText(true);

        ListView<HostelComplaint> list = new ListView<>();
        try {
            list.getItems().addAll(hostelComplaintDAO.findOpen());
        } catch (SQLException e) {
            content.getChildren().add(new Label("Error: " + e.getMessage()));
        }
        list.setCellFactory(v -> new ListCell<>() {
            @Override
            protected void updateItem(HostelComplaint c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null :
                        "[" + c.getStatus() + "] " + c.getCategory() + " - " + c.getDescription() + " (" + c.getStudentName() + ")");
            }
        });

        Label status = Theme.errorLabel();
        var resolveBtn = Theme.primaryButton("Mark Resolved");
        resolveBtn.setOnAction(e -> {
            HostelComplaint selected = list.getSelectionModel().getSelectedItem();
            if (selected == null) {
                status.setText("Select a complaint first.");
                return;
            }
            try {
                hostelComplaintDAO.resolveComplaint(selected.getId());
                status.setStyle("-fx-text-fill: green;");
                status.setText("Resolved - student notified automatically.");
                list.getItems().remove(selected);
            } catch (SQLException ex) {
                status.setText("Database error: " + ex.getMessage());
            }
        });

        VBox.setVgrow(list, Priority.ALWAYS);
        content.getChildren().addAll(subtitle, list, resolveBtn, status);
        setContent(content);
    }

    private static void showUploadResource() {
        VBox content = contentWrapper("Upload Academic Resource");
        TextField titleField = new TextField(); titleField.setPromptText("Title");
        TextField typeField = new TextField(); typeField.setPromptText("Type (PAPER/ASSIGNMENT/NOTES)");
        TextField subjectField = new TextField(); subjectField.setPromptText("Subject");
        TextField yearField = new TextField(); yearField.setPromptText("Target year (1-4)");
        TextField descField = new TextField(); descField.setPromptText("Description");
        Label status = Theme.errorLabel();
        var uploadBtn = Theme.primaryButton("Upload");

        uploadBtn.setOnAction(e -> {
            try {
                int year = Integer.parseInt(yearField.getText().trim());
                academicResourceDAO.upload(titleField.getText().trim(), typeField.getText().trim(),
                        subjectField.getText().trim(), year, teacher.getId(), descField.getText().trim());
                status.setStyle("-fx-text-fill: green;");
                status.setText("Resource uploaded!");
                titleField.clear(); typeField.clear(); subjectField.clear(); yearField.clear(); descField.clear();
            } catch (NumberFormatException ex) {
                status.setText("Year must be a number (1-4).");
            } catch (SQLException ex) {
                status.setText("Database error: " + ex.getMessage());
            }
        });

        VBox card = Theme.card();
        card.getChildren().addAll(titleField, typeField, subjectField, yearField, descField, uploadBtn, status);
        content.getChildren().add(card);
        setContent(content);
    }

    private static void showPostAnnouncement() {
        VBox content = contentWrapper("Post Announcement");
        TextField titleField = new TextField(); titleField.setPromptText("Title");
        TextField messageField = new TextField(); messageField.setPromptText("Message");
        TextField branchField = new TextField(); branchField.setPromptText("Target branch (blank = everyone)");
        TextField yearField = new TextField(); yearField.setPromptText("Target year (blank = everyone)");
        Label status = Theme.errorLabel();
        var postBtn = Theme.primaryButton("Post");

        postBtn.setOnAction(e -> {
            try {
                String branch = branchField.getText().trim();
                String yearText = yearField.getText().trim();
                Integer year = yearText.isBlank() ? null : Integer.parseInt(yearText);

                announcementDAO.post(teacher.getId(), titleField.getText().trim(), messageField.getText().trim(),
                        branch.isBlank() ? null : branch, year);

                status.setStyle("-fx-text-fill: green;");
                status.setText("Announcement posted!");
                titleField.clear(); messageField.clear(); branchField.clear(); yearField.clear();
            } catch (NumberFormatException ex) {
                status.setText("Year must be a number.");
            } catch (SQLException ex) {
                status.setText("Database error: " + ex.getMessage());
            }
        });

        VBox card = Theme.card();
        card.getChildren().addAll(titleField, messageField, branchField, yearField, postBtn, status);
        content.getChildren().add(card);
        setContent(content);
    }

    private static void showAnswerQuestions() {
        VBox content = contentWrapper("Unanswered Questions");
        Label subtitle = Theme.subtitle("Found using a NOT EXISTS subquery against question_replies.");

        ListView<Question> list = new ListView<>();
        try {
            list.getItems().addAll(questionDAO.findUnanswered());
        } catch (SQLException e) {
            content.getChildren().add(new Label("Error: " + e.getMessage()));
        }
        list.setCellFactory(v -> new ListCell<>() {
            @Override
            protected void updateItem(Question q, boolean empty) {
                super.updateItem(q, empty);
                setText(empty || q == null ? null : q.getTitle() + " - by " + q.getStudentName() + " : " + q.getDescription());
            }
        });

        TextField replyField = new TextField();
        replyField.setPromptText("Your reply");
        Label status = Theme.errorLabel();
        var replyBtn = Theme.primaryButton("Post Reply");

        replyBtn.setOnAction(e -> {
            Question selected = list.getSelectionModel().getSelectedItem();
            if (selected == null) {
                status.setText("Select a question first.");
                return;
            }
            try {
                questionDAO.addReply(selected.getId(), teacher.getId(), replyField.getText().trim());
                status.setStyle("-fx-text-fill: green;");
                status.setText("Reply posted!");
                list.getItems().remove(selected);
                replyField.clear();
            } catch (SQLException ex) {
                status.setText("Database error: " + ex.getMessage());
            }
        });

        VBox.setVgrow(list, Priority.ALWAYS);
        content.getChildren().addAll(subtitle, list, replyField, replyBtn, status);
        setContent(content);
    }
}
