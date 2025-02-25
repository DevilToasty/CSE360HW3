package application;

import databasePart1.DatabaseHelper;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class QuestionDetailView {

    private Scene scene;
    private Question question;
    private QuestionManager questionManager;
    private DatabaseHelper databaseHelper;
    private User currentUser;
    
    // store as instance variables
    private VBox answersContainer;    
    private VBox questionReplyContainer;  

    public QuestionDetailView(Question question, QuestionManager questionManager, DatabaseHelper databaseHelper, User currentUser) {
        this.question = question;
        this.questionManager = questionManager;
        this.databaseHelper = databaseHelper;
        this.currentUser = currentUser;
    }
    
    public void show(CustomTrackedStage primaryStage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
       
        VBox header = new VBox(10);
        header.setPadding(new Insets(10));

        Button backButton = BackButton.createBackButton(primaryStage);
        Label titleLabel = new Label(question.getTitle());
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        header.getChildren().addAll(backButton, titleLabel);
                
        Label fullTextLabel = new Label(question.getQuestionText());
        fullTextLabel.setWrapText(true);
        fullTextLabel.setStyle("-fx-font-size: 14px;");
        
        // reply container for the question
        questionReplyContainer = new VBox();
        Button replyButton = new Button("Reply");
        replyButton.setOnAction(e -> {
        	// checks if child (broken for now)
            if (questionReplyContainer.getChildren().isEmpty()) {
                ReplyBox replyBox = new ReplyBox(text -> {
                    try {
                        questionManager.createAnswer(currentUser.getUserName(), text, question);
                        // refresh the current view with updated answers
                        refreshContent();
                    } catch(Exception ex) {
                        ex.printStackTrace();
                    }
                });
                // adds to the nest
                questionReplyContainer.getChildren().add(replyBox);
            } else {
            	// main reply
                questionReplyContainer.getChildren().clear();
            }
        });
        
        VBox questionContainer = new VBox(10);
        questionContainer.getChildren().addAll(fullTextLabel, replyButton, questionReplyContainer);
        
        // for all the answers
        answersContainer = new VBox(10);
        loadAnswers();
        ScrollPane answersScroll = new ScrollPane(answersContainer);
        answersScroll.setFitToWidth(true);
        
        VBox mainContent = new VBox(20);
        mainContent.getChildren().addAll(questionContainer, new Label("Answers:"), answersScroll);
        
        root.setTop(header);
        root.setCenter(mainContent);
        
        scene = new Scene(root, 900, 700);
        primaryStage.showScene(scene);
    }
    
    // helper method to update the view
    private void loadAnswers() {
        answersContainer.getChildren().clear();
        for (Answer a : question.getAnswers()) {
            if (a.getParentAnswerId() == null) {
                AnswerView aView = new AnswerView(a, question, questionManager, currentUser, question.getAnswers(), this::refreshContent); // refreshes the page
                answersContainer.getChildren().add(aView);
            }
        }
    }
    
    // refresh the answers list from the database
    private void refreshContent() {
        questionManager.refreshQuestion(question);
        loadAnswers();
        questionReplyContainer.getChildren().clear();
    }
    
}
