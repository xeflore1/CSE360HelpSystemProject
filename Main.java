package project;

import javafx.application.Application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/*******
 * <p> Main class </p>
 * 
 * <p> Description:  It represents the main function of the whole application.</p> 
 * <p> Stores and creates the VBox used to create the help system. </p>
 * <p> Manages the GUI and includes the user creation and management </p> 
 * <p> Collaborators: Role, Admin, User</p>
 * 
 * @author Hassan Khan, Colby Taylor, Xavier Flores, Shashwat Balaji, Avinash Poguluri, Abil Damirbek uulu
*/
public class Main extends Application {
    private static List<User> userList = new ArrayList<>();
    private Admin adminUser = null;
    private User currentUser = null;
    private TextArea outputArea = new TextArea();
    private VBox optionBox = new VBox(10);  // Reusable optionBox to prevent multiple instances
    private static DatabaseHelper databaseHelper; // database variable
    private Role currRole; // keeps track of the current role of the user


    /**********************************************************************************************
	 * This is the method that performs the test cases
	 * 
	 * @param args	The standard argument list for a Java Mainline
	 * 
	 */
    public static void main(String[] args) {
        launch(args);
    }

    /*********
     * This is the method that create the VBox from javafx and sets up the starting
     * screen of the help system. 
     * 
     * @param theStage    The pop up stage that handles all of the user interaction
     */
    @Override
    public void start(Stage theStage) throws Exception { // Method for the first user account created (admin)
        theStage.setTitle("ASU Help System");

        // Initialize the database helper
        databaseHelper = new DatabaseHelper();
        
        try {
        	// connect to database
        	databaseHelper.connectToDatabase();  // Connect to the database
            // Load users from the database into the userList
            userList = databaseHelper.loadUsersFromDatabase();
        } catch (Exception e) {
            outputArea.appendText("Error loading users from the database: " + e.getMessage() + "\n");
        }

        // Check if userList is empty
        if (userList.isEmpty()) {
	        Pane firstLogin = new Pane();
	        VBox formContainer = new VBox(5); // Main container for all components
	        formContainer.setAlignment(Pos.CENTER);
	        formContainer.setPadding(new Insets(10, 10, 20, 10)); // Add bottom padding for buffer
	        
	        Text welcomeText = new Text("Welcome to the ASU Help System");
	        Button createUserButton = new Button("Create Admin");
	        formContainer.getChildren().addAll(welcomeText, createUserButton, outputArea);
	
	        outputArea.setPrefHeight(200);
	        outputArea.setEditable(false);
	        Scene mainScene = new Scene(formContainer, 500, 800);
	        theStage.setScene(mainScene);
	
	        // Create user fields and labels
	        Label usernameLabel = new Label("Enter Username:");
	        TextField usernameInput = new TextField();
	        Label passwordLabel = new Label("Enter Password:");
	        PasswordField passwordInput = new PasswordField();
	        Label confirmPasswordLabel = new Label("Confirm Password:");
	        PasswordField confirmPasswordInput = new PasswordField();
	        // Button creation
	        Button submitButton = new Button("Submit");
	        Button cancelButton = new Button("Cancel");
	
	        // Add all components to form container (initially hidden)
	        formContainer.getChildren().addAll(
	            usernameLabel, usernameInput, 
	            passwordLabel, passwordInput, 
	            confirmPasswordLabel, confirmPasswordInput,
	            submitButton, cancelButton
	        );
	        
	        // components are initially not visible
	        usernameLabel.setVisible(false);
	        usernameInput.setVisible(false);
	        passwordLabel.setVisible(false);
	        passwordInput.setVisible(false);
	        confirmPasswordLabel.setVisible(false);
	        confirmPasswordInput.setVisible(false);
	        submitButton.setVisible(false);
	        cancelButton.setVisible(false);
	
	        // Event handler to show admin creation form
	        createUserButton.setOnAction(event -> {
	            outputArea.appendText("You are the first user and will be made an Admin.\n");
	            usernameLabel.setVisible(true);
	            usernameInput.setVisible(true);
	            passwordLabel.setVisible(true);
	            passwordInput.setVisible(true);
	            confirmPasswordLabel.setVisible(true);
	            confirmPasswordInput.setVisible(true);
	            submitButton.setVisible(true);
	            cancelButton.setVisible(true);
	            createUserButton.setDisable(true);
	        });
	
	        // Submit button handler for admin creation
	        submitButton.setOnAction(event -> {
	            String username = usernameInput.getText();
	            char[] password = passwordInput.getText().toCharArray();
	            char[] confirmPassword = confirmPasswordInput.getText().toCharArray();
	            // create admin
	            if (Arrays.equals(password, confirmPassword)) {
	                if (adminUser == null) {
	                    adminUser = new Admin(username, password);
	                    adminUser.addRole(Role.ADMIN);
	                    userList.add(adminUser);
	                    currentUser = adminUser;
	                    outputArea.appendText("Admin account created.\n");
	                } else {
	                    User newUser = new User(username, password);
	                    newUser.addRole(Role.STUDENT);  // Default role for regular users
	                    userList.add(newUser);
	                    currentUser = newUser;
	                    outputArea.appendText("User account created.\n");
	                }
	
	                usernameLabel.setVisible(false);
	                usernameInput.setVisible(false);
	                passwordLabel.setVisible(false);
	                passwordInput.setVisible(false);
	                confirmPasswordLabel.setVisible(false);
	                confirmPasswordInput.setVisible(false);
	                submitButton.setVisible(false);
	                cancelButton.setVisible(false);
	
	                collectUserInfo();  // Collect additional user information
	            } else {
	                outputArea.appendText("Passwords do not match. Please try again.\n");
	            }
	        });
	
	        cancelButton.setOnAction(event -> {
	            usernameInput.clear();
	            passwordInput.clear();
	            confirmPasswordInput.clear();
	            createUserButton.setDisable(false);
	        });
	
	        theStage.show();
        }
        else {
        	// Set up the main container for existing users
            Pane existingUserPane = new Pane();
            VBox existingUserContainer = new VBox(5); // Main container for all components
            existingUserContainer.setAlignment(Pos.CENTER);
            existingUserContainer.setPadding(new Insets(10, 10, 20, 10)); // Add bottom padding for buffer
            
            // Welcome text
            Text existingUserText = new Text("Welcome back to the ASU Help System");
            existingUserContainer.getChildren().add(existingUserText);

            // Output area for messages
            outputArea.setPrefHeight(200);
            outputArea.setEditable(false);
            existingUserContainer.getChildren().add(outputArea);

            // Set the scene
            Scene existingUserScene = new Scene(existingUserContainer, 500, 800);
            theStage.setScene(existingUserScene);
          
            showSignInOrCreateAccount();

            theStage.show();
        	
        }
    }
    
    /*********
     * This is the method used to collect the rest of the user details when signing up.
     * This involves collecting the first name, middle name, preferred name, last name and email
     * 
     */
    private void collectUserInfo() {
        outputArea.appendText("Please enter your personal details:\n");

        // Create input fields for user details
        Label firstNameLabel = new Label("First Name:");
        TextField firstNameInput = new TextField();
        Label middleNameLabel = new Label("Middle Name:");
        TextField middleNameInput = new TextField();
        Label lastNameLabel = new Label("Last Name:");
        TextField lastNameInput = new TextField();
        Label preferredNameLabel = new Label("Preferred Name:");
        TextField preferredNameInput = new TextField();
        Label emailLabel = new Label("Email:");
        TextField emailInput = new TextField();

        Button submitDetailsButton = new Button("Submit Details");
        Button cancelDetailsButton = new Button("Cancel");

        // Create a VBox to hold the user info inputs
        VBox userInfoBox = new VBox(10, firstNameLabel, firstNameInput, 
                                     middleNameLabel, middleNameInput,
                                     lastNameLabel, lastNameInput,
                                     preferredNameLabel, preferredNameInput,
                                     emailLabel, emailInput,
                                     submitDetailsButton, cancelDetailsButton);
        userInfoBox.setAlignment(Pos.CENTER);
        ((VBox) outputArea.getParent()).getChildren().add(userInfoBox);

        // Submit button action for user details
        submitDetailsButton.setOnAction(event -> {
            String firstName = firstNameInput.getText();
            String middleName = middleNameInput.getText();
            String lastName = lastNameInput.getText();
            String preferredName = preferredNameInput.getText();
            String email = emailInput.getText();

            // Set the user details in the current user object
            if (currentUser != null) {
                currentUser.setFirstName(firstName);
                currentUser.setMiddleName(middleName);
                currentUser.setLastName(lastName);
                currentUser.setPreferredName(preferredName);
                currentUser.setEmail(email);
            }

            outputArea.appendText("User details saved.\n");
            
            // Take user to the login screen
            ((VBox) outputArea.getParent()).getChildren().remove(userInfoBox);
        	showSignInOrCreateAccount();
            /*if (currentUser instanceof Admin) {
            	((VBox) outputArea.getParent()).getChildren().remove(userInfoBox);
            	showSignInOrCreateAccount();
            }
            else {
            // Clear the user info box
            ((VBox) outputArea.getParent()).getChildren().remove(userInfoBox);
            //Set<Role> currRole = currentUser.getRoles();
            showUserOptions(currRole);  // Show the options for the student user
            } */
        });

        // Cancel button action
        cancelDetailsButton.setOnAction(event -> {
            ((VBox) outputArea.getParent()).getChildren().remove(userInfoBox);
            loginPrompt();  // Show the options for the user
        });
    }

    /*********
     * This is the method that acts as the home page for the users. 
     * Based on what role the user is the permissions will be different
     * 
     * @param role      The role of the user used to determine the visible options
     */
    private void showUserOptions(Role role) {
        outputArea.appendText("What would you like to do? Options:\n");

        clearPreviousOptionBox();  // Ensure only one options box is visible

        // Clear the optionBox before adding new options
        optionBox.getChildren().clear();

        // Create buttons that multiple users have
        Button signOutButton = new Button("Sign out");
        Button quitButton = new Button("Quit");
        Button aritcleButton = new Button("Article settings");

        // Add admin options only if the current user is an admin
        if (currRole == Role.ADMIN) {
            Button printUsersButton = new Button("Print users");
            Button deleteUserButton = new Button("Delete user");
            Button inviteUserButton = new Button("Invite a user");
            Button addOrRemoveRole = new Button("Add or remove a users role");
            Button resetUserButton = new Button("Reset a user's password");
            
            // Set button actions
            printUsersButton.setOnAction(e -> listUsers());
            deleteUserButton.setOnAction(e -> deleteUser());
            inviteUserButton.setOnAction(e -> inviteUser());
            addOrRemoveRole.setOnAction(e -> addRemoveRole());
            resetUserButton.setOnAction(e -> resetUser());
            aritcleButton.setOnAction(e -> {
				try {
					articleOptions();
				} catch (Exception e1) {
					outputArea.appendText("Error going to article options:\n");
					e1.printStackTrace();
				}
			});
            
            optionBox.getChildren().addAll(
                new Label("Select an option:"),
                signOutButton,
                printUsersButton,
                deleteUserButton,
                inviteUserButton,
                addOrRemoveRole,
                resetUserButton,
                aritcleButton,
                quitButton
            );
        } 
        else if (currRole == Role.INSTRUCTOR) {  	
        	optionBox.getChildren().addAll(
                    new Label("Select an option:"),
                    aritcleButton,
                    signOutButton,
                    quitButton
            );
        	
        	aritcleButton.setOnAction(e -> {
				try {
					articleOptions();
				} catch (Exception e1) {
					outputArea.appendText("Error going to article options:\n");
					e1.printStackTrace();
				}
			}); 
        	
        }
        
        
        else  if (currRole == Role.STUDENT){
            // For regular users and instructors, only show sign out and quit options
            optionBox.getChildren().addAll(
                new Label("Select an option:"),
                signOutButton,
                quitButton
            );
        }

        // Set sign out and quit button actions
        signOutButton.setOnAction(e -> signOut());
        quitButton.setOnAction(e -> {
        	
        	// Save the updated user list to the database
            try {
				databaseHelper.saveUserListToDatabase(userList);
			} catch (Exception e1) {
				outputArea.appendText("Error saving list to database\n");
				e1.printStackTrace();
			}
        	
            outputArea.appendText("Goodbye!\n");
            System.exit(0);
        });

        optionBox.setAlignment(Pos.CENTER);
        ((VBox) outputArea.getParent()).getChildren().add(optionBox);
    }

    /*********
     * This is the method used to sign out.
     */
    private void signOut() {
        outputArea.appendText("You have signed out.\n");
        currentUser = null;
        showSignInOrCreateAccount();
    }

    /*********
     * This is the method used to show the options when logged out
     */
    private void showSignInOrCreateAccount() {
        outputArea.appendText("Select an option:\n");

        clearPreviousOptionBox();  // Ensure only one options box is visible

        // Clear the optionBox before adding new options
        optionBox.getChildren().clear();

        // Create buttons for login and create account options
        Button loginButton = new Button("Login");
        Button createAccountButton = new Button("Create Account");

        // Set button actions
        loginButton.setOnAction(e -> loginPrompt());
        createAccountButton.setOnAction(e -> createAccount());

        // Add buttons to the option box
        optionBox.getChildren().setAll(
            new Label("Select an option:"),
            loginButton,
            createAccountButton
        );

        optionBox.setAlignment(Pos.CENTER);
        ((VBox) outputArea.getParent()).getChildren().add(optionBox);
    }

    /*********
     * This is the method used to show the login prompt when clicked on login
     */
    private void loginPrompt() {
        outputArea.appendText("Enter username and password to log in or enter an invitation code.\n");

        Label usernameLabel = new Label("Username:");
        TextField usernameInput = new TextField();
        Label passwordLabel = new Label("Password:");
        PasswordField passwordInput = new PasswordField();
        Button loginButton = new Button("Login");
        Button invitationButton = new Button("I have an invite code");
        Button forgotPasswordButton = new Button("I forgot my password");
        Button backButton = new Button("Back");

        VBox loginBox = new VBox(10, usernameLabel, usernameInput, passwordLabel, passwordInput, 
        		loginButton, invitationButton, forgotPasswordButton, backButton);
        loginBox.setAlignment(Pos.CENTER);
        ((VBox) outputArea.getParent()).getChildren().add(loginBox);
        
        clearPreviousOptionBox();

        loginButton.setOnAction(event -> {
            String username = usernameInput.getText();
            char[] password = passwordInput.getText().toCharArray();
            boolean userFound = false;

            for (User user : userList) {
                if (user.getUsername().equals(username) && Arrays.equals(password, user.getPassword())) {
                    outputArea.appendText("Login successful.\n");
                    currentUser = user;
                    userFound = true;
                    ((VBox) outputArea.getParent()).getChildren().remove(loginBox);
                    promptRoleSelection(currentUser);
                    break;
                }
            }
            if (!userFound) {
                outputArea.appendText("User not found or incorrect password.\n");
            }
        });
        // if the invite button is clicked the user will be taken to the invitation screen
        invitationButton.setOnAction(event -> {
        	((VBox) outputArea.getParent()).getChildren().remove(loginBox);
        	inviteLogin();
        });
        
        // If the forgot password button is clicked
        forgotPasswordButton.setOnAction(event -> {
            ((VBox) outputArea.getParent()).getChildren().remove(loginBox);
            resetLogin();  // Redirect to the reset password screen
        });
        
        // Back button functionality
        backButton.setOnAction(event -> {
            ((VBox) outputArea.getParent()).getChildren().remove(loginBox);
            showSignInOrCreateAccount();  // Show the sign-in/create account options again
        });
    }

    /*********
     * This is the method that prompts the user for what role they want to choose
     * for the current session
     * 
     * @param user      The user who is currently signed in
     */
    private void promptRoleSelection(User user) {
        outputArea.appendText("Select a role for this session:\n");

        clearPreviousOptionBox();  // Clear previous UI elements

        optionBox.getChildren().clear();
        
        ToggleGroup roleToggleGroup = new ToggleGroup();

        // Add radio buttons for each role
        for (Role role : user.getRoles()) {
            RadioButton roleOption = new RadioButton(role.toString());
            roleOption.setToggleGroup(roleToggleGroup);
            optionBox.getChildren().add(roleOption);
        }

        Button submitRoleButton = new Button("Submit");
        optionBox.getChildren().add(submitRoleButton);

        // Handle role selection
        submitRoleButton.setOnAction(e -> { 
        	// retrieve the selected button
        	RadioButton selectedRadioButton = (RadioButton) roleToggleGroup.getSelectedToggle();
        	// if admin was selected
        	if (selectedRadioButton != null && selectedRadioButton.getText().equals("ADMIN")) {
        		currRole = Role.ADMIN;
        	    showUserOptions(currRole);
        	}
        	// if instructor was selected
        	else if (selectedRadioButton != null && selectedRadioButton.getText().equals("INSTRUCTOR")) {
        		currRole = Role.INSTRUCTOR;
        	    showUserOptions(currRole);
        	}
        	// if student was selected
        	else if (selectedRadioButton != null && selectedRadioButton.getText().equals("STUDENT")) {
        		currRole = Role.STUDENT;
        	    showUserOptions(currRole);
        	}	 
            else {
                outputArea.appendText("Please select a role.\n");
            }
        });

        optionBox.setAlignment(Pos.CENTER);
        ((VBox) outputArea.getParent()).getChildren().add(optionBox);
    }
    
    
    /*********
     * This is the method used to show the login for when invite user is clicked.
     */
    private void inviteLogin() {
        // Inform the user
        outputArea.appendText("Enter your invite code.\n");

        // Create input fields for the invite code, username, and password
        Label inviteLabel = new Label("Invite Code:");
        TextField inviteInput = new TextField();
        Label usernameLabel = new Label("Username:");
        TextField usernameInput = new TextField();
        Label passwordLabel = new Label("Password:");
        PasswordField passwordInput = new PasswordField();
        Label confirmPasswordLabel = new Label("Confirm Password:");
        PasswordField confirmPasswordInput = new PasswordField();

        // Create buttons
        Button submitButton = new Button("Submit");
        Button backButton = new Button("Back");

        // Layout for the invite login form
        VBox inviteBox = new VBox(10, inviteLabel, inviteInput, usernameLabel, usernameInput, passwordLabel, 
                                  passwordInput, confirmPasswordLabel, confirmPasswordInput, submitButton, backButton);
        inviteBox.setAlignment(Pos.CENTER);
        ((VBox) outputArea.getParent()).getChildren().add(inviteBox);

        // Clear any previous options
        clearPreviousOptionBox();

        // Event handler for the submit button
        submitButton.setOnAction(event -> {
            // Get the invite code and validate it
            String inviteCode = inviteInput.getText();
            
            // Single role codes
            if (inviteCode.equals("STUDENTINVCODE")) {
                processInviteCode(usernameInput, passwordInput, confirmPasswordInput, Arrays.asList(Role.STUDENT), inviteBox);
            } else if (inviteCode.equals("INSTRUCTORINVCODE")) {
                processInviteCode(usernameInput, passwordInput, confirmPasswordInput, Arrays.asList(Role.INSTRUCTOR), inviteBox);
            } else if (inviteCode.equals("ADMININVCODE")) {
                processInviteCode(usernameInput, passwordInput, confirmPasswordInput, Arrays.asList(Role.ADMIN), inviteBox);
            }
            
            // Multiple roles codes
            else if (inviteCode.equals("STUDENTINSINVCODE")) {
                processInviteCode(usernameInput, passwordInput, confirmPasswordInput, Arrays.asList(Role.STUDENT, Role.INSTRUCTOR), inviteBox);
            } else if (inviteCode.equals("STUADINVCODE")) {
                processInviteCode(usernameInput, passwordInput, confirmPasswordInput, Arrays.asList(Role.STUDENT, Role.ADMIN), inviteBox);
            } else if (inviteCode.equals("ADMININSINVCODE")) {
                processInviteCode(usernameInput, passwordInput, confirmPasswordInput, Arrays.asList(Role.INSTRUCTOR, Role.ADMIN), inviteBox);
            } else if (inviteCode.equals("ADMININSSTUINVCODE")) {
                processInviteCode(usernameInput, passwordInput, confirmPasswordInput, Arrays.asList(Role.STUDENT, Role.INSTRUCTOR, Role.ADMIN), inviteBox);
            } else {
                outputArea.appendText("Invalid invitation code. Please try again.\n");
            }
        });

        // Event handler for the back button
        backButton.setOnAction(event -> {
            // Remove the invite login form and show the login page again
            ((VBox) outputArea.getParent()).getChildren().remove(inviteBox);
            loginPrompt();  // Return to the login screen
        });
    }
   
    /*********
     * This is the method used to display the reset login page
     */
    private void resetLogin() {
        outputArea.appendText("Enter your username and the OTP sent to your email:\n");

        // Label and text fields for entering username and OTP
        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        Label otpLabel = new Label("OTP:");
        TextField otpField = new TextField();
        
        // Button to confirm OTP
        Button confirmOtpButton = new Button("Confirm OTP");
        Button backButton = new Button("Back");

        // VBox layout to arrange the components vertically
        VBox resetBox = new VBox(10, usernameLabel, usernameField, otpLabel, otpField, confirmOtpButton, backButton);
        resetBox.setAlignment(Pos.CENTER);  // Align the components to the center

        // Add the resetBox to the existing VBox containing the outputArea
        ((VBox) outputArea.getParent()).getChildren().add(resetBox);

        // Clear any previous option boxes
        clearPreviousOptionBox();
        
     // Set the action for when the "Confirm OTP" button is pressed
        confirmOtpButton.setOnAction(event -> {
            String username = usernameField.getText().trim();
            String enteredOtp = otpField.getText().trim();

            User user = null; // Initialize user as null

            // Iterate through userList to find the user by username
            for (User u : userList) { // Assuming userList is a List<User> in your class
                if (u.getUsername().equals(username)) {
                    user = u; // Set user if found
                    break; // Exit the loop
                }
            }

            // Check if user exists and if the entered OTP matches the user's OTP
            if (user != null && user.getOneTimePassword() != null && user.getOneTimePassword().equals(enteredOtp)) {
                outputArea.appendText("OTP verified successfully. Please enter your new password:\n");
                ((VBox) outputArea.getParent()).getChildren().remove(resetBox); // Remove current box
                showNewPasswordForm(user); // Show form for new password
            } else {
                outputArea.appendText("Invalid username or OTP. OTP may have also expired. Please try again.\n");
            }
        });
     // Set the action for when the "Back" button is pressed
        backButton.setOnAction(event -> {
            ((VBox) outputArea.getParent()).getChildren().remove(resetBox); // Remove resetBox
            loginPrompt(); // Show login prompt again when going back
        });
    }
    
    /*********
     * This is the method used for generating a new password after successful OTP verification
     */
    private void showNewPasswordForm(User user) {
        outputArea.appendText("Enter your new password:\n");

        // Label and text fields for new password
        Label newPasswordLabel = new Label("New Password:");
        PasswordField newPasswordField = new PasswordField();
        Label confirmPasswordLabel = new Label("Confirm Password:");
        PasswordField confirmPasswordField = new PasswordField();
        Button updatePasswordButton = new Button("Update Password");
        Button cancelButton = new Button("Cancel");

        // VBox layout for new password input
        VBox passwordBox = new VBox(10, newPasswordLabel, newPasswordField, confirmPasswordLabel, confirmPasswordField, updatePasswordButton, cancelButton);
        passwordBox.setAlignment(Pos.CENTER);
        
        // Add the passwordBox to the existing VBox containing the outputArea
        ((VBox) outputArea.getParent()).getChildren().add(passwordBox);

        // Set the action for when the "Update Password" button is pressed
        updatePasswordButton.setOnAction(event -> {
            char[] newPassword = newPasswordField.getText().toCharArray();
            char[] confirmPassword = confirmPasswordField.getText().toCharArray();

            if (Arrays.equals(newPassword, confirmPassword) && newPassword.length > 0) {
                user.setPassword(newPassword); // Update user's password
                outputArea.appendText("Password updated successfully. You can now log in with your new password.\n");
                ((VBox) outputArea.getParent()).getChildren().remove(passwordBox); // Remove passwordBox
                loginPrompt(); // Redirect to login
            } else {
                outputArea.appendText("Passwords do not match or are invalid. Please try again.\n");
            }
        });
        
     // Set the action for when the "Cancel" button is pressed
        cancelButton.setOnAction(event -> {
            ((VBox) outputArea.getParent()).getChildren().remove(passwordBox); // Remove passwordBox
            loginPrompt(); // Redirect to login
        });
    }
        
    /*********
     * This is the method used to create a new user given the details from the invite screen
     * 
     * @param usernameInput    what the user inputs for username
     * @param passwordInput    what the user inputs for password
     * @param confirmPasswordInput        what the user inputs for confirmPasswordInput
     * @param roles    list of type Role of roles
     * @param createBox    the VBox used for javafx
     */
    private void processInviteCode(TextField usernameInput, PasswordField passwordInput, PasswordField confirmPasswordInput, List<Role> roles, VBox createBox) {
        String username = usernameInput.getText();
        char[] password = passwordInput.getText().toCharArray();
        char[] confirmPassword = confirmPasswordInput.getText().toCharArray();

        // Check if passwords match
        if (Arrays.equals(password, confirmPassword)) {
        	// create new user
            User newUser = new User(username, password);

            // Add all roles to the new user
            for (Role role : roles) {
                newUser.addRole(role);
            }

            // Collect additional user information
            clearPreviousOptionBox();
            collectUserInfo();  // Pass the newUser object to collectUserInfo
            userList.add(newUser);
            currentUser = newUser;
           
            // Notify user of the roles created
            outputArea.appendText("Account was successfully invited.\n");

            // After account creation, transition back to the login screen
            ((VBox) outputArea.getParent()).getChildren().remove(createBox);
        	
        } else {
            outputArea.appendText("Passwords don't match. Please try again.\n");
        }
    }

    /*********
     * This is the method used to create the account
     */
    private void createAccount() {
        outputArea.appendText("Enter details to create a new account.\n");

        // Create input fields for username and password
        Label usernameLabel = new Label("Username:");
        TextField usernameInput = new TextField();
        Label passwordLabel = new Label("Password:");
        PasswordField passwordInput = new PasswordField();
        Label confirmPasswordLabel = new Label("Confirm Password:");
        PasswordField confirmPasswordInput = new PasswordField();

        // Create radio buttons for user roles
        RadioButton studentRadioButton = new RadioButton("Student");
        RadioButton instructorRadioButton = new RadioButton("Instructor");
        ToggleGroup roleToggleGroup = new ToggleGroup();
        studentRadioButton.setToggleGroup(roleToggleGroup);
        instructorRadioButton.setToggleGroup(roleToggleGroup);
        studentRadioButton.setSelected(true);

        // Create buttons
        Button createButton = new Button("Create");
        Button backButton = new Button("Back");

        // Create a VBox for input fields
        VBox createBox = new VBox(10, usernameLabel, usernameInput, passwordLabel, passwordInput,
                                  confirmPasswordLabel, confirmPasswordInput,
                                  studentRadioButton, instructorRadioButton, 
                                  createButton, backButton);
        createBox.setAlignment(Pos.CENTER);
        ((VBox) outputArea.getParent()).getChildren().add(createBox);
        
        clearPreviousOptionBox();

        createButton.setOnAction(event -> {
            String username = usernameInput.getText();
            char[] password = passwordInput.getText().toCharArray();
            char[] confirmPassword = confirmPasswordInput.getText().toCharArray();

            if (Arrays.equals(password, confirmPassword)) {
                User newUser = new User(username, password);
                if (instructorRadioButton.isSelected()) {
                    newUser.addRole(Role.INSTRUCTOR);
                    outputArea.appendText("Instructor account created successfully.\n");
                } else {
                    newUser.addRole(Role.STUDENT);
                    outputArea.appendText("Student account created successfully.\n");
                }

                // Collect additional user information
                collectUserInfo(); // Pass the newUser object to collectUserInfo

                userList.add(newUser);
                currentUser = newUser;
                ((VBox) outputArea.getParent()).getChildren().remove(createBox);
            } else {
                outputArea.appendText("Passwords do not match. Please try again.\n");
            }
        });

        backButton.setOnAction(event -> {
            ((VBox) outputArea.getParent()).getChildren().remove(createBox);
            showSignInOrCreateAccount();  // Show the sign-in/create account options again
        });
    }

    /*********
     * This is the method used for admins to invite a user
     */
    private void inviteUser() {
        // Clear previous output and prepare the invite user view
        outputArea.appendText("Invite a new user.\n");

        // Label above check boxs
        Label title = new Label("Check the roles to assign to the new user:");
        // Check boxes for role selection (multiple roles can be selected)
        CheckBox studentCheckBox = new CheckBox("Student");
        CheckBox instructorCheckBox = new CheckBox("Instructor");
        CheckBox adminCheckBox = new CheckBox("Admin");

        // Button to invite the user
        Button inviteButton = new Button("Invite User");
        Button backButton = new Button("Back");

        // VBox layout to arrange the components vertically
        VBox inviteBox = new VBox(10, title, studentCheckBox, instructorCheckBox, adminCheckBox, 
        		inviteButton, backButton);
        inviteBox.setAlignment(Pos.CENTER);  // Align the components to the center

        // Add the new inviteBox to the existing VBox containing the outputArea
        ((VBox) outputArea.getParent()).getChildren().add(inviteBox);

        // Clear any previous option boxes
        clearPreviousOptionBox();

        // Set the action for when the "Invite User" button is pressed
        inviteButton.setOnAction(event -> {
        	// Add roles and print messages based on which check boxes are selected
        	// student + instructor + admin
        	if (studentCheckBox.isSelected() && instructorCheckBox.isSelected() && adminCheckBox.isSelected()) {
            	outputArea.appendText("Invite code: ADMININSSTUINVCODE\n");
            }
        	// student + instructor
        	else if (studentCheckBox.isSelected() && instructorCheckBox.isSelected()) {
                outputArea.appendText("Invite code: STUDENTINSINVCODE\n");
            }
        	// student + admin
        	else if (studentCheckBox.isSelected() && adminCheckBox.isSelected()) {
            	outputArea.appendText("Invite code: STUADINVCODE\n");            
            }
        	// instructor + admin
        	else if (instructorCheckBox.isSelected() && adminCheckBox.isSelected()) {
            	outputArea.appendText("Invite code: ADMININSINVCODE\n");
            }
        	// student
        	else if (studentCheckBox.isSelected()) {
                outputArea.appendText("Invite code: STUDENTINVCODE\n");
            }
        	// instructor
        	else if (instructorCheckBox.isSelected()) {
        		outputArea.appendText("Invite code: INSTRUCTORINVCODE\n");            
            }
        	// admin
        	else if (adminCheckBox.isSelected()) {
            	outputArea.appendText("Invite code: ADMININVCODE\n");
            }
            else {
                outputArea.appendText("No role selected. Please select at least one role.\n");
            }
            // Remove the createBox from the UI after successful account creation
            ((VBox) outputArea.getParent()).getChildren().remove(inviteBox);
            showUserOptions(Role.ADMIN);

        });

        // Set the action for when the "Back" button is pressed
        backButton.setOnAction(event -> {
            ((VBox) outputArea.getParent()).getChildren().remove(inviteBox);  // Remove the inviteBox
            showUserOptions(Role.ADMIN);  // Show options again when going back(); 
        });
    }
    
    /*********
     * This is the method used by the admin to reset the password of a given user
     */
    private void resetUser() {
        outputArea.appendText("Enter the username for password reset:\n");

        // Label and text field for entering the username
        Label title = new Label("Username:");
        TextField usernameField = new TextField();
        
        // Button to generate OTP for password reset
        Button generateOTPButton = new Button("Generate OTP");
        Button backButton = new Button("Back");

        // VBox layout to arrange the components vertically
        VBox resetBox = new VBox(10, title, usernameField, generateOTPButton, backButton);
        resetBox.setAlignment(Pos.CENTER);  // Align the components to the center

        // Add the resetBox to the existing VBox containing the outputArea
        ((VBox) outputArea.getParent()).getChildren().add(resetBox);

        // Clear any previous option boxes
        clearPreviousOptionBox();
        
        // Set the action for when the "Generate OTP" button is pressed
        generateOTPButton.setOnAction(event -> {
            String username = usernameField.getText().trim();
            
            if (!username.isEmpty()) {
                User[] foundUser = new User[1]; // Use an array to hold the found user
                
                // Find the user by username within the same method
                for (User user : userList) { // Assuming userList is a collection of users
                    if (user.getUsername().equals(username)) {
                        foundUser[0] = user; // Store the found user
                        break; // Exit the loop if the user is found
                    }
                }
                
                if (foundUser[0] != null) {
                    String otp = generateRandomString(7 + (int)(Math.random() * 6)); // Generate OTP
                    foundUser[0].setOneTimePassword(otp); // Set the OTP for the user
                    
                    outputArea.appendText("OTP generated and sent to the user's email. It will expire in 5min.\n");
                    outputArea.appendText("Generated OTP for user " + username + ": " + otp + "\n");
                    
                    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
                    scheduler.schedule(() -> {
                        String otpExpired = generateRandomString(7 + (int)(Math.random() * 6)); // Generate new OTP
                        foundUser[0].setOneTimePassword(otpExpired); // Set the new OTP for the user
                        outputArea.appendText("OTP for user " + username + " has expired and has been changed.\n");
                    }, 5, TimeUnit.MINUTES);
                    
                 // Optionally disable the "Generate OTP" button to prevent multiple OTPs
                    generateOTPButton.setDisable(true);
                } else {
                    outputArea.appendText("User not found. Please check the username.\n");
                }
            } else {
                outputArea.appendText("Please enter a valid username.\n");
            }
        });

        // Set the action for when the "Back" button is pressed
        backButton.setOnAction(event -> {
            // Remove the resetBox from the UI
            ((VBox) outputArea.getParent()).getChildren().remove(resetBox);
            showUserOptions(Role.ADMIN);  // Show options again when going back
        });
    }
    
    /*********
     * This is the method used for generating a random string for invite code and OTPs
     */
    private String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"; // Adjust characters as needed
        StringBuilder otpBuilder = new StringBuilder(length);
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            otpBuilder.append(characters.charAt(random.nextInt(characters.length())));
        }
        return otpBuilder.toString();
    }
        
    
    /*********
     * This is the method used for clearing the previous option box
     */
    private void clearPreviousOptionBox() {
        if (optionBox.getParent() != null) {
            ((VBox) outputArea.getParent()).getChildren().remove(optionBox);
        }
    }

    /*********
     * This is the method used for admins to list all users
     */
    private void listUsers() {
        outputArea.appendText("List of users:\n");
        for (User user : userList) {
            StringBuilder userInfo = new StringBuilder(user.getUsername());

            // Append names
            userInfo.append(" - Name: ");
            userInfo.append(user.getFirstName() != null ? user.getFirstName() : "N/A").append(" ");
            userInfo.append(user.getMiddleName() != null ? user.getMiddleName() : "N/A").append(" ");
            userInfo.append(user.getLastName() != null ? user.getLastName() : "N/A").append(" ");

            // Append email
            userInfo.append(" - Email: ");
            userInfo.append(user.getEmail() != null ? user.getEmail() : "N/A").append(" ");

            // Append roles
            if (user.getRoles() != null && !user.getRoles().isEmpty()) {
                userInfo.append(" - Role: ");
                for (Role role : user.getRoles()) {
                    userInfo.append(role.name()).append(" ");
                }
            }
            
            outputArea.appendText(userInfo.toString().trim() + "\n");
        }
    }

    /*********
     * This is the method used for admins to add or remove a users role
     */
    private void addRemoveRole() {
        // Inform the user
        outputArea.appendText("Enter a username and select a role to add or remove.\n");

        // Create input field for the username
        Label usernameLabel = new Label("Username:");
        TextField usernameInput = new TextField();

        // Create radio buttons for roles
        ToggleGroup roleGroup = new ToggleGroup();
        RadioButton studentRadio = new RadioButton("Student");
        studentRadio.setToggleGroup(roleGroup);
        RadioButton instructorRadio = new RadioButton("Instructor");
        instructorRadio.setToggleGroup(roleGroup);
        RadioButton adminRadio = new RadioButton("Admin");
        adminRadio.setToggleGroup(roleGroup);

        // Create Add and Remove buttons
        Button addButton = new Button("Add");
        Button removeButton = new Button("Remove");
        Button backButton = new Button("Back");

        // Layout for the add/remove role form
        VBox roleBox = new VBox(10, usernameLabel, usernameInput, studentRadio, instructorRadio, adminRadio, addButton, removeButton, backButton);
        roleBox.setAlignment(Pos.CENTER);
        ((VBox) outputArea.getParent()).getChildren().add(roleBox);

        // Clear any previous options
        clearPreviousOptionBox();

        // Event handler for the Add button
        addButton.setOnAction(event -> {
            String username = usernameInput.getText();
            User user = findUserByUsername(username, userList); // Method to find user by username
            if (user == null) {
                outputArea.appendText("User not found.\n");
                return;
            }

            Role selectedRole = getSelectedRole(roleGroup);
            if (selectedRole != null) {
                if (!user.getRoles().contains(selectedRole)) {
                    user.addRole(selectedRole);
                    outputArea.appendText("Added role " + selectedRole.name() + " to user " + username + ".\n");
                } else {
                    outputArea.appendText("User " + username + " already has the " + selectedRole.name() + " role.\n");
                }
            } else {
                outputArea.appendText("Please select a role to add.\n");
            }
        });

        // Event handler for the Remove button
        removeButton.setOnAction(event -> {
            String username = usernameInput.getText();
            User user = findUserByUsername(username, userList); // Method to find user by username
            if (user == null) {
                outputArea.appendText("User not found.\n");
                return;
            }

            Role selectedRole = getSelectedRole(roleGroup);
            if (selectedRole != null) {
                if (user.getRoles().contains(selectedRole)) {
                    if (user.getRoles().size() > 1) {
                        user.removeRole(selectedRole);
                        outputArea.appendText("Removed role " + selectedRole.name() + " from user " + username + ".\n");
                    } else {
                        outputArea.appendText("User " + username + " only has one role. Cannot remove the only role.\n");
                    }
                } else {
                    outputArea.appendText("User " + username + " does not have the " + selectedRole.name() + " role.\n");
                }
            } else {
                outputArea.appendText("Please select a role to remove.\n");
            }
        });
        
        // Event handler for back button
        backButton.setOnAction(event -> {
            ((VBox) outputArea.getParent()).getChildren().remove(roleBox);  // Remove the inviteBox
            showUserOptions(Role.ADMIN);  // Show options again when going back();  
        });
    }

    /*********
     * This is the method that gets the selected role from the rolegroup
     * 
     * @param roleGroup      The toggle group for roles
     */
    private Role getSelectedRole(ToggleGroup roleGroup) {
        RadioButton selectedRadio = (RadioButton) roleGroup.getSelectedToggle();
        if (selectedRadio != null) {
            switch (selectedRadio.getText()) {
                case "Student":
                    return Role.STUDENT;
                case "Instructor":
                    return Role.INSTRUCTOR;
                case "Admin":
                    return Role.ADMIN;
            }
        }
        return null;
    }

    /*********
     * This is the method that finds the user by username
     * 
     * @param username      string username that is inputted
     * @param userList      list of type users
     */
    private User findUserByUsername(String username, List<User> userList) {
        // Implement this method to find and return a user by their username
    	for(User user : userList) {
    		if(user.getUsername().equals(username)) {
    			return user;
    		}
    	}
    	return null; 
    }
    
    /*********
     * This is the method used for admins to delete a user
     */
    private void deleteUser() {
        outputArea.appendText("Delete a user by entering the username.\n");
        TextField usernameInput = new TextField();
        Button deleteButton = new Button("Delete");
        Button backButton = new Button("Back");

        VBox deleteBox = new VBox(10, new Label("Enter username:"), usernameInput, deleteButton, backButton);
        deleteBox.setAlignment(Pos.CENTER);
        ((VBox) outputArea.getParent()).getChildren().add(deleteBox);
        
        // Hide options while deleting
        clearPreviousOptionBox();

        deleteButton.setOnAction(event -> {
            String username = usernameInput.getText();
            
            // Create an alert dialog for confirmation
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Are you sure you want to delete the user: " + username + "?");
            alert.setContentText("This action cannot be undone.");

            // Show the confirmation dialog and wait for user response
            Optional<ButtonType> result = alert.showAndWait();

            // Check if the user clicked the OK button
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Proceed with deletion
                boolean userRemoved = userList.removeIf(user -> user.getUsername().equals(username));
                if (userRemoved) {
                    outputArea.appendText("User " + username + " has been deleted.\n");
                } else {
                    outputArea.appendText("User not found.\n");
                }
            } else {
                outputArea.appendText("Deletion canceled.\n");
            }
            
            // Remove the delete box after action is completed
            ((VBox) outputArea.getParent()).getChildren().remove(deleteBox);
            showUserOptions(Role.ADMIN);  // Show options again after deletion
        });

        // Back button functionality
        backButton.setOnAction(event -> {
            ((VBox) outputArea.getParent()).getChildren().remove(deleteBox);
            showUserOptions(Role.ADMIN);  // Show options again when going back
        });
    }

    // Article setting menu:
    private void articleOptions() throws Exception {        
        
    	outputArea.appendText("Select an option\n");
        
    	clearPreviousOptionBox();  // Ensure only one options box is visible
        // Clear the optionBox before adding new options
        optionBox.getChildren().clear();
        
        // All choices
        Button listArticles = new Button("List articles");
        Button listArticlesByGroup = new Button("List articles by group");
        Button viewArticle = new Button("View an article");
        Button createArticles = new Button("Create article");
        Button updateArticle = new Button("Update article");
        Button deleteArticles = new Button("Delete article");
        Button backupArticles = new Button("Backup articles");
        Button restoreArticles = new Button("Restore articles");
        Button back = new Button("Back");
        
        // Set button actions
        listArticles.setOnAction(e -> {
			try {
				String temp = databaseHelper.listArticles();
				outputArea.appendText(temp);
			} catch (SQLException e1) {
				outputArea.appendText("error listing articles");
				e1.printStackTrace();
			}
		});
        listArticlesByGroup.setOnAction(e -> {
        	try {
				listByGroup();
			} catch (Exception e1) {
				outputArea.appendText("error calling list articles by group\n");
				e1.printStackTrace();
			}
        });
        viewArticle.setOnAction(e -> {
        	try {
				viewArticle();
			} catch (Exception e1) {
				outputArea.appendText("error calling view article");
				e1.printStackTrace();
			}
        });
        createArticles.setOnAction(e -> {
			try {
				createArticle();
			} catch (Exception e1) {
				outputArea.appendText("error creating article");
				e1.printStackTrace();
			}
		});
        updateArticle.setOnAction(e -> {
        	try {
				updateArticle();
			} catch (Exception e1) {
				outputArea.appendText("error trying to call update article");
				e1.printStackTrace();
			}
        });
        deleteArticles.setOnAction(e -> {
			try {
				deleteArticle();
			} catch (Exception e1) {
				outputArea.appendText("error trying to delete article");
				e1.printStackTrace();
			}
		});
        backupArticles.setOnAction(e -> {
			try {
				backupArticles();
			} catch (Exception e1) {
				outputArea.appendText("error trying to call backup articles\n");
				e1.printStackTrace();
			}
		});
        restoreArticles.setOnAction(e -> {
			try {
				restoreArticles();
			} catch (Exception e1) {
				outputArea.appendText("error trying to call restore articles\n");
				e1.printStackTrace();
			}
		});
        back.setOnAction(e -> {
        	//((VBox) outputArea.getParent()).getChildren().remove(deleteBox);
            showUserOptions(currRole);  // Show options again when going back
        });
        
        optionBox.getChildren().addAll(
            new Label("Select an option:"),
            listArticles,
            listArticlesByGroup,
            viewArticle,
            createArticles,
            updateArticle,
            deleteArticles,
            backupArticles,
            restoreArticles,
            back
        );
        
        optionBox.setAlignment(Pos.CENTER);
        ((VBox) outputArea.getParent()).getChildren().add(optionBox);
    }
    
    // Method used to create an article
    private void createArticle() throws Exception {
    	
    	outputArea.appendText("Enter details to create a new account.\n");

        // Create input fields for username and password
    	Label level = new Label("Enter level:");
        TextField levelInput = new TextField();
    	Label group = new Label("Enter group (comma-seperated if multiple):");
        TextField groupInput = new TextField();
    	Label title = new Label("Enter title:");
        TextField titleInput = new TextField();
        Label authors = new Label("Enter authors:");
        TextField authorsInput = new TextField();
        Label articleAbstract = new Label("Enter article abstract:");
        TextField abstractInput = new TextField();
        Label keywords = new Label("Enter keywords (comma-seperated):");
        TextField keywordsInput = new TextField();
        Label body = new Label("Enter body:");
        TextField bodyInput = new TextField();
        Label references = new Label("Enter references:");
        TextField referencesInput = new TextField();
        
        // buttons
        Button createButton = new Button("Create article");
        Button backButton = new Button("Back");
     
        // Create a VBox for input fields
        VBox createBox = new VBox(10, level, levelInput, group, groupInput, title, titleInput, authors, authorsInput,
        		articleAbstract, abstractInput,
        		keywords, keywordsInput, 
        		body, bodyInput, references, referencesInput, createButton, backButton);
        
        createBox.setAlignment(Pos.CENTER);
        ((VBox) outputArea.getParent()).getChildren().add(createBox);
        
        clearPreviousOptionBox();
        
        createButton.setOnAction(event -> {
	        char[] levelChar = levelInput.getText().toCharArray();
	        char[] groupChar = groupInput.getText().toCharArray();
	        char[] titleChar = titleInput.getText().toCharArray();
	        char[] authorsChar = authorsInput.getText().toCharArray();
	        char[] articleAbstractChar = abstractInput.getText().toCharArray();
	        char[] keywordsChar = keywordsInput.getText().toCharArray();
	        char[] bodyChar = bodyInput.getText().toCharArray();
	        char[] referencesChar = referencesInput.getText().toCharArray();
	
	        try {
				databaseHelper.createArticle(levelChar, groupChar, titleChar, authorsChar, 
						articleAbstractChar, keywordsChar, bodyChar, referencesChar);
				outputArea.appendText("Article created successfully.\n");
				((VBox) outputArea.getParent()).getChildren().remove(createBox);
				articleOptions();
			} catch (SQLException e) {
				outputArea.appendText("Article not created successfully.\n");
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        });
        
        backButton.setOnAction(event -> {
            ((VBox) outputArea.getParent()).getChildren().remove(createBox);
            try {
				articleOptions();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  // Show user options
        });
    }
    
    // Method to list articles by group
    private void listByGroup() throws Exception {
            
        // Create input fields for sequence number
    	Label group = new Label("Enter group:");
        TextField groupInput = new TextField();
        // Buttons
    	Button listButton = new Button("List articles");
    	Button backButton = new Button("Back");
    	
    	// Create a VBox for input fields
        VBox listBox = new VBox(10, group, groupInput, listButton, backButton);
        
        listBox.setAlignment(Pos.CENTER);
        ((VBox) outputArea.getParent()).getChildren().add(listBox);
        
        clearPreviousOptionBox();
        
        // When delete is pressed
        listButton.setOnAction(event -> {
        	// get num input
        	String groupStr = groupInput.getText();
        	try {
        		// call delete method
				String list = databaseHelper.listArticlesByGroup(groupStr);
				outputArea.appendText(list);
				((VBox) outputArea.getParent()).getChildren().remove(listBox);
				articleOptions();
			} catch (SQLException e) {
				outputArea.appendText("error when calling list article\n");
				e.printStackTrace();
			} catch (Exception e) {
				outputArea.appendText("error going back to article options\n");
				e.printStackTrace();
			}
        });
        
        // when back is pressed
        backButton.setOnAction(event -> {
        	((VBox) outputArea.getParent()).getChildren().remove(listBox);
            try {
            	((VBox) outputArea.getParent()).getChildren().remove(listBox);
				articleOptions();
			} catch (Exception e) {
				outputArea.appendText("Error going back\n");
				e.printStackTrace();
			}  
        });
    }
    
    // Method to view a specific article
    private void viewArticle() throws Exception {
            
        // Create input fields for article
    	Label title = new Label("Enter article title:");
        TextField titleInput = new TextField();
        Label author = new Label("Enter author:");
        TextField authorInput = new TextField();
        // Buttons
    	Button viewButton = new Button("View article");
    	Button backButton = new Button("Back");
    	
    	// Create a VBox for input fields
        VBox listBox = new VBox(10, title, titleInput, author, authorInput, viewButton, backButton);
        
        listBox.setAlignment(Pos.CENTER);
        ((VBox) outputArea.getParent()).getChildren().add(listBox);
        
        clearPreviousOptionBox();
        
        // When delete is pressed
        viewButton.setOnAction(event -> {
        	// get num input
        	String artTitle = titleInput.getText();
        	String artAuthor = authorInput.getText();
        	try {
        		// call delete method
				String details = databaseHelper.getFormattedArticle(artTitle, artAuthor);
				outputArea.appendText(details);
				((VBox) outputArea.getParent()).getChildren().remove(listBox);
				articleOptions();
			} catch (SQLException e) {
				outputArea.appendText("error when calling list article\n");
				e.printStackTrace();
			} catch (Exception e) {
				outputArea.appendText("error going back to article options\n");
				e.printStackTrace();
			}
        });
        
        // when back is pressed
        backButton.setOnAction(event -> {
        	((VBox) outputArea.getParent()).getChildren().remove(listBox);
            try {
            	((VBox) outputArea.getParent()).getChildren().remove(listBox);
				articleOptions();
			} catch (Exception e) {
				outputArea.appendText("Error going back\n");
				e.printStackTrace();
			}  
        });
    }
    
    // Method to update an article
    private void updateArticle() throws Exception {
    	
    	outputArea.appendText("Update an article\n");

        // Create input fields for sequence number
    	Label seqNum = new Label("Enter article sequence number to update:");
        TextField seqNumInput = new TextField();
        Label level = new Label("Enter level:");
        TextField levelInput = new TextField();
    	Label group = new Label("Enter group:");
        TextField groupInput = new TextField();
    	Label title = new Label("Enter title:");
        TextField titleInput = new TextField();
        Label authors = new Label("Enter authors:");
        TextField authorsInput = new TextField();
        Label articleAbstract = new Label("Enter article abstract:");
        TextField abstractInput = new TextField();
        Label keywords = new Label("Enter keywords (comma-seperated):");
        TextField keywordsInput = new TextField();
        Label body = new Label("Enter body:");
        TextField bodyInput = new TextField();
        Label references = new Label("Enter references:");
        TextField referencesInput = new TextField();
        // Buttons
    	Button updateButton = new Button("Update article");
    	Button backButton = new Button("Back");
    	
    	// Create a VBox for input fields
        VBox updateBox = new VBox(10, seqNum, seqNumInput, level, levelInput, group, 
        		groupInput, title, titleInput, authors, authorsInput, articleAbstract, 
        		abstractInput,keywords, keywordsInput, body, bodyInput, references, 
        		referencesInput, updateButton, backButton);
        
        updateBox.setAlignment(Pos.CENTER);
        ((VBox) outputArea.getParent()).getChildren().add(updateBox);
        
        clearPreviousOptionBox();
        
        // When delete is pressed
        updateButton.setOnAction(event -> {
        	// get all inputs
        	int num = Integer.parseInt(seqNumInput.getText());
        	char[] levelChar = levelInput.getText().toCharArray();
	        char[] groupChar = groupInput.getText().toCharArray();
	        char[] titleChar = titleInput.getText().toCharArray();
	        char[] authorsChar = authorsInput.getText().toCharArray();
	        char[] articleAbstractChar = abstractInput.getText().toCharArray();
	        char[] keywordsChar = keywordsInput.getText().toCharArray();
	        char[] bodyChar = bodyInput.getText().toCharArray();
	        char[] referencesChar = referencesInput.getText().toCharArray();
        	try {
        		// call delete method
				databaseHelper.updateArticle(num, levelChar, groupChar, titleChar,
						authorsChar, articleAbstractChar, keywordsChar, bodyChar, referencesChar);
				outputArea.appendText("Article updated successfully\n");
				((VBox) outputArea.getParent()).getChildren().remove(updateBox);
				articleOptions();
			} catch (SQLException e) {
				outputArea.appendText("error when calling update article\n");
				e.printStackTrace();
			} catch (Exception e) {
				outputArea.appendText("error going back to article options\n");
				e.printStackTrace();
			}
        });
        
        // when back is pressed
        backButton.setOnAction(event -> {
        	((VBox) outputArea.getParent()).getChildren().remove(updateBox);
            try {
            	((VBox) outputArea.getParent()).getChildren().remove(updateBox);
				articleOptions(); 
			} catch (Exception e) {
				outputArea.appendText("Error going back\n");
				e.printStackTrace();
			}  
        });
    }
    
    // Method deletes existing article objects from the database:
    private void deleteArticle() throws Exception {
        
    	outputArea.appendText("Delete an article\n");

        // Create input fields for sequence number
    	Label seqNum = new Label("Enter article sequence number to delete:");
        TextField seqNumInput = new TextField();
        // Buttons
    	Button deleteButton = new Button("Delete article");
    	Button backButton = new Button("Back");
    	
    	// Create a VBox for input fields
        VBox deleteBox = new VBox(10, seqNum, seqNumInput, deleteButton, backButton);
        
        deleteBox.setAlignment(Pos.CENTER);
        ((VBox) outputArea.getParent()).getChildren().add(deleteBox);
        
        clearPreviousOptionBox();
        
        // When delete is pressed
        deleteButton.setOnAction(event -> {
        	// get num input
        	int num = Integer.parseInt(seqNumInput.getText());
        	try {
        		// call delete method
				databaseHelper.deleteArticle(num);
				outputArea.appendText("Article deleted successfully\n");
				((VBox) outputArea.getParent()).getChildren().remove(deleteBox);
				articleOptions();
			} catch (SQLException e) {
				outputArea.appendText("error when calling delete article\n");
				e.printStackTrace();
			} catch (Exception e) {
				outputArea.appendText("error going back to article options\n");
				e.printStackTrace();
			}
        });
        
        // when back is pressed
        backButton.setOnAction(event -> {
        	((VBox) outputArea.getParent()).getChildren().remove(deleteBox);
            try {
            	((VBox) outputArea.getParent()).getChildren().remove(deleteBox);
				articleOptions();
			} catch (Exception e) {
				outputArea.appendText("Error going back\n");
				e.printStackTrace();
			}  
        });
    }

    // Method saves existing article objects in the database:
    private void backupArticles() throws Exception {
        
    	outputArea.appendText("Backup articles\n");
        
    	// Create input fields for file name
    	Label file = new Label("Enter backup filename: \n");
        TextField fileInput = new TextField();
        Label group = new Label("Enter group name (optional): \n");
        TextField groupInput = new TextField();
        // Buttons
    	Button backupButton = new Button("Backup articles to file");
    	Button backButton = new Button("Back");
    	
    	// Create a VBox for input fields
        VBox backupBox = new VBox(10, file, fileInput, group, groupInput, backupButton, backButton);
        
        backupBox.setAlignment(Pos.CENTER);
        ((VBox) outputArea.getParent()).getChildren().add(backupBox);
        
        clearPreviousOptionBox();
        
        backupButton.setOnAction(event -> {
        	String groupStr = groupInput.getText();
        	// If not group was specified
        	if (groupStr.isEmpty()) {
	        	try {
					databaseHelper.backupArticles(fileInput.getText()); // Operation outsourced to DatabaseHelper.java
					outputArea.appendText("Backed up articles to: " + fileInput.getText() + "\n");
					((VBox) outputArea.getParent()).getChildren().remove(backupBox);
					articleOptions();
				} catch (SQLException e) {
					outputArea.appendText("Error calling backup articles\n");
					e.printStackTrace();
				} catch (IOException e) {
					outputArea.appendText("Error gettting text\n");
					e.printStackTrace();
				} catch (Exception e) {
					outputArea.appendText("Error going back to article options\n");
					e.printStackTrace();
				} 
        	}
        	else {
        		try {
					databaseHelper.backupArticlesByGroup(fileInput.getText(), groupStr); // Operation outsourced to DatabaseHelper.java
					outputArea.appendText("Backed up articles of group: " + groupStr + " to: " + fileInput.getText() + "\n");
					((VBox) outputArea.getParent()).getChildren().remove(backupBox);
					articleOptions();
				} catch (SQLException e) {
					outputArea.appendText("Error calling backup articles\n");
					e.printStackTrace();
				} catch (IOException e) {
					outputArea.appendText("Error gettting text\n");
					e.printStackTrace();
				} catch (Exception e) {
					outputArea.appendText("Error going back to article options\n");
					e.printStackTrace();
				} 
        	}
        });
        
        backButton.setOnAction(event -> {
        	((VBox) outputArea.getParent()).getChildren().remove(backupBox);
            try {
            	((VBox) outputArea.getParent()).getChildren().remove(backupBox);
				articleOptions();
			} catch (Exception e) {
				outputArea.appendText("Error going back\n");
				e.printStackTrace();
			}
        });
    }

    // Method loads previously saved article objects from the database:
    private void restoreArticles() throws Exception {
    	outputArea.appendText("Restore articles\n");
        
    	// Create input fields for file name
    	Label file = new Label("Enter backup filename: \n");
        TextField fileInput = new TextField();
        // Buttons
    	Button backupButton = new Button("Remove existing articles");
    	Button mergeButton = new Button("Merge articles");
    	Button backButton = new Button("Back");
    	
    	// Create a VBox for input fields
        VBox restoreBox = new VBox(10, file, fileInput, backupButton, mergeButton, backButton);
        
        restoreBox.setAlignment(Pos.CENTER);
        ((VBox) outputArea.getParent()).getChildren().add(restoreBox);
        
        clearPreviousOptionBox();
        
        backupButton.setOnAction(event -> {
        	try {
        		String fileName = fileInput.getText();
        		databaseHelper.restoreArticles(fileName); // Operation outsourced to DatabaseHelper.java
				outputArea.appendText("Restored articles successfully\n");
				((VBox) outputArea.getParent()).getChildren().remove(restoreBox);
				articleOptions();
			} catch (SQLException e) {
				outputArea.appendText("Error calling restore articles\n");
				e.printStackTrace();
			} catch (IOException e) {
				outputArea.appendText("Error gettting text\n");
				e.printStackTrace();
			} catch (Exception e) {
				outputArea.appendText("Error going back to article options\n");
				e.printStackTrace();
			} 
        });
        
        mergeButton.setOnAction(event -> {
        	try {
        		String fileName = fileInput.getText();
        		databaseHelper.mergeArticles(fileName); // Operation outsourced to DatabaseHelper.java
				outputArea.appendText("Merged articles successfully\n");
				((VBox) outputArea.getParent()).getChildren().remove(restoreBox);
				articleOptions();
			} catch (SQLException e) {
				outputArea.appendText("Error calling restore articles\n");
				e.printStackTrace();
			} catch (IOException e) {
				outputArea.appendText("Error gettting text\n");
				e.printStackTrace();
			} catch (Exception e) {
				outputArea.appendText("Error going back to article options\n");
				e.printStackTrace();
			} 
        });
        
        backButton.setOnAction(event -> {
        	((VBox) outputArea.getParent()).getChildren().remove(restoreBox);
            try {
            	((VBox) outputArea.getParent()).getChildren().remove(restoreBox);
				articleOptions();
			} catch (Exception e) {
				outputArea.appendText("Error going back\n");
				e.printStackTrace();
			}
        });
    } 
    
    
    
}