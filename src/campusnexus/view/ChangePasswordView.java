package campusnexus.view;

import campusnexus.model.Person;
import campusnexus.model.Student;
import campusnexus.model.Teacher;
import campusnexus.service.AuthService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;

import java.sql.SQLException;

public class ChangePasswordView {
    private static final AuthService authService = new AuthService();

    public static void show() {
        Person person = Session.getCurrentUser();

        VBox root = new VBox(16);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: " + Theme.PAGE_BG + ";");

        VBox card = Theme.card();
        card.setPrefWidth(360);

        Label title = Theme.title("Set a New Password");
        Label info = Theme.subtitle("This is your first login, " + person.getName() + ". Please choose a new password.");
        info.setWrapText(true);

        PasswordField newPassword = new PasswordField();
        newPassword.setPromptText("New password (min 4 characters)");

        PasswordField confirmPassword = new PasswordField();
        confirmPassword.setPromptText("Confirm new password");

        Label error = Theme.errorLabel();
        var submitBtn = Theme.primaryButton("Update Password");

        submitBtn.setOnAction(e -> {
            String pw = newPassword.getText().trim();
            String confirm = confirmPassword.getText().trim();

            if (pw.length() < 4) {
                error.setText("Password must be at least 4 characters.");
                return;
            }
            if (!pw.equals(confirm)) {
                error.setText("Passwords do not match.");
                return;
            }

            try {
                authService.changePassword(person.getId(), pw);
                person.setPassword(pw);
                person.setMustChangePassword(false);

                if (person instanceof Student student) {
                    StudentDashboardView.show(student);
                } else if (person instanceof Teacher teacher) {
                    TeacherDashboardView.show(teacher);
                }
            } catch (SQLException ex) {
                error.setText("Could not update password: " + ex.getMessage());
            }
        });

        card.getChildren().addAll(title, info, newPassword, confirmPassword, error, submitBtn);
        root.getChildren().add(card);

        Session.showScene(root, "Change Password", 900, 560);
    }
}
