package Encryption;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

/*******
 * <p> EncryptionUtils class </p>
 * 
 * <p> Description: A utility class providing helper methods for encoding conversions and initialization vector generation 
 * for encryption processes. </p>
 * 
 * @author Hassan Khan, Colby Taylor, Xavier Flores, Shashwat Balaji, Avinash Poguluri, Abil Damirbek uulu
 */

public class EncryptionUtils {
	private static int IV_SIZE = 16;
	
	/*********
     * This is the method used to convert bytes into char array
     * 
     * @param bytes 				bytes array
     * @return char					char array
     */
	public static char[] toCharArray(byte[] bytes) {		
        CharBuffer charBuffer = Charset.defaultCharset().decode(ByteBuffer.wrap(bytes));
        return Arrays.copyOf(charBuffer.array(), charBuffer.limit());
	}
	
	/*********
     * This is the method used to convert char into bytes array
     * 
     * @param char 					char array
     * @return bytes				bytes array
     */
	static byte[] toByteArray(char[] chars) {		
        ByteBuffer byteBuffer = Charset.defaultCharset().encode(CharBuffer.wrap(chars));
        return Arrays.copyOf(byteBuffer.array(), byteBuffer.limit());
	}
		
	/*********
     * This is the method used to initialize the vector given the text
     * 
     * @param text 					text
     * @return bytes				bytes array
     */
	public static byte[] getInitializationVector(char[] text) {
		char iv[] = new char[IV_SIZE];
		
		int textPointer = 0;
		int ivPointer = 0;
		while(ivPointer < IV_SIZE) {
			iv[ivPointer++] = text[textPointer++ % text.length];
		}
		
		return toByteArray(iv);
	}
	
	/*********
     * This is the method used to print the char array
     * 
     * @param chars 					char array
     */
	public static void printCharArray(char[] chars) {
		for(char c : chars) {
			System.out.print(c);
		}
	}
}