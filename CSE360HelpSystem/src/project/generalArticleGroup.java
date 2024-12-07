package project;

import java.util.ArrayList;
import java.util.List;

/*******
 * <p> generalArticleGroup class </p>
 * 
 * <p> Description: This class represents groups of articles within the system. </p>
 * <p> It stores information about group names and user roles within each group </p>
 * <p> Provides methods to manage users in each role category </p>
 * 
 * <p> Collaborators: User, Admin. </p>
 * 
 * @author Hassan Khan, Colby Taylor, Xavier Flores, Shashwat Balaji, Avinash Poguluri, Abil Damirbek uulu
 */

public class generalArticleGroup {

    // declaring needed variables
    private static List<generalArticleGroup> allGroups = new ArrayList<>();
    private String groupName;
    private List<User> admins;      // list of Admins
    private List<User> instructors; // list of Instructors
    private List<User> students; // list of Students 
    
    // Constructor, takes a group name
    public generalArticleGroup(String groupName) {
        this.groupName = groupName;
        this.admins = new ArrayList<>();
        this.instructors = new ArrayList<>();
        this.students = new ArrayList<>();
        allGroups.add(this);
    }

    // gettings and setters
    public static List<generalArticleGroup> getAllGroups() {
        return allGroups;
    }
    
    // GROUP METHODS
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    // ADMIN METHODS
    public List<User> getAdmins() {
        return admins;
    }
    
    /*******
    * This method is used to add to amins.
    * @param user             user
    */
    public void addToAdmins(User user) {
    	admins.add(user);
    }

    /*******
    * This method is used to remove from admins
    * @param user             user
    */
    public void removeFromAdmins(User user) {
    	admins.remove(user);
    }
    
    /*******
    * This method is used to check if the admin exits
    * @param user             user
    * @return bool            true or false
    */
    public boolean doesAdminExist(User user) {
    	for (User currUser : admins) {
    		// article exists
    		if (currUser.equals(user)) { // FIXME: MAY OR MAY NOT CHECK PROPERLY
    			return true;
    		}
    	}
    	return false;
    }
    
    // INSTRUCTOR WITH ACCESS METHODS
    // add user to instr. with access list
    public List<User> getInstructors() {
        return instructors;
    }
    
    /*******
    * This method is used to add to instructors.
    * @param user             user
    */
    public void addToInstuctors(User user) {
    	instructors.add(user);
    }

    /*******
    * This method is used to remove from instructors
    * @param user             user
    */
    public void removeFromInstructors(User user) {
    	instructors.remove(user);
    }
    
    /*******
    * This method is used to check if the instructor exists
    * @param user             user
    * @return bool            true or false
    */
    public boolean doesInstrExist(User user) {
    	for (User currUser : instructors) {
    		// article exists
    		if (currUser.equals(user)) {
    			return true;
    		}
    	}
    	return false;
    }

    // STUDENT LIST METHODS
    // getters and setters
    public List<User> getStudents() {
        return students;
    }
    
    /*******
    * This method is used to add to students.
    * @param user             user
    */
    public void addToStudentList(User user) {
    	students.add(user);
    }

    /*******
    * This method is used to remove from students.
    * @param user             user
    */
    public void removeFromStudentList(User user) {
    	students.remove(user);
    }
    
    /*******
    * This method is used to check if the student exists in student list
    * @param user             user
    * @return bool            true or false
    */
    public boolean doesStudentExistInStudentList(User user) {
    	for (User currUser : students) {
    		// article exists
    		if (currUser.equals(user)) {
    			return true;
    		}
    	}
    	return false;
    }
}
