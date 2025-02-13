package application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * This page displays a simple welcome message for the user.
 */
public class UserHomePage {

    public void show(CustomTrackedStage primaryStage, boolean showBack) {
        System.out.println("User " + primaryStage.getUser() + " logged in.");

        BorderPane borderPane = new BorderPane();

        if (showBack) {
            Button backButton = BackButton.createBackButton(primaryStage); // use premade back button style
            BorderPane.setMargin(backButton, new Insets(10));
            BorderPane.setAlignment(backButton, Pos.TOP_LEFT);
            borderPane.setTop(backButton);
        }

        Label userLabel = new Label("Hello, User!");
        userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        VBox centerBox = new VBox(userLabel);
        centerBox.setAlignment(Pos.CENTER);
        borderPane.setCenter(centerBox);

        Scene userScene = new Scene(borderPane, 800, 400);
        primaryStage.showScene(userScene); // custom method
        primaryStage.setTitle("User Page");
        primaryStage.show();
    }
}
