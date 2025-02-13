package databasePart1;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import application.Answer;
import application.Question;
import application.User;

/**
 * The DatabaseHelper class is responsible for managing the connection to the database,
 * performing operations such as user registration, login validation, and handling invitation codes.
 */
public class DatabaseHelper {

	// JDBC driver name and database URL 
	static final String JDBC_DRIVER = "org.h2.Driver";   
	static final String DB_URL = "jdbc:h2:~/FoundationDatabase";  

	//  Database credentials 
	static final String USER = "sa"; 
	static final String PASS = ""; 

	private Connection connection = null;
	private Statement statement = null; 

	//	PreparedStatement pstmt

	public void connectToDatabase() throws SQLException {
		try {
			Class.forName(JDBC_DRIVER); // Load the JDBC driver
			System.out.println("Connecting to database...");
			connection = DriverManager.getConnection(DB_URL, USER, PASS);
			statement = connection.createStatement(); 
			// You can use this command to clear the database and restart from fresh.
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
		
		// Create the invitation codes table
	    String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes ("
	            + "code VARCHAR(10) PRIMARY KEY, "
	            + "isUsed BOOLEAN DEFAULT FALSE)";
	    statement.execute(invitationCodesTable);
	    
	    // create a new table to store userOTP code access
	    String userOTPAccess = "CREATE TABLE IF NOT EXISTS UserOTP ("
				+ "userName VARCHAR(255) UNIQUE, "
				+ "tempPassword VARCHAR(255), "
	            + "isUsed BOOLEAN DEFAULT FALSE)";
	    statement.execute(userOTPAccess);
	    
	    
	    String questionsTable = "CREATE TABLE IF NOT EXISTS Questions ("
	    		+ "id UUID PRIMARY KEY, "
	    		+ "author VARCHAR(255) NOT NULL, "
	    		+ "questionText CLOB NOT NULL, "
	    		+ "referencedQuestionId UUID, "
	    		+ "timestamp TIMESTAMP NOT NULL)";
		statement.execute(questionsTable);
		
		String answersTable = "CREATE TABLE IF NOT EXISTS Answers ("
				+ "id UUID PRIMARY KEY, "
				+ "questionId UUID NOT NULL, "
				+ "answerText CLOB NOT NULL, "
				+ "author VARCHAR(255) NOT NULL, "
				+ "isApprovedSolution BOOLEAN DEFAULT FALSE, "
				+ "timestamp TIMESTAMP NOT NULL, "
				+ "FOREIGN KEY (questionId) REFERENCES Questions(id))";
		statement.execute(answersTable);
	    
	}

	// Check if the database is empty
	public boolean isDatabaseEmpty() throws SQLException {
		String query = "SELECT COUNT(*) AS count FROM cse360users";
		ResultSet resultSet = statement.executeQuery(query);
		if (resultSet.next()) {
			return resultSet.getInt("count") == 0;
		}
		return true;
	}

	// Registers a new user in the database.
	public void register(User user) throws SQLException {
		String insertUser = "INSERT INTO cse360users (userName, password, roles) VALUES (?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			pstmt.setString(3, user.getRoles());
			pstmt.executeUpdate();
		}
	}

	// Validates a user's login credentials.
	public boolean login(User user) throws SQLException {
		String query = "SELECT * FROM cse360users WHERE userName = ? AND password = ? AND roles = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			pstmt.setString(3, user.getRoles());
			try (ResultSet rs = pstmt.executeQuery()) {
				if(rs.next()) {
					this.markUserOTPAsUsed(user.getUserName()); // if user logged in normally remove OTP
					System.out.println("Removed user OTP.");
					return true;
				}else {
					return false;
				}
			}
		}
	}
	
	
	// ADDED BY BRADLEY //
	
	//gets a list of all users
	public List<User> getUsers() throws SQLException {
	    List<User> userList = new ArrayList<>();
	    String query = "SELECT userName, name, email, roles FROM cse360users";

	    try (Statement stmt = connection.createStatement();
	         ResultSet rs = stmt.executeQuery(query)) {
	        
	        // loop through the result set
	        while (rs.next()) {
	            String userName = rs.getString("userName");
	            String name = rs.getString("name");
	            String email = rs.getString("email");
	            String roles = rs.getString("roles");
	            
	            // create a User object and add it
	            User user = new User(userName, "NULL", name, email, roles); // hide password
	            userList.add(user);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return userList;
	}
	
	//get a user object from username
	public User getUser(String username) throws SQLException {
	    String query = "SELECT * FROM cse360users WHERE userName = ?";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	    	
			pstmt.setString(1, username);
			
			try (ResultSet rs = pstmt.executeQuery()) {
				
				while (rs.next()) { // should only be one user
					
		            String userName = rs.getString("userName");
		            String name = rs.getString("name");
		            String email = rs.getString("email");
		            String roles = rs.getString("roles");
		            
		            // create a User object and add it
		            User user = new User(userName, "NULL", name, email, roles); // hide password
		            return user;
		        }
			}
		} catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return null; // no user found
	}
	
	// changes user password
	public boolean changePassword(String username, String newPassword) {
	    String query = "SELECT * FROM cse360users WHERE userName = ?";
	    String updatePasswordQuery = "UPDATE cse360users SET password = ? WHERE userName = ?";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        // Check if the user with the given userName and password exists
	    	pstmt.setString(1, username);
	        
	        try (ResultSet rs = pstmt.executeQuery()) {
	        	
	            if (rs.next()) {
	                // verified, can update the password
	                try (PreparedStatement updateStmt = connection.prepareStatement(updatePasswordQuery)) {
	                    updateStmt.setString(1, newPassword);
	                    updateStmt.setString(2, username);
	                    int rowsUpdated = updateStmt.executeUpdate();

	                   if (rowsUpdated > 0) {
	                        System.out.println("Password for " + username + " successfully updated.");
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
	    return false; // Return false if the user was not found or the update failed
	}
	
	
	// Checks if a user already exists in the database based on their userName.
	public boolean doesUserExist(String userName) {
	    String query = "SELECT COUNT(*) FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            // If the count is greater than 0, the user exists
	            return rs.getInt(1) > 0;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false; // If an error occurs user doesn't exist
	}
	
	// deletes a user
	public boolean deleteUser(String userName) {
	    String query = "DELETE FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        
	        int rowsAffected = pstmt.executeUpdate();
	        
	        if (rowsAffected > 0) {
	            System.out.println("User " + userName + " deleted successfully.");
	            return true;
	        } else {
	            System.out.println("User " + userName + " not found.");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false; // no rows were deleted or an error occurred
	}
	
	// END ADDITION
	
	
	// Generates a new invitation code and inserts it into the database.
	public String generateInvitationCode() {
	    String code = UUID.randomUUID().toString().substring(0, 4); // Generate a random 4-character code
	    String query = "INSERT INTO InvitationCodes (code) VALUES (?)";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return code;
	}
	
	// Validates an invitation code to check if it is unused.
	public boolean validateInvitationCode(String code) {
	    String query = "SELECT * FROM InvitationCodes WHERE code = ? AND isUsed = FALSE";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            // Mark the code as used
	            markInvitationCodeAsUsed(code);
	            return true;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false;
	}
	
	// Marks the invitation code as used in the database.
	private void markInvitationCodeAsUsed(String code) {
	    String query = "UPDATE InvitationCodes SET isUsed = TRUE WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	
	// ADDED BY BRADLEY //
	
	public boolean hasRole(String username, String role) {
		if (getUserRoles(username).contains(role)) return true;
		return false;
	}
	
	// gets the number of roles
	public int getRoleCount(String username) {
		ArrayList<String> roleList = new ArrayList<String>();
		roleList.add("Staff");
		roleList.add("Student");
		roleList.add("Instructor");
		roleList.add("Admin");
		roleList.add("Reviewer");

		int count = 0;
	    for (String r : roleList) {
	    	if (getUserRoles(username).contains(r)) count++;
	    }
	    return count;
	}
	
	
	// gets the count of admin's
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

	
	// Retrieves the role of a user from the database using their UserName.
	public String getUserRoles(String userName) {
	    String query = "SELECT roles FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("roles"); // Return the role if user exists
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null; // If no user exists or an error occurs
	}
	
	public void addUserRole(String username, String role) {
	    // get the current roles for the user.
	    String query = "SELECT roles FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, username);
	        try (ResultSet rs = pstmt.executeQuery()) {
	            if (rs.next()) {
	                String rolesString = rs.getString("roles");
	                String newRoles;
	                // if there are no roles yet, set the roles string to the new role
	                if (rolesString == null || rolesString.trim().isEmpty()) {
	                    newRoles = role;
	                } else {
	                    // split the current roles string by comma and optional whitespace.
	                    String[] rolesArray = rolesString.split(",\\s*");
	                    List<String> rolesList = new ArrayList<>();
	                    boolean alreadyPresent = false;
	                    // add existing roles to the list while checking if the role is already present.
	                    for (String r : rolesArray) {
	                        if (r.equalsIgnoreCase(role)) {
	                            alreadyPresent = true;
	                        }
	                        rolesList.add(r);
	                    }
	                    // if the role already exists, we do nothing.
	                    if (alreadyPresent) {
	                        return;
	                    }
	                    // otherwise add the new role.
	                    rolesList.add(role);
	                    // join the list using a comma and a space.
	                    newRoles = String.join(", ", rolesList);
	                }
	                // update the user record with the new roles string
	                String updateQuery = "UPDATE cse360users SET roles = ? WHERE userName = ?";
	                try (PreparedStatement updatePstmt = connection.prepareStatement(updateQuery)) {
	                    updatePstmt.setString(1, newRoles);
	                    updatePstmt.setString(2, username);
	                    updatePstmt.executeUpdate();
	                }
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public void removeUserRole(String username, String role) {
	    // get the current roles for the user.
	    String query = "SELECT roles FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, username);
	        try (ResultSet rs = pstmt.executeQuery()) {
	            if (rs.next()) {
	                String rolesString = rs.getString("roles");
	                String newRoles;
	                // if there are no roles yet, set the roles string to the new role
	                if (rolesString == null || rolesString.trim().isEmpty()) {
	                    return;
	                } else {
	                    // split the current roles string by comma and optional whitespace.
	                    String[] rolesArray = rolesString.split(",\\s*");
	                    List<String> rolesList = new ArrayList<>();
	                    // add existing roles to the list while checking if the role is already present.
	                    for (String r : rolesArray) {
	                        if (r.equalsIgnoreCase(role)) {
	                            continue;
	                        }
	                        rolesList.add(r);
	                    }

	                    // join the list using a comma and a space.
	                    newRoles = String.join(", ", rolesList);
	                }
	                // update the user record with the new roles string
	                String updateQuery = "UPDATE cse360users SET roles = ? WHERE userName = ?";
	                try (PreparedStatement updatePstmt = connection.prepareStatement(updateQuery)) {
	                    updatePstmt.setString(1, newRoles);
	                    updatePstmt.setString(2, username);
	                    updatePstmt.executeUpdate();
	                }
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	

	
	public void addUserOTP(String username, String tempPassword) {

	    // MERGE statement to insert/update the OTP
	    String insertOrUpdateUserOTP = "MERGE INTO UserOTP (userName, tempPassword, isUsed) " +
	                                   "KEY (userName) " +
	                                   "VALUES (?, ?, FALSE)";

	    try (PreparedStatement pstmt = connection.prepareStatement(insertOrUpdateUserOTP)) {
	        pstmt.setString(1, username);
	        pstmt.setString(2, tempPassword);
	        pstmt.executeUpdate();
	        System.out.println("Added or updated OTP for " + username);
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	// checks if a user exists in the OTP table based on userName
	public boolean isUserInOPT(String userName) {
	    String query = "SELECT COUNT(*) FROM UserOTP WHERE userName = ? AND isUsed = FALSE";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            // if the count is greater than 0 the user has an active OTP
	            return rs.getInt(1) > 0;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false;
	}

	// validates the OTP and marks if validation succeeds
	public boolean validateUserOTP(String userName, String tempPassword) {
	    String query = "SELECT * FROM UserOTP WHERE userName = ? AND tempPassword = ? AND isUsed = FALSE";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        pstmt.setString(2, tempPassword);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            // Mark the OTP as used
	            markUserOTPAsUsed(userName);
	            return true;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false;
	}

	// marks the OTP as used
	public void markUserOTPAsUsed(String userName) {
	    String query = "UPDATE UserOTP SET isUsed = TRUE WHERE userName = ? AND isUsed = FALSE";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        pstmt.executeUpdate();
	        System.out.println("Marked OTP as used.");
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public boolean insertQuestion(Question q) {
		String query = "INSERT INTO Questions (id, author, questionText, referencedQuestionId, timestamp) VALUES (?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setObject(1, q.getId());
			pstmt.setString(2, q.getAuthor());
			pstmt.setString(3, q.getQuestionText());
			if(q.getReferencedQuestion() != null) {
				pstmt.setObject(4, q.getReferencedQuestion().getId());
			} else {
				pstmt.setObject(4, null);
			}
			pstmt.setTimestamp(5, Timestamp.valueOf(q.getTimestamp()));
			int rows = pstmt.executeUpdate();
			return rows > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	public boolean insertAnswer(Answer a, UUID questionId) {
		String query = "INSERT INTO Answers (id, questionId, answerText, author, isApprovedSolution, timestamp) VALUES (?, ?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setObject(1, a.getId());
			pstmt.setObject(2, questionId);
			pstmt.setString(3, a.getAnswerText());
			pstmt.setString(4, a.getAuthor());
			pstmt.setBoolean(5, a.isApprovedSolution());
			pstmt.setTimestamp(6, Timestamp.valueOf(a.getTimestamp()));
			int rows = pstmt.executeUpdate();
			return rows > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	public boolean updateQuestion(Question q) {
		String query = "UPDATE Questions SET author = ?, questionText = ?, referencedQuestionId = ?, timestamp = ? WHERE id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, q.getAuthor());
			pstmt.setString(2, q.getQuestionText());
			if(q.getReferencedQuestion() != null) {
				pstmt.setObject(3, q.getReferencedQuestion().getId());
			} else {
				pstmt.setObject(3, null);
			}
			pstmt.setTimestamp(4, Timestamp.valueOf(q.getTimestamp()));
			pstmt.setObject(5, q.getId());
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
			pstmt.setString(1, a.getAnswerText());
			pstmt.setString(2, a.getAuthor());
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
			while(rs.next()) {
				UUID id = (UUID) rs.getObject("id");
				String author = rs.getString("author");
				String questionText = rs.getString("questionText");
				Timestamp timestamp = rs.getTimestamp("timestamp");
				UUID referencedQuestionId = (UUID) rs.getObject("referencedQuestionId");
				Question q = new Question(author, questionText);
				questions.add(q);
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
			while(rs.next()) {
				UUID id = (UUID) rs.getObject("id");
				String answerText = rs.getString("answerText");
				String author = rs.getString("author");
				boolean isApprovedSolution = rs.getBoolean("isApprovedSolution");
				Timestamp timestamp = rs.getTimestamp("timestamp");
				Answer a = new Answer(answerText, author);
				if(isApprovedSolution) {
					a.markAsSolution();
				}
				answers.add(a);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return answers;
	}
	
	// END ADDITION
	

	// closes the database connection and statement.
	public void closeConnection() {
		try{ 
			if(statement!=null) statement.close(); 
		} catch(SQLException se2) { 
			se2.printStackTrace();
		} 
		try { 
			if(connection!=null) connection.close(); 
		} catch(SQLException se){ 
			se.printStackTrace(); 
		} 
	}
}
