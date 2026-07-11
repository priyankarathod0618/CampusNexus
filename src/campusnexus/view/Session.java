package campusnexus.view;

import campusnexus.model.Person;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Single-user desktop session state: the primary window and whoever is
 * currently logged in (null when nobody is). Deliberately simple - this
 * app has exactly one window and one active user at a time.
 */
public class Session {
    private static Stage primaryStage;
    private static Person currentUser;

    private Session() {
    }

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static void setCurrentUser(Person person) {
        currentUser = person;
    }

    public static Person getCurrentUser() {
        return currentUser;
    }

    public static void showScene(javafx.scene.Parent root, String title, double width, double height) {
        Scene scene = new Scene(root, width, height);
        primaryStage.setTitle("CampusNexus - " + title);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }
}
