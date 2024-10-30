package project;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bouncycastle.util.Arrays;

import Encryption.EncryptionHelper;
import Encryption.EncryptionUtils;

class DatabaseHelper {

	// JDBC driver name and database URL 
	static final String JDBC_DRIVER = "org.h2.Driver";   
	static final String DB_URL = "jdbc:h2:~/firstDatabase";  

	//  Database credentials 
	static final String USER = "sa"; 
	static final String PASS = ""; 

	private Connection connection = null;
	private Statement statement = null; 
	//	PreparedStatement pstmt
	
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
	}

	/*********
     * This is the method used when the program starts, to retrieve all users from database 
     * Exception handling takes care of any database errors
     * 
     * @return user list
     */
	public List<User> loadUsersFromDatabase() throws SQLException {
	    List<User> userList = new ArrayList<>();
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
     * @param articleAbstract	short description about article
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
                hasArticles = true;  // Set to true if at least one article is found
                articlesList.append("[").append(rs.getInt("id")).append("] ")
                            .append(rs.getString("title")).append(" by ")
                            .append(rs.getString("authors")).append("\n");
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
     * This is the method used to load selected article previously saved to the database
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
     * This is the method used to clear all memory of articles
     * Exception handling takes care of any database errors
     * 
     */
    private void clearArticles() throws SQLException {
        String deleteAll = "DELETE FROM help_articles";
        statement.executeUpdate(deleteAll);
    }
    
    /*********
     * This is the method used to close the connection of database
     * Exception handling takes care of any database errors
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
