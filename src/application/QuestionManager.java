package application;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import databasePart1.DatabaseHelper;

public class QuestionManager {

    // list of questions and answers
    private List<Question> questions;
    private List<Answer> answers;
    
    private final DatabaseHelper databaseHelper;
    
    public QuestionManager(DatabaseHelper databaseHelper) {
        this.questions = new ArrayList<>();
        this.databaseHelper = databaseHelper;
    }

    // create a new question.
    public void createQuestion(String author, String questionText) {
        if (!isValidQuestionText(questionText)) {
            throw new IllegalArgumentException("Question must be between 10 and 300 words.");
        }
        Question q = new Question(author, questionText);
        questions.add(q);
    }

    //create a question with a reference.
    public void createQuestion(String author, String questionText, Question referencedQuestion) {
        if (!isValidQuestionText(questionText)) {
            throw new IllegalArgumentException("Question must be between 10 and 300 words.");
        }
        Question q = new Question(author, questionText, referencedQuestion);
        questions.add(q);
    }

    // create a new answer for a given question.
    public void createAnswer(String author, String answerText, Question question) {
        if (!isValidAnswerText(answerText)) {
            throw new IllegalArgumentException("Answer must be between 10 and 500 words.");
        }
        Answer a = new Answer(answerText, author);
        question.addAnswer(a);
    }

    // mark answer as an approved solution
    public void markAnswerAsSolution(Question question, Answer answer) {
        question.markAnswerAsSolution(answer);
    }

    // remove an answer as a solution
    public void unmarkAnswerAsSolution(Question question, Answer answer) {
        question.unmarkAnswerAsSolution(answer);
    }

    // delete a question by its UUID (may need to delete answers?)
    public boolean deleteQuestion(UUID questionId) {
        Question q = findQuestionById(questionId);
        if (q != null) {
            questions.remove(q);
            return true;
        }
        return false;
    }

    // delete an answer by its UUID.
    public boolean deleteAnswer(UUID answerId) {
        for (Question q : questions) {
            List<Answer> answersCopy = new ArrayList<>(q.getAnswers());
            for (Answer a : answersCopy) {
                if (a.getId().equals(answerId)) {
                    q.removeAnswer(a);
                    return true;
                }
            }
        }
        return false;
    }

    // search for questions by keyword
    public List<Question> searchQuestions(String keyword) {
        List<Question> matching = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Keyword cannot be empty.");
        }
        for (Question q : questions) {
            if (q.getQuestionText().toLowerCase().contains(keyword.toLowerCase())) {
                matching.add(q);
            }
        }
        return matching;
    }

    // search for answers by keyword in the answer text
    public List<Answer> searchAnswers(String keyword) {
        List<Answer> matching = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Keyword cannot be empty.");
        }
        for (Question q : questions) {
            for (Answer a : q.getAnswers()) {
                if (a.getAnswerText().toLowerCase().contains(keyword.toLowerCase())) {
                    matching.add(a);
                }
            }
        }
        return matching;
    }

    // get all approved solutions across all questions (probably useless)
    public List<Answer> getAllApprovedSolutions() {
        List<Answer> approved = new ArrayList<>();
        for (Question q : questions) {
            approved.addAll(q.getApprovedSolutions());
        }
        return approved;
    }

    public List<Question> getAllQuestions() {
        return new ArrayList<>(questions);
    }

    // find question by UUID
    public Question findQuestionById(UUID id) {
        for (Question q : questions) {
            if (q.getId().equals(id)) {
                return q;
            }
        }
        return null;
    }

    // find an answer by UUID
    public Answer findAnswerById(UUID answerId) {
        for (Question q : questions) {
            for (Answer a : q.getAnswers()) {
                if (a.getId().equals(answerId)) {
                    return a;
                }
            }
        }
        return null;
    }
    
    // find list of question by username
    public List<Question> findQuestionsByAuthor(String username) {
        List<Question> userQuestions = new ArrayList<>();

        for (Question q : questions) {
            if (q.getAuthor().equals(username)) {
            	userQuestions.add(q);
            }
        }
        return null;
    }
    
    // find list of answers by username
    public List<Question> findAnswersByAuthor(String username) {
        List<Answer> userAnswers = new ArrayList<>();

        for (Answer a : answers) {
            if (a.getAuthor().equals(username)) {
            	userAnswers.add(a);
            }
        }
        return null;
    }

    private boolean isValidQuestionText(String text) {
        int wordCount = text.trim().split("\\s+").length;
        return wordCount >= 10 && wordCount <= 300;
    }

    private boolean isValidAnswerText(String text) {
        int wordCount = text.trim().split("\\s+").length;
        return wordCount >= 10 && wordCount <= 500;
    }
}
