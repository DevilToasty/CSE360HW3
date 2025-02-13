package application;

import databasePart1.DatabaseHelper;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * InstructorHomePage class represents the user interface for the instructor user.
 * This page includes only a back button to return to the previous page.
 */
public class StudentHomePage {
    private Scene scene;
    private final DatabaseHelper databaseHelper;
    private final User currentUser;

    public StudentHomePage(DatabaseHelper databaseHelper, User currentUser) {
        this.databaseHelper = databaseHelper;
        this.currentUser = currentUser;
        initializeUI();
    }

    /**
     * Initializes the UI components.
     */
    private void initializeUI() {
        BorderPane borderPane = new BorderPane();

        // Back Button at the top left
        Button backButton = new Button("<-- Back");
        backButton.setOnAction(e -> {
            if (scene.getWindow() instanceof CustomTrackedStage) {
                ((CustomTrackedStage) scene.getWindow()).goBack();
            }
        });
        BorderPane.setAlignment(backButton, Pos.TOP_LEFT);
        borderPane.setTop(backButton);
        Label userLabel = new Label("Hello,  Student!");
        userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        VBox centerBox = new VBox(userLabel);
        centerBox.setAlignment(Pos.CENTER);
        borderPane.setCenter(centerBox);

        scene = new Scene(borderPane, 800, 400);
    }

    /**
     * Displays the instructor home page in the provided primary stage.
     * @param primaryStage The primary stage where the scene will be displayed.
     */
    public void show(CustomTrackedStage primaryStage) {
        primaryStage.setTitle("Student Home");
        primaryStage.showScene(scene);
    }

    public Scene getScene() {
        return scene;
    }
}