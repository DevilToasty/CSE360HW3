package application;

import databasePart1.DatabaseHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import java.util.List;
import java.util.UUID;

public class StudentHomePage {

    private Scene scene;
    private final DatabaseHelper databaseHelper;
    private final QuestionManager questionManager;
    private final User currentUser;
    private ListView<String> questionsListView;
    private ListView<String> answersListView;
    private ListView<String> searchResultsListView;
    private TextField keywordSearchField;

    public StudentHomePage(DatabaseHelper databaseHelper, QuestionManager questionManager, User currentUser) {
        this.databaseHelper = databaseHelper;
        this.questionManager = questionManager;
        this.currentUser = currentUser;
    }

    public void show(CustomTrackedStage primaryStage) {
        BorderPane borderPane = new BorderPane();
        borderPane.setStyle("-fx-background-color: #ffffff;");

        Button backButton = BackButton.createBackButton(primaryStage);
        BorderPane.setMargin(backButton, new Insets(10));
        BorderPane.setAlignment(backButton, Pos.TOP_LEFT);
        borderPane.setTop(backButton);

        VBox centerBox = new VBox(15);
        centerBox.setPadding(new Insets(20));
        centerBox.setAlignment(Pos.TOP_CENTER);
        centerBox.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #ddd; -fx-border-width: 1px;");

        Label greetingLabel = new Label("Hello, " + currentUser.getUserName() + "!");
        greetingLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");

        TextField questionInput = new TextField();
        questionInput.setPromptText("Enter your question");
        questionInput.setPrefWidth(600);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");

        Button submitQuestionButton = new Button("Submit Question");
        submitQuestionButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        submitQuestionButton.setOnAction(e -> {
            errorLabel.setText("");
            String questionText = questionInput.getText().trim();
            if (questionText.isEmpty()) {
                errorLabel.setText("Question text cannot be empty.");
                return;
            }
            try {
                // Using question text for both title and body for simplicity
                questionManager.createQuestion(currentUser.getUserName(), questionText, questionText);
                System.out.println("Question created.");
                questionInput.clear();
                loadMyQuestions(errorLabel);
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error: " + ex.getMessage());
            }
        });

        questionsListView = new ListView<>();
        questionsListView.setPrefHeight(150);
        questionsListView.setStyle("-fx-background-color: #fff; -fx-border-color: #ccc;");

        Button viewMyQuestionsButton = new Button("View My Questions");
        viewMyQuestionsButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        viewMyQuestionsButton.setOnAction(e -> loadMyQuestions(errorLabel));

        Button deleteQuestionButton = new Button("Delete Selected Question");
        deleteQuestionButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        deleteQuestionButton.setOnAction(e -> deleteSelectedQuestion(errorLabel));

        Button viewAnswersButton = new Button("View Answers for Selected Question");
        viewAnswersButton.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white;");
        viewAnswersButton.setOnAction(e -> viewAnswersForSelectedQuestion(errorLabel));

        answersListView = new ListView<>();
        answersListView.setPrefHeight(150);
        answersListView.setStyle("-fx-background-color: #fff; -fx-border-color: #ccc;");

        TextField answerInput = new TextField();
        answerInput.setPromptText("Enter your answer for the selected question");
        answerInput.setPrefWidth(600);

        Button submitAnswerButton = new Button("Submit Answer");
        submitAnswerButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
        submitAnswerButton.setOnAction(e -> {
            errorLabel.setText("");
            String answerText = answerInput.getText().trim();
            if (answerText.isEmpty()) {
                errorLabel.setText("Answer text cannot be empty.");
                return;
            }
            String selected = questionsListView.getSelectionModel().getSelectedItem();
            if (selected == null || selected.isEmpty() || !selected.startsWith("[")) {
                errorLabel.setText("Please select a valid question to answer.");
                return;
            }
            try {
                int idStart = selected.indexOf('[') + 1;
                int idEnd = selected.indexOf(']');
                String idStr = selected.substring(idStart, idEnd);
                UUID questionId = UUID.fromString(idStr);
                Question q = questionManager.findQuestionById(questionId);
                if (q != null) {
                    questionManager.createAnswer(currentUser.getUserName(), answerText, q);
                    System.out.println("Answer submitted.");
                    answerInput.clear();
                    q.setResolved(true);
                    questionManager.markAnswerAsSolution(q, q.getAnswers().get(q.getAnswers().size() - 1));
                    viewAnswersForSelectedQuestion(errorLabel);
                } else {
                    errorLabel.setText("Selected question not found.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("Error submitting answer: " + ex.getMessage());
            }
        });

        Label searchSectionLabel = new Label("Search Options:");
        searchSectionLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        ComboBox<String> searchCriteriaComboBox = new ComboBox<>();
        searchCriteriaComboBox.getItems().addAll("Answered Questions", "Unanswered Questions", "Reviewers", "Keyword Search");
        searchCriteriaComboBox.getSelectionModel().selectFirst();
        searchCriteriaComboBox.setPrefWidth(200);
        searchCriteriaComboBox.setStyle(
            "-fx-background-color: #ffffff;" +
            "-fx-background-radius: 20;" +
            "-fx-border-color: #ccc;" +
            "-fx-border-radius: 20;" +
            "-fx-padding: 5 10 5 10;"
        );

        keywordSearchField = new TextField();
        keywordSearchField.setPromptText("Enter keyword to search for questions");
        keywordSearchField.setPrefWidth(400);

        Button searchButton = new Button("Search");
        searchButton.setStyle("-fx-background-color: #607D8B; -fx-text-fill: white;");
        searchButton.setOnAction(e -> searchByCriteria(searchCriteriaComboBox, errorLabel));

        searchResultsListView = new ListView<>();
        searchResultsListView.setPrefHeight(150);
        searchResultsListView.setStyle("-fx-background-color: #fff; -fx-border-color: #ccc;");

        centerBox.getChildren().addAll(
            greetingLabel,
            questionInput,
            submitQuestionButton,
            errorLabel,
            viewMyQuestionsButton,
            questionsListView,
            deleteQuestionButton,
            viewAnswersButton,
            answerInput,
            submitAnswerButton,
            new Label("Answers:"),
            answersListView,
            searchSectionLabel,
            searchCriteriaComboBox,
            keywordSearchField,
            searchButton,
            new Label("Search Results:"),
            searchResultsListView
        );
        borderPane.setCenter(centerBox);
        scene = new Scene(borderPane, 800, 700);
        primaryStage.showScene(scene);
    }

    private void loadMyQuestions(Label errorLabel) {
        questionsListView.getItems().clear();
        List<Question> myQuestions = questionManager.findQuestionsByAuthor(currentUser.getUserName());
        if (myQuestions.isEmpty()) {
            questionsListView.getItems().add("No questions found.");
        } else {
            for (Question q : myQuestions) {
                questionsListView.getItems().add("[" + q.getId().toString() + "] " + q.getQuestionText());
            }
        }
        answersListView.getItems().clear();
    }

    private void deleteSelectedQuestion(Label errorLabel) {
        String selected = questionsListView.getSelectionModel().getSelectedItem();
        if (selected == null || selected.isEmpty() || !selected.startsWith("[")) {
            errorLabel.setText("Please select a valid question to delete.");
            return;
        }
        try {
            int idStart = selected.indexOf('[') + 1;
            int idEnd = selected.indexOf(']');
            String idStr = selected.substring(idStart, idEnd);
            UUID questionId = UUID.fromString(idStr);
            if (questionManager.deleteQuestion(questionId)) {
                System.out.println("Deleted question with ID: " + questionId.toString());
                loadMyQuestions(errorLabel);
                answersListView.getItems().clear();
            } else {
                errorLabel.setText("Failed to delete the question.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            errorLabel.setText("Error deleting question: " + ex.getMessage());
        }
    }

    private void viewAnswersForSelectedQuestion(Label errorLabel) {
        String selected = questionsListView.getSelectionModel().getSelectedItem();
        if (selected == null || selected.isEmpty() || !selected.startsWith("[")) {
            errorLabel.setText("Please select a valid question to view its answers.");
            return;
        }
        try {
            int idStart = selected.indexOf('[') + 1;
            int idEnd = selected.indexOf(']');
            String idStr = selected.substring(idStart, idEnd);
            UUID questionId = UUID.fromString(idStr);
            List<Answer> answers = questionManager.findQuestionById(questionId).getAnswers();
            answersListView.getItems().clear();
            if (answers.isEmpty()) {
                answersListView.getItems().add("No answers found for this question.");
            } else {
                for (Answer a : answers) {
                    answersListView.getItems().add("[" + a.getId().toString() + "] " + a.getAnswerText());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            errorLabel.setText("Error loading answers: " + ex.getMessage());
        }
    }

    private void searchByCriteria(ComboBox<String> searchCriteriaComboBox, Label errorLabel) {
        searchResultsListView.getItems().clear();
        String criteria = searchCriteriaComboBox.getSelectionModel().getSelectedItem();
        try {
            if (criteria.equals("Answered Questions")) {
                List<Question> answered = questionManager.getAnsweredQuestions();
                if (answered.isEmpty()) {
                    searchResultsListView.getItems().add("No answered questions found.");
                } else {
                    for (Question q : answered) {
                        searchResultsListView.getItems().add("[" + q.getId().toString() + "] " + q.getQuestionText());
                    }
                }
            } else if (criteria.equals("Unanswered Questions")) {
                List<Question> unanswered = questionManager.getUnansweredQuestions();
                if (unanswered.isEmpty()) {
                    searchResultsListView.getItems().add("No unanswered questions found.");
                } else {
                    for (Question q : unanswered) {
                        searchResultsListView.getItems().add("[" + q.getId().toString() + "] " + q.getQuestionText());
                    }
                }
            } else if (criteria.equals("Reviewers")) {
                List<User> reviewers = questionManager.getReviewers();
                if (reviewers.isEmpty()) {
                    searchResultsListView.getItems().add("No reviewers found.");
                } else {
                    for (User reviewer : reviewers) {
                        searchResultsListView.getItems().add(reviewer.getUserName() + " (" + reviewer.getName() + ")");
                    }
                }
            } else if (criteria.equals("Keyword Search")) {
                String keyword = keywordSearchField.getText().trim();
                if (keyword.isEmpty()) {
                    errorLabel.setText("Please enter a keyword to search.");
                    return;
                }
                List<Question> keywordResults = questionManager.searchQuestionsByKeyword(keyword);
                if (keywordResults.isEmpty()) {
                    searchResultsListView.getItems().add("No questions found matching keyword: " + keyword);
                } else {
                    for (Question q : keywordResults) {
                        searchResultsListView.getItems().add("[" + q.getId().toString() + "] " + q.getQuestionText());
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            errorLabel.setText("Error performing search: " + ex.getMessage());
        }
    }
}
