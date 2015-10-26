package messenger;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import messenger.profile.AuthCode;
import messenger.profile.AuthCode.AuthKeyType;;

public class UpdateService {

	private static final byte[] updaterKeyData = new byte[] {};
	private static boolean initKey = false;
	private static AuthCode updaterKey;

	static {
		try {
			initKey();
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
		}
	}

	public static void checkVersion() throws IOException {
		byte[] version = getVersion();
		byte[] head = fetchHeadVersion();

		if (!Arrays.equals(version, head)) {

		}
	}

	/**
	 * Fetches the head binary program and verifies whether if this updater is an authorized person.
	 *
	 * @return the newest program binary data
	 * @throws IOException if an error occurs while connecting the the update server.
	 */
	public static InputStream fetchHead() throws IOException {

		Path update = ChatList.getUpdatePath().resolve("head.smga");
		if (Files.exists(update)) {
			try (InputStream verifyIn = new BufferedInputStream(Files.newInputStream(update, StandardOpenOption.READ))) {
				initKey();
				Signature verifier = Signature.getInstance("SHA256withRSA");
				verifier.initVerify((PublicKey) updaterKey.getKey());

				byte[] hash = new byte[256];
				byte[] buffer = new byte[256];
				int readBytes = 0;

				verifier.update(hash);
				while ((readBytes = verifyIn.read(buffer)) != -1) {
					verifier.update(buffer, 0, readBytes);
				}
				if (verifier.verify(hash, 0, 128)) {
					System.err.println("Update file has been corrupted.");
					return null;
				}

				return Files.newInputStream(update, StandardOpenOption.READ);
			} catch (SignatureException | InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException e) {
				throw new IOException("Unable to verify message", e);
			}

		} else {
			return null;
		}

	}

	/**
	 * Fetches the head (newest) version of this binary program.
	 *
	 * @return the newest version in a md5 digest.
	 * @throws IOException if an error occurs while connecting the the update server.
	 */
	public static byte[] fetchHeadVersion() throws IOException {
		Path update = ChatList.getUpdatePath().resolve("head.smga");
		if (Files.exists(update)) {
			try (InputStream updateData = Files.newInputStream(update, StandardOpenOption.READ)) {
				byte[] hash = new byte[32];
				updateData.read(hash);
				return hash;
			}
		} else {
			return null;
		}
	}

	/**
	 * Retrieves the version of this binary file.
	 *
	 * @return the version of this binary file in a sha256 digest.
	 */
	public static byte[] getVersion() {
		// TO DO: implement this method.
		return null;
	}

	private static void initKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
		if (!initKey) {
			AuthCode updaterKey = new AuthCode(AuthKeyType.Public, updaterKeyData);
			initKey = true;
		}
	}

	private UpdateService() {
	}
}
