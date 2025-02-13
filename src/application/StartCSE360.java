package application;

import javafx.application.Application;
import javafx.stage.Stage;
import java.sql.SQLException;

import databasePart1.DatabaseHelper;

public class StartCSE360 extends Application {

    private static final DatabaseHelper databaseHelper = new DatabaseHelper();
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        try {
            databaseHelper.connectToDatabase();  // Connect to the database
            // Create our custom tracked stage.
            CustomTrackedStage trackedStage = new CustomTrackedStage(databaseHelper);  

            if (databaseHelper.isDatabaseEmpty()) {
                new FirstPage(databaseHelper).show(trackedStage);
            } else {
                new SetupLoginSelectionPage(databaseHelper).show(trackedStage);
            }
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
