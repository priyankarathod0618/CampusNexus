package campusnexus.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

public class WelcomeView {

    public static void show() {
        VBox root = new VBox(28);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50));
        root.setStyle("-fx-background-color: " + Theme.PAGE_BG + ";");

        Label logo = new Label("CampusNexus");
        logo.setStyle("-fx-font-size: 34px; -fx-font-weight: bold; -fx-text-fill: " + Theme.NAVY + ";");

        Label tagline = Theme.subtitle("A Smart Campus Companion System");

        HBox cards = new HBox(20);
        cards.setAlignment(Pos.CENTER);
        cards.getChildren().addAll(
                entryCard("Explore Colleges", "Browse and compare colleges - no account needed",
                        ExploreCollegesView::show),
                entryCard("Campus Login", "Students & teachers sign in here", LoginView::show),
                entryCard("Admin Setup", "Add new student/teacher accounts", AdminSetupView::show)
        );

        Label about = Theme.subtitle("CampusNexus helps visitors explore colleges, students access campus\n" +
                "services, and teachers manage academic activities.");
        about.setWrapText(true);
        about.setAlignment(Pos.CENTER);

        root.getChildren().addAll(logo, tagline, cards, about);

        Session.showScene(root, "Welcome", 900, 560);
    }

    private static VBox entryCard(String title, String description, Runnable action) {
        VBox card = Theme.card();
        card.setPrefWidth(230);
        card.setAlignment(Pos.TOP_LEFT);

        Label titleLabel = Theme.sectionHeading(title);
        Label descLabel = Theme.subtitle(description);
        descLabel.setWrapText(true);

        var button = Theme.primaryButton("Open");
        button.setOnAction(e -> {
            try {
                action.run();
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Something went wrong: " + ex.getMessage());
                alert.showAndWait();
            }
        });

        card.getChildren().addAll(titleLabel, descLabel, button);
        return card;
    }
}
