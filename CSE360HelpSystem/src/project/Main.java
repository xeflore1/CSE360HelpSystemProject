package project;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    // List to store all users
    private static List<User> userList = new ArrayList<>();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // First user, admin is logging in
        if (userList.isEmpty()) {
            System.out.println("You are the first user and will be made an Admin.");

            // Prompt for username and password
            System.out.print("Enter username: ");
            String username = scanner.nextLine();

            System.out.print("Enter password: ");
            char[] password = scanner.nextLine().toCharArray();

            // Create the first user with Admin role
            User firstUser = new User(username, password, "First Admin", "admin@example.com");
            firstUser.addRole(Role.ADMIN);

            // Add the user to the list
            userList.add(firstUser);

            System.out.println("Admin account created. Please log in.");

            // Redirect back to login (for simplicity, just display info in this example)
            login(firstUser);
        }
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
}