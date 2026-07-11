package campusnexus.view;

import campusnexus.dao.CollegeDAO;
import campusnexus.model.College;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ExploreCollegesView {
    private static final CollegeDAO collegeDAO = new CollegeDAO();

    public static void show() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + Theme.PAGE_BG + ";");
        root.setPadding(new Insets(24));

        Label title = Theme.title("Explore Colleges");

        TextField searchField = new TextField();
        searchField.setPromptText("Search by city...");
        searchField.setPrefWidth(220);

        Button searchBtn = Theme.primaryButton("Search");
        Button clearBtn = Theme.secondaryButton("Show All");
        Button backBtn = Theme.secondaryButton("Back");

        HBox controls = new HBox(10, searchField, searchBtn, clearBtn, backBtn);
        controls.setAlignment(Pos.CENTER_LEFT);

        VBox header = new VBox(12, title, controls);
        header.setPadding(new Insets(0, 0, 16, 0));

        TableView<College> table = buildTable();
        table.setItems(FXCollections.observableArrayList(collegeDAO.findAll()));

        searchBtn.setOnAction(e ->
                table.setItems(FXCollections.observableArrayList(collegeDAO.searchByCity(searchField.getText().trim()))));
        clearBtn.setOnAction(e -> {
            searchField.clear();
            table.setItems(FXCollections.observableArrayList(collegeDAO.findAll()));
        });
        backBtn.setOnAction(e -> WelcomeView.show());

        root.setTop(header);
        root.setCenter(table);

        Session.showScene(root, "Explore Colleges", 900, 560);
    }

    private static TableView<College> buildTable() {
        TableView<College> table = new TableView<>();

        TableColumn<College, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(220);

        TableColumn<College, String> cityCol = new TableColumn<>("City");
        cityCol.setCellValueFactory(new PropertyValueFactory<>("city"));
        cityCol.setPrefWidth(120);

        TableColumn<College, Double> feesCol = new TableColumn<>("Fees");
        feesCol.setCellValueFactory(new PropertyValueFactory<>("fees"));
        feesCol.setPrefWidth(100);

        TableColumn<College, Double> ratingCol = new TableColumn<>("Rating");
        ratingCol.setCellValueFactory(new PropertyValueFactory<>("averageRating"));
        ratingCol.setPrefWidth(80);

        TableColumn<College, String> facilitiesCol = new TableColumn<>("Facilities");
        facilitiesCol.setCellValueFactory(new PropertyValueFactory<>("facilities"));
        facilitiesCol.setPrefWidth(320);

        table.getColumns().addAll(nameCol, cityCol, feesCol, ratingCol, facilitiesCol);
        return table;
    }
}
