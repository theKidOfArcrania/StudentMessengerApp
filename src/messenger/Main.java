package messenger;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.UserPrincipal;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import messenger.ui.MessengerApp;

import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

public class Main {

	public static void checkProfile() {

		String userName = System.getProperty("user.name");
		Path userProfilePath = Paths.get(System.getenv("USERPROFILE"));

		System.out.println(userProfilePath);

		if (!userProfilePath.getFileName().toString().equals(userName)) {
			System.out.println("Nice try... No tampering with the environment variables allowed...");
		} else if (!Files.exists(userProfilePath)) {
			System.out.println("Nice try... No tampering with the environment variables allowed...");
		} else {
			try {
				UserPrincipal currentUser = FileSystems.getDefault().getUserPrincipalLookupService().lookupPrincipalByName(userName);
				for (AclEntry acl : Files.getFileAttributeView(userProfilePath, AclFileAttributeView.class).getAcl()) {
					acl.f
					if (!owner.equals(currentUser)) {
						System.out.println("Nice try... No tampering with the environment variables allowed...");
					} else {
						System.out.println("Hello " + System.getProperty("user.name"));
					}
				}
			} catch (IOException e) {
				System.out.println("Nice try... No tampering with the environment variables allowed...");
			}

		}
	}

	public static void main(String[] args) throws Exception {
		runProgram();
	}

	public static void runProgram() throws IOException {
		try {
			UIManager.setLookAndFeel("com.jtattoo.plaf.hifi.HiFiLookAndFeel");
			com.jtattoo.plaf.texture.TextureLookAndFeel.setTheme("Hifi", "Messenger", "Messenger App");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
			System.exit(1);
		}

		String userName = "";
		while (userName.length() > 30 | userName.length() < 2) {
			userName = JOptionPane.showInputDialog(null, "Input your Username");
			if (userName == null) {
				return;
			}
			if (userName.length() > 30) {
				showMessageDialog(null, "Invalid Username. The maximum is 30 Characters.", "Messenger", WARNING_MESSAGE);
			}
			if (userName.length() < 2) {
				showMessageDialog(null, "Invalid Username. Username is too short.", "Messenger", WARNING_MESSAGE);
			}
			if (userName.equalsIgnoreCase("crunchycat")) {
				showMessageDialog(null, "Reserved for Developer", "Messenger", WARNING_MESSAGE);
			}
		}

		MessengerApp app = new MessengerApp(userName);
		app.setVisible(true);
	}
}
