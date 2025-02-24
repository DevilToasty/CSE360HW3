package application;

import java.time.LocalDateTime;
import java.util.UUID;

public class Answer {
    private final UUID id;
    private String answerText;
    private String author;
    private boolean isApprovedSolution;
    private Question referencedQuestion;
    private LocalDateTime timestamp;
    private UUID parentAnswerId;

    // constructor for a direct answer (no parent reply)
    public Answer(String answerText, String author) {
        this.id = UUID.randomUUID();
        this.answerText = answerText;
        this.author = author;
        this.timestamp = LocalDateTime.now();
        this.isApprovedSolution = false;
        this.parentAnswerId = null;
    }
    
    // constructor that accepts a parentAnswerId (for replying to an answer)
    public Answer(String answerText, String author, UUID parentAnswerId) {
        this.id = UUID.randomUUID();
        this.answerText = answerText;
        this.author = author;
        this.timestamp = LocalDateTime.now();
        this.isApprovedSolution = false;
        this.parentAnswerId = parentAnswerId;
    }
    
    public Answer(String answerText, String author, LocalDateTime timestamp, UUID id, boolean isApprovedSolution) {
        this.id = id;
        this.answerText = answerText;
        this.author = author;
        this.timestamp = timestamp;
        this.isApprovedSolution = isApprovedSolution;
        this.parentAnswerId = null;
    }
    
    public Answer(String answerText, String author, LocalDateTime timestamp, UUID id, boolean isApprovedSolution, Question referencedQuestion) {
        this(answerText, author, timestamp, id, isApprovedSolution);
        this.referencedQuestion = referencedQuestion;
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

    public void markAsSolution() {
        this.isApprovedSolution = true;
    }

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

    public void updateAnswerText(String newText) {
        this.answerText = newText;
    }
    
    public UUID getParentAnswerId() {
        return parentAnswerId;
    }
    
    public void setParentAnswerId(UUID parentAnswerId) {
        this.parentAnswerId = parentAnswerId;
    }
}
