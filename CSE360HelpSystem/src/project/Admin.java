package project;

import java.util.List;

public class Admin extends User {

	// initial constructor
    public Admin(String username, char[] password) {
        super(username, password);
        addRole(Role.ADMIN); // Ensure this user has the Admin role
    }

    // Method to invite a user
    public void inviteUser(String username, char[] password, String firstName, String middleName, String lastName, String preferredName, String email, List<User> userList) {
        // check if user exists
    	for (User user : userList) {
            if (user.getUsername() == (username)) {
                System.out.println("The username already exists.");
            }
        }
        
    	// create student
        User newUser = new User(username, password);
        newUser.setFirstName(firstName);
        newUser.setMiddleName(middleName);
        newUser.setLastName(lastName);
        newUser.setPreferredName(preferredName);
        newUser.setEmail(email);
        newUser.addRole(Role.STUDENT);  
        
        userList.add(newUser);
        System.out.println(" The provided username has been successfully created.");
    }

    // Method to reset a user account
    public void resetUserAccount(User user, char[] newPassword) {
    	// check if user exists
    	// FIXME implement if user exists function, you can use code from the main.j
    	if (user == null) {
            System.out.println("User not found.");        
        }
    	else {
	        user.setPassword(newPassword);
	        System.out.println("User account " + user.getUsername() + " has been reset.");
    	}
    }

    // Method to delete a user account, works
    public void deleteUserAccount(String username, List<User> userList) {
        User userToDelete = null;
        // search for user and set it to userToDelete
        for (User user : userList) {
            if (user.getUsername().equals(username)) {
                userToDelete = user;
                break;
            }
        }
        // if the user exists
        if (userToDelete != null) {
            userList.remove(userToDelete);
            // remove user 
            System.out.println("User has been deleted.");
        } 
        else {
            System.out.println("User not found.");
        }
    }

    // Method to list all user accounts
    public void listUserAccounts(List<User> userList) {
        System.out.println("Listing all users:");
        for (User user : userList) {
            System.out.println("Username: " + user.getUsername());
            System.out.println("First name: " + user.getFirstName());
            System.out.println("Last name: " + user.getLastName());
            System.out.println("Email: " + user.getEmail());
            System.out.println("Roles: " + user.getRoles());
            System.out.println();
            
        }
    }

    // Method to add a role to a user
    public void addRoleToUser(User user, Role role) {
        if (user.hasRole(role)) {
            System.out.println("User already has the role ");
        } 
        else {
            user.addRole(role);
            System.out.println("The role has been added to user ");
        }
    }

    // Method to remove a role from a user
    public void removeRoleFromUser(User user, Role role) {
        if (!user.hasRole(role)) {
            System.out.println("Username does not have the role");
        } 
        else {
            user.removeRole(role);
            System.out.println("The role has been removed from user ");
        }
    }
}