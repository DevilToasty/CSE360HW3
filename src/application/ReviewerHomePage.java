package application;

import databasePart1.DatabaseHelper;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * StudentHomePage class represents the user interface for the student user.
 * This page includes only a back button to return to the previous page.
 */
public class ReviewerHomePage {

    private final DatabaseHelper databaseHelper;
    private final User currentUser;

    public ReviewerHomePage(DatabaseHelper databaseHelper, User currentUser) {
        this.databaseHelper = databaseHelper;
        this.currentUser = currentUser;
    }

    /**
     * Displays the student home page in the provided primary stage.
     * @param primaryStage The primary stage where the scene will be displayed.
     */
    public void show(CustomTrackedStage primaryStage) {
        BorderPane borderPane = new BorderPane();

        // Back Button at the top left
        Button backButton = new Button("<-- Back");
        backButton.setOnAction(e -> primaryStage.goBack());
        BorderPane.setAlignment(backButton, Pos.TOP_LEFT);
        borderPane.setTop(backButton);
        Label userLabel = new Label("Hello,  Reviewer!");
        userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        VBox centerBox = new VBox(userLabel);
        centerBox.setAlignment(Pos.CENTER);
        borderPane.setCenter(centerBox);
        Scene scene = new Scene(borderPane, 800, 400);
        primaryStage.setTitle(" Reviewer Home");
        primaryStage.showScene(scene);
    }
}