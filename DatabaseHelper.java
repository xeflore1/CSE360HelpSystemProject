package project;
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
	
	public DatabaseHelper() throws Exception {
		encryptionHelper = new EncryptionHelper();
	}

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

    
	private void createTables() throws SQLException {
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
	}

	// when the program starts, retrieve all users from database 
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


    // This method updates userlist in the database
    /*public void saveUserListToDatabase(List<User> userList) throws Exception {
        String insertOrUpdateUser = "INSERT INTO cse360users (username, password, roles, email, firstName, middleName, lastName, preferredName, isOneTimePassword, oneTimePassword, otpExpiry) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE username = VALUES(username), password = VALUES(password), roles = VALUES(roles), email = VALUES(email), firstName = VALUES(firstName), middleName = VALUES(middleName), lastName = VALUES(lastName), preferredName = VALUES(preferredName), isOneTimePassword = VALUES(isOneTimePassword), oneTimePassword = VALUES(oneTimePassword), otpExpiry = VALUES(otpExpiry)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(insertOrUpdateUser)) {
            for (User user : userList) {
            	// Convert char[] password to byte[] for encryption
        	    byte[] passwordBytes = new String(user.getPassword()).getBytes();

        	    // Encrypt the password using the username as part of the IV (since email isn't provided at this stage)
        	    String encryptedPassword = Base64.getEncoder().encodeToString(
        	            encryptionHelper.encrypt(passwordBytes, EncryptionUtils.getInitializationVector(user.getUsername().toCharArray()))
        	    );
        	    
                pstmt.setString(1, user.getUsername());
                pstmt.setString(2, encryptedPassword);  // Adjust password storage
                pstmt.setString(3, serializeRoles(user.getRoles()));  // Convert roles set to string
                pstmt.setString(4, user.getEmail());
                pstmt.setString(5, user.getFirstName());
                pstmt.setString(6, user.getMiddleName());
                pstmt.setString(7, user.getLastName());
                pstmt.setString(8, user.getPreferredName());
                pstmt.setBoolean(9, user.isOneTimePassword());
                pstmt.setString(10, user.getOneTimePassword());
                pstmt.setTimestamp(11, Timestamp.valueOf(user.getOtpExpiry()));

                pstmt.executeUpdate();  // Insert or update user
            }
        }
    } */
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



	// Check if the database is empty
	public boolean isDatabaseEmpty() throws SQLException {
		String query = "SELECT COUNT(*) AS count FROM cse360users";
		ResultSet resultSet = statement.executeQuery(query);
		if (resultSet.next()) {
			return resultSet.getInt("count") == 0;
		}
		return true;
	}

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

	// method converts roles to a comma seperated string
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


	// method converts comma seperated string back into roles
	private Set<Role> deserializeRoles(String rolesString) {
	    Set<Role> roles = new HashSet<>();
	    String[] roleNames = rolesString.split(",");  // Split the string into an array

	    // Loop through the array and convert each string to a Role enum
	    for (String roleName : roleNames) {
	        roles.add(Role.valueOf(roleName.trim()));  // Add the Role to the set
	    }

	    return roles;  // Return the populated set of roles
	}


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
