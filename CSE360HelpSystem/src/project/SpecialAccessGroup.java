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
    // loaded constructor
    public SpecialAccessGroup(String groupName, List<Long> articles, List<User> admins, List<User> instructorsWithAccess, List<User> instructorsWithAdminRights, List<User> studentsWithViewingRights) {
        this.groupName = groupName;
        this.articles = articles;
        this.admins = admins;
        this.instructorsWithAccess = instructorsWithAccess;
        this.instructorsWithAdminRights = instructorsWithAdminRights;
        this.studentsWithViewingRights = studentsWithViewingRights;
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
    // given a unique long id, check if aricles exists in the list
    public boolean doesArticleExist(Long uniqueId) {
    	for (Long currId : articles) {
    		// article exists
    		if (currId.equals(uniqueId)) {
    			return true;
    		}
    	}
    	return false;
    }
    // given an articles unique long id, add it to the groups article list
    public boolean addToArticles(Long uniqueId) {
    	if (doesArticleExist(uniqueId)) {
    		return false; // return false if article wasn't added
    	}
    	else {
    		articles.add(uniqueId);
    		return true; // return true if article was added
    	}
    }
    
    // given a unique long id, remove an article from article list
    public boolean removeFromArticles(Long uniqueId) {
    	if (doesArticleExist(uniqueId)) {
    		articles.remove(uniqueId);
    		return true; // return true if article was removed
    	}
    	else {
    		return false; // return false if article wasn't removed
    	}
    }
    
    // ADMIN METHODS
    public List<User> getAdmins() {
        return admins;
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
    // add user to admin list
    public boolean addToAdmins(User user) {
    	if (doesAdminExist(user)) {
    		return false; // admin already exists
    	}
    	else {
    		admins.add(user);
    		return true;
    	}
    }

    // remove user from admin list
    public boolean removeFromAdmins(User user) {
    	if (doesAdminExist(user)) {
    		admins.remove(user);
    		return true; // admin was removed
    	}
    	else {
    		return false;
    	}
    }
    
    // INSTRUCTOR WITH ACCESS METHODS
    // add user to instr. with access list
    public List<User> getInstructorsWithAccess() {
        return instructorsWithAccess;
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
    // add to instructor with access list
    public boolean addToInstrWithAccess(User user) {
    	if (doesInstrExistInAccessList(user)) {
    		return false;
    	}
    	else {
    		instructorsWithAccess.add(user);
    		return true;
    	}
    }

    // remove user from instr. with access list
    public boolean removeFromInstrWithAccess(User user) {
    	if (doesInstrExistInAccessList(user)) {
    		instructorsWithAccess.remove(user);
    		return true;
    	}
    	else {
    		return false;
    	}
    }

    // INSTRUCTOR WITH ADMIN RIGHTS METHODS
    public List<User> getInstructorsWithAdminRights() {
        return instructorsWithAdminRights;
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
    // add to instructor with admin rights list
    public boolean addToInstrWithAdminRights(User user) {
    	if (doesInstrExistInAdminRightsList(user)) {
    		return false;
    	}
    	else {
    		instructorsWithAdminRights.add(user);
    		return true;
    	}
    }

    // remove user from instructor with admin rights list
    public boolean removeFromInstrWithAdminRights(User user) {
    	if (doesInstrExistInAdminRightsList(user)) {
    		instructorsWithAdminRights.remove(user);
    		return true;
    	}
    	else {
    		return false;
    	}
    }

    // STUDENT LIST METHODS
    public List<User> getStudentsWithAccess() {
        return studentsWithViewingRights;
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
    // add to student list
    public boolean addToStudentList(User user) {
    	if (doesStudentExistInStudentList(user)) {
    		return false;
    	}	
    	else {
    		studentsWithViewingRights.add(user);
    		return true;
    	}
    }

    // remove user from student list
    public boolean removeFromStudentList(User user) {
    	if (doesStudentExistInStudentList(user)) {
    		studentsWithViewingRights.remove(user);
    		return true;
    	}	
    	else {
    		return false;
    	}    
    }
}
