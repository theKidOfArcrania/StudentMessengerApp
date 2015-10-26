package messenger.profile;

import java.awt.Image;
import java.util.HashMap;

import messenger.ui.image.ImageHelper;

public class Profile {

	private static void createProfile() {

	}

	private final String name;
	private final HashMap<String, Property<?>> properties = new HashMap<>();
	private AuthCode publicAuth;
	private AuthCode privateAuth;

	public Profile(String name) {
		this.name = name;
	}

	public Image getImage() {
		properties.get("profile");
		Image ImageHelper.loadImage("messenger/ui/image/DefaultProfile.jpg");
	}
}
