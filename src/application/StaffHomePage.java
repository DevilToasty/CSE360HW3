package application;

import databasePart1.DatabaseHelper;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class StaffHomePage {

    private final DatabaseHelper databaseHelper;
    private final User currentUser;

    public StaffHomePage(DatabaseHelper databaseHelper, User currentUser) {
        this.databaseHelper = databaseHelper;
        this.currentUser = currentUser;
    }

    
    public void show(CustomTrackedStage primaryStage, QuestionManager questionManager) {
        BorderPane borderPane = new BorderPane();

        Button backButton = new Button("<-- Back");
        backButton.setOnAction(e -> primaryStage.goBack());
        BorderPane.setAlignment(backButton, Pos.TOP_LEFT);
        borderPane.setTop(backButton);
        Label userLabel = new Label("Hello,  Staff!");
        userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Button discussionButton = new Button("View Discussion");
        
		discussionButton.setOnAction(a -> {
		       new DiscussionView(databaseHelper, questionManager, currentUser).show(primaryStage);     
		});
        

        VBox centerBox = new VBox(userLabel);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.getChildren().add(discussionButton);
        borderPane.setCenter(centerBox);

        Scene scene = new Scene(borderPane, 800, 400);
        primaryStage.setTitle("Staff Home");
        primaryStage.showScene(scene);
    }
}