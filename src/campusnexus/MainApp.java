package campusnexus;

import campusnexus.util.ReminderScheduler;
import campusnexus.view.Session;
import campusnexus.view.WelcomeView;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {
    private final ReminderScheduler reminderScheduler = new ReminderScheduler();

    @Override
    public void start(Stage primaryStage) {
        Session.setPrimaryStage(primaryStage);
        reminderScheduler.start();

        primaryStage.setOnCloseRequest(e -> reminderScheduler.stop());

        WelcomeView.show();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
