package databasePart1;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import application.Answer;
import application.Question;
import application.User;

public class DatabaseHelper {
    static final String JDBC_DRIVER = "org.h2.Driver";
    static final String DB_URL = "jdbc:h2:~/FoundationDatabase";
    static final String USER = "sa";
    static final String PASS = "";
    private Connection connection = null;
    private Statement statement = null;
    
    private String sanitize(String input) {
        if (input == null) return null;
        return input.trim();
    }
    
    public void connectToDatabase() throws SQLException {
		try {
			Class.forName(JDBC_DRIVER); // Load the JDBC driver
			System.out.println("Connecting to database...");
			connection = DriverManager.getConnection(DB_URL, USER, PASS);
			statement = connection.createStatement(); 
			// You can use this command to clear the database and restart from fresh. DO NOT DELETE
			//statement.execute("DROP ALL OBJECTS");

			createTables();  // Create the necessary tables if they don't exist
		} catch (ClassNotFoundException e) {
			System.err.println("JDBC Driver not found: " + e.getMessage());
		}
	}
    
    private void createTables() throws SQLException {
        String userTable = "CREATE TABLE IF NOT EXISTS cse360users ("
            + "id INT AUTO_INCREMENT PRIMARY KEY, "
            + "userName VARCHAR(255) UNIQUE, "
            + "password VARCHAR(255), "
            + "email VARCHAR(255) UNIQUE, "
            + "name VARCHAR(255), "
            + "roles VARCHAR(255))";
        statement.execute(userTable);
        
        // AHHH SO MANY
        String approvedReviewersTable = "CREATE TABLE IF NOT EXISTS ApprovedReviewers ("
            + "ownerUserName VARCHAR(255) NOT NULL, "      // the user who owns this list
            + "reviewerName VARCHAR(255) NOT NULL, "  // the approved reviewer
            + "reviewerRating DOUBLE, "
            + "PRIMARY KEY (ownerUserName, reviewerName))";
        statement.execute(approvedReviewersTable);
        
        String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes ("
            + "code VARCHAR(10) PRIMARY KEY, "
            + "isUsed BOOLEAN DEFAULT FALSE)";
        statement.execute(invitationCodesTable);
        
        String userOTPAccess = "CREATE TABLE IF NOT EXISTS UserOTP ("
            + "userName VARCHAR(255) UNIQUE, "
            + "tempPassword VARCHAR(255), "
            + "isUsed BOOLEAN DEFAULT FALSE)";
        statement.execute(userOTPAccess);
        
        String questionsTable = "CREATE TABLE IF NOT EXISTS Questions ("
            + "id UUID PRIMARY KEY, "
            + "author VARCHAR(255) NOT NULL, "
            + "questionTitle CLOB NOT NULL, "
            + "questionText CLOB NOT NULL, "
            + "referencedQuestionId UUID, "
            + "timestamp TIMESTAMP NOT NULL, "
            + "resolved BOOLEAN DEFAULT FALSE)";
        statement.execute(questionsTable);
        
        String answersTable = "CREATE TABLE IF NOT EXISTS Answers ("
                + "id UUID PRIMARY KEY, "
                + "questionId UUID NOT NULL, "
                + "answerText CLOB NOT NULL, "
                + "author VARCHAR(255) NOT NULL, "
                + "isApprovedSolution BOOLEAN DEFAULT FALSE, "
                + "timestamp TIMESTAMP NOT NULL, "
                + "parentAnswerId UUID, "
                + "FOREIGN KEY (questionId) REFERENCES Questions(id), "
                + "FOREIGN KEY (parentAnswerId) REFERENCES Answers(id))";
        statement.execute(answersTable);
    }
    
    public boolean isDatabaseEmpty() throws SQLException {
        String query = "SELECT COUNT(*) AS count FROM cse360users";
        ResultSet resultSet = statement.executeQuery(query);
        if(resultSet.next()){
            return resultSet.getInt("count") == 0;
        }
        return true;
    }
    
    // user methods
    
    public void register(User user) throws SQLException {
        String insertUser = "INSERT INTO cse360users (userName, password, roles, email, name) VALUES (?, ?, ?, ?, ?)";
        try(PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
            pstmt.setString(1, sanitize(user.getUserName()));
            pstmt.setString(2, sanitize(user.getPassword()));
            pstmt.setString(3, sanitize(user.getRoles()));
            pstmt.setString(4, sanitize(user.getEmail()));
            pstmt.setString(5, sanitize(user.getName()));
            pstmt.executeUpdate();
        }
    }
    
    public boolean login(User user) throws SQLException {
        String query = "SELECT * FROM cse360users WHERE userName = ? AND password = ? AND roles = ?";
        try(PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, sanitize(user.getUserName()));
            pstmt.setString(2, sanitize(user.getPassword()));
            pstmt.setString(3, sanitize(user.getRoles()));
            try(ResultSet rs = pstmt.executeQuery()){
                if(rs.next()){
                    this.markUserOTPAsUsed(sanitize(user.getUserName()));
                    return true;
                } else {
                    return false;
                }
            }
        }
    }
    
    public List<User> getUsers() throws SQLException {
        List<User> userList = new ArrayList<>();
        String query = "SELECT * FROM cse360users";
        try(Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query)) {
            while(rs.next()){
                String userName = sanitize(rs.getString("userName"));
                String password = sanitize(rs.getString("password"));
                String email = sanitize(rs.getString("email"));
                String name = sanitize(rs.getString("name"));
                String roles = sanitize(rs.getString("roles"));
                Map<String, Double> approvedReviewers = getApprovedReviewers(userName);
                User user = new User(userName, password, name, email, roles);
                user.setApprovedReviewers(approvedReviewers);
                userList.add(user);
            }
        }
        return userList;
    }
    
    public User getUser(String username) throws SQLException {
        String query = "SELECT * FROM cse360users WHERE userName = ?";
        try(PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, sanitize(username));
            try(ResultSet rs = pstmt.executeQuery()){
                if(rs.next()){
                    String userName = sanitize(rs.getString("userName"));
                    String password = sanitize(rs.getString("password"));
                    String email = sanitize(rs.getString("email"));
                    String name = sanitize(rs.getString("name"));
                    String roles = sanitize(rs.getString("roles"));
                    Map<String, Double> approvedReviewers = getApprovedReviewers(userName);
                    return new User(userName, password, name, email, roles, approvedReviewers);
                }
            }
        }
        return null;
    }
    
    public boolean addApprovedReviewer(String ownerUserName, String reviewerName, double rating) {
        String query = "INSERT INTO ApprovedReviewers (ownerUserName, reviewerName, reviewerRating) VALUES (?, ?, ?)";
        try(PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, sanitize(ownerUserName));
            pstmt.setString(2, sanitize(reviewerName));
            pstmt.setDouble(3, rating);
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch(SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean updateApprovedReviewerRating(String ownerUserName, String reviewerName, double rating) {
        String query = "UPDATE ApprovedReviewers SET reviewerRating = ? WHERE ownerUserName = ? AND reviewerName = ?";
        try(PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setDouble(1, rating);
            pstmt.setString(2, sanitize(ownerUserName));
            pstmt.setString(3, sanitize(reviewerName));
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch(SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean removeApprovedReviewer(String ownerUserName, String reviewerName) {
        String query = "DELETE FROM ApprovedReviewers WHERE ownerUserName = ? AND reviewerName = ?";
        try(PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, sanitize(ownerUserName));
            pstmt.setString(2, sanitize(reviewerName));
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch(SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public Map<String, Double> getApprovedReviewers(String ownerUserName) {
        Map<String, Double> map = new HashMap<>();
        String query = "SELECT reviewerName, reviewerRating FROM ApprovedReviewers WHERE ownerUserName = ?";
        try(PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, sanitize(ownerUserName));
            try(ResultSet rs = pstmt.executeQuery()){
                while(rs.next()){
                    String reviewerName = sanitize(rs.getString("reviewerName"));
                    double rating = rs.getDouble("reviewerRating");
                    map.put(reviewerName, rating);
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return map;
    }
    
    
    public boolean updateApprovedReviewers(String userName, String approvedReviewers) {
        String query = "UPDATE cse360users SET approvedReviewers = ? WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, sanitize(approvedReviewers));
            pstmt.setString(2, sanitize(userName));
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean updateUserRating(String userName, Double rating) {
        String query = "UPDATE cse360users SET reviewerRating = ? WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            if (rating != null) {
                pstmt.setDouble(1, rating);
            } else {
                pstmt.setNull(1, Types.DOUBLE);
            }
            pstmt.setString(2, sanitize(userName));
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean changePassword(String username, String newPassword) {
        String query = "SELECT * FROM cse360users WHERE userName = ?";
        String updatePasswordQuery = "UPDATE cse360users SET password = ? WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, sanitize(username));
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    try (PreparedStatement updateStmt = connection.prepareStatement(updatePasswordQuery)) {
                        updateStmt.setString(1, sanitize(newPassword));
                        updateStmt.setString(2, sanitize(username));
                        int rowsUpdated = updateStmt.executeUpdate();
                        if (rowsUpdated > 0) {
                            return true;
                        }
                    }
                } else {
                    System.out.println("User not found.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean doesUserExist(String userName) {
        String query = "SELECT COUNT(*) FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, sanitize(userName));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteUser(String userName) {
        String query = "DELETE FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, sanitize(userName));
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                return true;
            } else {
                System.out.println("User " + sanitize(userName) + " not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String generateInvitationCode() {
        String code = UUID.randomUUID().toString().substring(0, 4);
        String query = "INSERT INTO InvitationCodes (code) VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, sanitize(code));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return code;
    }

    public boolean validateInvitationCode(String code) {
        String query = "SELECT * FROM InvitationCodes WHERE code = ? AND isUsed = FALSE";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, sanitize(code));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                markInvitationCodeAsUsed(sanitize(code));
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void markInvitationCodeAsUsed(String code) {
        String query = "UPDATE InvitationCodes SET isUsed = TRUE WHERE code = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, sanitize(code));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean hasRole(String username, String role) {
        if (getUserRoles(username).contains(sanitize(role))) return true;
        return false;
    }

    public int getRoleCount(String username) {
        ArrayList<String> roleList = new ArrayList<String>();
        roleList.add("Staff");
        roleList.add("Student");
        roleList.add("Instructor");
        roleList.add("Admin");
        roleList.add("Reviewer");
        int count = 0;
        for (String r : roleList) {
            if (getUserRoles(username).contains(sanitize(r))) count++;
        }
        return count;
    }

    public int getAdminCount() {
        String query = "SELECT COUNT(*) AS count FROM cse360users WHERE roles LIKE ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, "%Admin%");
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public String getUserRoles(String userName) {
        String query = "SELECT roles FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, sanitize(userName));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return sanitize(rs.getString("roles"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void addUserRole(String username, String role) {
        String query = "SELECT roles FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, sanitize(username));
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String rolesString = sanitize(rs.getString("roles"));
                    String newRoles;
                    if (rolesString == null || rolesString.trim().isEmpty()) {
                        newRoles = sanitize(role);
                    } else {
                        String[] rolesArray = rolesString.split(",\\s*");
                        List<String> rolesList = new ArrayList<>();
                        boolean alreadyPresent = false;
                        for (String r : rolesArray) {
                            if (r.equalsIgnoreCase(sanitize(role))) {
                                alreadyPresent = true;
                            }
                            rolesList.add(r);
                        }
                        if (alreadyPresent) {
                            return;
                        }
                        rolesList.add(sanitize(role));
                        newRoles = String.join(", ", rolesList);
                    }
                    String updateQuery = "UPDATE cse360users SET roles = ? WHERE userName = ?";
                    try (PreparedStatement updatePstmt = connection.prepareStatement(updateQuery)) {
                        updatePstmt.setString(1, sanitize(newRoles));
                        updatePstmt.setString(2, sanitize(username));
                        updatePstmt.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeUserRole(String username, String role) {
        String query = "SELECT roles FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, sanitize(username));
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String rolesString = sanitize(rs.getString("roles"));
                    String newRoles;
                    if (rolesString == null || rolesString.trim().isEmpty()) {
                        return;
                    } else {
                        String[] rolesArray = rolesString.split(",\\s*");
                        List<String> rolesList = new ArrayList<>();
                        for (String r : rolesArray) {
                            if (r.equalsIgnoreCase(sanitize(role))) {
                                continue;
                            }
                            rolesList.add(r);
                        }
                        newRoles = String.join(", ", rolesList);
                    }
                    String updateQuery = "UPDATE cse360users SET roles = ? WHERE userName = ?";
                    try (PreparedStatement updatePstmt = connection.prepareStatement(updateQuery)) {
                        updatePstmt.setString(1, sanitize(newRoles));
                        updatePstmt.setString(2, sanitize(username));
                        updatePstmt.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addUserOTP(String username, String tempPassword) {
        String insertOrUpdateUserOTP = "MERGE INTO UserOTP (userName, tempPassword, isUsed) KEY (userName) VALUES (?, ?, FALSE)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertOrUpdateUserOTP)) {
            pstmt.setString(1, sanitize(username));
            pstmt.setString(2, sanitize(tempPassword));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isUserInOPT(String userName) {
        String query = "SELECT COUNT(*) FROM UserOTP WHERE userName = ? AND isUsed = FALSE";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, sanitize(userName));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean validateUserOTP(String userName, String tempPassword) {
        String query = "SELECT * FROM UserOTP WHERE userName = ? AND tempPassword = ? AND isUsed = FALSE";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, sanitize(userName));
            pstmt.setString(2, sanitize(tempPassword));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                markUserOTPAsUsed(sanitize(userName));
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void markUserOTPAsUsed(String userName) {
        String query = "UPDATE UserOTP SET isUsed = TRUE WHERE userName = ? AND isUsed = FALSE";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, sanitize(userName));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

// methods for questions and answers
    
    public boolean insertQuestion(Question q) {
        String query = "INSERT INTO Questions (id, author, questionTitle, questionText, referencedQuestionId, timestamp, resolved) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setObject(1, q.getId());
            pstmt.setString(2, sanitize(q.getAuthor()));
            pstmt.setString(3, sanitize(q.getTitle()));
            pstmt.setString(4, sanitize(q.getQuestionText()));
            if(q.getReferencedQuestion() != null) {
                pstmt.setObject(5, q.getReferencedQuestion().getId());
            } else {
                pstmt.setObject(5, null);
            }
            pstmt.setTimestamp(6, Timestamp.valueOf(q.getTimestamp()));
            pstmt.setBoolean(7, q.isResolved());
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean insertAnswer(Answer a, UUID questionId) {
        String query = "INSERT INTO Answers (id, questionId, answerText, author, isApprovedSolution, timestamp, parentAnswerId) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setObject(1, a.getId());
            pstmt.setObject(2, questionId);
            pstmt.setString(3, sanitize(a.getAnswerText()));
            pstmt.setString(4, sanitize(a.getAuthor()));
            pstmt.setBoolean(5, a.isApprovedSolution());
            pstmt.setTimestamp(6, Timestamp.valueOf(a.getTimestamp()));
            pstmt.setObject(7, a.getParentAnswerId());
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean updateQuestion(Question q) {
        String query = "UPDATE Questions SET author = ?, questionTitle = ?, questionText = ?, referencedQuestionId = ?, timestamp = ?, resolved = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, sanitize(q.getAuthor()));
            pstmt.setString(2, sanitize(q.getTitle()));
            pstmt.setString(3, sanitize(q.getQuestionText()));
            if(q.getReferencedQuestion() != null) {
                pstmt.setObject(4, q.getReferencedQuestion().getId());
            } else {
                pstmt.setObject(4, null);
            }
            pstmt.setTimestamp(5, Timestamp.valueOf(q.getTimestamp()));
            pstmt.setBoolean(6, q.isResolved());
            pstmt.setObject(7, q.getId());
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean updateAnswer(Answer a) {
        String query = "UPDATE Answers SET answerText = ?, author = ?, isApprovedSolution = ?, timestamp = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, sanitize(a.getAnswerText()));
            pstmt.setString(2, sanitize(a.getAuthor()));
            pstmt.setBoolean(3, a.isApprovedSolution());
            pstmt.setTimestamp(4, Timestamp.valueOf(a.getTimestamp()));
            pstmt.setObject(5, a.getId());
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean deleteQuestion(UUID questionId) {
        String query = "DELETE FROM Questions WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setObject(1, questionId);
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean deleteAnswer(UUID answerId) {
        String query = "DELETE FROM Answers WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setObject(1, answerId);
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public List<Question> getAllQuestionsFromDB() {
        List<Question> questions = new ArrayList<>();
        String query = "SELECT * FROM Questions";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                try { // try catch to make it so if one breaks it doesn't stop
                    UUID id = (UUID) rs.getObject("id");
                    String author = sanitize(rs.getString("author"));
                    String questionTitle = sanitize(rs.getString("questionTitle"));
                    String questionText = sanitize(rs.getString("questionText"));
                    Timestamp ts = rs.getTimestamp("timestamp");
                    LocalDateTime ldt = ts.toLocalDateTime();
                    boolean resolved = rs.getBoolean("resolved");
                    Question q = new Question(author, questionTitle, questionText, ldt, id, resolved);
                    questions.add(q);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return questions;
    }
    
    public List<Answer> getAnswersForQuestion(UUID questionId) {
        List<Answer> answers = new ArrayList<>();
        String query = "SELECT * FROM Answers WHERE questionId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setObject(1, questionId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                try {
                    UUID id = (UUID) rs.getObject("id");
                    String answerText = sanitize(rs.getString("answerText"));
                    String author = sanitize(rs.getString("author"));
                    boolean isApprovedSolution = rs.getBoolean("isApprovedSolution");
                    Timestamp ts = rs.getTimestamp("timestamp");
                    LocalDateTime ldt = ts.toLocalDateTime();
                    Answer a = new Answer(answerText, author, ldt, id, isApprovedSolution);
                    UUID parentId = (UUID) rs.getObject("parentAnswerId");
                    a.setParentAnswerId(parentId);
                    answers.add(a);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return answers;
    }

    public void closeConnection() {
        try {
            if(statement != null) statement.close();
        } catch(SQLException se2) {
            se2.printStackTrace();
        }
        try {
            if(connection != null) connection.close();
        } catch(SQLException se) {
            se.printStackTrace();
        }
    }
}
