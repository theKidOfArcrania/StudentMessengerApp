package messenger.profile;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.imageio.ImageIO;

import messenger.ChatList;
import messenger.IllegalPermissionAccessException;
import messenger.KeyExistsException;
import messenger.profile.AuthCode.AuthKeyType;
import messenger.ui.image.ImageHelper;

public class Profile {
	public static final String PROF_STUB = "prof.ID", PROF_IMAGE = "profile.eimg";

	public static void createProfile(String profileName, String userName, AuthCode authPassword) throws IOException {
		if (authPassword.getKeyType() != AuthKeyType.Secret) {
			throw new IllegalArgumentException("Password must be a Secret Key Auth");
		}
		Path profilePath = getProfilePath(profileName),
		profStub = profilePath.resolve(PROF_STUB);

		//Creates directories and sets attributes, hidden and system
		Files.createDirectories(profilePath);
		Files.setAttribute(profilePath, "dos:hidden", true);
		Files.setAttribute(profilePath, "dos:system", true);

		try (DataOutputStream out = new DataOutputStream(Files.newOutputStream(profStub, StandardOpenOption.CREATE_NEW))) {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			KeyPair auths = keyGen.genKeyPair();
			Cipher c = Cipher.getInstance("RSA");

			// Get encoded/encrypted version.
			authPassword.initCipher(c, Cipher.WRAP_MODE);
			byte[] passHash = authPassword.getHash(),
			pubAuth = auths.getPublic().getEncoded(),
			prvAuth = c.wrap(auths.getPrivate()),
			salt = new byte[8];
			
			SecureRandom rnd = SecureRandom.getInstanceStrong();
			rnd.nextBytes(salt);

			// Write to output stream.
			out.writeUTF(userName);
			out.writeInt(passHash.length);
			out.write(salt);
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
	private BufferedImage profile;
	private AuthCode publicAuth, privateAuth;
	private HashMap<UUID, AuthCode> chatAuths;
	private final byte[] passSalt = new byte[8];
	private byte[] prvAuth, passHash;

	@SuppressWarnings("unused")
	public Profile(String name) throws IOException {
		try {
			this.userID = UUID.nameUUIDFromBytes(name.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new InternalError("Cannot load UTF-8 encoding");
		}
		this.name = name;

		Path profilePath = getProfilePath(userID),
		profStub = profilePath.resolve(PROF_STUB),
		profImage = profilePath.resolve(PROF_IMAGE);
		try {
			try (DataInputStream in = new DataInputStream(Files.newInputStream(profStub))) {
				// Read from input stream
				in.readUTF(); // disregard the name parameter.
				passHash = new byte[in.readInt()];
				in.readFully(passSalt);
				in.readFully(passHash);
				byte[] pubAuth = new byte[in.readInt()];
				in.readFully(pubAuth);
				prvAuth = new byte[in.readInt()];
				in.readFully(prvAuth);

				// Load public auth.
				publicAuth = new AuthCode(AuthKeyType.Public, pubAuth);
			}

			if (Files.exists(profImage)) {
				Cipher c = Cipher.getInstance("RSA");
				publicAuth.initCipher(c, Cipher.DECRYPT_MODE);
				try (InputStream in = new CipherInputStream(Files.newInputStream(profImage), c)) {
					profile = ImageIO.read(in);
				}
			} else {
				profile = ImageHelper.loadImage("messenger/ui/image/DefaultProfile.jpg");
			}
		} catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException | NoSuchPaddingException e) {
			throw new IOException("Unable to open profile.", e);
		} catch (EOFException e) {
			throw new IOException("Corrupted profile file.");
		}
	}

	public void appendKeyRing(String name, AuthCode key) throws KeyExistsException {
		UUID nameID;
		try {
			nameID = UUID.nameUUIDFromBytes(name.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			nameID = UUID.nameUUIDFromBytes(name.getBytes());
			e.printStackTrace();
		}

		if (chatAuths.containsKey(nameID)) {
			throw new KeyExistsException();
		}

		// TO DO: write to profile file.

		chatAuths.put(nameID, key);
	}

	public boolean authenticate(AuthCode authPassword) throws Exception {
		for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; i++) {
			if (Arrays.equals(passHash, authPassword.getHash(passSalt, (byte) i))) {
				Cipher c = Cipher.getInstance("RSA");
				authPassword.initCipher(c, Cipher.UNWRAP_MODE);
				privateAuth = new AuthCode(c.unwrap(prvAuth, "RSA", Cipher.PRIVATE_KEY));
				refreshKeyRing();
				return true;
			}
		}
		return false;
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

	public BufferedImage getProfileImage() {
		return profile;
	}

	public AuthCode getPublicAuth() {
		return publicAuth;
	}

	public void refreshKeyRing() {
		// TO DO: write to profile path.
	}

	public void setProfileImage(BufferedImage newProfile) throws IOException, IllegalPermissionAccessException {
		if (privateAuth == null) {
			throw new IllegalPermissionAccessException("You must have the private auth key to set profile");
		}

		// Attempt to write to profile file before setting thingy.
		try {
			Cipher c = Cipher.getInstance("RSA");

			privateAuth.initCipher(c, Cipher.ENCRYPT_MODE);

			Path profilePath = getProfilePath(userID),
			profImage = profilePath.resolve(PROF_IMAGE);

			try (CipherOutputStream out = new CipherOutputStream(Files.newOutputStream(profImage), c)) {
				ImageIO.write(newProfile, "png", out);
			}
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
			throw new IOException(e);
		}

		profile = newProfile;
	}
}
