package project;

import java.util.ArrayList;
import java.util.List;
// FIXME TEST IF FUNCITONS WORK
public class SpecialAccessGroup {
    private String groupName;
    private List<Long> articles; // Articles with encrypted bodies, long represents the unique id of an article
    private List<User> admins;      // Admins with admin rights for this group, cant view decrypted articles
    private List<User> instructorsWithAccess; // Instructors with viewing rights
    private List<User> instructorsWithAdminRights; // Instructors with admin rights
    private List<User> studentsWithViewingRights; // Students who can view encrypted articles
    
    // Constructor, takes a group name
    public SpecialAccessGroup(String groupName) {
        this.groupName = groupName;
        this.articles = new ArrayList<>();
        this.admins = new ArrayList<>();
        this.instructorsWithAccess = new ArrayList<>();
        this.instructorsWithAdminRights = new ArrayList<>();
        this.studentsWithViewingRights = new ArrayList<>();
    }

    // GROUP METHODS
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    // ARTICLE METHODS
    public List<Long> getArticles() {
        return articles;
    }
    // given an articles unique long id, add it to the groups article list
    public void addToArticles(Long uniqueId) {
    	articles.add(uniqueId);
    }
    
    // given a unique long id, remove an article from article list
    public void removeFromArticles(Long uniqueId) {
    	articles.remove(uniqueId);
    }
    
    // given a unique long id, check if aricles exists in the list
    public boolean doesArticleExist(Long uniqueId) {
    	for (Long currId : articles) {
    		// article exists
    		if (currId == uniqueId) {
    			return true;
    		}
    	}
    	return false;
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
    public List<User> getInstructorsWithAccess() {
        return instructorsWithAccess;
    }
    
    // add to instructor with access list
    public void addToInstrWithAccess(User user) {
    	instructorsWithAccess.add(user);
    }

    // remove user from instr. with access list
    public void removeFromInstrWithAccess(User user) {
    	instructorsWithAccess.remove(user);
    }
    
    // checks if user exist in instr. with access list
    public boolean doesInstrExistInAccessList(User user) {
    	for (User currUser : instructorsWithAccess) {
    		// article exists
    		if (currUser.equals(user)) { // FIXME: MAY OR MAY NOT CHECK PROPERLY
    			return true;
    		}
    	}
    	return false;
    }

    // INSTRUCTOR WITH ADMIN RIGHTS METHODS
    public List<User> getInstructorsWithAdminRights() {
        return instructorsWithAdminRights;
    }
    
    // add to instructor with admin rights list
    public void addToInstrWithAdminRights(User user) {
    	instructorsWithAdminRights.add(user);
    }

    // remove user from instructor with admin rights list
    public void removeFromInstrWithAdminRights(User user) {
    	instructorsWithAdminRights.remove(user);
    }
    
    // checks if user exist in instructor with admin rights list
    public boolean doesInstrExistInAdminRightsList(User user) {
    	for (User currUser : instructorsWithAdminRights) {
    		// article exists
    		if (currUser.equals(user)) { // FIXME: MAY OR MAY NOT CHECK PROPERLY
    			return true;
    		}
    	}
    	return false;
    }

    // STUDENT LIST METHODS
    public List<User> getStudentsWithAccess() {
        return studentsWithViewingRights;
    }
    
    // add to student list
    public void addToStudentList(User user) {
    	studentsWithViewingRights.add(user);
    }

    // remove user from student list
    public void removeFromStudentList(User user) {
    	studentsWithViewingRights.remove(user);
    }
    
    // checks if user exist in student list
    public boolean doesStudentExistInStudentList(User user) {
    	for (User currUser : studentsWithViewingRights) {
    		// article exists
    		if (currUser.equals(user)) { // FIXME: MAY OR MAY NOT CHECK PROPERLY
    			return true;
    		}
    	}
    	return false;
    }
}