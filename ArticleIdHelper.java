package project;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class ArticleIdHelper {

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

    private static long bytesToLong(byte[] bytes) {
        long result = 0;
        for (int i = 0; i < bytes.length; i++) {
            result <<= 8;
            result |= (bytes[i] & 0xFF);
        }
        return result;
    }
}
