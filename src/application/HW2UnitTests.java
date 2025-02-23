/*
package application;
import databasePart1.DatabaseHelper;
import java.util.List;
import java.util.UUID;
public class HW2UnitTests {
    public static void main(String[] args) {
        testQuestionManagerFunctions();
    }
    private static void testQuestionManagerFunctions() {
        int testsRun = 0, testsPassed = 0;
        DatabaseHelper dbHelperDummy = new DatabaseHelper();
        QuestionManager qm = new QuestionManager(dbHelperDummy);
        try {
            
        	// Test 1: Create a valid question
            testsRun++;
            qm.createQuestion("User1", "A", "This is a valid question for testing that contains more than ten words to satisfy the requirement.");
            if(qm.getAllQuestions().size() == 1) { testsPassed++; System.out.println("Test 1 passed"); } else { System.out.println("Test 1 failed"); }
            
            // Test 2: Create an invalid question (too few words)
            testsRun++;
            boolean caught = false;
            try { qm.createQuestion("User2", "A", "Too short"); } catch(IllegalArgumentException e) { caught = true; }
            if(caught) { testsPassed++; System.out.println("Test 2 passed"); } else { System.out.println("Test 2 failed"); }
            
            // Test 3: Create a valid answer
            testsRun++;
            Question q1 = qm.getAllQuestions().get(0);
            int answerCount = q1.getAnswers().size();
            qm.createAnswer("Responder1", "This is a valid answer text MAKE IT LONGER NOW IT FITS THE WORD LEMGTH MAKE IT LONGER NOW IT FITS THE WORD LEMGTH.", q1);
            if(q1.getAnswers().size() == answerCount + 1) { testsPassed++; System.out.println("Test 3 passed"); } else { System.out.println("Test 3 failed"); }
            
            // Test 4: Create an invalid answer (too few words)
            testsRun++;
            caught = false;
            try { qm.createAnswer("Responder2", "Short", q1); } catch(IllegalArgumentException e) { caught = true; }
            if(caught) { testsPassed++; System.out.println("Test 4 passed"); } else { System.out.println("Test 4 failed"); }
            
            // Test 5: Mark an answer as solution
            testsRun++;
            Answer a1 = q1.getAnswers().get(0);
            qm.markAnswerAsSolution(q1, a1);
            if(a1.isApprovedSolution()) { testsPassed++; System.out.println("Test 5 passed"); } else { System.out.println("Test 5 failed"); }
            
            // Test 6: Unmark an answer as solution
            testsRun++;
            qm.unmarkAnswerAsSolution(q1, a1);
            if(!a1.isApprovedSolution()) { testsPassed++; System.out.println("Test 6 passed"); } else { System.out.println("Test 6 failed"); }
            
            // Test 7: Update question text
            testsRun++;
            String oldText = q1.getQuestionText();
            q1.updateQuestionText("Updated question text for in-memory testing of update functionality.");
            if(!q1.getQuestionText().equals(oldText)) { testsPassed++; System.out.println("Test 7 passed"); } else { System.out.println("Test 7 failed"); }
            
            // Test 8: Update answer text
            testsRun++;
            String oldAnswerText = a1.getAnswerText();
            a1.updateAnswerText("Updated answer text for in-memory testing of answer update.");
            if(!a1.getAnswerText().equals(oldAnswerText)) { testsPassed++; System.out.println("Test 8 passed"); } else { System.out.println("Test 8 failed"); }
            
            // Test 9: Search questions (should find updated question)
            testsRun++;
            List<Question> searchResults = qm.searchQuestions("Updated");
            if(searchResults.size() > 0) { testsPassed++; System.out.println("Test 9 passed"); } else { System.out.println("Test 9 failed"); }
            
            // Test 10: Delete answer from question
            testsRun++;
            int countBefore = q1.getAnswers().size();
            boolean delAns = qm.deleteAnswer(a1.getId());
            if(delAns && q1.getAnswers().size() == countBefore - 1) { testsPassed++; System.out.println("Test 10 passed"); } else { System.out.println("Test 10 failed"); }
            
            // Test 11: Delete question from memory
            testsRun++;
            boolean delQ = qm.deleteQuestion(q1.getId());
            if(delQ && qm.getAllQuestions().isEmpty()) { testsPassed++; System.out.println("Test 11 passed"); } else { System.out.println("Test 11 failed"); }
            
            // Test 12: Create a question with reference (chaining)
            testsRun++;
            qm.createQuestion("User3", "Original chained question text that meets the requirements for testing by hitting the word count now maybe.", null);
            Question orig = qm.getAllQuestions().get(0);
            qm.createQuestion("User4", "A", "Follow-up question that references the original chained question and hits all of the word count stuff.", orig);
            if(qm.getAllQuestions().size() == 2) { testsPassed++; System.out.println("Test 12 passed"); } else { System.out.println("Test 12 failed"); }
            
            // Test 13: Create multiple answers and mark them as solutions
            testsRun++;
            qm.createAnswer("Responder3", "First multiple answer valid text MAKE IT LONGER NOW IT FITS THE WORD LEMGTH.", orig);
            qm.createAnswer("Responder4", "Second multiple answer valid text  MAKE IT LONGER NOW IT FITS THE WORD LEMGTH.", orig);
            List<Answer> multiAnswers = orig.getAnswers();
            if(multiAnswers.size() >= 2) {
                qm.markAnswerAsSolution(orig, multiAnswers.get(0));
                qm.markAnswerAsSolution(orig, multiAnswers.get(1));
                if(orig.getApprovedSolutions().size() == 2) { testsPassed++; System.out.println("Test 13 passed"); } else { System.out.println("Test 13 failed"); }
            } else { System.out.println("Test 13 failed"); }
            
            // Test 14: getApprovedSolutions returns correct list size
            testsRun++;
            if(orig.getApprovedSolutions().size() == 2) { testsPassed++; System.out.println("Test 14 passed"); } else { System.out.println("Test 14 failed"); }
            
            // Test 15: findQuestionById returns the correct question
            testsRun++;
            UUID origId = orig.getId();
            Question found = qm.findQuestionById(origId);
            if(found != null && found.getId().equals(origId)) { testsPassed++; System.out.println("Test 15 passed"); } else { System.out.println("Test 15 failed"); }
            
            // Test 16: findAnswerById returns the correct answer
            testsRun++;
            Answer sampleAnswer = multiAnswers.get(0);
            Answer foundAnswer = qm.findAnswerById(sampleAnswer.getId());
            if(foundAnswer != null && foundAnswer.getId().equals(sampleAnswer.getId())) { testsPassed++; System.out.println("Test 16 passed"); } else { System.out.println("Test 16 failed"); }
            
            // Test 17: deleteAnswer returns false for non-existent answer
            testsRun++;
            if(!qm.deleteAnswer(UUID.randomUUID())) { testsPassed++; System.out.println("Test 17 passed"); } else { System.out.println("Test 17 failed"); }
            
            // Test 18: deleteQuestion returns false for non-existent question
            testsRun++;
            if(!qm.deleteQuestion(UUID.randomUUID())) { testsPassed++; System.out.println("Test 18 passed"); } else { System.out.println("Test 18 failed"); }
            
            // Test 19: Update question text via QM and verify
            testsRun++;
            orig.updateQuestionText("Another update to chained question text.");
            if(orig.getQuestionText().contains("Another update")) { testsPassed++; System.out.println("Test 19 passed"); } else { System.out.println("Test 19 failed"); }
            
            // Test 20: Update answer text via QM and verify
            testsRun++;
            Answer ansToUpdate = multiAnswers.get(0);
            ansToUpdate.updateAnswerText("Another update to multiple answer text.");
            if(ansToUpdate.getAnswerText().contains("Another update")) { testsPassed++; System.out.println("Test 20 passed"); } else { System.out.println("Test 20 failed"); }
            
            // Test 21: Mark an answer as solution that is not part of the question (should throw exception)
            testsRun++;
            boolean exceptionThrown = false;
            Answer stray = new Answer("Stray answer text that is valid for testing.", "StrayResponder");
            try { qm.markAnswerAsSolution(orig, stray); } catch(IllegalArgumentException e) { exceptionThrown = true; }
            if(exceptionThrown) { testsPassed++; System.out.println("Test 21 passed"); } else { System.out.println("Test 21 failed"); }
            
            // Test 22: Unmark an answer as solution that is not marked (should throw exception)
            testsRun++;
            exceptionThrown = false;
            try { qm.unmarkAnswerAsSolution(orig, stray); } catch(IllegalArgumentException e) { exceptionThrown = true; }
            if(exceptionThrown) { testsPassed++; System.out.println("Test 22 passed"); } else { System.out.println("Test 22 failed"); }
            
            // Test 23: Delete an answer and ensure approvedSolutions list updates
            testsRun++;
            int approvedBefore = orig.getApprovedSolutions().size();
            Answer toRemove = multiAnswers.get(1);
            qm.deleteAnswer(toRemove.getId());
            if(orig.getApprovedSolutions().size() == approvedBefore - 1) { testsPassed++; System.out.println("Test 23 passed"); } else { System.out.println("Test 23 failed"); }
            
            // Test 24: getAllQuestions returns correct count after operations
            testsRun++;
            
            if(qm.getAllQuestions().size() >= 1) { testsPassed++; System.out.println("Test 24 passed"); } else { System.out.println("Test 24 failed"); }
            // Test 25: Insert question into DB
            DatabaseHelper dbHelper = new DatabaseHelper();
            dbHelper.connectToDatabase();
            Question dbQ = new Question("DBUser", "A", "Database test question that meets the requirements for insertion.");
            boolean insertedQ = dbHelper.insertQuestion(dbQ);
            if(insertedQ) { testsPassed++; System.out.println("Test 25 passed"); } else { System.out.println("Test 25 failed"); }
            
            // Test 26: Insert answer into DB
            testsRun++;
            Answer dbA = new Answer("Database test answer that is valid for insertion purposes.", "DBResponder");
            dbA.markAsSolution();
            boolean insertedA = dbHelper.insertAnswer(dbA, dbQ.getId());
            if(insertedA) { testsPassed++; System.out.println("Test 26 passed"); } else { System.out.println("Test 26 failed"); }
            
            // Test 27: Update question in DB
            testsRun++;
            dbQ.updateQuestionText("Updated DB question text for testing update functionality.");
            boolean updatedQ = dbHelper.updateQuestion(dbQ);
            if(updatedQ) { testsPassed++; System.out.println("Test 27 passed"); } else { System.out.println("Test 27 failed"); }
            
            // Test 28: Update answer in DB
            testsRun++;
            dbA.updateAnswerText("Updated DB answer text for testing update functionality.");
            dbA.unmarkAsSolution();
            boolean updatedA = dbHelper.updateAnswer(dbA);
            if(updatedA) { testsPassed++; System.out.println("Test 28 passed"); } else { System.out.println("Test 28 failed"); }
            
            // Test 29: Retrieve all questions from DB
            testsRun++;
            List<Question> dbQuestions = dbHelper.getAllQuestionsFromDB();
            if(dbQuestions.size() >= 1) { testsPassed++; System.out.println("Test 29 passed"); } else { System.out.println("Test 29 failed"); }
            
            // Test 30: Retrieve answers for DB question
            testsRun++;
            List<Answer> dbAnswers = dbHelper.getAnswersForQuestion(dbQ.getId());
            if(dbAnswers.size() >= 1) { testsPassed++; System.out.println("Test 30 passed"); } else { System.out.println("Test 30 failed"); }
            
            // Test 31: Delete answer from DB
            testsRun++;
            boolean dbDelA = dbHelper.deleteAnswer(dbA.getId());
            if(dbDelA) { testsPassed++; System.out.println("Test 31 passed"); } else { System.out.println("Test 31 failed"); }
            
            // Test 32: Delete question from DB
            testsRun++;
            System.out.println("Total tests run: " + testsRun + ", Passed: " + testsPassed);
            dbHelper.closeConnection();
        } catch(Exception e) {
            System.out.println("Exception during tests: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
*/
