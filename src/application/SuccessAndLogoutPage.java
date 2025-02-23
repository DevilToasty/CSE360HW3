package application;

import databasePart1.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class SuccessAndLogoutPage {
	
	private final DatabaseHelper databaseHelper;
    private final QuestionManager questionManager;

    public SuccessAndLogoutPage(DatabaseHelper databaseHelper, QuestionManager questionManager) {
        this.databaseHelper = databaseHelper;
        this.questionManager = questionManager;
    }

	public void show(CustomTrackedStage primaryStage) { // don't implement back button her
    	VBox layout = new VBox(10);
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

	    Label userLabel = new Label("Success! Please log in again.");
	    userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
	    
	    Button loginButton = new Button("Login");
	    loginButton.setOnAction(a -> {
	    	primaryStage.clearHistory(); // remove page history
	    	primaryStage.setUser(null); // log out user
        	new SetupLoginSelectionPage(databaseHelper, questionManager).show(primaryStage);
        });
	    
        layout.getChildren().addAll(userLabel, loginButton);

	    Scene userScene = new Scene(layout, 800, 400);

        primaryStage.setScene(userScene);
        primaryStage.clearHistory(); // prevent user from going back
        primaryStage.setTitle("Success!");
        primaryStage.show();
    	
    }

	public void show(CustomTrackedStage primaryStage, String text) { // don't implement back button here
    	VBox layout = new VBox(10);
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
	    
	    Label userLabel = new Label("Success! OTP is: " + text);
	    userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
	    	    
	    // Button to log in again
	    Button loginButton = new Button("Login");
	    loginButton.setOnAction(a -> {
	    	primaryStage.clearHistory(); // remove page history
	    	primaryStage.setUser(null); // log out user
        	new SetupLoginSelectionPage(databaseHelper, questionManager).show(primaryStage);
        });
	    
        layout.getChildren().addAll(userLabel, loginButton);

	    Scene userScene = new Scene(layout, 800, 400);

        primaryStage.setScene(userScene);
        primaryStage.clearHistory(); // prevent user from going back.
        primaryStage.setTitle("Success!");
        primaryStage.show();
    }
}