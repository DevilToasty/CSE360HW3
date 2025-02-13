// Added by Bradley

package application;

import databasePart1.DatabaseHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * AdminPage class represents the user interface for the admin user.
 * This page displays a simple welcome message for the admin.
 */

public class AdminHomePage {
	
    private final DatabaseHelper databaseHelper;

    public AdminHomePage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }
    
	/**
     * Displays the admin page in the provided primary stage.
     * @param primaryStage The primary stage where the scene will be displayed.
     */
    public void show(CustomTrackedStage primaryStage) {
    	
    	System.out.println("User " + primaryStage.getUser() + " logged in.");

    	VBox layout = new VBox();
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
        
        Label adminLabel = new Label("Hello, Admin!");
        adminLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Button userManager = new Button("Manager Users"); 
        userManager.setOnAction(a -> {
            new AdminUserManager(databaseHelper).show(primaryStage);
        	System.out.println("Manage Pressed");

        });
        
        BorderPane borderPane = new BorderPane();

        Button backButton = BackButton.createBackButton(primaryStage); // premade back button style
        
        BorderPane.setMargin(backButton, new Insets(10)); // adds padding outside the button
        BorderPane.setAlignment(backButton, Pos.TOP_LEFT);
        borderPane.setTop(backButton);

        layout.getChildren().addAll(adminLabel, userManager);
        borderPane.setCenter(layout);
        Scene adminScene = new Scene(borderPane, 800, 400);

        primaryStage.setTitle("Admin Page");
        primaryStage.showScene(adminScene);
    }
}