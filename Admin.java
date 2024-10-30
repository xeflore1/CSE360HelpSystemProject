package project;

import java.util.List;

/*******
 * <p> Admin class </p>
 * 
 * <p> Description:  Handles administrative tasks like inviting users, deleting users, listing users, assigning/removing roles.</p> 
 * <p> A subclass of User provides general behavior with super administrative privileges.</p>
 * <p> Collaborators: User, Role.</p> 
 * 
 * @author Hassan Khan, Colby Taylor, Xavier Flores, Shashwat Balaji, Avinash Poguluri, Abil Damirbek uulu
*/

public class Admin extends User {

	// initial constructor
    public Admin(String username, char[] password) {
        super(username, password);
        addRole(Role.ADMIN); // Ensure this user has the Admin role
    }

    /*********
     * This is the method used to invite a user
     * 
     * @param username    		username of user
     * @param password			password of user
     * @param firstName			first name of user
     * @param middleName		middle name of user
     * @param lastName			last name of user
     * @param preferredName		preferred name of user
     * @param email				email of user
     * @param userList			user list of all users
     */
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

    /*********
     * This is the method used to reset a user's account
     * 
     * @param user    			particular user
     * @param newPassword		new password of user
     */
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

    /*********
     * This is the method used to delete a user's account
     * 
     * @param username    		username of user
     * @param userList			list of all users
     */
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

    /*********
     * This is the method used to list user accounts
     * 
     * @param userList			list of all users
     */
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

    /*********
     * This is the method used to add a role to user
     * 
     * @param user				particular user
     * @param role				role to add
     */
    public void addRoleToUser(User user, Role role) {
        if (user.hasRole(role)) {
            System.out.println("User already has the role ");
        } 
        else {
            user.addRole(role);
            System.out.println("The role has been added to user ");
        }
    }

    /*********
     * This is the method used to remove a role from user
     * 
     * @param user				particular user
     * @param role				role to remove
     */
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
