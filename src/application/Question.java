package application;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Question {
    private UUID id;
    private String author;
    private String questionText;
    private String questionTitle;
    private List<Answer> answers;
    private List<Answer> approvedSolutions;
    private Question referencedQuestion;
    private LocalDateTime timestamp;
    private boolean resolved;

    private void validateTitle(String title) {
        if (title != null && title.trim().split("\\s+").length > 20) {
            throw new IllegalArgumentException("Question title must be at most 20 words.");
        }
    }

    public Question(String author, String title, String text) {
        validateTitle(title);
        this.id = UUID.randomUUID();
        this.author = author;
        this.questionTitle = title;
        this.questionText = text;
        this.answers = new ArrayList<>();
        this.approvedSolutions = new ArrayList<>();
        this.timestamp = LocalDateTime.now();
        this.resolved = false;
    }

    public Question(String author, String title, String text, Question referenced) {
        this(author, title, text);
        this.referencedQuestion = referenced;
    }

    public Question(String author, String questionTitle, String questionText, LocalDateTime timestamp, UUID id, boolean resolved) {
        validateTitle(questionTitle);
        this.id = id;
        this.author = author;
        this.questionTitle = questionTitle;
        this.questionText = questionText;
        this.answers = new ArrayList<>();
        this.approvedSolutions = new ArrayList<>();
        this.timestamp = timestamp;
        this.resolved = resolved;
    }
    
    public Question(String author, String questionTitle, String questionText, LocalDateTime timestamp, UUID id, boolean resolved, Question referencedQuestion) {
        this(author, questionTitle, questionText, timestamp, id, resolved);
        this.referencedQuestion = referencedQuestion;
    }

    public UUID getId() {
        return id;
    }

    public String getQuestionText() {
        return questionText;
    }

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return questionTitle;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Question getReferencedQuestion() {
        return referencedQuestion;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public List<Answer> getApprovedSolutions() {
        return new ArrayList<>(approvedSolutions);
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public void addAnswer(Answer answer) {
        answer.setQuestion(this);
        answers.add(answer);
    }

    public void removeAnswer(Answer answer) {
        answers.remove(answer);
        approvedSolutions.remove(answer);
        answer.removeQuestion();
    }

    public void markAnswerAsSolution(Answer answer) {
        if (!answers.contains(answer)) {
            throw new IllegalArgumentException("Answer does not belong to this question.");
        }
        answer.markAsSolution();
        if (!approvedSolutions.contains(answer)) {
            approvedSolutions.add(answer);
        }
        resolved = true;
    }

    public void unmarkAnswerAsSolution(Answer answer) {
        if (approvedSolutions.contains(answer)) {
            answer.unmarkAsSolution();
            approvedSolutions.remove(answer);
            if (approvedSolutions.isEmpty()) {
                resolved = false;
            }
        } else {
            throw new IllegalArgumentException("Answer is not marked as an approved solution.");
        }
    }

    public void updateQuestionText(String newText) {
        this.questionText = newText;
    }
}
