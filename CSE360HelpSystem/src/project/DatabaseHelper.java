package project;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bouncycastle.util.Arrays;

import Encryption.EncryptionHelper;
import Encryption.EncryptionUtils;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/*******
 * <p> DatabaseHelper class </p>
 * 
 * <p> Description: A class created for the database functionalality for the CSE360 Help System </p>
 * 
 * @author Hassan Khan, Colby Taylor, Xavier Flores, Shashwat Balaji, Avinash Poguluri, Abil Damirbek uulu
*/
class DatabaseHelper {

	// JDBC driver name and database URL 
	static final String JDBC_DRIVER = "org.h2.Driver";   
	static final String DB_URL = "jdbc:h2:~/firstDatabase";  

	//  Database credentials 
	static final String USER = "sa"; 
	static final String PASS = ""; 

	private Connection connection = null;
	private Statement statement = null; 
	
    // list of users
    private static List<User> userList = new ArrayList<>();
	
	private EncryptionHelper encryptionHelper;
	//private ArticleIdHelper articleIdHelper;
	
	public DatabaseHelper() throws Exception {
		encryptionHelper = new EncryptionHelper();
	}

	/*********
     * This is the method used to connect to the database 
     * Exception handling takes care of any database errors
     */
	public void connectToDatabase() throws SQLException {
		try {
			Class.forName(JDBC_DRIVER); // Load the JDBC driver
			System.out.println("Connecting to database...");
			connection = DriverManager.getConnection(DB_URL, USER, PASS);
			statement = connection.createStatement(); 
			createTables();  // Create the necessary tables if they don't exist
		} catch (ClassNotFoundException e) {
			System.err.println("JDBC Driver not found: " + e.getMessage());
		}
	}

     /*********
     * This is the method used to create tables in the database 
     * Exception handling takes care of any database errors
     */
	private void createTables() throws SQLException {
	    // table for users
		String userTable = "CREATE TABLE IF NOT EXISTS cse360users ("
	            + "id INT AUTO_INCREMENT PRIMARY KEY, "
	            + "username VARCHAR(255), "
	            + "password VARBINARY(255), "  // Store password as binary data for better security
	            + "isOneTimePassword BOOLEAN, "  // Flag for one-time password
	            + "oneTimePassword VARCHAR(255), "  // Store the one-time password
	            + "otpExpiry TIMESTAMP, "  // Date and time for OTP expiration
	            + "email VARCHAR(255) UNIQUE, "
	            + "firstName VARCHAR(255), "
	            + "middleName VARCHAR(255), "
	            + "lastName VARCHAR(255), "
	            + "preferredName VARCHAR(255), "
	            + "roles VARCHAR(255), "  // Store roles as a serialized string or JSON
	            + "topicProficiencies TEXT"  // Store topic proficiencies as serialized data or JSON
	            + ")";
	    statement.execute(userTable);
	    // table for articles
	    String articleTable = "CREATE TABLE IF NOT EXISTS help_articles ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "unique_id BIGINT NOT NULL, "
	    		+ "level VARCHAR(255), "
                + "groupId VARCHAR(255), "
                + "title VARCHAR(255), "
                + "authors VARCHAR(255), "
                + "abstract CLOB, "
                + "keywords VARCHAR(255), "
                + "body CLOB, "
                + "references CLOB)";
        statement.execute(articleTable);
        // table for special access groups
        String specialAccessGroupTable = "CREATE TABLE IF NOT EXISTS special_access_groups ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "group_name VARCHAR(255) NOT NULL)";
        statement.execute(specialAccessGroupTable);
        System.out.println("after group table");
        // table for special articles list
        // Table for special articles
        String specialArticlesTable = "CREATE TABLE IF NOT EXISTS special_articles ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "group_name VARCHAR(255) NOT NULL, "
                + "article_id BIGINT NOT NULL)";
        statement.execute(specialArticlesTable);
        System.out.println("after article table");

        // Table for special admins list
        String specialAdminsTable = "CREATE TABLE IF NOT EXISTS special_admins ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "group_name VARCHAR(255) NOT NULL, "
                + "username VARCHAR(255) NOT NULL, "
                + "firstName VARCHAR(255) NOT NULL)";
        statement.execute(specialAdminsTable);

        // Table for instructors with viewing access
        String instructorsAccessTable = "CREATE TABLE IF NOT EXISTS special_instructors_access ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "group_name VARCHAR(255) NOT NULL, "
                + "username VARCHAR(255), "
                + "firstName VARCHAR(255))";
        statement.execute(instructorsAccessTable);

        // Table for instructors with admin rights
        String instructorsAdminTable = "CREATE TABLE IF NOT EXISTS special_instructors_admin ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "group_name VARCHAR(255) NOT NULL, "
                + "username VARCHAR(255), "
                + "firstName VARCHAR(255))";
        statement.execute(instructorsAdminTable);

        // Table for students with viewing rights
        String studentsViewingTable = "CREATE TABLE IF NOT EXISTS special_students_viewing ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "group_name VARCHAR(255) NOT NULL, "
                + "username VARCHAR(255), "
                + "firstName VARCHAR(255))";
        statement.execute(studentsViewingTable); 
	}

     /*********
     * This is the method used when the program starts, to retrieve all users from database 
     * Exception handling takes care of any database errors
     * 
     * @return user list
     */
	public List<User> loadUsersFromDatabase() throws SQLException {
	    //List<User> userList = new ArrayList<>();
	    String selectAllUsers = "SELECT username, password, roles, email, firstName, middleName, lastName, preferredName, isOneTimePassword, oneTimePassword, otpExpiry FROM cse360users";
	    
	    try (Statement stmt = connection.createStatement();
	         ResultSet rs = stmt.executeQuery(selectAllUsers)) {
	        
	        while (rs.next()) {
	            User user = new User(
	                rs.getString("username"),
	                rs.getString("password").toCharArray()  // Convert back to char[]
	            );
	            user.setEmail(rs.getString("email"));
	            user.setFirstName(rs.getString("firstName"));
	            user.setMiddleName(rs.getString("middleName"));
	            user.setLastName(rs.getString("lastName"));
	            user.setPreferredName(rs.getString("preferredName"));
	            // Corrected this line: using 'rs' instead of 'resultSet'
	            user.setRoles(deserializeRoles(rs.getString("roles")));  // Convert back to Set<Role>
	            user.setOneTimePassword(rs.getBoolean("isOneTimePassword"));
	            user.setOneTimePassword(rs.getString("oneTimePassword"));
	            // Check for null value before converting to LocalDateTime
	            Timestamp otpExpiryTimestamp = rs.getTimestamp("otpExpiry");
	            if (otpExpiryTimestamp != null) {
	                user.setOtpExpiry(otpExpiryTimestamp.toLocalDateTime());
	            } else {
	                user.setOtpExpiry(null); // or handle as needed
	            }

	            userList.add(user);  // Add user to the list
	        }
	    }

	    return userList;
	}

	/*********
     * This is the method used to save the userlist to database 
     * Exception handling takes care of any database errors
     * 
     * @param userList    		list of all users
     */
	public void saveUserListToDatabase(List<User> userList) throws SQLException {
	    String insertUser = "INSERT INTO cse360users (username, password, roles, email, firstName, middleName, lastName, preferredName, isOneTimePassword, oneTimePassword, otpExpiry) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	    String updateUser = "UPDATE cse360users SET password = ?, roles = ?, email = ?, firstName = ?, middleName = ?, lastName = ?, preferredName = ?, isOneTimePassword = ?, oneTimePassword = ?, otpExpiry = ? WHERE username = ?";

	    for (User user : userList) {
	        // Attempt to insert the user
	        try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
	            pstmt.setString(1, user.getUsername());
	            pstmt.setString(2, new String(user.getPassword())); // Convert char[] to String
	            pstmt.setString(3, serializeRoles(user.getRoles())); // Serialize roles
	            pstmt.setString(4, user.getEmail());
	            pstmt.setString(5, user.getFirstName());
	            pstmt.setString(6, user.getMiddleName());
	            pstmt.setString(7, user.getLastName());
	            pstmt.setString(8, user.getPreferredName());
	            pstmt.setBoolean(9, user.isOneTimePassword());
	            pstmt.setString(10, user.getOneTimePassword());
	            pstmt.setObject(11, user.getOtpExpiry());

	            pstmt.executeUpdate();
	        } catch (SQLException e) {
	            // If the user already exists (e.g., unique constraint violation), update instead
	            if (e.getErrorCode() == 23505) { // H2 error code for unique constraint violation
	                try (PreparedStatement pstmt = connection.prepareStatement(updateUser)) {
	                    pstmt.setString(1, new String(user.getPassword()));
	                    pstmt.setString(2, serializeRoles(user.getRoles()));
	                    pstmt.setString(3, user.getEmail());
	                    pstmt.setString(4, user.getFirstName());
	                    pstmt.setString(5, user.getMiddleName());
	                    pstmt.setString(6, user.getLastName());
	                    pstmt.setString(7, user.getPreferredName());
	                    pstmt.setBoolean(8, user.isOneTimePassword());
	                    pstmt.setString(9, user.getOneTimePassword());
	                    pstmt.setObject(10, user.getOtpExpiry());
	                    pstmt.setString(11, user.getUsername());

	                    pstmt.executeUpdate();
	                }
	            } else {
	                throw e; // Rethrow if the error is not a unique constraint violation
	            }
	        }
	    }
	}
	
	/*********
        * This is the method used to load the list of special access groups
        * Exception handling takes care of any database errors
        * 
        * @return special access group    	list of all special access
        */
	public List<SpecialAccessGroup> loadSpecialAccessGroups() throws SQLException {
	    List<SpecialAccessGroup> specialAccessGroups = new ArrayList<>();

	    // Set to track the processed group names to avoid duplicates
	    Set<String> processedGroupNames = new HashSet<>();

	    // Set to track processed users to avoid duplicates across all lists
	    Set<String> processedUsers = new HashSet<>();  // Track users by unique identifier
	    // Separate sets for instructors in each category to allow same user in both lists
	    Set<String> processedInstructorsWithAccess = new HashSet<>();
	    Set<String> processedInstructorsWithAdminRights = new HashSet<>();
	    Set<Long> processedArticles = new HashSet<>();

	    // Map of group_id to groupName
	    String groupQuery = "SELECT id, group_name FROM special_access_groups";
	    Map<Integer, String> groupMap = new HashMap<>();
	    try (Statement stmt = connection.createStatement();
	         ResultSet rs = stmt.executeQuery(groupQuery)) {
	        while (rs.next()) {
	            groupMap.put(rs.getInt("id"), rs.getString("group_name"));
	        }
	    }

	    // Rebuild the SpecialAccessGroup list
	    for (Map.Entry<Integer, String> entry : groupMap.entrySet()) {
	        int groupId = entry.getKey();
	        String groupName = entry.getValue();

	        // Skip if the group has already been processed
	        if (processedGroupNames.contains(groupName)) {
	            continue;  // Skip this iteration if group is already in the list
	        }

	        // Mark this group as processed
	        processedGroupNames.add(groupName);

	        // Load admins for the group
	        List<User> admins = new ArrayList<>();
	        String adminsQuery = "SELECT username, firstName FROM special_admins WHERE group_name = ?";
	        try (PreparedStatement pstmt = connection.prepareStatement(adminsQuery)) {
	            pstmt.setString(1, groupName);
	            try (ResultSet rs = pstmt.executeQuery()) {
	                while (rs.next()) {
	                    String userIdentifier = rs.getString("username") + rs.getString("firstName");
	                    if (!processedUsers.contains(userIdentifier)) {
	                        User user = findUser(rs.getString("username"), rs.getString("firstName"));
	                        if (user != null) {
	                            admins.add(user);
	                            processedUsers.add(userIdentifier);  // Mark user as processed
	                        }
	                    }
	                }
	            }
	        }

	        // Load instructors with access for the group
	        List<User> instructorsWithAccess = new ArrayList<>();
	        String instructorsAccessQuery = "SELECT username, firstName FROM special_instructors_access WHERE group_name = ?";
	        try (PreparedStatement pstmt = connection.prepareStatement(instructorsAccessQuery)) {
	            pstmt.setString(1, groupName);
	            try (ResultSet rs = pstmt.executeQuery()) {
	                while (rs.next()) {
	                    String userIdentifier = rs.getString("username") + rs.getString("firstName");
	                    if (!processedInstructorsWithAccess.contains(userIdentifier)) {
	                        User user = findUser(rs.getString("username"), rs.getString("firstName"));
	                        if (user != null) {
	                            instructorsWithAccess.add(user);
	                            processedInstructorsWithAccess.add(userIdentifier);  // Mark instructor as processed for this list
	                            processedUsers.add(userIdentifier);  // Mark user as processed globally
	                        }
	                    }
	                }
	            }
	        }

	        // Load instructors with admin rights for the group
	        List<User> instructorsWithAdminRights = new ArrayList<>();
	        String instructorsAdminQuery = "SELECT username, firstName FROM special_instructors_admin WHERE group_name = ?";
	        try (PreparedStatement pstmt = connection.prepareStatement(instructorsAdminQuery)) {
	            pstmt.setString(1, groupName);
	            try (ResultSet rs = pstmt.executeQuery()) {
	                while (rs.next()) {
	                    String userIdentifier = rs.getString("username") + rs.getString("firstName");
	                    if (!processedInstructorsWithAdminRights.contains(userIdentifier)) {
	                        User user = findUser(rs.getString("username"), rs.getString("firstName"));
	                        if (user != null) {
	                            instructorsWithAdminRights.add(user);
	                            processedInstructorsWithAdminRights.add(userIdentifier);  // Mark instructor as processed for this list
	                            processedUsers.add(userIdentifier);  // Mark user as processed globally
	                        }
	                    }
	                }
	            }
	        }

	        // Load students with viewing rights for the group
	        List<User> studentsWithViewingRights = new ArrayList<>();
	        String studentsViewingQuery = "SELECT username, firstName FROM special_students_viewing WHERE group_name = ?";
	        try (PreparedStatement pstmt = connection.prepareStatement(studentsViewingQuery)) {
	            pstmt.setString(1, groupName);
	            try (ResultSet rs = pstmt.executeQuery()) {
	                while (rs.next()) {
	                    String userIdentifier = rs.getString("username") + rs.getString("firstName");
	                    if (!processedUsers.contains(userIdentifier)) {
	                        User user = findUser(rs.getString("username"), rs.getString("firstName"));
	                        if (user != null) {
	                            studentsWithViewingRights.add(user);
	                            processedUsers.add(userIdentifier);  // Mark user as processed globally
	                        }
	                    }
	                }
	            }
	        }

	        // Load articles for the group
	        List<Long> articles = new ArrayList<>();
	        String articleQuery = "SELECT article_id FROM special_articles WHERE group_name = ?";
	        try (PreparedStatement pstmt = connection.prepareStatement(articleQuery)) {
	            pstmt.setString(1, groupName);
	            try (ResultSet rs = pstmt.executeQuery()) {
	                while (rs.next()) {
	                    Long articleId = rs.getLong("article_id");
	                    if (!processedArticles.contains(articleId)) {
	                        articles.add(articleId);
	                        processedArticles.add(articleId);  // Mark article as processed
	                    }
	                }
	            }
	        }

	        // Add to the list of special access groups
	        specialAccessGroups.add(new SpecialAccessGroup(
	            groupName, 
	            articles, 
	            admins, 
	            instructorsWithAccess, 
	            instructorsWithAdminRights, 
	            studentsWithViewingRights
	        ));
	    }

	    return specialAccessGroups;
	} 

	/*********
        * This is a helper method used to find a user
        * Exception handling takes care of any database errors
        * 
        * @param username   	username
	* @param firstName 	first name
 	* @return User		User
        */
	private User findUser(String username, String firstName) {
	    for (User curr : userList) {
	        if (curr.getFirstName().equals(firstName) && curr.getUsername().equals(username)) {
	            return curr;
	        }
	    }
	    return null;
	}

	/*********
        * This is a method used to save special access groups to the database
        * Exception handling takes care of any database errors
        * 
        * @param specialAccessGroups		list of special access groups
        */
	public void saveSpecialAccessGroupsToDatabase(List<SpecialAccessGroup> specialAccessGroups) throws SQLException {
	    // SQL queries for insertion
	    String insertGroupQuery = "INSERT INTO special_access_groups (group_name) VALUES (?)";
	    String insertArticleQuery = "INSERT INTO special_articles (group_name, article_id) VALUES (?, ?)";
	    String insertAdminQuery = "INSERT INTO special_admins (group_name, username, firstName) VALUES (?, ?, ?)";
	    String insertInstructorAccessQuery = "INSERT INTO special_instructors_access (group_name, username, firstName) VALUES (?, ?, ?)";
	    String insertInstructorAdminQuery = "INSERT INTO special_instructors_admin (group_name, username, firstName) VALUES (?, ?, ?)";
	    String insertStudentViewingQuery = "INSERT INTO special_students_viewing (group_name, username, firstName) VALUES (?, ?, ?)";

	    for (SpecialAccessGroup group : specialAccessGroups) {
	        // Insert into special_access_groups
	        try (PreparedStatement pstmt = connection.prepareStatement(insertGroupQuery)) {
	            pstmt.setString(1, group.getGroupName());
	            pstmt.executeUpdate();

	            // Retrieve the generated group_id
	            int groupId = -1;
	            try (ResultSet rs = pstmt.getGeneratedKeys()) {
	                if (rs.next()) {
	                    groupId = rs.getInt(1);
	                }
	            }

	            // Insert articles into special_articles if not empty or null
	            if (group.getArticles() != null && !group.getArticles().isEmpty()) {
	                try (PreparedStatement pstmtO = connection.prepareStatement(insertArticleQuery)) {
	                    for (Long articleId : group.getArticles()) {
	                        pstmtO.setString(1, group.getGroupName());
	                        pstmtO.setLong(2, articleId);
	                        pstmtO.executeUpdate();
	                    }
	                }
	            }

	            // Insert admins into special_admins if not empty or null
	            if (group.getAdmins() != null && !group.getAdmins().isEmpty()) {
	                try (PreparedStatement pstmtT = connection.prepareStatement(insertAdminQuery)) {
	                    for (User admin : group.getAdmins()) {
	                        pstmtT.setString(1, group.getGroupName());
	                        pstmtT.setString(2, admin.getUsername());
	                        pstmtT.setString(3, admin.getFirstName());
	                        pstmtT.executeUpdate();
	                    }
	                }
	            }

	            // Insert instructors with access into special_instructors_access if not empty or null
	            if (group.getInstructorsWithAccess() != null && !group.getInstructorsWithAccess().isEmpty()) {
	                try (PreparedStatement pstmtTh = connection.prepareStatement(insertInstructorAccessQuery)) {
	                    for (User instructor : group.getInstructorsWithAccess()) {
	                        pstmtTh.setString(1, group.getGroupName());
	                        pstmtTh.setString(2, instructor.getUsername());
	                        pstmtTh.setString(3, instructor.getFirstName());
	                        pstmtTh.executeUpdate();
	                    }
	                }
	            }

	            // Insert instructors with admin rights into special_instructors_admin if not empty or null
	            if (group.getInstructorsWithAdminRights() != null && !group.getInstructorsWithAdminRights().isEmpty()) {
	                try (PreparedStatement pstmtF = connection.prepareStatement(insertInstructorAdminQuery)) {
	                    for (User instructor : group.getInstructorsWithAdminRights()) {
	                        pstmtF.setString(1, group.getGroupName());
	                        pstmtF.setString(2, instructor.getUsername());
	                        pstmtF.setString(3, instructor.getFirstName());
	                        pstmtF.executeUpdate();
	                    }
	                }
	            }

	            // Insert students with viewing rights into special_students_viewing if not empty or null
	            if (group.getStudentsWithAccess() != null && !group.getStudentsWithAccess().isEmpty()) {
	                try (PreparedStatement pstmtFi = connection.prepareStatement(insertStudentViewingQuery)) {
	                    for (User student : group.getStudentsWithAccess()) {
	                        pstmtFi.setString(1, group.getGroupName());
	                        pstmtFi.setString(2, student.getUsername());
	                        pstmtFi.setString(3, student.getFirstName());
	                        pstmtFi.executeUpdate();
	                    }
	                }
	            }

	        } catch (SQLException e) {
	            e.printStackTrace();  // Handle exceptions as necessary
	            throw e;
	        }
	    }
	}
	
       /*********
       * This is the method used to check if the database is empty
       * Exception handling takes care of any database errors
       * 
       * @return boolean
       */
	public boolean isDatabaseEmpty() throws SQLException {
		String query = "SELECT COUNT(*) AS count FROM cse360users";
		ResultSet resultSet = statement.executeQuery(query);
		if (resultSet.next()) {
			return resultSet.getInt("count") == 0;
		}
		return true;
	}

	/*********
     * This is the method used to create the initial register
     * Exception handling takes care of any database errors
     * 
     * @param username    		username of user
     * @param password    		password of user
     * @param role    			role of user
     */
	public void initialRegister(String username, char[] password, Role role) throws Exception {
	    // Convert char[] password to byte[] for encryption
	    byte[] passwordBytes = new String(password).getBytes();

	    // Encrypt the password using the username as part of the IV (since email isn't provided at this stage)
	    String encryptedPassword = Base64.getEncoder().encodeToString(
	            encryptionHelper.encrypt(passwordBytes, EncryptionUtils.getInitializationVector(username.toCharArray()))
	    );

	    // Insert the new user into the database table
	    String insertUser = "INSERT INTO cse360users (username, password, roles) VALUES (?, ?, ?)";
	    try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
	        pstmt.setString(1, username);  // Insert username
	        pstmt.setString(2, encryptedPassword);  // Use encrypted password
	        pstmt.setString(3, role.name());  // Set the role
	        pstmt.executeUpdate();
	    }

	    // Clear the password array after processing for security reasons
	    //Arrays.fill(password, '\0');
	}

	/*********
     * This is the method used to create the final register
     * Exception handling takes care of any database errors
     * 
     * @param username    		username of user
     * @param firstName			first name of user
     * @param middleName		middle name of user
     * @param lastName			last name of user
     * @param preferredtName	preferred name of user
     * @param email    			email of user
     */
	public void finalRegister(String username, String firstName, String middleName, String lastName, String preferredName, String email) throws Exception {
	    // SQL query to update the user's details
	    String updateUserDetails = "UPDATE cse360users SET firstName = ?, middleName = ?, lastName = ?, preferredName = ?, email = ? WHERE username = ?";
	    
	    try (PreparedStatement pstmt = connection.prepareStatement(updateUserDetails)) {
	        // Set the user details in the prepared statement
	        pstmt.setString(1, firstName);     // First name
	        pstmt.setString(2, middleName);    // Middle name
	        pstmt.setString(3, lastName);      // Last name
	        pstmt.setString(4, preferredName); // Preferred name
	        pstmt.setString(5, email);         // Email
	        pstmt.setString(6, username);      // Username (used to identify the user)

	        // Execute the update
	        int rowsUpdated = pstmt.executeUpdate();
	        if (rowsUpdated > 0) {
	            System.out.println("User details updated successfully.");
	        } else {
	            System.out.println("User not found or no details were changed.");
	        }
	    } catch (SQLException e) {
	        System.err.println("Error updating user details: " + e.getMessage());
	        throw e;  // Re-throw the exception to handle it outside if needed
	    }
	}

	/*********
     * This is the method used to log into database
     * Exception handling takes care of any database errors
     * 
     * @param username    		username of user
     * @param password    		password of user
     * @param role    			role of user
     * @return boolean
     */
	public boolean login(String email, String password, String role) throws Exception {
		// encrypt the password using email as thats whats stored in the database
		String encryptedPassword = Base64.getEncoder().encodeToString(
				encryptionHelper.encrypt(password.getBytes(), EncryptionUtils.getInitializationVector(email.toCharArray()))
		);	
		// find user
		String query = "SELECT * FROM cse360users WHERE email = ? AND password = ? AND role = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, email);
			pstmt.setString(2, encryptedPassword);
			pstmt.setString(3, role);
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		}
	}
	
	/*********
     * This is the method used to check if user exists
     * 
     * @param email    			email of user
     * @return 	boolean
     */
	public boolean doesUserExist(String email) {
	    String query = "SELECT COUNT(*) FROM cse360users WHERE email = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        
	        pstmt.setString(1, email);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            // If the count is greater than 0, the user exists
	            return rs.getInt(1) > 0;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false; // If an error occurs, assume user doesn't exist
	}

	/*********
     * This is the method used to display users by admin
     * Exception handling takes care of any database errors
     */
	public void displayUsersByAdmin() throws Exception{
		String sql = "SELECT * FROM cse360users"; 
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery(sql); 

		while(rs.next()) { 
			// Retrieve by column name 
			int id  = rs.getInt("id"); 
			String  email = rs.getString("email"); 
			String role = rs.getString("role");  
			String encryptedPassword = rs.getString("password"); 
			char[] decryptedPassword = EncryptionUtils.toCharArray(
					encryptionHelper.decrypt(
							Base64.getDecoder().decode(
									encryptedPassword
							), 
							EncryptionUtils.getInitializationVector(email.toCharArray())
					)	
			);

			// Display values 
			System.out.print("ID: " + id); 
			System.out.print(", Email: " + email); 
			System.out.print(", Encrypted Password: " + encryptedPassword);
			System.out.print(", Decrypted Password: "); 
			EncryptionUtils.printCharArray(decryptedPassword);
			System.out.println(", Role: " + role); 
			
			Arrays.fill(decryptedPassword, '0');
		} 
	}
	
	/*********
     * This is the method used to display users by user
     * Exception handling takes care of any database errors
     */
	public void displayUsersByUser() throws Exception{
		String sql = "SELECT * FROM cse360users"; 
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery(sql); 

		while(rs.next()) { 
			// Retrieve by column name 
			int id  = rs.getInt("id"); 
			String  email = rs.getString("email"); 
			String role = rs.getString("role");  
			String encryptedPassword = rs.getString("password"); 
			char[] decryptedPassword = EncryptionUtils.toCharArray(
					encryptionHelper.decrypt(
							Base64.getDecoder().decode(
									encryptedPassword
							), 
							EncryptionUtils.getInitializationVector(email.toCharArray())
					)	
			);

			// Display values 
			System.out.print("ID: " + id); 
			System.out.print(", Email: " + email); 
			System.out.print(", Password: "); 
			EncryptionUtils.printCharArray(decryptedPassword);
			System.out.println(", Role: " + role); 
			
			Arrays.fill(decryptedPassword, '0');
		} 
	}

	/*********
     * This is the method used serialize roles
     * 
     * @param roles 			set of roles
     * @return role string 
     */
	private String serializeRoles(Set<Role> roles) {
	    StringBuilder rolesStringBuilder = new StringBuilder();
	    
	    for (Role role : roles) {
	        rolesStringBuilder.append(role.name()).append(",");  // Append each role name followed by a comma
	    }
	    
	    // Remove the last comma if rolesStringBuilder is not empty
	    if (rolesStringBuilder.length() > 0) {
	        rolesStringBuilder.setLength(rolesStringBuilder.length() - 1);  // Remove the trailing comma
	    }

	    return rolesStringBuilder.toString();  // Convert StringBuilder to String
	}

	/*********
     * This is the method used deserialize roles
     * 
     * @param rolestring 			string of roles
     * @return set of roles
     */
	private Set<Role> deserializeRoles(String rolesString) {
	    Set<Role> roles = new HashSet<>();
	    String[] roleNames = rolesString.split(",");  // Split the string into an array

	    // Loop through the array and convert each string to a Role enum
	    for (String roleName : roleNames) {
	        roles.add(Role.valueOf(roleName.trim()));  // Add the Role to the set
	    }

	    return roles;  // Return the populated set of roles
	}

	/*********
     * This is the method used to create the article
     * Exception handling takes care of any database errors
     * 
     * @param level    			(beginner, intermediate, advanced, expert)
     * @param groupId			identifiers used for groups of related articles
     * @param title				title of article
     * @param authors			authors of article
     * @param articleAbstract		short description about article
     * @param keywords			words used to process searches
     * @param body				body of help article
     * @param references		reference links or similar materials
     */
    public void createArticle(char[] level, char[] group, char[] title, char[] authors, char[] articleAbstract,
            char[] keywords, char[] body, char[] references) throws SQLException {
		// create a long id for the article given its parameters
    	long uniqueId = ArticleIdHelper.generateArticleId(level, group, title, authors, articleAbstract, keywords, body, references);  // Generate unique ID based on inputs
		
    	// used when restoring articles, check if the article already exists
    	boolean check = articleExistsByUniqueId(uniqueId);
    	
    	if (!check) {
			String insertArticle = "INSERT INTO help_articles (unique_id, level, groupId, title, authors, abstract, keywords, body, references) "
			           + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
			
			try (PreparedStatement pstmt = connection.prepareStatement(insertArticle)) {
			pstmt.setLong(1, uniqueId);  // Use the generated articleId
			pstmt.setString(2, new String(level));
			pstmt.setString(3, new String(group));
			pstmt.setString(4, new String(title));
			pstmt.setString(5, new String(authors));
			pstmt.setString(6, new String(articleAbstract));
			pstmt.setString(7, new String(keywords));
			pstmt.setString(8, new String(body));
			pstmt.setString(9, new String(references));
			pstmt.executeUpdate();
			} finally {
			clearCharArray(level);
			clearCharArray(group);
			clearCharArray(title);
			clearCharArray(authors);
			clearCharArray(articleAbstract);
			clearCharArray(keywords);
			clearCharArray(body);
			clearCharArray(references);
			}
    	}
    	else {
    		System.out.print("article already exists");
    	}
	}   
	
    /*********
     * This is the method used to clear the char array
     * @param array			character array
     */
    private void clearCharArray(char[] array) {
        Arrays.fill(array, '\u0000');  // Clear char array after use
    }
    
    /*********
     * This is the method used to list the articles
     * Exception handling takes care of any database errors
     * 
     * @return articleList 			string of list
     */
    public String listArticles() throws SQLException {
        String query = "SELECT * FROM help_articles";
        ResultSet rs = statement.executeQuery(query);
        StringBuilder articlesList = new StringBuilder("Articles:\n");
        
        while (rs.next()) {
            articlesList.append("[").append(rs.getInt("id")).append("] ")
                        .append(rs.getString("title")).append(" by ")
                        .append(rs.getString("authors")).append("\n");
            
            System.out.println(rs.getLong("unique_id"));
        }
        
        return articlesList.toString();
    }

    /*********
     * This is the method used to list the articles by group
     * Exception handling takes care of any database errors
     * 
     * @param groupId 			string of groupId
     * @return articleList 		string of article list
     */

    public String listArticlesByGroup(String groupId) throws SQLException {
    	
    	// case if user enters all or nothing
    	if (groupId.equals("all") || groupId.equals("")) {
        	System.out.println("aaaaaahahhahah");
    		return listArticles();
        }
    	
    	// Use LIKE to find articles that contain the specified groupId
        String query = "SELECT * FROM help_articles WHERE groupId LIKE ?";
        StringBuilder articlesList = new StringBuilder("Articles in Group [").append(groupId).append("]:\n");
        
        // Prepare the wildcard pattern for the LIKE clause
        String likeGroupId = "%" + groupId + "%"; // For example: "%java%"

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, likeGroupId);  // Set the groupId parameter with wildcard
            
            ResultSet rs = pstmt.executeQuery();
            boolean hasArticles = false;
            
            while (rs.next()) {
                hasArticles = true;  
                // Append article details in short form
                articlesList.append(rs.getInt("id")).append(". ")
                            .append(rs.getString("title")).append(" by ")
                            .append(rs.getString("authors")).append("\n")
                            .append("Abstract: ").append(rs.getString("abstract")).append("\n\n");
            }
            
            if (!hasArticles) {
                articlesList.append("No articles found in this group.\n");
            }
        } catch (SQLException e) {
            System.out.println("Error listing articles by group: " + e.getMessage());
            throw e;  // Re-throw the exception to handle it elsewhere if needed
        }
        
        return articlesList.toString();
    }

    
    /*********
     * This is the method used to list the articles by level
     * Exception handling takes care of any database errors
     * 
     * @param levelId 			string of levelId
     * @return articleList 		string of article list
     */

    public String listArticlesByLevel(String level) throws SQLException {
       
    	// case if all or nothing was entered
    	if (level.equals("all") || level.equals("")) {
        	System.out.println("aaaaaahahhahah");
    		return listArticles();
        }
    	
    	// Use LIKE to find articles that contain the specified groupId
        String query = "SELECT * FROM help_articles WHERE level LIKE ?";
        StringBuilder articlesList = new StringBuilder("Articles with level [").append(level).append("]:\n");
        
        // Prepare the wildcard pattern for the LIKE clause
        String likeLevel = "%" + level + "%"; // For example: "%java%"

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, likeLevel);  // Set the level parameter with wildcard
            
            ResultSet rs = pstmt.executeQuery();
            boolean hasArticles = false;
            
            while (rs.next()) {
                hasArticles = true;  
                // Append article details in short form
                articlesList.append(rs.getInt("id")).append(". ")
                            .append(rs.getString("title")).append(" by ")
                            .append(rs.getString("authors")).append("\n")
                            .append("Abstract: ").append(rs.getString("abstract")).append("\n\n");
            }
            
            if (!hasArticles) {
                articlesList.append("No articles found with specified level.\n");
            }
        } catch (SQLException e) {
            System.out.println("Error listing articles by level: " + e.getMessage());
            throw e;  // Re-throw the exception to handle it elsewhere if needed
        }
        
        return articlesList.toString();
    }
    /*********
     * This is the method used to list the articles by unique long id
     * Exception handling takes care of any database errors
     * 
     * @param id 			long of id
     * @return articleList 		string of article list
     */

    public String listArticlesByUniqueLongId(long id) throws SQLException {
        
    	if (id == 0) {
    		return listArticles();
        }
    	
    	// Use LIKE to find articles that contain the specified groupId
        String query = "SELECT * FROM help_articles WHERE unique_id LIKE ?";
        StringBuilder articlesList = new StringBuilder("\nArticle with unique id [").append(id).append("]:\n");
        
        // Prepare the wildcard pattern for the LIKE clause
        String likeId = "%" + id + "%"; // For example: "%java%"

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, likeId);  // Set the level parameter with wildcard
            
            ResultSet rs = pstmt.executeQuery();
            boolean hasArticles = false;
            
            while (rs.next()) {
                hasArticles = true;  
                // Append article details in short form
                articlesList.append(rs.getInt("id")).append(". ")
                            .append(rs.getString("title")).append(" by ")
                            .append(rs.getString("authors")).append("\n")
                            .append("Abstract: ").append(rs.getString("abstract")).append("\n\n");
            }
            
            if (!hasArticles) {
                articlesList.append("No article found with specified id.\n");
            }
        } catch (SQLException e) {
            System.out.println("Error listing articles by id: " + e.getMessage());
            throw e;  // Re-throw the exception to handle it elsewhere if needed
        }
        
        return articlesList.toString();
    }
    
    /*********
     * This is the method used to list the articles by level and group
     * Exception handling takes care of any database errors
     * 
     * @param level 			String of level
     * @param group 			String of group
     * @return articleList 		string of article list
     */

    public String listArticlesByLevelAndGroup(String level, String group) throws SQLException {
        // If "all" or nothing was entered for level or group, adjust the query accordingly
        boolean isAllLevels = level.equals("all") || level.isEmpty();
        boolean isAllGroups = group.equals("all") || group.isEmpty();

        // Build the base query with placeholders for dynamic criteria
        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM help_articles WHERE ");
        
        // Adjust query to include level and group conditions
        if (!isAllLevels) {
            queryBuilder.append("level LIKE ? ");
        }
        if (!isAllGroups) {
            if (!isAllLevels) queryBuilder.append("AND ");
            queryBuilder.append("groupId LIKE ? ");
        }

        String query = queryBuilder.toString();
        StringBuilder articlesList = new StringBuilder("\nArticles in Group [").append(group).append("]:\n");
        
        // Prepare wildcards for the LIKE clause
        String likeLevel = "%" + level + "%";
        String likeGroup = "%" + group + "%";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            int paramIndex = 1;
            if (!isAllLevels) pstmt.setString(paramIndex++, likeLevel);
            if (!isAllGroups) pstmt.setString(paramIndex++, likeGroup);
            
            ResultSet rs = pstmt.executeQuery();
            
            int articleNumber = 0;
            boolean hasArticles = false;

            while (rs.next()) {
                hasArticles = true;
                
                // Append article details in short form
                articlesList.append(rs.getInt("id")).append(". ")
                            .append(rs.getString("title")).append(" by ")
                            .append(rs.getString("authors")).append("\n")
                            .append("Abstract: ").append(rs.getString("abstract")).append("\n\n");
                articleNumber++;
            }
            
            // Display the total number of matching articles
            if (hasArticles) {
                articlesList.append("Number of articles matching the criteria: ").append(articleNumber).append("\n");
            } else {
                articlesList.append("No articles found with the specified level and group.\n");
            }
        } catch (SQLException e) {
            System.out.println("Error listing articles by level and group: " + e.getMessage());
            throw e;
        }

        return articlesList.toString();
    }

    
    /*********
     * This is the method used to update the article
     * Exception handling takes care of any database errors
     * 
     * @param level    			(beginner, intermediate, advanced, expert)
     * @param groupId			identifiers used for groups of related articles
     * @param title				title of article
     * @param authors			authors of article
     * @param articleAbstract	short description about article
     * @param keywords			words used to process searches
     * @param body				body of help article
     * @param references		reference links or similar materials
     */
    public void updateArticle(long id, char[] level, char[] groupId, char[] title, char[] authors, char[] articleAbstract, char[] keywords, char[] body, char[] references) throws SQLException {
        // Generate a new unique ID based on the updated fields
        long newUniqueId = ArticleIdHelper.generateArticleId(level, groupId, title, authors, articleAbstract, keywords, body, references);

        String updateQuery = "UPDATE help_articles SET level = ?, groupId = ?, title = ?, authors = ?, abstract = ?, keywords = ?, body = ?, references = ?, unique_id = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(updateQuery)) {
            pstmt.setString(1, new String(level));
            pstmt.setString(2, new String(groupId));
            pstmt.setString(3, new String(title));
            pstmt.setString(4, new String(authors));
            pstmt.setString(5, new String(articleAbstract));
            pstmt.setString(6, new String(keywords));
            pstmt.setString(7, new String(body));
            pstmt.setString(8, new String(references));
            pstmt.setLong(9, newUniqueId);  // Set the unique_id based on new data
            pstmt.setLong(10, id);  // Specify the article ID to update

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Article updated successfully.");
            } else {
                System.out.println("Article with ID " + id + " not found.");
            }
        } catch (SQLException e) {
            System.out.println("Error updating article: " + e.getMessage());
            throw e;
        } finally {
            // Clear sensitive character arrays after use
            clearCharArray(level);
            clearCharArray(groupId);
            clearCharArray(title);
            clearCharArray(authors);
            clearCharArray(articleAbstract);
            clearCharArray(keywords);
            clearCharArray(body);
            clearCharArray(references);
        }
    }

    
    /*********
     * This is the method used to delete article from database
     * Exception handling takes care of any database errors
     * 
     * @param articleId  		list of all users
     */
    public void deleteArticle(int articleId) throws SQLException {
        String deleteArticle = "DELETE FROM help_articles WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteArticle)) {
            pstmt.setInt(1, articleId);
            pstmt.executeUpdate();
        }
    }
    
    /*********
     * This is the method used to backup articles
     * Exception handling takes care of any database errors
     * 
     * @param filename 		 filename used to back up
     */
    public void backupArticles(String filename) throws SQLException, IOException {
        String query = "SELECT * FROM help_articles";
        ResultSet rs = statement.executeQuery(query);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            while (rs.next()) {
                // Create the line to be written
                String line = rs.getString("level") + ","
                			+ rs.getString("groupId") + ","
                            + rs.getString("title") + "," 
                            + rs.getString("authors") + ","
                            + rs.getString("abstract") + "," 
                            + rs.getString("keywords") + "," 
                            + rs.getString("body") + "," 
                            + rs.getString("references");

                // Debug print to check the format of the line
                System.out.println("Writing line to backup: " + line);

                // Write the line to the file
                writer.write(line);
                writer.newLine();
            }
        }
    }
    
    /*********
     * This is the method used to backup articles
     * Exception handling takes care of any database errors
     * 
     * @param filename 		 	filename used to back up
     * @param groupId		  	groupId used for grouped articles
     */
    public void backupArticlesByGroup(String filename, String groupId) throws SQLException, IOException {
        // Use LIKE to find articles that contain the specified groupId
        String query = "SELECT * FROM help_articles WHERE groupId LIKE ?";
        String likeGroupId = "%" + groupId + "%"; // Pattern for matching group

        try (PreparedStatement pstmt = connection.prepareStatement(query);
             BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            
            pstmt.setString(1, likeGroupId);  // Set the groupId parameter with wildcard
            ResultSet rs = pstmt.executeQuery();

            boolean hasArticles = false;

            while (rs.next()) {
                hasArticles = true;

             // Create the line to be written
                String line = rs.getString("level") + ","
                			+ rs.getString("groupId") + ","
                            + rs.getString("title") + "," 
                            + rs.getString("authors") + ","
                            + rs.getString("abstract") + "," 
                            + rs.getString("keywords") + "," 
                            + rs.getString("body") + "," 
                            + rs.getString("references");

                // Debug print to check the format of the line
                System.out.println("Writing line to backup: " + line);

                // Write the line to the file
                writer.write(line);
                writer.newLine();
            }

            // If no articles were found in the specified group, add a message to the file
            if (!hasArticles) {
                System.out.println("No articles found for group: " + groupId);
                writer.write("No articles found for group: " + groupId);
                writer.newLine();
            }

        } catch (SQLException e) {
            System.out.println("Error backing up articles by group: " + e.getMessage());
            throw e;
        }
    }

    /*********
     * This is the method used to backup articles by special access groups
     * Exception handling takes care of any database errors
     * 
     * @param filename 		 	filename used to back up
     * @param articleId		  	articleId used for list of articles
     */
    public void backupArticlesBySpecialAccessGroup(String filename, List<Long> articleIds) throws SQLException, IOException {
        // Construct a query with the IN clause to handle multiple IDs
        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM help_articles WHERE unique_id IN (");
        
        // Add placeholders for each article ID
        for (int i = 0; i < articleIds.size(); i++) {
            queryBuilder.append("?");
            if (i < articleIds.size() - 1) {
                queryBuilder.append(",");
            }
        }
        queryBuilder.append(")");

        // Prepare the SQL query
        String query = queryBuilder.toString();

        try (PreparedStatement pstmt = connection.prepareStatement(query);
             BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) { // Append mode
            // Set the values for the placeholders in the PreparedStatement
            for (int i = 0; i < articleIds.size(); i++) {
                pstmt.setLong(i + 1, articleIds.get(i)); // Set each article ID
            }

            ResultSet rs = pstmt.executeQuery();

            boolean hasArticles = false;

            while (rs.next()) {
                hasArticles = true;

                // Create the line to be written
                String line = rs.getString("level") + ","
                        + rs.getString("groupId") + ","
                        + rs.getString("title") + ","
                        + rs.getString("authors") + ","
                        + rs.getString("abstract") + ","
                        + rs.getString("keywords") + ","
                        + rs.getString("body") + ","
                        + rs.getString("references");

                // Debug print to check the format of the line
                System.out.println("Writing line to backup: " + line);

                // Write the line to the file
                writer.write(line);
                writer.newLine();
            }

            // If no articles were found, add a message to the file
            if (!hasArticles) {
                System.out.println("No articles found for the given IDs");
                writer.write("No articles found for the given IDs");
                writer.newLine();
            }

        } catch (SQLException e) {
            System.out.println("Error backing up articles: " + e.getMessage());
            throw e;
        }
    }
    /*********
     * This is the method used to restore articles
     * Exception handling takes care of any database errors
     * 
     * @param filename 		 filename used to restore
     */
    public void restoreArticles(String filename) throws SQLException, IOException {
        clearArticles();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length < 8) {  // Adjusted to 9 for the number of fields
                    System.err.println("Skipping line due to insufficient fields: " + line);
                    continue;
                }

                // Create the article using the fields without passing uniqueId
                createArticle(fields[0].toCharArray(), fields[1].toCharArray(), fields[2].toCharArray(),
                        fields[3].toCharArray(), fields[4].toCharArray(), fields[5].toCharArray(), 
                        fields[6].toCharArray(), fields[7].toCharArray());
            }
        }
    }


    /*********
     * This is the method used to merge selected article previously saved to the database
     * Exception handling takes care of any database errors
     * 
     * @param filename 		 filename used to load
     */
    public void mergeArticles(String filename) throws SQLException, IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length < 8) {
                    System.err.println("Skipping line due to insufficient fields: " + line);
                    continue;
                }
                createArticle(fields[0].toCharArray(), fields[1].toCharArray(), fields[2].toCharArray(),
                        fields[3].toCharArray(), fields[4].toCharArray(), fields[5].toCharArray(), 
                        fields[6].toCharArray(), fields[7].toCharArray());
            }
        }
    }
    
    /*********
     * This is the method used to check if an article already exists given a long
     * Exception handling takes care of any database errors
     * 
     * @param uniqueId 		 long used for unique Id
     */
    public boolean articleExistsByUniqueId(long uniqueId) throws SQLException {
        String query = "SELECT COUNT(*) FROM help_articles WHERE unique_id = ?";
        boolean exists = false;

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setLong(1, uniqueId);  // Set the unique_id parameter
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // If the count is greater than 0, the article exists
                exists = rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("Error checking article existence: " + e.getMessage());
            throw e;
        }

        return exists;  // Return true if the article exists, false otherwise
    }

    /*********
     * This is the method used to get the formatted article
     * Exception handling takes care of any database errors
     * 
     * @param title 		 title of article
     * @param author 		 author of article
     */
    public String getFormattedArticle(String title, String author) throws SQLException {
        StringBuilder formattedArticle = new StringBuilder();
        String query = "SELECT * FROM help_articles WHERE title = ? AND authors = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, title);
            pstmt.setString(2, author);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Add article details to the StringBuilder
                formattedArticle.append("****Article Details****").append("\n")
                				.append("Title: ").append(rs.getString("title")).append("\n")
                                .append("Authors: ").append(rs.getString("authors")).append("\n")
                                .append("Level: ").append(rs.getString("level")).append("\n")
                                .append("Group ID: ").append(rs.getString("groupId")).append("\n")
                                .append("Abstract: ").append(rs.getString("abstract")).append("\n")
                                .append("Keywords: ").append(rs.getString("keywords")).append("\n")
                                .append("Body:\n").append(rs.getString("body")).append("\n")
                                .append("References:\n").append(rs.getString("references")).append("\n")
                				.append("***********************").append("\n");
            } else {
                return "No article found with the specified title and author.";
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving article: " + e.getMessage());
            throw e;
        }

        return formattedArticle.toString(); // Return the formatted article string
    }

    /*********
     * This is the method used to get the formatted article given the sequence number
     * Exception handling takes care of any database errors
     * 
     * @param seqNum 		 sequence number of article
     */

    public String getFormattedArticleWithSeq(int seqNum) throws SQLException {
        StringBuilder formattedArticle = new StringBuilder();
        String query = "SELECT * FROM help_articles WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, seqNum);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Add article details to the StringBuilder
                formattedArticle.append("****Article Details****").append("\n")
                				.append("Title: ").append(rs.getString("title")).append("\n")
                                .append("Authors: ").append(rs.getString("authors")).append("\n")
                                .append("Level: ").append(rs.getString("level")).append("\n")
                                .append("Group ID: ").append(rs.getString("groupId")).append("\n")
                                .append("Abstract: ").append(rs.getString("abstract")).append("\n")
                                .append("Keywords: ").append(rs.getString("keywords")).append("\n")
                                .append("Body:\n").append(rs.getString("body")).append("\n")
                                .append("References:\n").append(rs.getString("references")).append("\n")
                				.append("***********************").append("\n");
            } else {
                return "No article found with the specified title and author.";
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving article: " + e.getMessage());
            throw e;
        }

        return formattedArticle.toString(); // Return the formatted article string
    }
    /*********
     * This is the method used to clear all memory of articles
     * Exception handling takes care of any database errors
     * 
     */
    private void clearArticles() throws SQLException {
        String deleteAll = "DELETE FROM help_articles";
        statement.executeUpdate(deleteAll);
    }
    
    /*********
     * This is the method used to get a unique id by the id
     * 
     * @return articleId		long id
     */
    public long getUniqueIdById(int id) {
        String query = "SELECT unique_id FROM help_articles WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong("unique_id");
                } else {
                    System.out.println("Article with ID " + id + " not found.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1; // Return -1 if the article is not found or an error occurs
    }
    
    /*********
     * This is the method used to get a unique id by title and author
     * 
     * @param title 			string of title
     * @param author 			string of author
     * @return articleId		long id
     */
    public long getUniqueIdByTitleAndAuthor(String title, String author) {
        String query = "SELECT unique_id FROM help_articles WHERE title = ? AND authors = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, title);
            statement.setString(2, author);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong("unique_id");
                } else {
                    System.out.println("Article with title \"" + title + "\" and author \"" + author + "\" not found.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1; // Return -1 if the article is not found or an error occurs
    }

    /*********
     * This is the method used to update article groupId given seqnum
     * Exception handling takes care of any database errors
     *
     * @param id 			int of id
     * @param newGroup 			string of newGroup
     */
    public void updateArticleGroupId(int id, String newGroup) throws SQLException {
        String updateQuery = "UPDATE help_articles SET groupId = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(updateQuery)) {
            // Set the new group ID and article ID in the query
            pstmt.setString(1, newGroup);
            pstmt.setInt(2, id);

            // Execute the update
            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Article with ID " + id + " updated successfully to group \"" + newGroup + "\".");
            } else {
                System.out.println("Article with ID " + id + " not found.");
            }
        } catch (SQLException e) {
            System.out.println("Error updating article groupId: " + e.getMessage());
            throw e;
        }
    }
    
    /*********
     * This is the method used to reset article groupId given group
     * Exception handling takes care of any database errors
     *
     * @param id 			int of id
     * @param group 			string of group
     */
    public void resetArticleGroupId(int id, String group) throws SQLException {
        String selectQuery = "SELECT groupId FROM help_articles WHERE id = ?";
        String updateQuery = "UPDATE help_articles SET groupId = ? WHERE id = ?";

        try (PreparedStatement selectStmt = connection.prepareStatement(selectQuery);
             PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
            
            // Check the current groupId for the specified article ID
            selectStmt.setInt(1, id);
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    String currentGroupId = rs.getString("groupId");

                    // Only proceed if the groupId matches the given group
                    if (currentGroupId != null && currentGroupId.equals(group)) {
                        updateStmt.setString(1, ""); // Reset groupId to empty string
                        updateStmt.setInt(2, id);
                        int rowsUpdated = updateStmt.executeUpdate();

                        if (rowsUpdated > 0) {
                            System.out.println("Article with ID " + id + " groupId reset to empty.");
                        } else {
                            System.out.println("Failed to reset groupId for article with ID " + id + ".");
                        }
                    } else {
                        System.out.println("GroupId does not match the provided group for article ID " + id + ".");
                    }
                } else {
                    System.out.println("Article with ID " + id + " not found.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error resetting article groupId: " + e.getMessage());
            throw e;
        }
    }
    
    /*********
     * This is the method used to reset articles by groupId
     * Exception handling takes care of any database errors
     * 
     * @param groupId 			string of group
     */
    public void resetArticlesByGroupId(String groupId) throws SQLException {
        String selectQuery = "SELECT id FROM help_articles WHERE groupId = ?";
        String updateQuery = "UPDATE help_articles SET groupId = '' WHERE id = ?";
        
        try (PreparedStatement selectStmt = connection.prepareStatement(selectQuery);
             PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
            
            // Step 1: Select all articles with the matching groupId
            selectStmt.setString(1, groupId);
            ResultSet resultSet = selectStmt.executeQuery();
            
            boolean articlesReset = false;
            
            // Step 2: Iterate through the result set and reset each article's groupId
            while (resultSet.next()) {
                int articleId = resultSet.getInt("id"); // Get the article ID
                
                // Reset the groupId for the current article
                updateStmt.setInt(1, articleId);
                updateStmt.executeUpdate();
                articlesReset = true;
            }
            
            // Step 3: Output the result
            if (articlesReset) {
                System.out.println("All articles with groupId '" + groupId + "' have been reset.");
            } else {
                System.out.println("No articles found with groupId '" + groupId + "'.");
            }
            
        } catch (SQLException e) {
            System.out.println("Error resetting articles: " + e.getMessage());
            throw e;
        }
    }

    /*********
     * This is the method used to close the connection to the database
     *
     */
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
