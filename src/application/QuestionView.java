package application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class QuestionView extends VBox {
    private Question question;
    
    public QuestionView(Question question) {
        this.question = question;
        setPadding(new Insets(10));
        setSpacing(8);
        setStyle("-fx-background-color: #ffffff; -fx-border-color: #ddd; -fx-border-width: 1px; -fx-border-radius: 5px; -fx-background-radius: 5px;");
        buildView();
    }
    
    private void buildView() {
    	
    	// custom view of each question snippet

        Label titleLabel = new Label(question.getTitle());
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        Label authorLabel = new Label(question.getAuthor());
        authorLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().addAll(titleLabel);
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(spacer, authorLabel);
        
        
        String snippet = getSnippet(question.getQuestionText(), 40); // grabs word count
        Label snippetLabel = new Label(snippet);
        snippetLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #333;");
        snippetLabel.setWrapText(true);
        

        Label answerCountLabel = new Label("Answers: " + question.getAnswers().size());
        answerCountLabel.setStyle("-fx-font-size: 12px;");
        Button replyButton = new Button("Reply");

        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.getChildren().add(answerCountLabel);
        HBox footerSpacer = new HBox();
        HBox.setHgrow(footerSpacer, Priority.ALWAYS); // fine tuning
        footer.getChildren().addAll(footerSpacer, replyButton);
        
        getChildren().addAll(header, snippetLabel, footer);
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
            sb.append("..."); // too longggggg
        }
        return sb.toString().trim();
    }
    
    public Question getQuestion() {
        return question;
    }
}
