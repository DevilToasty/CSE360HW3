package application;

import javafx.stage.Stage;
import javafx.scene.Scene;
import java.util.Stack;

import databasePart1.DatabaseHelper;

public class CustomTrackedStage extends Stage {

    private Stack<Scene> sceneHistory = new Stack<>();
    private User user;
    private DatabaseHelper databaseHelper;

    public CustomTrackedStage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void showScene(Scene newScene) {
        if (this.getScene() != null) {
            sceneHistory.push(this.getScene());
        }
        this.setScene(newScene);
        this.show();
    }

    public void goBack() {
        if (!sceneHistory.isEmpty()) {
            Scene previousScene = sceneHistory.pop();
            Boolean isWelcome = (Boolean) previousScene.getProperties().get("isWelcomePage");
            if (isWelcome != null && isWelcome) { // to check if we need to update login stuff
                new WelcomeLoginPage(databaseHelper).show(this, this.getUser());
                return;
            }
            this.setScene(previousScene);
            this.show();
        } else {
            System.out.println("No previous scene.");
        }
    }

    public void setLastScene(Scene newScene) {
        if (newScene != null) {
            sceneHistory.push(newScene);
        }
    }

    public void clearHistory() {
        sceneHistory.clear();
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
