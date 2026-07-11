package campusnexus.view;

import campusnexus.exception.AccountNotFoundException;
import campusnexus.exception.InvalidCredentialsException;
import campusnexus.model.Person;
import campusnexus.model.Student;
import campusnexus.model.Teacher;
import campusnexus.service.AuthService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.SQLException;

public class LoginView {
    private static final AuthService authService = new AuthService();

    public static void show() {
        VBox root = new VBox(16);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: " + Theme.PAGE_BG + ";");

        VBox card = Theme.card();
        card.setPrefWidth(360);
        card.setAlignment(Pos.CENTER_LEFT);

        Label title = Theme.title("Campus Login");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Label error = Theme.errorLabel();

        var loginBtn = Theme.primaryButton("Login");
        var backBtn = Theme.secondaryButton("Back");
        HBox buttons = new HBox(10, loginBtn, backBtn);

        loginBtn.setOnAction(e -> {
            error.setText("");
            try {
                Person person = authService.login(emailField.getText().trim(), passwordField.getText().trim());
                Session.setCurrentUser(person);

                if (person.isMustChangePassword()) {
                    ChangePasswordView.show();
                } else if (person instanceof Student student) {
                    StudentDashboardView.show(student);
                } else if (person instanceof Teacher teacher) {
                    TeacherDashboardView.show(teacher);
                }
            } catch (AccountNotFoundException | InvalidCredentialsException ex) {
                error.setText(ex.getMessage());
            } catch (SQLException ex) {
                error.setText("Database error: " + ex.getMessage());
            }
        });

        backBtn.setOnAction(e -> WelcomeView.show());

        card.getChildren().addAll(title, emailField, passwordField, error, buttons);
        root.getChildren().add(card);

        Session.showScene(root, "Login", 900, 560);
    }
}
