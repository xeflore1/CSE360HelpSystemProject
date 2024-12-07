package project;

import java.util.ArrayList;
import java.util.List;

/*******
 * <p> SpecialAccessGroup class </p>
 * 
 * <p> Description: This class represents groups with special access to encrypted articles. </p>
 * <p> It manages group names, articles, and user roles. </p>
 * <p> Provides methods to add, remove, and verify users or articles within the group. </p>
 * 
 * <p> Collaborators: User, Admin. </p>
 * 
 * @author Hassan Khan, Colby Taylor, Xavier Flores, Shashwat Balaji, Avinash Poguluri, Abil Damirbek uulu
 */

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
    // getters and setters
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    // ARTICLE METHODS
    // getters and setters
    public List<Long> getArticles() {
        return articles;
    }

    /*******
    * This method is used to check if aricles exists in the list given a unique long id
    * @param uniqueId             long of id
    * @return bool                true or false
    */
    public boolean doesArticleExist(Long uniqueId) {
    	for (Long currId : articles) {
    		// article exists
    		if (currId.equals(uniqueId)) {
    			return true;
    		}
    	}
    	return false;
    }

    /*******
    * This method is used to add an article to the groups article list given a unique long id
    * @param uniqueId             long of id
    * @return bool                true or false
    */
    public boolean addToArticles(Long uniqueId) {
    	if (doesArticleExist(uniqueId)) {
    		return false; // return false if article wasn't added
    	}
    	else {
    		articles.add(uniqueId);
    		return true; // return true if article was added
    	}
    }
    
    /*******
    * This method is used to remove an article from the groups article list given a unique long id
    * @param uniqueId             long of id
    * @return bool                true or false
    */
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
    // setters and getters
    public List<User> getAdmins() {
        return admins;
    }
    
    /*******
    * This method is used to check if admin/user exists.
    * @param user                 user
    * @return bool                true or false
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
    
    /*******
    * This method is used to add to admins
    * @param user             user
    */
    public boolean addToAdmins(User user) {
    	if (doesAdminExist(user)) {
    		return false; // admin already exists
    	}
    	else {
    		admins.add(user);
    		return true;
    	}
    }

    /*******
    * This method is used to remove from admins
    * @param user             user
    */
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
    // getters and setters
    public List<User> getInstructorsWithAccess() {
        return instructorsWithAccess;
    }
    
    /*******
    * This method is used to check if the instructor exists in access list
    * @param user             user
    * @return bool            true or false
    */
    public boolean doesInstrExistInAccessList(User user) {
    	for (User currUser : instructorsWithAccess) {
    		// article exists
    		if (currUser.equals(user)) { // FIXME: MAY OR MAY NOT CHECK PROPERLY
    			return true;
    		}
    	}
    	return false;
    }

    
    /*******
    * This method is used to add to instructors with access.
    * @param user             user
    * @return bool             true or false
    */
    public boolean addToInstrWithAccess(User user) {
    	if (doesInstrExistInAccessList(user)) {
    		return false;
    	}
    	else {
    		instructorsWithAccess.add(user);
    		return true;
    	}
    }

    /*******
    * This method is used to remove from instructors with access.
    * @param user             user
    * @return bool             true or false
    */
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
    // getters and setters
    public List<User> getInstructorsWithAdminRights() {
        return instructorsWithAdminRights;
    }

    /*******
    * This method is used to check if user exists in instructor with admin rights list
    * @param user             user
    * @return bool             true or false
    */
    public boolean doesInstrExistInAdminRightsList(User user) {
    	for (User currUser : instructorsWithAdminRights) {
    		// article exists
    		if (currUser.equals(user)) { // FIXME: MAY OR MAY NOT CHECK PROPERLY
    			return true;
    		}
    	}
    	return false;
    }

    /*******
    * This method is used to add to instructor with admin rights list
    * @param user             user
    * @return bool             true or false
    */
    public boolean addToInstrWithAdminRights(User user) {
    	if (doesInstrExistInAdminRightsList(user)) {
    		return false;
    	}
    	else {
    		instructorsWithAdminRights.add(user);
    		return true;
    	}
    }

    /*******
    * This method is used to remove to instructor with admin rights list
    * @param user             user
    * @return bool             true or false
    */
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
    // getters and setters
    public List<User> getStudentsWithAccess() {
        return studentsWithViewingRights;
    }

    /*******
    * This method is used to check if user exists in student list
    * @param user             user
    * @return bool             true or false
    */
    public boolean doesStudentExistInStudentList(User user) {
    	for (User currUser : studentsWithViewingRights) {
    		// article exists
    		if (currUser.equals(user)) { // FIXME: MAY OR MAY NOT CHECK PROPERLY
    			return true;
    		}
    	}
    	return false;
    }

    /*******
    * This method is used to add to student list.
    * @param user             user
    * @return bool             true or false
    */
    public boolean addToStudentList(User user) {
    	if (doesStudentExistInStudentList(user)) {
    		return false;
    	}	
    	else {
    		studentsWithViewingRights.add(user);
    		return true;
    	}
    }

    /*******
    * This method is used to remove from student list.
    * @param user             user
    * @return bool             true or false
    */
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
