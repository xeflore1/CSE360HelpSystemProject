package simpleDatabase;

/**
 * <p> Article Database Manager. </p>
 * 
 * <p> Description: The main class in the article database manager-- prompts the 
 * user to input their desired option and enacts the corresponding operation for
 * the selection. </p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2024 (modifications made to original file) </p>
 * 
 * @author Lynn Robert Carter, Hassan Khan (editor)
 * 
 * @version 1.00		2024-09-13	Initial baseline derived from the Even Recognizer
 * @version 1.01		2024-10-20	Functionality modified to fit HW6 parameters
 */

import java.sql.SQLException;
import java.util.Scanner;

/*
 * 
 * Class "StartCSE30.java" presents menu operations and basic option handling. 
 * Saved data is stored in the "databaseHelper" variable, from DatabaseHelper.java.
 * 
 */

public class StartCSE360 {

	// Instance variables:
    private static DatabaseHelper databaseHelper;
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws Exception {
        databaseHelper = new DatabaseHelper();
        try {
            databaseHelper.connectToDatabase();  // Forms a connection to the database

            articleFlow();  // Handles the article operations
            
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("Good Bye!!");
            databaseHelper.closeConnection();
        } // Error handling
    }

    // Menu presented to user:
    private static void articleFlow() throws Exception {
        String choice;
        do {
            System.out.println("\nHelp Article Management:");
            System.out.println("1. List Articles");
            System.out.println("2. Create Article");
            System.out.println("3. Delete Article");
            System.out.println("4. Backup Articles");
            System.out.println("5. Restore Articles");
            System.out.println("6. Exit");
            System.out.print("Enter your choice: "); // Presents options 1-6 to the user
            choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    databaseHelper.listArticles();
                    // Handles operation (1) from user-- prints all article objects in the database:
                    break;
                case "2":
                    createArticle();
                    break;
                case "3":
                    deleteArticle();
                    break;
                case "4":
                    backupArticles();
                    break;
                case "5":
                    restoreArticles();
                    break;
                case "6":
                    System.out.println("Exiting...");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        } while (!choice.equals("6"));
    }

    // Handles operation (2) from user-- creates new article objects in the database:
    private static void createArticle() throws Exception {
        System.out.print("Enter title: ");
        char[] title = scanner.nextLine().toCharArray();
        System.out.print("Enter author(s): ");
        char[] authors = scanner.nextLine().toCharArray();
        System.out.print("Enter abstract: ");
        char[] articleAbstract = scanner.nextLine().toCharArray();
        System.out.print("Enter keywords (comma-separated): ");
        char[] keywords = scanner.nextLine().toCharArray();
        System.out.print("Enter body: ");
        char[] body = scanner.nextLine().toCharArray();
        System.out.print("Enter references: ");
        char[] references = scanner.nextLine().toCharArray(); //stores article information for constructor

        databaseHelper.createArticle(title, authors, articleAbstract, keywords, body, references);
        System.out.println("Article created successfully.");
    } // Creates a new article in the database via overloaded constructor

    // Handles operation (3) from user-- deletes existing article objects from the database:
    private static void deleteArticle() throws Exception {
        System.out.print("Enter article sequence number to delete: ");
        int seqNum = Integer.parseInt(scanner.nextLine());
        databaseHelper.deleteArticle(seqNum);
        System.out.println("Article deleted successfully.");
    }

    // Handles operation (4) from user-- saves existing article objects in the database:
    private static void backupArticles() throws Exception {
        System.out.print("Enter backup filename: ");
        String filename = scanner.nextLine();
        databaseHelper.backupArticles(filename); // Operation outsourced to DatabaseHelper.java
        System.out.println("Backup completed.");
    }

    // Handles operation (5) from user-- loads previously saved article objects from the database:
    private static void restoreArticles() throws Exception {
        System.out.print("Enter backup filename: ");
        String filename = scanner.nextLine();
        databaseHelper.restoreArticles(filename); // Operation outsourced to DatabaseHelper.java
        System.out.println("Restore completed.");
    }
}
