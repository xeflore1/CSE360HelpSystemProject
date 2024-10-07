package project;
import java.util.HashSet;
import java.util.Set;

public class User {
    private String username;
    private char[] password;
    private String name;
    private String email;
    private Set<Role> roles; // Set of roles for the user

    // Constructor
    public User(String username, char[] password, String name, String email) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.email = email;
        this.roles = new HashSet<>();
    }

    // Method to add a role
    public void addRole(Role role) {
        roles.add(role);
    }

    // Method to remove a role
    public void removeRole(Role role) {
        if (roles.contains(role)) {
            roles.remove(role);
        } else {
            System.out.println("Role " + role + " not found for user " + username);
        }
    }

    // Method to check if a user has a specific role
    public boolean hasRole(Role role) {
        return roles.contains(role);
    }

    // Method to display user info
    public void displayUserInfo() {
        System.out.println("Username: " + username);
        System.out.println("Name: " + name);
        System.out.println("Email: " + email);
        System.out.println("Roles: " + roles);
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public char[] getPassword() {
        return password;
    }

    public void setPassword(char[] password) {
        this.password = password;
    }
    
    // Method to clear the password (for security)
    public void clearPassword() {
        for (int i = 0; i < password.length; i++) {
            password[i] = '\0'; // Overwrite password with null characters
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
