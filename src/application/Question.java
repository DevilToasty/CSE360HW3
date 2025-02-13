package application;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Question {

    private final UUID id;
    private String author;
    private String questionText;
    private List<Answer> answers; // all answers.
    private List<Answer> approvedSolutions; 
    private Question referencedQuestion;   
    private LocalDateTime timestamp;

    public Question(String author, String text) {
        this.id = UUID.randomUUID(); // generate a unique ID
        this.author = author;
        this.questionText = text;
        this.answers = new ArrayList<>();
        this.approvedSolutions = new ArrayList<>();
        this.timestamp = LocalDateTime.now();
    }

    public Question(String author, String text, Question referenced) {
        this(author, text);
        this.referencedQuestion = referenced;
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

    public void addAnswer(Answer answer) {
        answer.setQuestion(this);
        answers.add(answer);
    }

    // remove answer from question
    public void removeAnswer(Answer answer) {
        answers.remove(answer);
        approvedSolutions.remove(answer); 
        answer.removeQuestion();
    }

    // mark answer as approved solution
    public void markAnswerAsSolution(Answer answer) {
        if (!answers.contains(answer)) {
            throw new IllegalArgumentException("Answer does not belong to this question.");
        }
        answer.markAsSolution();
        if (!approvedSolutions.contains(answer)) {
            approvedSolutions.add(answer);
        }
    }

    // unapprove a solution (remove its approved status).
    public void unmarkAnswerAsSolution(Answer answer) {
        if (approvedSolutions.contains(answer)) {
            answer.unmarkAsSolution();
            approvedSolutions.remove(answer);
        } else {
            throw new IllegalArgumentException("Answer is not marked as an approved solution.");
        }
    }
    
    // optionally update the question text (probably not used)
    public void updateQuestionText(String newText) {
        this.questionText = newText;
    }
}
