package messenger.crypto;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

public class AuthCode {
	public enum AuthKeyType {
		Public, Private, Secret;
	}

	public static KeyPair generatePublicKey(byte[] password) throws NoSuchAlgorithmException {
		KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
		if (password == null) {
			keygen.initialize(1024);
		} else {
			keygen.initialize(1024, new SecureRandom(password));
		}

		return keygen.genKeyPair();
	}

	public static Key generateSecretKey(byte[] password) throws NoSuchAlgorithmException {
		final int KEY_SIZE = 128;
		if (password == null) {
			byte[] keyCode = new byte[KEY_SIZE];
			System.arraycopy(computeHash(password), 0, keyCode, 0, KEY_SIZE);
			return new SecretKeySpec(keyCode, "AES");
		} else {
			KeyGenerator keygen = KeyGenerator.getInstance("AES");
			keygen.init(KEY_SIZE);
			return keygen.generateKey();
		}
	}

	private static byte[] computeHash(byte[] in) throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		return digest.digest(in);
	}

	private final Key key;
	private final AuthKeyType keyType;

	private final byte[] hash;

	public AuthCode(AuthKeyType keyType, byte[] encoded) throws NoSuchAlgorithmException, InvalidKeySpecException {
		KeyFactory keyFact;
		this.keyType = keyType;
		switch (keyType) {
		case Public:
			keyFact = KeyFactory.getInstance("RSA");
			key = keyFact.generatePublic(new X509EncodedKeySpec(encoded));
			break;
		case Private:
			keyFact = KeyFactory.getInstance("RSA");
			key = keyFact.generatePrivate(new PKCS8EncodedKeySpec(encoded));
			break;
		case Secret:
			if (encoded.length != 16) {
				throw new IllegalArgumentException("AES password encoded MUST be 16 bytes in length");
			}
			key = new SecretKeySpec(encoded, "AES");
			break;
		default:
			throw new IllegalArgumentException("Illegal keyType");
		}
		hash = computeHash(encoded);
	}

	public AuthCode(AuthKeyType keyType, Key key) throws NoSuchAlgorithmException {
		this.key = key;
		this.keyType = keyType;

		String algorithm = key.getAlgorithm();
		if (!algorithm.equals("AES") && !algorithm.equals("RSA")) {
			throw new IllegalArgumentException("Key must be of algorithm RSA or AES");
		}
		hash = computeHash(key.getEncoded());
	}

	public byte[] getHash() {
		return hash.clone();
	}

	/**
	 * @return the key
	 */
	public Key getKey() {
		return key;
	}

	/**
	 * @return the keyType
	 */
	public AuthKeyType getKeyType() {
		return keyType;
	}

	public void initCipher(Cipher c, int opmode) throws InvalidKeyException {
		c.init(opmode, key);
	}
}
