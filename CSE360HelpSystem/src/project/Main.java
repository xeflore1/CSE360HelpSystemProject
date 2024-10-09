package project;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Main extends Application {
    private static List<User> userList = new ArrayList<>();
    private Admin adminUser = null;
    private User currentUser = null;
    private TextArea outputArea = new TextArea();
    private VBox optionBox = new VBox(10);  // Reusable optionBox to prevent multiple instances

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage theStage) {
        theStage.setTitle("ASU Help System");

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
    
    // Method used to collect the rest of the user details when signing up
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
            
            // Clear the user info box
            ((VBox) outputArea.getParent()).getChildren().remove(userInfoBox);
            showUserOptions();  // Show the options for the user
        });

        // Cancel button action
        cancelDetailsButton.setOnAction(event -> {
            ((VBox) outputArea.getParent()).getChildren().remove(userInfoBox);
            showUserOptions();  // Show the options for the user
        });
    }

    // Method acts as the home page for the users
    private void showUserOptions() {
        outputArea.appendText("What would you like to do? Options:\n");

        clearPreviousOptionBox();  // Ensure only one options box is visible

        // Clear the optionBox before adding new options
        optionBox.getChildren().clear();

        // Create buttons for each user option
        Button signOutButton = new Button("Sign out");
        Button quitButton = new Button("Quit");

        // Add admin options only if the current user is an admin
        if (currentUser instanceof Admin) {
            Button printUsersButton = new Button("Print users");
            Button deleteUserButton = new Button("Delete user");
            Button inviteUserButton = new Button("Invite a user");

            // Set button actions
            printUsersButton.setOnAction(e -> listUsers());
            deleteUserButton.setOnAction(e -> deleteUser());
            inviteUserButton.setOnAction(e -> inviteUser());

            optionBox.getChildren().addAll(
                new Label("Select an option:"),
                signOutButton,
                printUsersButton,
                deleteUserButton,
                inviteUserButton,
                quitButton
            );
        } else {
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
            outputArea.appendText("Goodbye!\n");
            System.exit(0);
        });

        optionBox.setAlignment(Pos.CENTER);
        ((VBox) outputArea.getParent()).getChildren().add(optionBox);
    }

    // Sign out method
    private void signOut() {
        outputArea.appendText("You have signed out.\n");
        currentUser = null;
        showSignInOrCreateAccount();
    }

    // This page gives users the option to login or create an accounut
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

    // This method serves as the login page
    // FIXME add role selection screen 
    private void loginPrompt() {
        outputArea.appendText("Enter username and password to log in or enter an invitation code.\n");

        Label usernameLabel = new Label("Username:");
        TextField usernameInput = new TextField();
        Label passwordLabel = new Label("Password:");
        PasswordField passwordInput = new PasswordField();
        Button loginButton = new Button("Login");
        Button invitationButton = new Button("I have an invite code");
        Button backButton = new Button("Back");

        VBox loginBox = new VBox(10, usernameLabel, usernameInput, passwordLabel, passwordInput, loginButton, invitationButton, backButton);
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
                    showUserOptions();
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
        
        // Back button functionality
        backButton.setOnAction(event -> {
            ((VBox) outputArea.getParent()).getChildren().remove(loginBox);
            showSignInOrCreateAccount();  // Show the sign-in/create account options again
        });
    }

    // If a user has an invite code this screen allows them to create their accounut 
    // FIXME add roles
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
            if (inviteCode.equals("STUDENTCODE")) {
                processInviteCode(usernameInput, passwordInput, confirmPasswordInput, Role.STUDENT, inviteBox);
            } else if (inviteCode.equals("INSTRUCTORCODE")) {
                processInviteCode(usernameInput, passwordInput, confirmPasswordInput, Role.INSTRUCTOR, inviteBox);
            } else if (inviteCode.equals("ADMINCODE")) {
                processInviteCode(usernameInput, passwordInput, confirmPasswordInput, Role.ADMIN, inviteBox);
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
    
    // Method used to create a new user given the details from the invite screen
    private void processInviteCode(TextField usernameInput, PasswordField passwordInput, PasswordField confirmPasswordInput, Role role, VBox createBox) {
        String username = usernameInput.getText();
        char[] password = passwordInput.getText().toCharArray();
        char[] confirmPassword = confirmPasswordInput.getText().toCharArray();

        // Check if passwords match
        if (Arrays.equals(password, confirmPassword)) {
            User newUser = new User(username, password);
            newUser.addRole(role);

            // Collect additional user information
            clearPreviousOptionBox();
            collectUserInfo();  // Pass the newUser object to collectUserInfo
            userList.add(newUser);
            currentUser = newUser;
            outputArea.appendText(role.name() + " account created successfully.\n");

            // After account creation, transition back to the login screen
            ((VBox) outputArea.getParent()).getChildren().remove(createBox);
            //loginPrompt();
        } else {
            outputArea.appendText("Passwords don't match. Please try again.\n");
        }
    }

    // Method used to create a new account
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

    // admin function to invite a user
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
        	if (studentCheckBox.isSelected() && instructorCheckBox.isSelected() && adminCheckBox.isSelected()) {
            	outputArea.appendText("Invite code: ADMININSSTUINVCODE.\n");
            }
        	else if (studentCheckBox.isSelected() && instructorCheckBox.isSelected()) {
                outputArea.appendText("Invite code: STUDENTINSINVCODE.\n");
            }
        	else if (studentCheckBox.isSelected() && adminCheckBox.isSelected()) {
            	outputArea.appendText("Invite code: STUADINVCODE.\n");            
            }
        	else if (instructorCheckBox.isSelected() && adminCheckBox.isSelected()) {
            	outputArea.appendText("Invite code: ADMININSINVCODE.\n");
            }
        	else if (studentCheckBox.isSelected()) {
                outputArea.appendText("Invite code: STUDENTINVCODE.\n");
            }
        	else if (instructorCheckBox.isSelected()) {
            	outputArea.appendText("Invite code: INSTRUCTORINVCODE.\n");            
            }
        	else if (adminCheckBox.isSelected()) {
            	outputArea.appendText("Invite code: ADMININVCODE.\n");
            }
            else {
                outputArea.appendText("No role selected. Please select at least one role.\n");
            }
            // Remove the createBox from the UI after successful account creation
            ((VBox) outputArea.getParent()).getChildren().remove(inviteBox);
            showUserOptions();

        });

        // Set the action for when the "Back" button is pressed
        backButton.setOnAction(event -> {
            ((VBox) outputArea.getParent()).getChildren().remove(inviteBox);  // Remove the inviteBox
            showSignInOrCreateAccount();  // Show the previous sign-in/create account options again
        });
    }
    
    // method used to clear the previous option box
    private void clearPreviousOptionBox() {
        if (optionBox.getParent() != null) {
            ((VBox) outputArea.getParent()).getChildren().remove(optionBox);
        }
    }

    // admin method used to list all users
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

    // admin method used to delete a user
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
            showUserOptions();  // Show options again after deletion
        });

        // Back button functionality
        backButton.setOnAction(event -> {
            ((VBox) outputArea.getParent()).getChildren().remove(deleteBox);
            showUserOptions();  // Show options again when going back
        });
    }
}