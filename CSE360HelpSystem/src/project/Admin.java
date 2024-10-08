package project;

import java.util.List;

public class Admin extends User {

	// initial constructor
    public Admin(String username, char[] password) {
        super(username, password);
        addRole(Role.ADMIN); // Ensure this user has the Admin role
    }
	// FIXME constructor needs to be updated to new user constructors
    public Admin(String username, char[] password, String firstName, String middleName, String lastName, String preferredName, String email) {
        super(username, password, email, new Name(firstName, middleName, lastName, preferredName));
        addRole(Role.ADMIN); // Ensure this user has the Admin role
    }

    // Method to invite a user
    public void inviteUser(String username, char[] password, String name, String email, List<User> userList) {
    	for (User user : userList) {
            if (user.getUsername().equals(username)) {
                System.out.println("User " + username + " already exists.");
                return;
            }
        }
        //User newUser = new User(username, password, name, email);
        //userList.add(newUser);
        System.out.println("User " + username + " has been invited.");
    }

    // Method to reset a user account
    public void resetUserAccount(User user, char[] newPassword) {
        user.setPassword(newPassword);
        System.out.println("User account " + user.getUsername() + " has been reset.");
    }

    // Method to delete a user account
    public void deleteUserAccount(User user, List<User> userList) {
        userList.remove(user);
        System.out.println("User account " + user.getUsername() + " has been deleted.");
    }

    // Method to list all user accounts
    public void listUserAccounts(List<User> userList) {
        System.out.println("Listing all users:");
        for (User user : userList) {
            System.out.println("Username: " + user.getUsername());
        }
    }

    // Method to add a role to a user
    public void addRoleToUser(User user, Role role) {
        user.addRole(role);
        System.out.println("Role " + role + " has been added to user " + user.getUsername());
    }

    // Method to remove a role from a user
    public void removeRoleFromUser(User user, Role role) {
        user.removeRole(role);
        System.out.println("Role " + role + " has been removed from user " + user.getUsername());
    }
}
