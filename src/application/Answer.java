package application;

import java.time.LocalDateTime;
import java.util.UUID;

public class Answer {

    private final UUID id;
    private String answerText;
    private String author;
    private boolean isApprovedSolution;
    private Question referencedQuestion; // parent question
    private LocalDateTime timestamp;

    public Answer(String answerText, String author) {
        this.id = UUID.randomUUID(); // gen unique ID
        this.answerText = answerText;
        this.author = author;
        this.timestamp = LocalDateTime.now();
        this.isApprovedSolution = false;
    }

    public UUID getId() {
        return id;
    }
    
    public String getAnswerText() {
        return answerText;
    }

    public String getAuthor() {
        return author;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public boolean isApprovedSolution() {
        return isApprovedSolution;
    }

    // mark this answer as approved
    public void markAsSolution() {
        this.isApprovedSolution = true;
    }

    // remove  approved solution status
    public void unmarkAsSolution() {
        this.isApprovedSolution = false;
    }

    public void setQuestion(Question question) {
        this.referencedQuestion = question;
    }

    public Question getQuestion() {
        return referencedQuestion;
    }

    public void removeQuestion() {
        this.referencedQuestion = null;
    }
    
    // optionally update the answer text (prob not use)
    public void updateAnswerText(String newText) {
        this.answerText = newText;
    }
}
