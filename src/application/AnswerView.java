package application;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class AnswerView extends VBox {
    private Answer answer;
    private Question question;
    private QuestionManager questionManager;
    private User currentUser;
    private List<Answer> allAnswers; // list of all answers for the question
    
    private Label answerLabel;
    private Button seeMoreButton;
    private boolean expanded = false;
    private VBox replyContainer;
    private VBox nestedRepliesContainer;
    
    public AnswerView(Answer answer, Question question, QuestionManager questionManager, User currentUser, List<Answer> allAnswers) {
        this.answer = answer;
        this.question = question;
        this.questionManager = questionManager;
        this.currentUser = currentUser;
        this.allAnswers = allAnswers;
        setPadding(new Insets(10));
        setSpacing(8);
        setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc; -fx-border-width: 1px; -fx-border-radius: 5px; -fx-background-radius: 5px;");
        buildView();
    }
    
    private void buildView() {

        HBox header = new HBox(10);
        Label authorLabel = new Label("By: " + answer.getAuthor());
        authorLabel.setStyle("-fx-font-weight: bold;");
        header.getChildren().add(authorLabel);
        
        answerLabel = new Label(getSnippet(answer.getAnswerText(), 40));
        answerLabel.setWrapText(true);
        
        // “See more” button if the answer text is longer than the snippet
        seeMoreButton = new Button("See more");
        if (answer.getAnswerText().split("\\s+").length <= 40) {
            seeMoreButton.setVisible(false);
        }
        seeMoreButton.setOnAction(e -> toggleExpanded());
        
        Button replyButton = new Button("Reply");
        replyButton.setOnAction(e -> toggleReplyBox());
        
        // container for the reply box (appears when reply is clicked).
        replyContainer = new VBox();
        replyContainer.setSpacing(5);
        
        nestedRepliesContainer = new VBox();
        nestedRepliesContainer.setSpacing(5);
        nestedRepliesContainer.setPadding(new Insets(10, 0, 0, 20)); // indent nested replies
        
        getChildren().addAll(header, answerLabel, seeMoreButton, replyButton, replyContainer, nestedRepliesContainer);
        
        loadNestedReplies();
    }
    
    private String getSnippet(String fullText, int maxWords) {
        if (fullText == null || fullText.isEmpty()) return "";
        String[] words = fullText.split("\\s+");
        int count = Math.min(words.length, maxWords);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(words[i]).append(" ");
        }
        if (words.length > maxWords) {
            sb.append("...");
        }
        return sb.toString().trim();
    }
    
    private void toggleExpanded() {
        if (expanded) {
            answerLabel.setText(getSnippet(answer.getAnswerText(), 40));
            seeMoreButton.setText("See more");
            expanded = false;
        } else {
            answerLabel.setText(answer.getAnswerText());
            seeMoreButton.setText("See less");
            expanded = true;
        }
    }
    
    private void toggleReplyBox() {
        if (replyContainer.getChildren().isEmpty()) {
            ReplyBox replyBox = new ReplyBox(text -> {
                // when replying to an answer, create a new answer whose parentAnswerId is set
                try {
                    // create the reply answer and mark it as a reply.
                    Answer newAnswer = new Answer(text, currentUser.getUserName(), answer.getId());
                    question.getAnswers().add(newAnswer);
                    questionManager.createAnswer(currentUser.getUserName(), text, question);
                    // refresh replies.
                    nestedRepliesContainer.getChildren().clear();
                    loadNestedReplies();
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            });
            replyContainer.getChildren().add(replyBox);
        } else {
            replyContainer.getChildren().clear();
        }
    }
    
    private void loadNestedReplies() {
        nestedRepliesContainer.getChildren().clear();
        for (Answer a : allAnswers) {
            if (a.getParentAnswerId() != null && a.getParentAnswerId().equals(answer.getId())) {
                AnswerView nestedView = new AnswerView(a, question, questionManager, currentUser, allAnswers);
                nestedRepliesContainer.getChildren().add(nestedView);
            }
        }
    }
}
