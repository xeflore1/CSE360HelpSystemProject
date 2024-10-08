package project;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class User {
    private String username;
    private char[] password;  // Non-string data type for password
    private boolean isOneTimePassword;  // Flag for one-time password
    private LocalDateTime otpExpiry;  // Date and time when the one-time password expires
    private String email;
    private Name fullName;  // Encapsulates first, middle, last, and preferred name
    private Set<Role> roles;  // Set of roles for the user
    private Set<TopicProficiency> topicProficiencies;  // Set of topic proficiencies

    // Enum for system-recognized topics and proficiency levels
    public enum Proficiency {
        BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
    }

    // Enum for topics
    public enum Topic {
        JAVA, JAVAFX, SOFTENG, STUDY, PROJECT  // Add more topics as needed
    }

    // Nested class to represent topic proficiency
    public class TopicProficiency {
        private Topic topic;
        private Proficiency proficiency;

        public TopicProficiency(Topic topic, Proficiency proficiency) {
            this.topic = topic;
            this.proficiency = proficiency;
        }

        public Topic getTopic() {
            return topic;
        }

        public Proficiency getProficiency() {
            return proficiency;
        }

        public void setProficiency(Proficiency proficiency) {
            this.proficiency = proficiency;
        }
    }

    // initial constructor
    public User(String username, char[] password) {
        this.username = username;
        this.password = password;
        this.roles = new HashSet<>();
    }
    
    // full Constructor
    public User(String username, char[] password, String email, Name fullName) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        this.isOneTimePassword = false;  // Default to false, can be changed later
        this.otpExpiry = null;  // Default, can be set later when OTP is active
        this.roles = new HashSet<>();
        this.topicProficiencies = new HashSet<>();

        // Initialize default proficiencies (Intermediate level for all topics)
        for (Topic topic : Topic.values()) {
            this.topicProficiencies.add(new TopicProficiency(topic, Proficiency.INTERMEDIATE));
        }
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
        //System.out.println("Name: " + fullName.getFullName());
        System.out.println("Email: " + email);
        System.out.println("Roles: " + roles);
        System.out.println("One-Time Password: " + (isOneTimePassword ? "Yes" : "No"));
        if (otpExpiry != null) {
            System.out.println("OTP Expiry: " + otpExpiry);
        }

        // Display topic proficiencies
        /*System.out.println("Proficiencies:");
        for (TopicProficiency proficiency : topicProficiencies) {
            System.out.println(proficiency.getTopic() + ": " + proficiency.getProficiency());
        } */
    }

    // Method to clear the password (for security)
    public void clearPassword() {
        for (int i = 0; i < password.length; i++) {
            password[i] = '\0';  // Overwrite password with null characters
        }
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

    public boolean isOneTimePassword() {
        return isOneTimePassword;
    }

    public void setOneTimePassword(boolean oneTimePassword) {
        isOneTimePassword = oneTimePassword;
    }

    public LocalDateTime getOtpExpiry() {
        return otpExpiry;
    }

    public void setOtpExpiry(LocalDateTime otpExpiry) {
        this.otpExpiry = otpExpiry;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Name getFullName() {
        return fullName;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    // Nested class for full name
    public static class Name {
        private String firstName;
        private String middleName;
        private String lastName;
        private String preferredName;

        public Name(String firstName, String middleName, String lastName, String preferredName) {
            this.firstName = firstName;
            this.middleName = middleName;
            this.lastName = lastName;
            this.preferredName = preferredName;
        }

        public String getFullName() {
            return preferredName != null ? preferredName : firstName + " " + middleName + " " + lastName;
        }

        // Getters for each part of the name
        public String getFirstName() {
            return firstName;
        }

        public String getMiddleName() {
            return middleName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getPreferredName() {
            return preferredName;
        }
    }
}
