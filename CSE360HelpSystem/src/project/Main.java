package project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import project.User.Name;

public class Main {
    // List to store all users
    private static List<User> userList = new ArrayList<>();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
    	
    	Admin adminUser = null;
        // First user, admin is logging in
        if (userList.isEmpty()) {
            System.out.println("You are the first user and will be made an Admin.");

            // Prompt for username and password
            System.out.print("Enter username: ");
            String username = scanner.nextLine();
           
            // Variable to contain password
            char[] password = passwordDoubleChecker();
            
            // Create the first user with Admin role
            adminUser = new Admin(username, password);
            adminUser.addRole(Role.ADMIN);
            

            // Add the user to the list
            userList.add(adminUser);

            System.out.println("Admin account created. Please log in.");
            // Redirect back to login (for simplicity, just display info in this example)
            // Admin needs to finalize details
            finalLogin(adminUser);  
        }
        boolean check = true;
        // switch statement
        while(check) {
        	// Welcome Screen
            // Ask for what user would like to do 
            System.out.println("Welcome to the 360 Welcome Page!");
            System.out.println("What would you like to do? Options:");
            System.out.println("(Login, Print users, Delete user, Quit)");
            String option = scanner.nextLine();
            int choice = 0;
            if(option.equals("Login")) {
            	choice = 1;
            }
            else if(option.equals("Print users")) {
            	choice = 2;
            }
            else if(option.equals("Delete user")) {
            	choice = 3; 
            }
            // switch statement
	        switch (choice) {
		    case 1:
		        login();
		        System.out.println();
		        break;
		    case 2:
		    	adminUser.listUserAccounts(userList);
		    	System.out.println();
		    	break;
		    case 3: 
		    	adminUser.deleteUserAccount("newUser", userList);
		    	break;
		    case 4:
		    	System.out.println("Goodbye!");
		    	check = false;
		    default:
		        System.out.println("Invalid choice");
		        break;
	        }
        }
        
    }
    // regular login
    public static void login() {
        System.out.println("Welcome to the 360 help system!" );
        System.out.println("Do you already have an account? Yes or No");
        String check = scanner.nextLine();
        // Account already exists
        if(check.equals("Yes")) {
        	 // Ask for username and password
        	 System.out.print("Enter username: ");
             String username = scanner.nextLine();
             char[] password = passwordDoubleChecker();
             // search for user
             for (User user : userList) {
            	 // check if username matches
            	 if(user.getUsername().equals(username)) {
            		 // check if password matches
            		 if(Arrays.equals(password, user.getPassword())) {
            			 // Check if user has multiple roles
            			 if(user.hasMultipleRoles()) {
	            			 System.out.println("Select a role for this session (Student, Instructor, Admin): ");
	                         String role = scanner.nextLine();
	                         // user is a student
	                         if (role.equals("Student") && user.hasRole(Role.STUDENT)) {
	                        	 System.out.println("ADD CODE TO GO TO HOME PAGE STUDENT");
	                         }
	                         // user is a instructor
	                         else if (role.equals("Instructor") && user.hasRole(Role.INSTRUCTOR)) {
	                        	 System.out.println("ADD CODE TO GO TO HOME PAGE INSTRUCTOR");
	                         }
	                         // user is a admin
	                         else if (role.equals("Admin") && user.hasRole(Role.ADMIN)) {
	                        	 System.out.println("ADD CODE TO GO TO HOME PAGE ADMIN");
	                         }
	                         System.out.println("Your account does not have that role");
            			 }
            			 // user only has one role
            			 else {
            				 if (user.hasRole(Role.STUDENT)) {
	                        	 System.out.println("ADD CODE TO GO TO HOME PAGE STUDENT");
	                         }
	                         // user is a instructor
	                         else if (user.hasRole(Role.INSTRUCTOR)) {
	                        	 System.out.println("ADD CODE TO GO TO HOME PAGE INSTRUCTOR");
	                         }
	                         // user is a admin
	                         else if (user.hasRole(Role.ADMIN)) {
	                        	 System.out.println("ADD CODE TO GO TO HOME PAGE ADMIN");
	                         }
            			 }
            		 }
            	 }
             }
             System.out.println("User not found");
        	 
        }
        else {
        	// Send user to account creation 
        	initialLogin();
        }
    }
    // initial login 
    public static void initialLogin() {
        System.out.println("\nHello, welcome to the registration page!");
        System.out.println("Do you have an invintation key? Yes or No");
        String check = scanner.nextLine();
        // Invitation key route
        if(check.equals("Yes")) {
        	// Acquire username and password
        	System.out.print("Enter username: ");
	        String username = scanner.nextLine();
	        char[] password = passwordDoubleChecker();
	        // Change role based on invitation code
        	System.out.println("Enter Invitation code: ");
	        String inviteCode = scanner.nextLine();
	        // Student invite code
	        if(inviteCode.equals("giveStudent")) {
	        	User newUser = new User(username, password);
	            newUser.addRole(Role.STUDENT);
	            userList.add(newUser);
	            finalLogin(newUser);
	        }
	        // Instructor invite code
	        else if(inviteCode.equals("giveInstructor")) {
	        	User newUser = new User(username, password);
	        	newUser.addRole(Role.INSTRUCTOR);
	        	userList.add(newUser);
	        	finalLogin(newUser);
	        }
	        // Admin invite code
	        else if(inviteCode.equals("giveAdmin")) {
	        	User newUser = new User(username, password);
	        	newUser.addRole(Role.ADMIN);
	        	userList.add(newUser);
	        	finalLogin(newUser);
	        }
	        else {
	        	System.out.println("invalid code unable to create account");
	        }
        }
        // normal log in
        else {
	        System.out.print("Enter username: ");
	        String username = scanner.nextLine();
	        System.out.print("Enter password: ");
	        char[] password = passwordDoubleChecker();
	        User newUser = new User(username, password);
	        // Since no invite code was provided new user is student by default 
            newUser.addRole(Role.STUDENT);
            userList.add(newUser);
        	finalLogin(newUser);
        }
        
    }
    
   
    // Finish setting up account
    public static void finalLogin(User newUser) {
    	// Acquire name and email from user
    	System.out.println("Welcome user, you need to finalize your information");
    	System.out.println("Enter email: ");
        String email = scanner.nextLine();
        System.out.println("Enter first name: ");
        String firstName = scanner.nextLine();
        System.out.println("Enter perfered first name: ");
        String perferedFirstName = scanner.nextLine();
        System.out.println("Enter middle name: ");
        String middleName = scanner.nextLine();
        System.out.println("Enter last name: ");
        String lastName = scanner.nextLine();
        // Create full name for the new user
        Name fullname = new Name(firstName, middleName, lastName, perferedFirstName);
        newUser.setFullName(fullname);
        newUser.setEmail(email);
        System.out.println("login print:");
        newUser.displayUserInfo();
    	
    }

	//Helper method to print char[] password
	private static void printPassword(char[] password) {
	    for (char c : password) {
	        System.out.print(c);
	    }
	    System.out.println(); // Move to a new line after printing the password
	}
	
	public static char[] passwordDoubleChecker() {
		do {
        	// enter password
            System.out.print("Enter password: ");
            char[] password = scanner.nextLine().toCharArray();
            // Re enter the password
            System.out.print("Re enter the password: ");
            char[] secondPassword = scanner.nextLine().toCharArray();
            
            // Compare the two passwords using Arrays.equals()
            if (Arrays.equals(password, secondPassword)) {
                System.out.println("Passwords match.");
                return password;
            } else {
                System.out.println("Passwords do not match.");
            }
        } while(true);
	}
}