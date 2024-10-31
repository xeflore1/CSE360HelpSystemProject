package Encryption;

import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

/*******
 * <p> EncryptionHelper class </p>
 * 
 * <p> Description: A helper class for encrypting and decrypting data using the AES algorithm in CBC mode with 
 *  padding. Using the BouncyCastle, the class uses secure data handling. </p>
 * 
 * @author Hassan Khan, Colby Taylor, Xavier Flores, Shashwat Balaji, Avinash Poguluri, Abil Damirbek uulu
 */
public class EncryptionHelper {

	// Bouncy Castle identifier
	private static String BOUNCY_CASTLE_PROVIDER_IDENTIFIER = "BC";	
	private Cipher cipher;
	
	// byte key for encryption
	byte[] keyBytes = new byte[] {
            0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
            0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f,
            0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17 };
	private SecretKey key = new SecretKeySpec(keyBytes, "AES");

	/*********
     * This is the constructor used to initialize encryptionhelper 
     * and initializes cipher using the Bouncy Castle
     * Exception handling takes care of any database errors
     * 
     */
	public EncryptionHelper() throws Exception {
		Security.addProvider(new BouncyCastleProvider());
		cipher = Cipher.getInstance("AES/CBC/PKCS5Padding", BOUNCY_CASTLE_PROVIDER_IDENTIFIER);		
	}
	
	/*********
     * This is the method used to encrypt plaintext data given the vector
     * Exception handling takes care of any database errors
     * 
     * @param plainText 			plaintext data
     * @param initializationVector	initialization vector
     * @return byte					byte array
     */
	public byte[] encrypt(byte[] plainText, byte[] initializationVector) throws Exception {
		cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(initializationVector));
		return cipher.doFinal(plainText);
	}
	
	/*********
     * This is the method used to decrypt cipherText data given the vector
     * Exception handling takes care of any database errors
     * 
     * @param cipherText 			ciphertext data
     * @param initializationVector	initialization vector
     * @return byte					byte array
     */
	public byte[] decrypt(byte[] cipherText, byte[] initializationVector) throws Exception {
		cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(initializationVector));
		return cipher.doFinal(cipherText);
	}
	
}
