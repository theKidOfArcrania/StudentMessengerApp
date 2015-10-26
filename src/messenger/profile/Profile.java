package messenger.profile;

import java.awt.Image;
import java.io.IOException;

import messenger.ChatList;

public class Profile {

	private static void createProfile() {

	}

	private static void loadProfile(String profileUsername) {

	}

	private final String name;
	private Image profile;
	private AuthCode authPassword;
	private AuthCode publicAuth;
	private AuthCode privateAuth;

	public Profile(String name) {
		this.name = name;
	}

	public void authenticate(AuthCode authPassword) throws IOException {
		Path profilePath = ChatList.getMainRoot().resolve("profile");
		// TO DO: authenticate the password.
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
