package simpleDatabase;

/**
 * <p> Article Database Methods & Functionalities. </p>
 * 
 * <p> Description: This class handles method calls from StartCSE360.java and performs 
 * the relevant operation. The class also handles storage for the articles and interacts
 * with H2 as necessary. </p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2024 (modifications made to original file) </p>
 * 
 * @author Lynn Robert Carter, Hassan Khan (editor)
 * 
 * @version 1.00		2024-09-13	Initial baseline derived from the Even Recognizer
 * @version 1.01		2024-10-20	Functionality modified to fit HW6 parameters
 */

import java.sql.*;
import java.io.*;
import java.util.*;

/*
 * 
 * Class "DatabaseHelper.java" handles all database operations called from StartCSE360.java.
 * Class interacts with H2 to ensure that data persists between different program runs.
 * 
 */

class DatabaseHelper {

	// Instance variables:
    static final String JDBC_DRIVER = "org.h2.Driver";
    static final String DB_URL = "jdbc:h2:~/helpArticlesDB";
    static final String USER = "sa";
    static final String PASS = "";

    private Connection connection = null;
    private Statement statement = null;

    // Method establishes connection with H2 to access existing database information:
    public void connectToDatabase() throws SQLException {
        try {
            Class.forName(JDBC_DRIVER); // Load the JDBC driver
            System.out.println("Connecting to database...");
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            statement = connection.createStatement();
            createTables();  // Create tables if they don't exist
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found: " + e.getMessage());
        }
    }
    
    // Method creates article object table:
    private void createTables() throws SQLException {
        String articleTable = "CREATE TABLE IF NOT EXISTS help_articles ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "title VARCHAR(255), "
                + "authors VARCHAR(255), "
                + "abstract CLOB, "
                + "keywords VARCHAR(255), "
                + "body CLOB, "
                + "references CLOB)";
        statement.execute(articleTable);
    }

    // Creates the article and all relevant information based on parameters inputted into StartCSE360.java:
    public void createArticle(char[] title, char[] authors, char[] articleAbstract, char[] keywords, char[] body, char[] references) throws SQLException {
        String insertArticle = "INSERT INTO help_articles (title, authors, abstract, keywords, body, references) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertArticle)) {
            pstmt.setString(1, new String(title));
            pstmt.setString(2, new String(authors));
            pstmt.setString(3, new String(articleAbstract));
            pstmt.setString(4, new String(keywords));
            pstmt.setString(5, new String(body));
            pstmt.setString(6, new String(references));
            pstmt.executeUpdate();
        } finally {
            clearCharArray(title);
            clearCharArray(authors);
            clearCharArray(articleAbstract);
            clearCharArray(keywords);
            clearCharArray(body);
            clearCharArray(references);
        }
    }

    // Prints all existing articles in the database:
    public void listArticles() throws SQLException {
        String query = "SELECT * FROM help_articles";
        ResultSet rs = statement.executeQuery(query);
        System.out.println("Articles:");
        while (rs.next()) {
            System.out.println("[" + rs.getInt("id") + "] " + rs.getString("title") + " by " + rs.getString("authors"));
        }
    }

    // Deletes selected article from the database:
    public void deleteArticle(int articleId) throws SQLException {
        String deleteArticle = "DELETE FROM help_articles WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteArticle)) {
            pstmt.setInt(1, articleId);
            pstmt.executeUpdate();
        }
    }

    // Backs up and saves selected article from database:
    public void backupArticles(String filename) throws SQLException, IOException {
        String query = "SELECT * FROM help_articles";
        ResultSet rs = statement.executeQuery(query);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            while (rs.next()) {
                writer.write(rs.getInt("id") + "," + rs.getString("title") + "," + rs.getString("authors") + ","
                        + rs.getString("abstract") + "," + rs.getString("keywords") + "," + rs.getString("body") + "," + rs.getString("references"));
                writer.newLine();
            }
        }
    }

    // Loads selected article previously saved to the database:
    public void restoreArticles(String filename) throws SQLException, IOException {
        clearArticles();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                createArticle(fields[1].toCharArray(), fields[2].toCharArray(), fields[3].toCharArray(),
                        fields[4].toCharArray(), fields[5].toCharArray(), fields[6].toCharArray());
            }
        }
    }

    // Memory freeing classes:
    private void clearArticles() throws SQLException {
        String deleteAll = "DELETE FROM help_articles";
        statement.executeUpdate(deleteAll);
    }

    private void clearCharArray(char[] array) {
        Arrays.fill(array, '\u0000');  // Clear char array after use
    }

    // Handles operation (6)-- exists program:
    public void closeConnection() {
        try {
            if (statement != null) statement.close();
        } catch (SQLException se2) {
            se2.printStackTrace();
        }
        try {
            if (connection != null) connection.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }
}
