package application;

/**
 * The User class represents a user entity in the system.
 * It contains the user's details such as userName, password, and role.
 */
public class User {
    private String userName;
    private String password;
    private String email;
    private String roles; // e.g., "Admin, Student, Instructor"
    private String name;

    public User(String userName, String password, String roles) {
        this.userName = userName;
        this.password = password;
        this.email = "";
        this.roles = roles;
    }
    
    // Constructor to initialize a new User object with userName, password, and role.
    public User(String userName, String password, String email, String roles) {
        this.userName = userName;
        this.password = password;
        this.email = email;
        this.roles = roles;
    }
    
 // Constructor to initialize a new User object with userName, password, and role.
    public User(String userName, String password, String name, String email, String roles) {
        this.userName = userName;
        this.password = password;
        this.email = email;
        this.name = name;
        this.roles = roles;
    }
    
    public String getUserName() { return userName; }
    public String getName() { return name; }
    public String getPassword() { return password; }
    public String getEmail() { return email; }
    public String getRoles() { return roles; }
    
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setName(String name) { this.name = name; }
    public void setRoles(String roles) { this.roles=roles; }

}
