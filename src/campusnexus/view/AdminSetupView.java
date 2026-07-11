package campusnexus.view;

import campusnexus.dao.CollegeDAO;
import campusnexus.dao.UserDAO;
import campusnexus.exception.DuplicateEmailException;
import campusnexus.exception.DuplicateRollNumberException;
import campusnexus.model.College;
import campusnexus.model.Student;
import campusnexus.model.Teacher;
import campusnexus.service.AdminService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.List;

public class AdminSetupView {
    private static final String ADMIN_CODE = "ADMIN@123";
    private static final AdminService adminService = new AdminService();
    private static final UserDAO userDAO = new UserDAO();
    private static final CollegeDAO collegeDAO = new CollegeDAO();

    public static void show() {
        VBox root = new VBox(16);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: " + Theme.PAGE_BG + ";");

        VBox card = Theme.card();
        card.setPrefWidth(340);

        Label title = Theme.title("Admin Login");
        PasswordField codeField = new PasswordField();
        codeField.setPromptText("Enter admin code");
        Label error = Theme.errorLabel();

        var submitBtn = Theme.primaryButton("Enter");
        var backBtn = Theme.secondaryButton("Back");

        submitBtn.setOnAction(e -> {
            if (ADMIN_CODE.equals(codeField.getText().trim())) {
                showAdminPanel();
            } else {
                error.setText("Incorrect admin code.");
            }
        });
        backBtn.setOnAction(e -> WelcomeView.show());

        card.getChildren().addAll(title, codeField, error, new HBox(10, submitBtn, backBtn));
        root.getChildren().add(card);

        Session.showScene(root, "Admin Login", 900, 560);
    }

    private static void showAdminPanel() {
        BorderPaneWithBack layout = new BorderPaneWithBack();

        TabPane tabs = new TabPane();
        tabs.getTabs().addAll(
                new Tab("Add Student", addStudentForm()),
                new Tab("Add Teacher", addTeacherForm()),
                new Tab("View Members", viewMembersPanel())
        );
        tabs.getTabs().forEach(t -> t.setClosable(false));

        layout.setContent(tabs);
        Session.showScene(layout.getRoot(), "College Admin Setup", 900, 620);
    }

    private static VBox addStudentForm() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(20));

        ComboBox<College> collegeBox = new ComboBox<>(FXCollections.observableArrayList(collegeDAO.findAll()));
        collegeBox.setPromptText("Select college");
        TextField name = new TextField(); name.setPromptText("Name");
        TextField email = new TextField(); email.setPromptText("College email");
        TextField phone = new TextField(); phone.setPromptText("Phone number (10 digits)");
        TextField rollNumber = new TextField(); rollNumber.setPromptText("Roll number");
        TextField branch = new TextField(); branch.setPromptText("Branch");
        TextField year = new TextField(); year.setPromptText("Year (1-4)");
        TextField hostelBlock = new TextField(); hostelBlock.setPromptText("Hostel block");

        Label status = Theme.errorLabel();
        var submitBtn = Theme.primaryButton("Add Student");

        submitBtn.setOnAction(e -> {
            try {
                College college = collegeBox.getValue();
                if (college == null) {
                    status.setText("Please select a college.");
                    return;
                }
                adminService.addStudent(name.getText().trim(), email.getText().trim(), phone.getText().trim(),
                        college.getId(), rollNumber.getText().trim(), branch.getText().trim(),
                        Integer.parseInt(year.getText().trim()), hostelBlock.getText().trim());

                status.setStyle("-fx-text-fill: green;");
                status.setText("Student account created. First-time password is the phone number.");
                name.clear(); email.clear(); phone.clear(); rollNumber.clear();
                branch.clear(); year.clear(); hostelBlock.clear();

            } catch (NumberFormatException ex) {
                status.setStyle("-fx-text-fill: #D64545;");
                status.setText("Year must be a number (1-4).");
            } catch (DuplicateEmailException | DuplicateRollNumberException ex) {
                status.setStyle("-fx-text-fill: #D64545;");
                status.setText(ex.getMessage());
            } catch (SQLException ex) {
                status.setStyle("-fx-text-fill: #D64545;");
                status.setText("Database error: " + ex.getMessage());
            }
        });

        box.getChildren().addAll(Theme.sectionHeading("Add Student Account"), collegeBox, name, email, phone,
                rollNumber, branch, year, hostelBlock, submitBtn, status);
        return box;
    }

    private static VBox addTeacherForm() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(20));

        ComboBox<College> collegeBox = new ComboBox<>(FXCollections.observableArrayList(collegeDAO.findAll()));
        collegeBox.setPromptText("Select college");
        TextField name = new TextField(); name.setPromptText("Name");
        TextField email = new TextField(); email.setPromptText("College email");
        TextField phone = new TextField(); phone.setPromptText("Phone number (10 digits)");
        TextField employeeId = new TextField(); employeeId.setPromptText("Employee ID");
        TextField department = new TextField(); department.setPromptText("Department");
        TextField subject = new TextField(); subject.setPromptText("Subject");

        Label status = Theme.errorLabel();
        var submitBtn = Theme.primaryButton("Add Teacher");

        submitBtn.setOnAction(e -> {
            try {
                College college = collegeBox.getValue();
                if (college == null) {
                    status.setText("Please select a college.");
                    return;
                }
                adminService.addTeacher(name.getText().trim(), email.getText().trim(), phone.getText().trim(),
                        college.getId(), employeeId.getText().trim(), department.getText().trim(), subject.getText().trim());

                status.setStyle("-fx-text-fill: green;");
                status.setText("Teacher account created. First-time password is the phone number.");
                name.clear(); email.clear(); phone.clear(); employeeId.clear(); department.clear(); subject.clear();

            } catch (DuplicateEmailException ex) {
                status.setStyle("-fx-text-fill: #D64545;");
                status.setText(ex.getMessage());
            } catch (SQLException ex) {
                status.setStyle("-fx-text-fill: #D64545;");
                status.setText("Database error: " + ex.getMessage());
            }
        });

        box.getChildren().addAll(Theme.sectionHeading("Add Teacher Account"), collegeBox, name, email, phone,
                employeeId, department, subject, submitBtn, status);
        return box;
    }

    private static VBox viewMembersPanel() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(20));

        ListView<String> list = new ListView<>();
        try {
            List<Student> students = userDAO.findAllStudents();
            List<Teacher> teachers = userDAO.findAllTeachers();

            list.getItems().add("-- Students (" + students.size() + ") --");
            for (Student s : students) {
                list.getItems().add(s.getName() + " | " + s.getBranch() + " Year " + s.getYear() + " | " + s.getEmail());
            }
            list.getItems().add("-- Teachers (" + teachers.size() + ") --");
            for (Teacher t : teachers) {
                list.getItems().add(t.getName() + " | " + t.getDepartment() + " | " + t.getSubject());
            }
        } catch (SQLException ex) {
            list.getItems().add("Error loading members: " + ex.getMessage());
        }

        box.getChildren().addAll(Theme.sectionHeading("Campus Members"), list);
        VBox.setVgrow(list, javafx.scene.layout.Priority.ALWAYS);
        return box;
    }

    /** Small helper so every tabbed screen gets the same Back button without repeating layout code. */
    private static class BorderPaneWithBack {
        private final javafx.scene.layout.BorderPane root = new javafx.scene.layout.BorderPane();

        BorderPaneWithBack() {
            root.setStyle("-fx-background-color: " + Theme.PAGE_BG + ";");
            root.setPadding(new Insets(20));

            var backBtn = Theme.secondaryButton("Back to Welcome");
            backBtn.setOnAction(e -> WelcomeView.show());

            HBox top = new HBox(backBtn);
            top.setPadding(new Insets(0, 0, 12, 0));
            root.setTop(top);
        }

        void setContent(javafx.scene.Node node) {
            root.setCenter(node);
        }

        javafx.scene.Parent getRoot() {
            return root;
        }
    }
}
