package project;

import java.util.ArrayList;
import java.util.List;

public class generalArticleGroup {

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
    
    // add user to admin list
    public void addToAdmins(User user) {
    	admins.add(user);
    }

    // remove user from admin list
    public void removeFromAdmins(User user) {
    	admins.remove(user);
    }
    
    // checks if user exist in admin list
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
    
    // add to instructor with access list
    public void addToInstuctors(User user) {
    	instructors.add(user);
    }

    // remove user from instr. with access list
    public void removeFromInstructors(User user) {
    	instructors.remove(user);
    }
    
    // checks if user exist in instr. with access list
    public boolean doesInstrExist(User user) {
    	for (User currUser : instructors) {
    		// article exists
    		if (currUser.equals(user)) { // FIXME: MAY OR MAY NOT CHECK PROPERLY
    			return true;
    		}
    	}
    	return false;
    }

    // STUDENT LIST METHODS
    public List<User> getStudents() {
        return students;
    }
    
    // add to student list
    public void addToStudentList(User user) {
    	students.add(user);
    }

    // remove user from student list
    public void removeFromStudentList(User user) {
    	students.remove(user);
    }
    
    // checks if user exist in student list
    public boolean doesStudentExistInStudentList(User user) {
    	for (User currUser : students) {
    		// article exists
    		if (currUser.equals(user)) { // FIXME: MAY OR MAY NOT CHECK PROPERLY
    			return true;
    		}
    	}
    	return false;
    }
}
