package campusnexus.view;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Central place for the "dark academic theme" described in the master plan:
 * navy/charcoal sidebar, one amber accent, generous spacing.
 * Plain inline styles are used (no external .css resource) so this drops
 * into any project layout without classpath resource-loading issues.
 */
public class Theme {
    public static final String NAVY = "#1B2333";
    public static final String CHARCOAL = "#232B3D";
    public static final String AMBER = "#E8A33D";
    public static final String LIGHT_TEXT = "#F4F6FA";
    public static final String MUTED_TEXT = "#A9B2C3";
    public static final String CARD_BG = "#2C3550";
    public static final String PAGE_BG = "#F5F6FA";
    public static final String DARK_TEXT = "#1B2333";

    public static Button primaryButton(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: " + AMBER + "; -fx-text-fill: " + DARK_TEXT + "; "
                + "-fx-font-weight: bold; -fx-padding: 8 18; -fx-background-radius: 6; -fx-cursor: hand;");
        return b;
    }

    public static Button secondaryButton(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: transparent; -fx-text-fill: " + LIGHT_TEXT + "; "
                + "-fx-border-color: " + MUTED_TEXT + "; -fx-border-radius: 6; -fx-background-radius: 6; "
                + "-fx-padding: 8 18; -fx-cursor: hand;");
        return b;
    }

    public static Button sidebarButton(String text) {
        Button b = new Button(text);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setStyle("-fx-background-color: transparent; -fx-text-fill: " + LIGHT_TEXT + "; "
                + "-fx-alignment: CENTER_LEFT; -fx-padding: 10 16; -fx-font-size: 13px; -fx-cursor: hand;");
        b.setOnMouseEntered(e -> b.setStyle("-fx-background-color: " + CARD_BG + "; -fx-text-fill: " + AMBER
                + "; -fx-alignment: CENTER_LEFT; -fx-padding: 10 16; -fx-font-size: 13px; -fx-cursor: hand; -fx-background-radius: 4;"));
        b.setOnMouseExited(e -> b.setStyle("-fx-background-color: transparent; -fx-text-fill: " + LIGHT_TEXT
                + "; -fx-alignment: CENTER_LEFT; -fx-padding: 10 16; -fx-font-size: 13px; -fx-cursor: hand;"));
        return b;
    }

    public static Label title(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        l.setStyle("-fx-text-fill: " + DARK_TEXT + ";");
        return l;
    }

    public static Label sectionHeading(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        l.setStyle("-fx-text-fill: " + DARK_TEXT + ";");
        return l;
    }

    public static Label subtitle(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");
        return l;
    }

    public static Label errorLabel() {
        Label l = new Label();
        l.setStyle("-fx-text-fill: #D64545; -fx-font-size: 12px;");
        l.setWrapText(true);
        return l;
    }

    public static VBox card() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(18));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 10; "
                + "-fx-border-color: #E2E5EC; -fx-border-radius: 10;");
        return box;
    }

    public static Color amberColor() {
        return Color.web(AMBER);
    }
}
