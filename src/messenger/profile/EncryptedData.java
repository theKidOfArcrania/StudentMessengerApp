package messenger.profile;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

public class EncryptedData {

	public static final int ENCRYPT_MODE = 1;
	public static final int DECRYPT_MODE = 2;
	private static final int KEY_LENGTH = 16;

	private final int mode;
	private Cipher sessionCrypt;
	private Cipher dataCrypt;

	public EncryptedData(int mode) {
		if (mode != 1 && mode != 2) {
			throw new IllegalArgumentException("Invalid mode");
		}
		this.mode = mode;

		try {
			sessionCrypt = Cipher.getInstance("RSA");
			dataCrypt = Cipher.getInstance("AES/CBC/PKCS5Padding");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			//Should not occur.
			throw new RuntimeException(e);
		}

		sessionCrypt
	}
}
