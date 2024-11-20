package project;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/*******
 * <p> ArticleIdHelper class </p>
 * 
 * <p> Description: A class created for generating a unique identifier for help articles based on 
 * level, groupId, title, authors, abstract, keywords, body, and references. Using the SHA-256 algorithm, 
 * the class converts the hash to a long integer to be the article's unique identifier. </p>
 * 
 * @author Hassan Khan, Colby Taylor, Xavier Flores, Shashwat Balaji, Avinash Poguluri, Abil Damirbek uulu
*/

public class ArticleIdHelper {

	/*********
     	* This is the method used to generate the unique identifier using SHA-256 hashing
     	* 
    	* @param level    			(beginner, intermediate, advanced, expert)
    	* @param groupId			identifiers used for groups of related articles
     	* @param title				title of article
     	* @param authors			authors of article
     	* @param articleAbstract	short description about article
     	* @param keywords			words used to process searches
     	* @param body				body of help article
     	* @param references		reference links or similar materials
     	*/
	public static long generateArticleId(char[] level, char[] groupId, char[] title, char[] authors, char[] articleAbstract, char[] keywords, char[] body, char[] references) {
        try {
            // Concatenate the fields to form a unique string
            String combined = new String(level) + new String(groupId) + new String(title) 
                    + new String(authors) + new String(articleAbstract) + new String(keywords) 
                    + new String(body) + new String(references);

            // Use SHA-256 hashing
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(combined.getBytes(StandardCharsets.UTF_8));

            // Convert first 8 bytes of the hash to a long
            return bytesToLong(Arrays.copyOfRange(hashBytes, 0, 8));

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing algorithm not available", e);
        }
    }

     /*********
     * This is the method used to convert bytes to long
     * 
     * @param bytes    		the first 8 bytes of the hash	
     */
    private static long bytesToLong(byte[] bytes) {
        long result = 0;
        for (int i = 0; i < bytes.length; i++) {
            result <<= 8;
            result |= (bytes[i] & 0xFF);
        }
        return result;
    }
}
