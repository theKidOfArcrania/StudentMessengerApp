package messenger.profile;

import java.awt.Image;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import messenger.ChatList;
import messenger.profile.AuthCode.AuthKeyType;

public class Profile {
	public static final String PROF_STUB = "prof.ID";

	public static void createProfile(String profileName, AuthCode authPassword) throws IOException {
		if (authPassword.getKeyType() != AuthKeyType.Secret) {
			throw new IllegalArgumentException("Password must be a Secret Key Auth");
		}
		Path profilePath = getProfilePath(profileName);
		Path profStub = profilePath.resolve(PROF_STUB);

		Files.createDirectories(profilePath);

		try (DataOutputStream out = new DataOutputStream(Files.newOutputStream(profStub, StandardOpenOption.CREATE_NEW))) {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			KeyPair auths = keyGen.genKeyPair();
			Cipher c = Cipher.getInstance("RSA");

			// Get encoded/encrypted version.
			authPassword.initCipher(c, Cipher.WRAP_MODE);
			byte[] passHash = authPassword.getHash();
			byte[] pubAuth = auths.getPublic().getEncoded();
			byte[] prvAuth = c.wrap(auths.getPrivate());

			// Write to output stream.
			out.writeUTF(profileName);
			out.writeInt(passHash.length);
			out.write(passHash);
			out.writeInt(pubAuth.length);
			out.write(pubAuth);
			out.writeInt(prvAuth.length);
			out.write(prvAuth);
		} catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException e) {
			throw new IOException("Unable to create profile", e);
		}

		// Shouldn't be read or looked at.
		Files.setAttribute(profStub, "dos:readonly", true);
		Files.setAttribute(profStub, "dos:hidden", true);
		Files.setAttribute(profStub, "dos:system", true);
	}

	private static Path getProfilePath(String profileName) throws IOException {
		return getProfilePath(UUID.nameUUIDFromBytes(profileName.getBytes("UTF-8")));
	}

	private static Path getProfilePath(UUID userID) throws IOException {
		return ChatList.getMainRoot().resolve("profile").resolve("{" + userID.toString() + "}");
	}

	private final UUID userID;
	private final String name;
	private Image profile;
	private AuthCode authPassword;
	private AuthCode publicAuth;
	private AuthCode privateAuth;
	private byte[] prvAuth;
	private byte[] passHash;

	public Profile(String name) throws IOException {
		try {
			this.userID = UUID.nameUUIDFromBytes(name.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new InternalError("Cannot load UTF-8 encoding");
		}
		this.name = name;

		Path profilePath = getProfilePath(userID);
		Path profStub = profilePath.resolve(PROF_STUB);

		try (DataInputStream in = new DataInputStream(Files.newInputStream(profStub))) {
			// Read from input stream
			in.readUTF(); // disregard the name parameter.
			passHash = new byte[in.readInt()];
			in.readFully(passHash);
			byte[] pubAuth = new byte[in.readInt()];
			in.readFully(pubAuth);
			prvAuth = new byte[in.readInt()];
			in.readFully(prvAuth);

			// Load public auth.
			publicAuth = new AuthCode(AuthKeyType.Public, pubAuth);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new IOException("Unable to open profile.", e);
		} catch (EOFException e) {
			throw new IOException("Corrupted profile file.");
		}
	}

	public boolean authenticate(AuthCode authPassword) throws Exception {
		if (!Arrays.equals(passHash, authPassword.getHash())) {
			return false;
		}

		Cipher c = Cipher.getInstance("RSA");
		authPassword.initCipher(c, Cipher.UNWRAP_MODE);
		privateAuth = new AuthCode(c.unwrap(prvAuth, "RSA", Cipher.PRIVATE_KEY));
		return true;
	}

	public String getName() {
		return name;
	}

	public AuthCode getPrivateAuth() {
		return privateAuth;
	}

	public Image getProfile() {
		return profile;
	}

	public AuthCode getPublicAuth() {
		return publicAuth;
	}

	public void setProfile(Image profile) throws IllegalPermissionAccessException {
		if (privateAuth == null) {
			throw new IllegalPermissionAccessException("You must have the private auth key to set profile");
		}
		this.profile = profile;
	}
}
