package test;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import project.DatabaseHelper;
import project.User;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TestCaseFile {
    private DatabaseHelper dbHelper;

    @Before
    public void setUp() throws Exception {
    	DatabaseHelper dhHelper; // Database variable
        dbHelper.connectToDatabase(); // Set up a connection before each test
    }

    @After
    public void tearDown() throws Exception {
        dbHelper = null;  // Clean up after each test
    }

    @Test
    public void testConnectToDatabase() {
        try {
            dbHelper.connectToDatabase();  // Test connection setup
            assertNotNull("Connection should != null after connecting", dbHelper);
        } catch (SQLException e) {
            fail("Exception occurred during database connection: " + e.getMessage());
        }
    }

    @Test
    public void testDatabaseEmpty() {
        try {
            boolean isEmpty = dbHelper.isDatabaseEmpty(); // Check if database is empty
            assertTrue("Database should be empty for initial setup", isEmpty);
        } catch (SQLException e) {
            fail("Exception occurred while checking if database is empty: " + e.getMessage());
        }
    }

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

    @Test
    void testDisplayUsersByUser() throws Exception {
        // Set up a test user
        addUserToTestDatabase("user@example.com", "userPassword", "User");

        // Capture the output of displayUsersByUser method
        String expectedOutput = "ID: 1, Email: user@example.com, Password: userPassword, Role: User";
        assertEquals(expectedOutput, captureDisplayUsersByUser());
    }
    
    @Test
    void testSerializeRoles() {
        Set<Role> roles = Set.of(Role.ADMIN, Role.USER);
        String serialized = databaseHelper.serializeRoles(roles);
        assertEquals("ADMIN,USER", serialized);
    }

    @Test
    void testDeserializeRoles() {
        String rolesString = "ADMIN,USER";
        Set<Role> roles = databaseHelper.deserializeRoles(rolesString);
        assertTrue(roles.contains(Role.ADMIN));
        assertTrue(roles.contains(Role.USER));
    }
    
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
    
    @Test
    void testListArticles() throws SQLException {
        String articlesList = databaseHelper.listArticles();
        assertTrue(articlesList.contains("Intro to Java by Jeremy Jeremy"));
    }
    
    @Test
    void testListArticlesByGroup() throws SQLException {
        String articlesList = databaseHelper.listArticlesByGroup("java");
        assertTrue(articlesList.contains("Intro to Java by Jeremy Jeremy"));
    }
    
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
        assertTrue(articleExists("Advanced Java"));
    }
    
    @Test
    void testDeleteArticle() throws SQLException {
        int articleId = 1; // ID of an article to delete
        databaseHelper.deleteArticle(articleId);
        assertFalse(articleExistsById(articleId));
    }
    
    @Test
    void testBackupArticles() throws SQLException, IOException {
        String filename = "backup_test.txt";
        databaseHelper.backupArticles(filename);

        File file = new File(filename);
        assertTrue(file.exists());

        // Clean up
        file.delete();
    }

}