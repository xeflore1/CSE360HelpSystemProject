package project;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;


/*******
 * <p> User class </p>
 * 
 * <p> Description:  It represents the general users in the system.<p> 
 * <p> Stores information about the users such as username, password, roles, personal information, etc.<p>
 * <p> Manages roles and provides methods to implement role-related behavior, like addRole, removeRole, hasRole.<p> 
 * <p> Collaborators: Role, Admin.</p>
 * 
 * @author Hassan Khan, Colby Taylor, Xavier Flores, Shashwat Balaji, Avinash Poguluri, Abil Damirbek uulu
*/

public class User {
    private String username;
    private char[] password;  // Non-string data type for password
    private long id;
    private boolean isOneTimePassword;  // Flag for one-time password
    private String oneTimePassword;
    private LocalDateTime otpExpiry;  // Date and time when the one-time password expires
    private String email;
    private String firstName;
    private String middleName;
    private String lastName;
    private String preferredName;
    private Set<Role> roles;  // Set of roles for the user
    private Set<TopicProficiency> topicProficiencies;  // Set of topic proficiencies
    private Map<String, Set<String>> specialAccessGroups;
    
    public User() {
        // Initialize the specialAccessGroups map to prevent NullPointerException
        this.specialAccessGroups = new HashMap<>();
        this.id = id;
    }

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

        // Check if user has access to a specific article in a group
        public boolean hasAccessToArticle(String group, String articleId) {
            return Optional.ofNullable(specialAccessGroups.get(group))
                           .map(articles -> articles.contains(articleId))
                           .orElse(false);
        }
    }

    // initial constructor
    public User(String username, char[] password) {
        this.username = username;
        this.password = password;
        this.roles = new HashSet<>();
        this.isOneTimePassword = false;  // Default to false, can be changed later
        this.otpExpiry = null;  // Default, can be set later when OTP is active
        this.roles = new HashSet<>();
        this.topicProficiencies = new HashSet<>();

        // Initialize default proficiencies (Intermediate level for all topics)
        for (Topic topic : Topic.values()) {
            this.topicProficiencies.add(new TopicProficiency(topic, Proficiency.INTERMEDIATE));
        }
    }

    /*******
    * This method is used to add a role to the roles.
    */
    public void addRole(Role role) {
        roles.add(role);
    }
    
    /*******
    * This method is used to set the roles.
    */
    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    /*******
    * This method is used to remove a role from the roles.
    */
    public void removeRole(Role role) {
        if (roles.contains(role)) {
            roles.remove(role);
        } else {
            System.out.println("Role " + role + " not found for user " + username);
        }
    }

    /*******
    * This method is used to check if the roles has a role.
    * @return bool 		true or false
    */
    public boolean hasRole(Role role) {
        return roles.contains(role);
    }

    /*******
    * This method is used to check if roles is multiple.
    * @return bool 		true or false
    */
    public boolean hasMultipleRoles() {
        return roles.size() > 1;
    }

    /*******
    * This method is used to display user info.
    */
    public void displayUserInfo() {
        System.out.println("Username: " + username);
        System.out.println("Name: " + firstName + " " + middleName + " " + lastName);
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

    /*******
    * This method is used to clear the password for security.
    */
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
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
    	this.id = id;
    }

    public boolean isOneTimePassword() {
        return isOneTimePassword;
    }

    public void setOneTimePassword(boolean oneTimePassword) {
        isOneTimePassword = oneTimePassword;
    }
    
    public String getOneTimePassword() {
    	return oneTimePassword;
    }
    
    public void setOneTimePassword(String oneTimePassword) {
    	this.oneTimePassword = oneTimePassword;
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

    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getMiddleName() {
        return middleName;
    }
    
    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getPreferredName() {
        return preferredName;
    }
    
    public void setPreferredName(String preferredName) {
        this.preferredName = preferredName;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    /*******
    * This method returns the special access group a user belongs to.
    * @param specialList 		list of special access groups
    * @param currentUser		the user
    * @return group			string of the group
    */
    public String getSpecialAccessGroups(List<SpecialAccessGroup> specialList, User currentUser) {
	// if the special list is empty
        if (specialList.isEmpty()) {
            return "No special access groups assigned.";
        }
        
        StringBuilder result = new StringBuilder("Special Access Groups:\n");
        for (SpecialAccessGroup i : specialList) {
		// this if statement is used to check if user exists within our lists
        	if (i.doesAdminExist(currentUser) || i.doesInstrExistInAccessList(currentUser) || 
        			i.doesInstrExistInAdminRightsList(currentUser) || i.doesStudentExistInStudentList(currentUser)) {
        		result.append("[" + i.getGroupName() + "]\n");
        	}
        }
        
        return result.toString();
    }

	/*******
        * This method removes user's access to a specific article in a special access group
        * @param group			string of group
        * @return bool			true or false
        */
	public boolean removeSpecialAccessGroup(String group) {
	    // Check if the group exists and then remove it
	    if (specialAccessGroups.containsKey(group)) {
	        specialAccessGroups.remove(group);
	        return true;  // Indicate successful removal
	    }
	    return false;  // Indicate that the group was not found
	}

	/*******
        * This method adds a special access group
        * @param group			string of group
        */
	public void addSpecialAccessGroup(String group) {
	    specialAccessGroups.computeIfAbsent(group, k -> new HashSet<>());
	}
	
    /*******
    * This method will list and print the special access groups.
    */
    public void listSpecialAccessGroups() {
        if (specialAccessGroups == null) {
            System.out.println("specialAccessGroups is null");
        } else {
	    // this for each will print out each separate group
            specialAccessGroups.forEach((group, articles) -> {
                System.out.println("Group: " + group);
                articles.forEach(article -> System.out.println(" - Article ID: " + article));
            });
        }
    }
}
