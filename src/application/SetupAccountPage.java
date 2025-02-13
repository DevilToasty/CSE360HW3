package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.sql.SQLException;

import databasePart1.*;

public class SetupAccountPage {

    private final DatabaseHelper databaseHelper;

    public SetupAccountPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    /**
     * Displays the Setup Account page in the provided custom stage.
     * @param primaryStage the custom stage where the scene will be displayed.
     */
    public void show(CustomTrackedStage primaryStage) {
        // Input fields for userName, password, and invitation code
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter userName");
        userNameField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);

        TextField inviteCodeField = new TextField();
        inviteCodeField.setPromptText("Enter Invitation Code");
        inviteCodeField.setMaxWidth(250);

        // Label to display error messages
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        Button setupButton = new Button("Setup");

        setupButton.setOnAction(a -> {
            // Retrieve user input
            String code = inviteCodeField.getText();
            String userName = userNameField.getText();
            String userError = UserNameRecognizer.checkForValidUserName(userName);
            String password = passwordField.getText();
            String passwordError = PasswordRecognizer.evaluatePassword(password);

            if (!userError.isEmpty() || !passwordError.isEmpty()) {
                errorLabel.setText("Username error: " + userError + ". Password error: " + passwordError);
                return;
            }

            try {
                // Check if the user already exists
                if (!databaseHelper.doesUserExist(userName)) {
                    // Validate the invitation code
                    if (databaseHelper.validateInvitationCode(code)) {
                        // Create a new user and register them in the database
                        User user = new User(userName, password, "Student");
                        databaseHelper.register(user);

                        // Navigate to the Welcome Login Page.
                        // (Assuming WelcomeLoginPage has a show(CustomTrackedStage, User) method.)
                        new WelcomeLoginPage(databaseHelper).show(primaryStage, user);
                    } else {
                        errorLabel.setText("Please enter a valid invitation code");
                    }
                } else {
                    errorLabel.setText("This username is taken! Please choose another.");
                }
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
            }
        });

        // Create a BorderPane layout to allow back button placement
        BorderPane borderPane = new BorderPane();

        Button backButton = BackButton.createBackButton(primaryStage); // premade button style

        BorderPane.setMargin(backButton, new Insets(10));  // adds padding outside the button
        BorderPane.setAlignment(backButton, Pos.TOP_LEFT);
        borderPane.setTop(backButton);

        // Center layout with form fields
        VBox centerLayout = new VBox(10);
        centerLayout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        centerLayout.getChildren().addAll(userNameField, passwordField, inviteCodeField, setupButton, errorLabel);
        borderPane.setCenter(centerLayout);

        // Create the scene and use our custom stage to display it.
        Scene scene = new Scene(borderPane, 800, 400);
        primaryStage.setTitle("Account Setup");
        primaryStage.showScene(scene);
    }
}



/*
/**
 * SetupAccountPage class handles the account setup process for new users.
 * Users provide their userName, password, and a valid invitation code to register.
 
public class SetupAccountPage {
	
    private final DatabaseHelper databaseHelper;
    // DatabaseHelper to handle database operations.
    public SetupAccountPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    /**
     * Displays the Setup Account page in the provided stage.
     * @param primaryStage The primary stage where the scene will be displayed.

    public void show(Stage primaryStage) {
    	// Input fields for userName, password, and invitation code
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter userName");
        userNameField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);
        
        TextField inviteCodeField = new TextField();
        inviteCodeField.setPromptText("Enter InvitationCode");
        inviteCodeField.setMaxWidth(250);
        
        
        // Label to display error messages for invalid input or registration issues
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        
        //Role Selection
        ToggleGroup group = new ToggleGroup();
        
        RadioButton studentButton = new RadioButton("user");
        studentButton.setToggleGroup(group);
        //studentButton.setSelected(true);
        
        RadioButton adminButton = new RadioButton("admin");
        adminButton.setToggleGroup(group);
        //adminButton.setSelected(true);
        
        //RadioButton role = (RadioButton)group.getSelectedToggle();
        
        //Actual Setup
        Button setupButton = new Button("Setup");
        
        setupButton.setOnAction(a -> {
        	// Retrieve user input
            String userName = userNameField.getText();
            String password = passwordField.getText();
            UserNameRecognizer.checkForValidUserName(userName);
        	PasswordEvaluator.evaluatePassword(password);
            String code = inviteCodeField.getText();           
            try {
            	//Check username--can't get it to output errors
            	//UserNameRecognizer.checkForValidUserName(userName);
            	if (UserNameRecognizer.checkForValidUserName(userName) != "")
            		errorLabel.setText(UserNameRecognizer.checkForValidUserName(userName));
            	// Check password
            	//PasswordEvaluator.evaluatePassword(password);
            	else if (!PasswordEvaluator.foundUpperCase)
            		errorLabel.setText("Password: please add an uppercase letter");
        		
            	else if (!PasswordEvaluator.foundLowerCase)
        			errorLabel.setText("Password: please add an lowercase letter");
        		
            	else if (!PasswordEvaluator.foundNumericDigit)
        			errorLabel.setText("Password: please add a Numeric digit; ");
        			
            	else if (!PasswordEvaluator.foundSpecialChar)
        			errorLabel.setText("Password: please add a special character; ");
        			
            	else if (!PasswordEvaluator.foundLongEnough)
        			errorLabel.setText("Password: not long enough");
        		
            	else if (PasswordEvaluator.foundOtherChar)
        			errorLabel.setText("Password: please get rid of invalid character"); //Edit: OtherChar error msg
            	
            	// Check if the user already exists
            	else if(!databaseHelper.doesUserExist(userName)) {
            		
            		// Validate the invitation code
            		if(databaseHelper.validateInvitationCode(code)) {
            			
            			
            			// Create a new user and register them in the database
            	        RadioButton role = (RadioButton)group.getSelectedToggle();
		            	User user=new User(userName, password, role.getText());
		            	
		                databaseHelper.register(user);
		                
		             // Navigate to the Welcome Login Page
		                new WelcomeLoginPage(databaseHelper).show(primaryStage,user);
            		}
            		else {
            			errorLabel.setText("Please enter a valid invitation code");
            		}
            	}
            	else {
            		errorLabel.setText("This useruserName is taken!!.. Please use another to setup an account");
            	}
            	
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
            }
        });

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        layout.getChildren().addAll(userNameField, passwordField,inviteCodeField,studentButton, adminButton, setupButton, errorLabel);

        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("Account Setup");
        primaryStage.show();
    }
}
*/