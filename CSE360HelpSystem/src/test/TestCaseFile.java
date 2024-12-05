package project;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

import project.DatabaseHelper;
import project.User;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TestCaseFile {
    private DatabaseHelper dbHelper;
    private static DatabaseHelper databaseHelper; // database variable

    @Before
    public void setUp() throws Exception {
    	DatabaseHelper databaseHelper; // Database variable
        dbHelper.connectToDatabase(); // Set up a connection before each test
    }

    @After
    public void tearDown() throws Exception {
        dbHelper = null;  // Clean up after each test
    }

    // Tests coneting ot database
    @Test
    public void testConnectToDatabase() {
        try {
            dbHelper.connectToDatabase();  // Test connection setup
            assertNotNull("Connection should != null after connecting", dbHelper);
        } catch (SQLException e) {
            fail("Exception occurred during database connection: " + e.getMessage());
        }
    }

    // Tests checking if the database is empty
    @Test
    public void testDatabaseEmpty() {
        try {
            boolean isEmpty = dbHelper.isDatabaseEmpty(); // Check if database is empty
            assertTrue("Database should be empty for initial setup", isEmpty);
        } catch (SQLException e) {
            fail("Exception occurred while checking if database is empty: " + e.getMessage());
        }
    }

    // tests the ability to save and load users
    @Test
    public void testSaveAndLoadUsers() {
        try {
            // Set up a test user
            List<User> testUsers = new ArrayList<>();
            User user = new User("testUser", "testPassword".toCharArray());
            user.addRole(Role.STUDENT);
            testUsers.add(user);

            dbHelper.saveUserListToDatabase(testUsers);  // Save users to database
            List<User> loadedUsers = dbHelper.loadUsersFromDatabase(); // Load users from database

            assertNotNull("Loaded users should != null", loadedUsers);
            assertEquals("Only 1 user in the database", 1, loadedUsers.size());
            assertEquals("Username should match the saved username", "testUser", loadedUsers.get(0).getUsername());
        } catch (SQLException e) {
            fail("Exception occurred during save/load test: " + e.getMessage());
        }
    }

    // Tests the method that registers the first user
    @Test
    public void testInitialRegister() {
        try {
            // Register a new user
            dbHelper.initialRegister("newUser", "newPassword".toCharArray(), Role.ADMIN);
            List<User> users = dbHelper.loadUsersFromDatabase();

            assertNotNull("Users list should not be null", users);
            assertEquals("Database should contain one user after registration", 1, users.size());
            assertEquals("Role should be ADMIN for the new user", Role.ADMIN, users.get(0).getRoles().iterator().next());
        } catch (Exception e) {
            fail("Exception during initial registration test: " + e.getMessage());
        }
    }

    // tests serializing roles
    @Test
    void testSerializeRoles() {
        Set<Role> roles = Set.of(Role.ADMIN, Role.USER);
        String serialized = databaseHelper.serializeRoles(roles);
        assertEquals("ADMIN,USER", serialized);
    }

    // tests deserializing roles
    @Test
    void testDeserializeRoles() {
        String rolesString = "ADMIN,USER";
        Set<Role> roles = databaseHelper.deserializeRoles(rolesString);
        assertTrue(roles.contains(Role.ADMIN));
        assertTrue(roles.contains(Role.USER));
    }

    // tests creating articles
    @Test
    void testCreateArticle() throws SQLException {
        char[] level = "beginner".toCharArray();
        char[] group = "java".toCharArray();
        char[] title = "Intro to Java".toCharArray();
        char[] authors = "Jeremy Jeremy".toCharArray();
        char[] articleAbstract = "Java basics".toCharArray();
        char[] keywords = "java,basics".toCharArray();
        char[] body = "Java is a programming language...".toCharArray();
        char[] references = "www.java.com".toCharArray();

        databaseHelper.createArticle(level, group, title, authors, articleAbstract, keywords, body, references);
        assertTrue(articleExists("Intro to Java"));
    }

    // tests listing articles
    @Test
    void testListArticles() throws SQLException {
        String articlesList = databaseHelper.listArticles();
        assertTrue(articlesList.contains("Intro to Java by Jeremy Jeremy"));
    }

    // tests listing articles by group
    @Test
    void testListArticlesByGroup() throws SQLException {
        String articlesList = databaseHelper.listArticlesByGroup("java");
        assertTrue(articlesList.contains("Intro to Java by Jeremy Jeremy"));
    }

    // tests updating articles
    @Test
    void testUpdateArticle() throws SQLException {
        long id = 1L; // Assumes test article with ID 1 exists
        char[] level = "advanced".toCharArray();
        char[] group = "java".toCharArray();
        char[] title = "Advanced Java".toCharArray();
        char[] authors = "Jane Doe".toCharArray();
        char[] articleAbstract = "Java advanced topics".toCharArray();
        char[] keywords = "java,advanced".toCharArray();
        char[] body = "Advanced Java content...".toCharArray();
        char[] references = "www.advancedjava.com".toCharArray();

        databaseHelper.updateArticle(id, level, group, title, authors, articleAbstract, keywords, body, references);
        assertTrue(databaseHelper.articleExistsByUniqueId(id));
    }

    // tests deleting articles
    @Test
    void testDeleteArticle() throws SQLException {
        int articleId = 1; // ID of an article to delete
        databaseHelper.deleteArticle(articleId);
        assertFalse(databaseHelper.articleExistsByUniqueId(articleId));
    }

    // tests backing articles up
    @Test
    void testBackupArticles() throws SQLException, IOException {
        String filename = "backup_test.txt";
        databaseHelper.backupArticles(filename);

        String file = new String(filename);
    }
    
    // JUnit Test to validate addToArticles() method
    public static class SpecialAccessGroupTest {

        private SpecialAccessGroup specialAccessGroup;

        @BeforeEach
        public void setUp() {
            // Create a new SpecialAccessGroup object before each test
            specialAccessGroup = new SpecialAccessGroup("Test Group");
        }

        @Test
        public void testAddAdminToGroup() {
            //  new admin to be added
            User admin = new User();
            
            // Add admin to list
            assertTrue(specialAccessGroup.addToAdmins(admin));
            
            // See if admin exists in its list
            assertTrue(specialAccessGroup.doesAdminExist(admin));
            // Check that the admin exists in lists it doesn't belong in 
            assertTrue(specialAccessGroup.doesInstrExistInAccessList(admin));
            assertTrue(specialAccessGroup.doesInstrExistInAdminRightsList(admin));
            assertTrue(specialAccessGroup.doesStudentExistInStudentList(admin));
        }

        @Test
        public void testAddInstrWithViewingRights() {
            //  new instructor to be added
            User instructor = new User();
            
            // Add instructor to list of instructors with article access
            assertTrue(specialAccessGroup.addToInstrWithAccess(instructor));
            
            // Assert that the instructor is in its list
            assertTrue(specialAccessGroup.doesInstrExistInAccessList(instructor));
            // Check that the instructor doesn't exist in lists it doesn't belong in
            assertTrue(specialAccessGroup.doesStudentExistInStudentList(instructor));
            assertTrue(specialAccessGroup.doesInstrExistInAdminRightsList(instructor));
            assertTrue(specialAccessGroup.doesAdminExist(instructor));
        }

        @Test
        public void testAddToArticles_MultipleArticles() {
            // Test adding multiple articles
            Long articleId1 = 123L;
            Long articleId2 = 456L;
            
            // Add the first article
            assertTrue(specialAccessGroup.addToArticles(articleId1));
            
            // Add the second article
            assertTrue(specialAccessGroup.addToArticles(articleId2));
            
            // Assert both articles are in the list
            assertTrue(specialAccessGroup.getArticles().contains(articleId1));
            assertTrue(specialAccessGroup.getArticles().contains(articleId2));
            
            // Assert the list size is 2
            assertEquals(2, specialAccessGroup.getArticles().size());
        }
    } 

}
