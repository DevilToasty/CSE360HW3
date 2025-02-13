package application;

import javafx.scene.control.Button;

public class BackButton {

    /**
     * Creates a back button with pre-defined style and action to go back to the previous page.
     *
     * @param primaryStage The CustomTrackedStage instance to handle navigation.
     * @return A fully configured back button.
     */
    public static Button createBackButton(CustomTrackedStage primaryStage) {
        Button backButton = new Button("<-- Back");
        
        // Go back action
        backButton.setOnAction(a -> {
            primaryStage.goBack();
        });
        
        // Apply button style
        backButton.setStyle("-fx-padding: 10 20 10 20; -fx-background-color: lightgrey;");
        
        return backButton;
    }
}
