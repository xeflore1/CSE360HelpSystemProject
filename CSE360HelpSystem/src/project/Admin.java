package project;

import java.util.List;

public class Admin extends User {

    // Constructor
    public Admin(String username, String password, String name, String email) {
        super(username, password, name, email);
    }

    // Method to invite others to join the app
    public void inviteUser(String email) {
        // Implementation to send an invitation to the email
        System.out.println("Invitation sent to: " + email);
    }

    // Method to reset a user's account (could reset password or other details)
    public void resetUserAccount(User user) {
        // Implementation to reset user details
        System.out.println("User account reset for: " + user.getUsername());
    }

    // Method to delete a user account
    public void deleteUserAccount(User user, List<User> userList) {
        // Implementation to remove user from the system
        userList.remove(user);
        System.out.println("User account deleted for: " + user.getUsername());
    }

    // Method to list all user accounts
    public void listAllUsers(List<User> userList) {
        System.out.println("Listing all users:");
        for (User user : userList) {
            System.out.println(user);
        }
    }

    // Method to add a role to a user
    public void addRoleToUser(User user, String role) {
        // Implementation to assign a role to a user
        System.out.println("Added role '" + role + "' to user: " + user.getUsername());
    }

    // Method to remove a role from a user
    public void removeRoleFromUser(User user, String role) {
        // Implementation to remove a role from a user
        System.out.println("Removed role '" + role + "' from user: " + user.getUsername());
    }

    // Method to log out
    public void logout() {
        // Implementation to log out the admin
        System.out.println("Admin " + getUsername() + " logged out.");
    }
}
