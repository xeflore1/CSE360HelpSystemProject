package project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

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
            adminUser = new Admin(username, password, "First Admin", "admin@example.com");
            adminUser.addRole(Role.ADMIN);

            // Add the user to the list
            userList.add(adminUser);

            System.out.println("Admin account created. Please log in.");
            // Redirect back to login (for simplicity, just display info in this example)
            login(adminUser);
            // FIXME admin needs to finish setting up account
            
        }
        // User attempting to login in is not the first
        firstLogin();
        adminUser.listUserAccounts(userList);
        
    }

    public static void firstLogin() {
    	// first login
        System.out.println("Hello, welcome to the login page!");
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        char[] password = passwordDoubleChecker();
        System.out.println("Enter Invintation code: ");
        String inviteCode = scanner.nextLine();
        if(inviteCode.equals("giveStudent")) {
        	User newUser = new User(username, password);
            newUser.addRole(Role.STUDENT);
            userList.add(newUser);
        }
        else if(inviteCode.equals("giveInstructor")) {
        	User newUser = new User(username, password);
        	newUser.addRole(Role.INSTRUCTOR);
        	userList.add(newUser);
        }
        else if(inviteCode.equals("giveAdmin")) {
        	User newUser = new User(username, password);
        	newUser.addRole(Role.ADMIN);
        	userList.add(newUser);
        }
        else {
        	System.out.println("invalid code unable to create account");
        }
        
    }
    
   
    // Finish setting up account
    public static void secondLogin(User newUser) {
    	
    	
    }
    
    
    // Simple login method (for now, just display the user info)
    public static void login(User user) {
        System.out.println("Welcome, " + user.getUsername() + "!");
        System.out.println("your pass is: ");
        printPassword(user.getPassword());
        user.displayUserInfo();
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