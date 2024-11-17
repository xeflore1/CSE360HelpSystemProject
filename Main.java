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
    private static List<String> genericQuestions = new ArrayList<>(); // list of generic questions
    private static List<String> specificQuestions = new ArrayList<>(); // list of specific questions
    private static List<SpecialAccessGroup> specialAccessGroupsList = new ArrayList<>(); // list of special access groups



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
	    Button manageStudentsButton = new Button("Manage Students");
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
        	// Button declaration
        	Button specialAccessGroups = new Button("Special access group options");
        	
        	optionBox.getChildren().addAll(
                    new Label("Select an option:"),
                    aritcleButton,
                    specialAccessGroups,
                    manageStudentsButton,
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
            
        	manageStudentsButton.setOnAction(e -> manageStudents());
        	
        	specialAccessGroups.setOnAction(e -> {
        		try {
					specialAccessGroupOptions();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
        	});
        }
        
        
        else if (currRole == Role.STUDENT) {
            // For regular users and instructors, only show sign out and quit options
            Button sendGenericMsg = new Button("Send generic message");
            Button sendSpecificMsg = new Button("Send specific message");
            Button viewArticles = new Button("View articles");
        	optionBox.getChildren().addAll(
                new Label("Select an option:"),
                sendGenericMsg,
                sendSpecificMsg,
                viewArticles,
                signOutButton,
                quitButton
            );
        	sendGenericMsg.setOnAction( e -> {
        		sendGenericMessage();
        	});
        	sendSpecificMsg.setOnAction( e -> {
        		sendSpecificMessage();
        	});
        	viewArticles.setOnAction(( e-> {
        		try {
        			studentListArticles();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
        	}));
        	
        }

        // Set sign out, quit and manage student button actions
        signOutButton.setOnAction(e -> signOut());
	    //manageStudentsButton.setOnAction(e -> manageStudents());
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
    
    // gets user given their username and first name
    private User getUser(String name, String username) {
    	for (User user : userList) {
    		if (user.getFirstName().equals(name) && user.getUsername().equals(username)) {
    			return user;
    		}
    	}
    	return null;
    }
    
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

    /*********
     * This is the method used for instructors to manage students
     */
    private void manageStudents() {
        outputArea.appendText("Manage Students:\n");
        // Buttons for different student management actions
        Button addSpecialAccessButton = new Button("Add Student to Special Access Group");
        Button listStudentsButton = new Button("List Students and Their Access to Groups");
        Button removeSpecialAccessButton = new Button("Remove Student's Special Access to Group");
        Button deleteStudentButton = new Button("Delete Student From Help System");
        Button backButton = new Button("Back");
        VBox manageBox = new VBox(10, addSpecialAccessButton, listStudentsButton, removeSpecialAccessButton, deleteStudentButton, backButton);
        manageBox.setAlignment(Pos.CENTER);
        ((VBox) outputArea.getParent()).getChildren().add(manageBox);
        // Hide options while managing students
        clearPreviousOptionBox();
        // Add to Special Access Group functionality
        addSpecialAccessButton.setOnAction(event -> {
            TextField usernameInput = new TextField();
            TextField groupInput = new TextField();
            Button confirmAddGroupButton = new Button("Confirm Add to Group");
            VBox addBox = new VBox(10, new Label("Enter student username:"), usernameInput,
                    new Label("Enter special access group:"), groupInput, confirmAddGroupButton, backButton);
            addBox.setAlignment(Pos.CENTER);
            ((VBox) outputArea.getParent()).getChildren().remove(manageBox);
            ((VBox) outputArea.getParent()).getChildren().add(addBox);
            confirmAddGroupButton.setOnAction(addEvent -> {
                String username = usernameInput.getText();
                String group = groupInput.getText();
                // Find student and add to special access group
                User user = userList.stream().filter(u -> u.getUsername().equals(username) && u.hasRole(Role.STUDENT)).findFirst().orElse(null);
                if (user != null) {
                    user.addSpecialAccessGroup(group); // Assuming User class has this method
                    outputArea.appendText("Student " + username + " added to group " + group + ".\n");
                } else {
                    outputArea.appendText("Student not found.\n");
                }
                ((VBox) outputArea.getParent()).getChildren().remove(addBox);
                manageStudents();
            });
        });
        // List Students and Groups functionality
        listStudentsButton.setOnAction(event -> {
            outputArea.appendText("List of Students and their Groups:\n");
            userList.stream()
                    .filter(user -> user.hasRole(Role.STUDENT))
                    .forEach(user -> outputArea.appendText("- " + user.getUsername() + ": " + user.getSpecialAccessGroups() + "\n")); // Assuming User class has getSpecialAccessGroups()
        });
        // Remove Special Access Group functionality
        removeSpecialAccessButton.setOnAction(event -> {
            TextField usernameInput = new TextField();
            TextField groupInput = new TextField();
            Button confirmRemoveGroupButton = new Button("Confirm Remove from Group");
            VBox removeBox = new VBox(10, new Label("Enter student username:"), usernameInput,
                    new Label("Enter special access group to remove:"), groupInput, confirmRemoveGroupButton, backButton);
            removeBox.setAlignment(Pos.CENTER);
            ((VBox) outputArea.getParent()).getChildren().remove(manageBox);
            ((VBox) outputArea.getParent()).getChildren().add(removeBox);
            confirmRemoveGroupButton.setOnAction(removeEvent -> {
                String username = usernameInput.getText();
                String group = groupInput.getText();
                // Find student and remove from special access group
                User user = userList.stream().filter(u -> u.getUsername().equals(username) && u.hasRole(Role.STUDENT)).findFirst().orElse(null);
                if (user != null && user.removeSpecialAccessGroup(group) == true) { // Assuming User class has removeSpecialAccessGroup()
                    outputArea.appendText("Special access group " + group + " removed from student " + username + ".\n");
                } else {
                    outputArea.appendText("Student not found or group not assigned.\n");
                }
                ((VBox) outputArea.getParent()).getChildren().remove(removeBox);
                manageStudents();
            });
        });
        // Delete Student functionality
        deleteStudentButton.setOnAction(event -> {
            TextField usernameInput = new TextField();
            Button confirmDeleteButton = new Button("Confirm Delete");
            VBox deleteBox = new VBox(10, new Label("Enter student username to delete:"), usernameInput, confirmDeleteButton, backButton);
            deleteBox.setAlignment(Pos.CENTER);
            ((VBox) outputArea.getParent()).getChildren().remove(manageBox);
            ((VBox) outputArea.getParent()).getChildren().add(deleteBox);
            confirmDeleteButton.setOnAction(deleteEvent -> {
                String username = usernameInput.getText();
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmation");
                alert.setHeaderText("Are you sure you want to delete the student: " + username + "?");
                alert.setContentText("This action cannot be undone.");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    boolean userRemoved = userList.removeIf(user -> user.getUsername().equals(username) && user.hasRole(Role.STUDENT));
                    if (userRemoved) {
                        outputArea.appendText("Student " + username + " has been deleted.\n");
                    } else {
                        outputArea.appendText("User not found or the user is not a student.\n");
                    }
                } else {
                    outputArea.appendText("Deletion canceled.\n");
                }
                ((VBox) outputArea.getParent()).getChildren().remove(deleteBox);
                manageStudents();
            });
        });
        // Back button functionality
        backButton.setOnAction(event -> {
            ((VBox) outputArea.getParent()).getChildren().removeIf(node -> node != outputArea);
            showUserOptions(Role.INSTRUCTOR);
        });
    }

    /*********
     * This is the method used to display the menu options for article settings
     * Exception handling takes care of any database errors
     */
    private void articleOptions() throws Exception {        
        
    	outputArea.appendText("Select an option\n");
        
    	clearPreviousOptionBox();  // Ensure only one options box is visible
        // Clear the optionBox before adding new options
        optionBox.getChildren().clear();
        
        // All choices for article settings
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
    
    /*********
     * This is the method used to create an article
     * Exception handling takes care of any database errors
     */
    private void createArticle() throws Exception {
    	
    	outputArea.appendText("Enter details to create a new account.\n");

        // Create input fields for article details
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

	// set action for create
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

	// set action for back
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
    
    /*********
     * This is the method used to list articles by group.
     * Exception handling takes care of any database errors
     */
    // FIXME: ENWNEWNENWENWNEWNENWENWENWENWNENWENWENWNE

    private void listByGroup() throws Exception {
            
        // Create input fields for sequence number
    	Label group = new Label("Enter group (or all):");
        TextField groupInput = new TextField();
        // Buttons
    	Button listButton = new Button("List articles");
    	Button backButton = new Button("Back");
    	
    	// Create a VBox for input fields
        VBox listBox = new VBox(10, group, groupInput, listButton, backButton);
        
        listBox.setAlignment(Pos.CENTER);
        ((VBox) outputArea.getParent()).getChildren().add(listBox);
        
        clearPreviousOptionBox();
        
        // When list by group is pressed
        listButton.setOnAction(event -> {
        	// get num input
        	String groupStr = groupInput.getText();
        	try {
        		// call list by group method
				String list = databaseHelper.listArticlesByGroup(groupStr);
				outputArea.appendText(list);
				((VBox) outputArea.getParent()).getChildren().remove(listBox);
				showUserOptions(currRole);
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
				showUserOptions(currRole);
			} catch (Exception e) {
				outputArea.appendText("Error going back\n");
				e.printStackTrace();
			}  
        });
    }

    /*********
     * This is the method used to list articles by level.
     * Exception handling takes care of any database errors
     */
    // FIXME: ENWNEWNENWENWNEWNENWENWENWENWNENWENWENWNE

    private void listByLevel() throws Exception {
        
        // Create input fields for sequence number
    	Label level = new Label("Enter level (beginner, intermediate, advanced, expert, all):");
        TextField levelInput = new TextField();
        // Buttons
    	Button listButton = new Button("List articles");
    	Button backButton = new Button("Back");
    	
    	// Create a VBox for input fields
        VBox listBox = new VBox(10, level, levelInput, listButton, backButton);
        
        listBox.setAlignment(Pos.CENTER);
        ((VBox) outputArea.getParent()).getChildren().add(listBox);
        
        clearPreviousOptionBox();
        
        // When list by group is pressed
        listButton.setOnAction(event -> {
        	// get num input
        	String levelStr = levelInput.getText();
        	try {
        		// call list by group method
				String list = databaseHelper.listArticlesByLevel(levelStr);
				outputArea.appendText(list);
				((VBox) outputArea.getParent()).getChildren().remove(listBox);
				showUserOptions(currRole);
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
				showUserOptions(currRole);
			} catch (Exception e) {
				outputArea.appendText("Error going back\n");
				e.printStackTrace();
			}  
        });
    }
    
    /*********
     * This is the method used to list articles by Unique Long Id.
     * Exception handling takes care of any database errors
     */
    // FIXME: ENWNEWNENWENWNEWNENWENWENWENWNENWENWENWNE

    private void listByUniqueLongId() throws Exception {
        
        // Create input fields for sequence number
    	Label id = new Label("Enter unique long id:");
        TextField idInput = new TextField();
        // Buttons
    	Button listButton = new Button("List article");
    	Button backButton = new Button("Back");
    	
    	// Create a VBox for input fields
        VBox listBox = new VBox(10, id, idInput, listButton, backButton);
        
        listBox.setAlignment(Pos.CENTER);
        ((VBox) outputArea.getParent()).getChildren().add(listBox);
        
        clearPreviousOptionBox();
        
        // When list by group is pressed
        listButton.setOnAction(event -> {
        	// get num input
            long idLong = Long.parseLong(idInput.getText());
        	try {
        		// call list by group method
				String list = databaseHelper.listArticlesByUniqueLongId(idLong);
				outputArea.appendText(list);
				((VBox) outputArea.getParent()).getChildren().remove(listBox);
				showUserOptions(currRole);
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
				showUserOptions(currRole);
			} catch (Exception e) {
				outputArea.appendText("Error going back\n");
				e.printStackTrace();
			}  
        });
    }
    
    /*********
     * This is the method used to list articles by level and group.
     * Exception handling takes care of any database errors
     */
    // FIXME: ENWNEWNENWENWNEWNENWENWENWENWNENWENWENWNE

    private void listByLevelAndGroup() throws Exception {
        
        // Create input fields for sequence number
    	Label level = new Label("Enter level (beginner, intermediate, advanced, expert, all):");
        TextField levelInput = new TextField();
        Label group = new Label("Enter group (or all):");
        TextField groupInput = new TextField();
        // Buttons
    	Button listButton = new Button("List articles");
    	Button backButton = new Button("Back");
    	
    	// Create a VBox for input fields
        VBox listBox = new VBox(10, level, levelInput, group, groupInput, listButton, backButton);
        
        listBox.setAlignment(Pos.CENTER);
        ((VBox) outputArea.getParent()).getChildren().add(listBox);
        
        clearPreviousOptionBox();
        
        // When list by group is pressed
        listButton.setOnAction(event -> {
        	// get num input
        	String levelStr = levelInput.getText();
        	String groupStr = groupInput.getText();
        	try {
        		// call list by group method
				String list = databaseHelper.listArticlesByLevelAndGroup(levelStr, groupStr);
				outputArea.appendText(list);
				((VBox) outputArea.getParent()).getChildren().remove(listBox);
				showUserOptions(currRole);
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
				showUserOptions(currRole);
			} catch (Exception e) {
				outputArea.appendText("Error going back\n");
				e.printStackTrace();
			}  
        });
    }

    /*********
     * This is the method used to list an article in detail by sequence number.
     * Exception handling takes care of any database errors
     */
    // FIXME: ENWNEWNENWENWNEWNENWENWENWENWNENWENWENWNE

    private void listBySeqNum() throws Exception {
        
        // Create input fields for sequence number
    	Label seqNum = new Label("Enter sequence number:");
        TextField seqNumInput = new TextField();
        // Buttons
    	Button listButton = new Button("List article");
    	Button backButton = new Button("Back");
    	
    	// Create a VBox for input fields
        VBox listBox = new VBox(10, seqNum, seqNumInput, listButton, backButton);
        
        listBox.setAlignment(Pos.CENTER);
        ((VBox) outputArea.getParent()).getChildren().add(listBox);
        
        clearPreviousOptionBox();
        
        // When list by group is pressed
        listButton.setOnAction(event -> {
        	// get num input
            int seqNumId = Integer.parseInt(seqNumInput.getText());
        	try {
        		// call list by group method
				String list = databaseHelper.getFormattedArticleWithSeq(seqNumId);
				outputArea.appendText(list);
				((VBox) outputArea.getParent()).getChildren().remove(listBox);
				showUserOptions(currRole);
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
				showUserOptions(currRole);
			} catch (Exception e) {
				outputArea.appendText("Error going back\n");
				e.printStackTrace();
			}  
        });
    }
    /*********
     * This is the method used to view an article
     * Exception handling takes care of any database errors
     */
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
        
        // When view is pressed
        viewButton.setOnAction(event -> {
        	// get num input
        	String artTitle = titleInput.getText();
        	String artAuthor = authorInput.getText();
        	try {
        		// call view method
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
    
    /*********
     * This is the method used to update articles
     * Exception handling takes care of any database errors
     */
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
        
        // When update is pressed
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
        		// call update method
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
    
    /*********
     * This is the method used to delete articles from the database
     * Exception handling takes care of any database errors
     */
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

    /*********
     * This is the method used to back up articles in database
     * Exception handling takes care of any database errors
     */
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

    /*********
     * This is the method used to restore articles
     * Exception handling takes care of any database errors
     */
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

	// action button for merge    
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

	// action button for merge
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

	// action button for back
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
    
    // method for sending a generic message 
    // FIXME: ENWNEWNENWENWNEWNENWENWENWENWNENWENWENWNE

    private void sendGenericMessage() {
        // Label to prompt the user
        Label promptLabel = new Label("Enter your question: ");
        
        // Text field for user input
        TextField questionInput = new TextField();
        
        // Button to submit the question
        Button submitButton = new Button("Submit");
        Button backButton = new Button("Back");
        
        // VBox layout for the form
        VBox messageBox = new VBox(10, promptLabel, questionInput, submitButton, backButton);
        messageBox.setAlignment(Pos.CENTER);

        // Add the form to the main output area (or container)
        ((VBox) outputArea.getParent()).getChildren().add(messageBox);

        // Clear any previous option boxes to show only the current one
        clearPreviousOptionBox();
        
        // Set action for submit button
        submitButton.setOnAction(event -> {
            String question = questionInput.getText().trim();
            
            if (!question.isEmpty()) {
                // Append the question to the output area and the list
                outputArea.appendText("Entered question: " + question + "\n");
                genericQuestions.add(question);
                for (String question1 : genericQuestions) {
                    System.out.println("Question: " + question1);
                }

                
                // Clear the input box after submission
                questionInput.clear();
            } else {
                outputArea.appendText("Please enter a valid question.\n");
            }
        });
        backButton.setOnAction(event -> {
            ((VBox) outputArea.getParent()).getChildren().remove(messageBox);
            showUserOptions(currRole);  // Show options again when going back
        });
    }
    
    // method for sending a specific message
    // FIXME: ENWNEWNENWENWNEWNENWENWENWENWNENWENWENWNE
    private void sendSpecificMessage() {
        // Label to prompt the user
        Label promptLabel = new Label("Enter unavailable info: ");
        
        // Text field for user input
        TextField questionInput = new TextField();
        
        // Button to submit the question
        Button submitButton = new Button("Submit");
        Button backButton = new Button("Back");
        
        // VBox layout for the form
        VBox messageBox = new VBox(10, promptLabel, questionInput, submitButton, backButton);
        messageBox.setAlignment(Pos.CENTER);

        // Add the form to the main output area (or container)
        ((VBox) outputArea.getParent()).getChildren().add(messageBox);

        // Clear any previous option boxes to show only the current one
        clearPreviousOptionBox();
        
        // Set action for submit button
        submitButton.setOnAction(event -> {
            String question = questionInput.getText().trim();
            
            if (!question.isEmpty()) {
                // Append the question to the output area and the list
                outputArea.appendText("Entered info: " + question + "\n");
                specificQuestions.add(question);
                for (String question1 : specificQuestions) {
                    System.out.println("Question: " + question1);
                }

                
                // Clear the input box after submission
                questionInput.clear();
            } else {
                outputArea.appendText("Please enter a valid question.\n");
            }
        });
        backButton.setOnAction(event -> {
            ((VBox) outputArea.getParent()).getChildren().remove(messageBox);
            showUserOptions(currRole);  // Show options again when going back
        });
    }

    // article list function for students
    // FIXME: ENWNEWNENWENWNEWNENWENWENWENWNENWENWENWNE
    private void studentListArticles() {
        // Labels and input fields for each search criterion
        Label title = new Label("Select an option ");
        
        // Buttons
        Button listByLevel = new Button("List articles by level");
        Button listByGroup = new Button("List articles by group");
        Button listById = new Button("List articles by unique long identifier");
        Button listByLevelAndGroup = new Button("List articles by level and group");
        Button viewWithSeqNum = new Button("View article in detail using sequence number");

        Button backButton = new Button("Back");

        // Create a VBox for input fields and buttons
        VBox listBox = new VBox(10, title, listByLevel, listByGroup, listById, listByLevelAndGroup, viewWithSeqNum, backButton);
        listBox.setAlignment(Pos.CENTER);
        ((VBox) outputArea.getParent()).getChildren().add(listBox);

        clearPreviousOptionBox();

        // When list button is pressed
        listByLevel.setOnAction(event -> {
        	try {
        		((VBox) outputArea.getParent()).getChildren().remove(listBox);
				listByLevel();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        });

		listByGroup.setOnAction(event -> {
			try {
				((VBox) outputArea.getParent()).getChildren().remove(listBox);
				listByGroup();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		listById.setOnAction(event -> {
			try {
				((VBox) outputArea.getParent()).getChildren().remove(listBox);
				listByUniqueLongId();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		listByLevelAndGroup.setOnAction(event -> {
			try {
				((VBox) outputArea.getParent()).getChildren().remove(listBox);
				listByLevelAndGroup();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		viewWithSeqNum.setOnAction(event -> {
			try {
				((VBox) outputArea.getParent()).getChildren().remove(listBox);
				listBySeqNum();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
        // When back button is pressed
        backButton.setOnAction(event -> {
            ((VBox) outputArea.getParent()).getChildren().remove(listBox);
            try {
                articleOptions();
            } catch (Exception e) {
                outputArea.appendText("Error going back\n");
                e.printStackTrace();
            }
        });
    }
    
    // options list for special access groups
    // FIXME: ENWNEWNENWENWNEWNENWENWENWENWNENWENWENWNE

    private void specialAccessGroupOptions() throws Exception {
    	outputArea.appendText("Select an option\n");
        
    	clearPreviousOptionBox();  // Ensure only one options box is visible
        // Clear the optionBox before adding new options
        optionBox.getChildren().clear();
        
        // All choices for article settings
        Button createGroup = new Button("Create Special Access Group");
        Button viewGroup = new Button("View Special Access Group");
        Button editGroup = new Button("Edit a Special Access Group");
        Button articeGroup = new Button("Add article to a Special Access Group");
        Button deleteGroup = new Button("Delete Special Access Group");
        Button backupGroup = new Button("Backup Special Access Group");
        Button restoreGroup = new Button("Restore Special Access Group");
        Button back = new Button("Back");
        
        // Set button actions
        createGroup.setOnAction(e -> {
			createSpecialAccessGroup();
		});
        viewGroup.setOnAction(e -> {
        	viewSpecialAccessGroup();
		});
        editGroup.setOnAction(e -> {
        	editSpecialAccessGroup();
		});
        articeGroup.setOnAction(e -> {
        	addArticleToSpecialAccessGroup();
        });
		deleteGroup.setOnAction(e -> {
			removeArticleFromSpecialAccessGroup();
		});
       
        back.setOnAction(e -> {
        	//((VBox) outputArea.getParent()).getChildren().remove(deleteBox);
            showUserOptions(currRole);  // Show options again when going back
        });
        
        if (currRole == Role.INSTRUCTOR) {
	        optionBox.getChildren().addAll(
	            new Label("Select an option:"),
	            createGroup,
	            viewGroup,
	            editGroup,
	            articeGroup,
	            deleteGroup,
	            back
	        );
        }
        else if (currRole == Role.ADMIN) {
	        optionBox.getChildren().addAll(
	            new Label("Select an option:"),
	            editGroup,
	            back
	        );
        }
        
        optionBox.setAlignment(Pos.CENTER);
        ((VBox) outputArea.getParent()).getChildren().add(optionBox);
    }
    
    // method to create a special access group
    // FIXME: ENWNEWNENWENWENWENWNEWNENWENW
    private void createSpecialAccessGroup() {
    	outputArea.appendText("Enter name for new Special Access Group\n");
    	
    	clearPreviousOptionBox();  // Ensure only one options box is visible
        // Clear the optionBox before adding new options
        optionBox.getChildren().clear();
        // Labels
        Label name = new Label("Name");
        TextField nameInput = new TextField();
        // Buttons 
        Button createGroup = new Button("Create");
        Button back = new Button("Back");
        
        createGroup.setOnAction(e -> {
        	String groupName = nameInput.getText();
        	nameInput.clear();
        	// create newGroup
        	SpecialAccessGroup newGroup = new SpecialAccessGroup(groupName);
        	// first instructor to be added, give viewing and admin privliges 
        	if (currRole == Role.INSTRUCTOR) {
        		newGroup.addToInstrWithAccess(currentUser);
        		newGroup.addToInstrWithAdminRights(currentUser);
        	}
        	// add to list
        	specialAccessGroupsList.add(newGroup);
        	/*for (SpecialAccessGroup p : specialAccessGroupsList) {
        		if (p.getGroupName().equals(newGroup.getGroupName())) {
                	outputArea.appendText("Group: \"" + groupName + "\" successfully created\n");
        		}
        	}   */     	
        	outputArea.appendText("Group: \"" + groupName + "\" successfully created\n");

        });
        back.setOnAction(e -> {
        	try {
				specialAccessGroupOptions();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}  // Show options again when going back
        });
        
        optionBox.getChildren().addAll(
        	name, 
        	nameInput, 
        	createGroup, 
        	back
        );
        optionBox.setAlignment(Pos.CENTER);
        ((VBox) outputArea.getParent()).getChildren().add(optionBox);  
    }
    // method to view a special access group
    // FIXME: ENWNEWNENWENWENWENWNEWNENWENW finish actual viewing stuff
    private void viewSpecialAccessGroup() {
    	outputArea.appendText("Enter name group name to be viewed\n");
    	
    	clearPreviousOptionBox();  // Ensure only one options box is visible
        // Clear the optionBox before adding new options
        optionBox.getChildren().clear();
        // Labels
        Label name = new Label("Name");
        TextField nameInput = new TextField();
        // Buttons 
        Button viewAllButton = new Button("View All");
        Button viewAllInAdminsButton = new Button("View all in Admins list");
        Button viewAllInInstrWithAccess = new Button("View all in instructors with article access list");
        Button viewAllInInstrWithAdmin = new Button("View all in instructors with admin privileges list");
        Button viewAllInStudents  = new Button("View all in students list");
        Button back = new Button("Back");  
        
        // view all 
        viewAllButton.setOnAction(e -> {
        	// retrieve group
        	String groupName = nameInput.getText();
            SpecialAccessGroup group = null;
            // search group list for group
        	for (SpecialAccessGroup i : specialAccessGroupsList) {
        		if (i.getGroupName().equals(groupName)) {
                	group = i;
          		}
        	}  
        	// if group exists
        	if (group != null) {
	        	// retrieve all lists
	        	List<User> admins = group.getAdmins();
	        	List<User> instrWithAccess = group.getInstructorsWithAccess();
	        	List<User> instrWithAdmin = group.getInstructorsWithAdminRights();
	        	List<User> students = group.getStudentsWithAccess();
            	outputArea.appendText("\n*********************************************\n");
	        	outputArea.appendText("All users in : \"" + group.getGroupName() + "\"\n");
            	outputArea.appendText("\nUsers in Admin list:\n");
	        	for (User u : admins) {
	            	outputArea.appendText("Name: " + u.getFirstName() + " Username: " + u.getUsername() + "\n");
	        	} 
	        	outputArea.appendText("\nUsers in Instructors with access list: \n");
	        	for (User u : instrWithAccess) {
	            	outputArea.appendText("Name: " + u.getFirstName() + " Username: " + u.getUsername() + "\n");
	        	} 
	        	outputArea.appendText("\nUsers in Instructors with admin privileges list: \n");
	        	for (User u : instrWithAdmin) {
	            	outputArea.appendText("Name: " + u.getFirstName() + " Username: " + u.getUsername() + "\n");
	        	} 
	        	outputArea.appendText("\nUsers in Students list: \n");
	        	for (User u : students) {
	            	outputArea.appendText("Name: " + u.getFirstName() + " Username: " + u.getUsername() + "\n");
	        	} 
            	outputArea.appendText("\n*********************************************\n");
	        	nameInput.clear();
        	}
        	else {
        		outputArea.appendText("Invalid group\n");
        	}
        });
       
        // view all in admins
        viewAllInAdminsButton.setOnAction(e -> {
        	// retrieve group
        	String groupName = nameInput.getText();
            SpecialAccessGroup group = null;
            // search group list for group
        	for (SpecialAccessGroup i : specialAccessGroupsList) {
        		if (i.getGroupName().equals(groupName)) {
                	group = i;
          		}
        	}  
        	// if group exists
        	if (group != null) {
	        	// retrieve all lists
	        	List<User> admins = group.getAdmins();
            	outputArea.appendText("\n*********************************************\n");
            	outputArea.appendText("All admins in group: \"" + group.getGroupName() + "\"\n");
	        	for (User u : admins) {
	            	outputArea.appendText("Name: " + u.getFirstName() + " Username: " + u.getUsername() + "\n");
	        	} 
            	outputArea.appendText("*********************************************\n");
	        	nameInput.clear();
        	}
        	else {
        		outputArea.appendText("Invalid group\n");
        	}
        });

        // view all in instructors with access
        viewAllInInstrWithAccess.setOnAction(e -> {
        	// retrieve group
        	String groupName = nameInput.getText();
            SpecialAccessGroup group = null;
            // search group list for group
        	for (SpecialAccessGroup i : specialAccessGroupsList) {
        		if (i.getGroupName().equals(groupName)) {
                	group = i;
          		}
        	}  
        	// if group exists
        	if (group != null) {
	        	// retrieve all lists
	        	List<User> instrWithAccess = group.getInstructorsWithAccess();
            	outputArea.appendText("\n*********************************************\n");
	        	outputArea.appendText("All instructors with article access in group: \"" + group.getGroupName() + "\"\n");
	        	for (User u : instrWithAccess) {
	            	outputArea.appendText("Name: " + u.getFirstName() + " Username: " + u.getUsername() + "\n");
	        	} 
            	outputArea.appendText("*********************************************\n");
	        	nameInput.clear();
        	}
        	else {
        		outputArea.appendText("Invalid group\n");
        	}
        });
        
        // view all in instructors with admin priv.
        viewAllInInstrWithAdmin.setOnAction(e -> {
        	// retrieve group
        	String groupName = nameInput.getText();
            SpecialAccessGroup group = null;
            // search group list for group
        	for (SpecialAccessGroup i : specialAccessGroupsList) {
        		if (i.getGroupName().equals(groupName)) {
                	group = i;
          		}
        	}  
        	// if group exists
        	if (group != null) {
	        	// retrieve all lists
	        	List<User> instrWithAdmin = group.getInstructorsWithAdminRights();
            	outputArea.appendText("\n*********************************************\n");
	        	outputArea.appendText("All instructors with admin privileges in group: \"" + group.getGroupName() + "\"\n");
	        	for (User u : instrWithAdmin) {
	            	outputArea.appendText("Name: " + u.getFirstName() + " Username: " + u.getUsername() + "\n");
	        	} 
            	outputArea.appendText("*********************************************\n");
	        	nameInput.clear();
        	}
        	else {
        		outputArea.appendText("Invalid group\n");
        	}
        });
       
        // view all in students
        viewAllInStudents.setOnAction(e -> {
        	// retrieve group
        	String groupName = nameInput.getText();
            SpecialAccessGroup group = null;
            // search group list for group
        	for (SpecialAccessGroup i : specialAccessGroupsList) {
        		if (i.getGroupName().equals(groupName)) {
                	group = i;
          		}
        	}  
        	// if group exists
        	if (group != null) {
	        	// retrieve all lists
	        	List<User> students = group.getStudentsWithAccess();
            	outputArea.appendText("\n*********************************************\n");
	        	outputArea.appendText("All students in group: \"" + group.getGroupName() + "\"\n");
	        	for (User u : students) {
	            	outputArea.appendText("Name: " + u.getFirstName() + " Username: " + u.getUsername() + "\n");
	        	} 
            	outputArea.appendText("*********************************************\n");
	        	nameInput.clear();
        	}
        	else {
        		outputArea.appendText("Invalid group\n");
        	}
        });
       
        // back
        back.setOnAction(e -> {
        	try {
				specialAccessGroupOptions();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}  // Show options again when going back
        });
        
        optionBox.getChildren().addAll(
        	name, 
        	nameInput, 
        	viewAllButton, 
        	viewAllInAdminsButton,
        	viewAllInInstrWithAccess,
        	viewAllInInstrWithAdmin,
        	viewAllInStudents,
        	back
        );
        optionBox.setAlignment(Pos.CENTER);
        ((VBox) outputArea.getParent()).getChildren().add(optionBox);  
    }
    // method to edit a special access group
    // FIXME: ENWNEWNENWENWENWENWNEWNENWENW
    private void editSpecialAccessGroup() {
        outputArea.appendText("Edit Special Access Group Permissions\n");

        clearPreviousOptionBox(); // Ensure only one options box is visible
        // Clear the optionBox before adding new options
        optionBox.getChildren().clear();

        // Labels and Inputs
        Label name = new Label("Group Name:");
        TextField groupNameInput = new TextField();
        Label details = new Label("Details of user to be added/removed: ");
        Label firstNameLabel = new Label("First Name:");
        TextField firstNameInput = new TextField();
        Label usernameLabel = new Label("Username:");
        TextField usernameInput = new TextField();

        // Radio Buttons for selecting the list
        ToggleGroup listToggleGroup = new ToggleGroup();
        RadioButton adminListButton = new RadioButton("Admin List");
        adminListButton.setToggleGroup(listToggleGroup);
        RadioButton instructorsAccessButton = new RadioButton("Instructors with Article Access");
        instructorsAccessButton.setToggleGroup(listToggleGroup);
        RadioButton instructorsAdminButton = new RadioButton("Instructors with Admin Privileges");
        instructorsAdminButton.setToggleGroup(listToggleGroup);
        RadioButton studentListButton = new RadioButton("Student List");
        studentListButton.setToggleGroup(listToggleGroup);

        // Buttons
        Button addUser = new Button("Add User");
        Button removeUser = new Button("Remove User");
        Button viewUsers = new Button("View Users");
        Button back = new Button("Back");

        addUser.setOnAction(e -> {
            // retrieve group
        	String groupName = groupNameInput.getText();
        	// represent the given group
        	SpecialAccessGroup group = null;
        	for (SpecialAccessGroup i : specialAccessGroupsList) {
        		// group is found
        		if (i.getGroupName().equals(groupName)) {
        			group = i;
        			break;
        		}
        	} 
        	// if group exists
        	if (group != null) {
        		// if user has correct permissions
	        	if (group.doesAdminExist(currentUser) || group.doesInstrExistInAdminRightsList(currentUser)) {
		        	// retrieve user details
	        		String firstName = firstNameInput.getText();
		        	String username = usernameInput.getText();
		        	User user = getUser(firstName, username);
		        	String permissions = "";
		        	// check if user exists
		        	if (user != null) {
		        		String selectedGroup = "";
			            if (listToggleGroup.getSelectedToggle() == adminListButton && (user.hasRole(Role.ADMIN))) {
			            	permissions = "Admins";
			            	// logic to add user to admins, first clear inputs
			                group.addToAdmins(user);
			                groupNameInput.clear();
			                firstNameInput.clear();
			                usernameInput.clear();
			                listToggleGroup.selectToggle(null);
			            } else if (listToggleGroup.getSelectedToggle() == instructorsAccessButton && (user.hasRole(Role.INSTRUCTOR))) {
			            	permissions = "Instructors with article access";
			            	// logic to add user to instructorsWithAccess
			            	group.addToInstrWithAccess(user);
			            	groupNameInput.clear();
			                firstNameInput.clear();
			                usernameInput.clear();
			                listToggleGroup.selectToggle(null);
			            } else if (listToggleGroup.getSelectedToggle() == instructorsAdminButton && (user.hasRole(Role.INSTRUCTOR))) {
			            	permissions = "Instructors with admin permissions";
			            	// logic to add user to instructorsWithAdminRights
			            	group.addToInstrWithAdminRights(user);
			            	groupNameInput.clear();
			                firstNameInput.clear();
			                usernameInput.clear();
			                listToggleGroup.selectToggle(null);
			            } else if (listToggleGroup.getSelectedToggle() == studentListButton && (user.hasRole(Role.STUDENT))) {
			            	permissions = "Students";
			            	// Add logic to add user to studentsWithViewingRights
			            	group.addToStudentList(user);
			            	groupNameInput.clear();
			                firstNameInput.clear();
			                usernameInput.clear();
			                listToggleGroup.selectToggle(null);
			            } else {
			                outputArea.appendText("Invalid credentials.\n");
			                return;
			            }
			            outputArea.appendText("User: \"" + username + "\" added to special access group: \"" + groupName +
			            		"\" with permissions: \"" + permissions + "\".\n");
		        	}
		        	else {
		        		outputArea.appendText("Invalid user\n");
		        	}
	            }
	            else {
	            	outputArea.appendText("Request denied\n");
	            }
        	}
        	else {
            	outputArea.appendText("Invalid group\n");
        	}
        });

        // delete button
        removeUser.setOnAction(e -> {
            // retrieve group
        	String groupName = groupNameInput.getText();
        	// represent the given group
        	SpecialAccessGroup group = null;
        	for (SpecialAccessGroup i : specialAccessGroupsList) {
        		// group is found
        		if (i.getGroupName().equals(groupName)) {
        			group = i;
        			break;
        		}
        	} 
        	// if group exists
        	if (group != null) {
        		// if user has correct permissions
	        	if (group.doesAdminExist(currentUser) || group.doesInstrExistInAdminRightsList(currentUser)) {
		        	// retrieve user details
	        		String firstName = firstNameInput.getText();
		        	String username = usernameInput.getText();
		        	User user = getUser(firstName, username);
		        	String permissions = "";
		        	// check if user exists
		        	if (user != null) {
		        		String selectedGroup = "";
			            if (listToggleGroup.getSelectedToggle() == adminListButton && group.doesAdminExist(user)) {
			            	permissions = "Admin group";
			            	// logic to remove user from admins, first clear inputs
			                group.removeFromAdmins(user);
			                groupNameInput.clear();
			                firstNameInput.clear();
			                usernameInput.clear();
			                outputArea.appendText("User removed from Admin List\n");
			            } else if (listToggleGroup.getSelectedToggle() == instructorsAccessButton && group.doesInstrExistInAccessList(user)) {
			            	permissions = "Instructors with article access";
			            	// logic to remove user from instructorsWithAccess
			            	group.removeFromInstrWithAccess(user);
			                groupNameInput.clear();
			                firstNameInput.clear();
			                usernameInput.clear();
			            	group.addToInstrWithAccess(user);
			                outputArea.appendText("User removed from Instructors with Article Access\n");
			            } else if (listToggleGroup.getSelectedToggle() == instructorsAdminButton && group.doesInstrExistInAdminRightsList(user)) {
			            	permissions = "Instructors with admin permissions";
			            	// logic to remove user from instructorsWithAdminRights
			            	group.removeFromInstrWithAdminRights(user);
			                groupNameInput.clear();
			                firstNameInput.clear();
			                usernameInput.clear();
			            	group.addToInstrWithAdminRights(user);
			                outputArea.appendText("User removed from Instructors with Admin Privileges\n");
			            } else if (listToggleGroup.getSelectedToggle() == studentListButton && group.doesStudentExistInStudentList(user)) {
			            	permissions = "Student list";
			            	// Add logic to remove user from studentsWithViewingRights
			            	group.removeFromStudentList(user);
			                groupNameInput.clear();
			                firstNameInput.clear();
			                usernameInput.clear();
			            	group.addToStudentList(user);
			                outputArea.appendText("User removed from Student List\n");
			            } else {
			                outputArea.appendText("Invalid credentials.\n");
			                return;
			            }
			            outputArea.appendText("User: \"" + username + "\" removed from special access group: \"" + groupName +
			            		"\" with permissions: \"" + permissions + "\".\n");
		        	}
		        	else {
		        		outputArea.appendText("Invalid user\n");
		        	}
	            }
	            else {
	            	outputArea.appendText("Request denied\n");
	            }
        	}
        	else {
            	outputArea.appendText("Invalid group\n");
        	}
        });
        back.setOnAction(e -> {
            try {
                specialAccessGroupOptions();
            } catch (Exception e1) {
                e1.printStackTrace();
            } // Show options again when going back
        });

        // Add all elements to the optionBox
        optionBox.getChildren().addAll(
            name,
            groupNameInput,
            details,
            firstNameLabel,
            firstNameInput,
            usernameLabel,
            usernameInput,
            adminListButton,
            instructorsAccessButton,
            instructorsAdminButton,
            studentListButton,
            addUser,
            removeUser,
            viewUsers,
            back
        );
        optionBox.setAlignment(Pos.CENTER);
        ((VBox) outputArea.getParent()).getChildren().add(optionBox);
    }
    
    // method to add an article to a special access group
    // FIXME: ENWNEWNENWENWENWENWNEWNENWENW
    private void addArticleToSpecialAccessGroup() {
        outputArea.appendText("Add Article to Special Access Group\n");
        
        clearPreviousOptionBox();  // Ensure only one options box is visible
        optionBox.getChildren().clear();
        
        // Input fields
        Label groupNameLabel = new Label("Group Name:");
        TextField groupNameInput = new TextField();
        
        Label seqNumberLabel = new Label("Article Sequence Number:");
        TextField seqNumberInput = new TextField();
        
        // Buttons
        Button addButton = new Button("Add Article");
        Button backButton = new Button("Back");
        
        // Set button actions
        addButton.setOnAction(e -> {
            String groupName = groupNameInput.getText();
            int seqNumberInt = Integer.parseInt(seqNumberInput.getText());
            if (groupName.isEmpty() || seqNumberInt == 0) {
                outputArea.appendText("Please provide both the group name and the sequence number.\n");
                return;
            }
            // retrieve articles unique long id
            long uniqueId = databaseHelper.getUniqueIdById(seqNumberInt);
            // find given group
            SpecialAccessGroup group = null;
            for (SpecialAccessGroup gp : specialAccessGroupsList) {
            	if (gp.getGroupName().equals(groupName)) {
            		group = gp;
            	}
            }
            // if group is valid
            if (group != null) {
            	// if article is valid
            	if (uniqueId != -1) {
            		group.addToArticles(uniqueId);
            		groupNameInput.clear();
            		seqNumberInput.clear();
            		outputArea.appendText("Article successfully added to \"" + groupName + "\".\n");
            	}
            	else {
            		outputArea.appendText("Invalid article.\n");
            	}
            }
            else {
                outputArea.appendText("Invalid group.\n");
            }
        });
        
        backButton.setOnAction(e -> {
            try {
                specialAccessGroupOptions();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        
        // Add components to the option box
        optionBox.getChildren().addAll(
            groupNameLabel,
            groupNameInput,
            seqNumberLabel,
            seqNumberInput,
            addButton,
            backButton
        );
        
        optionBox.setAlignment(Pos.CENTER);
        ((VBox) outputArea.getParent()).getChildren().add(optionBox);
    }
    
 // method to add an article to a special access group
    // FIXME: ENWNEWNENWENWENWENWNEWNENWENW
    private void removeArticleFromSpecialAccessGroup() {
        outputArea.appendText("Remove Article from Special Access Group\n");
        
        clearPreviousOptionBox();  // Ensure only one options box is visible
        optionBox.getChildren().clear();
        
        // Input fields
        Label groupNameLabel = new Label("Group Name:");
        TextField groupNameInput = new TextField();
        
        Label seqNumberLabel = new Label("Article Sequence Number:");
        TextField seqNumberInput = new TextField();
        
        // Buttons
        Button addButton = new Button("Add Article");
        Button backButton = new Button("Back");
        
        // Set button actions
        addButton.setOnAction(e -> {
            String groupName = groupNameInput.getText();
            int seqNumberInt = Integer.parseInt(seqNumberInput.getText());
            if (groupName.isEmpty() || seqNumberInt == 0) {
                outputArea.appendText("Please provide both the group name and the sequence number.\n");
                return;
            }
            // retrieve articles unique long id
            long uniqueId = databaseHelper.getUniqueIdById(seqNumberInt);
            // find given group
            SpecialAccessGroup group = null;
            for (SpecialAccessGroup gp : specialAccessGroupsList) {
            	if (gp.getGroupName().equals(groupName)) {
            		group = gp;
            	}
            }
            // if group is valid
            if (group != null) {
            	// if article is valid
            	if (uniqueId != -1) {
            		group.removeFromArticles(uniqueId);
            		List<Long> temp = group.getArticles();
            		for (Long i : temp) {
            			System.out.println(i);
            		}
            		groupNameInput.clear();
            		seqNumberInput.clear();
            		outputArea.appendText("Article successfully removed from \"" + groupName + "\".\n");
            	}
            	else {
            		outputArea.appendText("Invalid article.\n");
            	}
            }
            else {
                outputArea.appendText("Invalid group.\n");
            }
        });
        
        backButton.setOnAction(e -> {
            try {
                specialAccessGroupOptions();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        
        // Add components to the option box
        optionBox.getChildren().addAll(
            groupNameLabel,
            groupNameInput,
            seqNumberLabel,
            seqNumberInput,
            addButton,
            backButton
        );
        
        optionBox.setAlignment(Pos.CENTER);
        ((VBox) outputArea.getParent()).getChildren().add(optionBox);
    }
}