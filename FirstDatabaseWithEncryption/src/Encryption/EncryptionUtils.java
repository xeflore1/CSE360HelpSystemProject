package Encryption;

/**
 * <p> Article Database Encryption Protocol. </p>
 * 
 * <p> Description: This class handles the encryption operations from main. </p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2024 </p>
 * 
 * @author Lynn Robert Carter
 * 
 * @version 1.00		2024-09-13	Initial baseline derived from the Even Recognizer
 */

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

public class EncryptionUtils {
	private static int IV_SIZE = 16;
	
	public static char[] toCharArray(byte[] bytes) {		
        CharBuffer charBuffer = Charset.defaultCharset().decode(ByteBuffer.wrap(bytes));
        return Arrays.copyOf(charBuffer.array(), charBuffer.limit());
	}
	
	static byte[] toByteArray(char[] chars) {		
        ByteBuffer byteBuffer = Charset.defaultCharset().encode(CharBuffer.wrap(chars));
        return Arrays.copyOf(byteBuffer.array(), byteBuffer.limit());
	}
		
	public static byte[] getInitializationVector(char[] text) {
		char iv[] = new char[IV_SIZE];
		
		int textPointer = 0;
		int ivPointer = 0;
		while(ivPointer < IV_SIZE) {
			iv[ivPointer++] = text[textPointer++ % text.length];
		}
		
		return toByteArray(iv);
	}
	
	public static void printCharArray(char[] chars) {
		for(char c : chars) {
			System.out.print(c);
		}
	}
}
