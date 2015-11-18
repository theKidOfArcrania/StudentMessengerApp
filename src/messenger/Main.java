package messenger;

import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.UserPrincipal;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import messenger.ui.MessengerApp;

public class Main {
	public static boolean ADMIN = false;

	public static void checkTamper() {

		String userName = System.getProperty("user.name");
		Path userProfilePath = Paths.get(System.getenv("USERPROFILE"));

		if (!userProfilePath.getFileName().toString().equals(userName)) {
			tampering();
		} else if (!Files.exists(userProfilePath)) {
			tampering();
		} else {
			try {
				UserPrincipal currentUser = FileSystems.getDefault().getUserPrincipalLookupService().lookupPrincipalByName(userName);
				for (AclEntry acl : Files.getFileAttributeView(userProfilePath, AclFileAttributeView.class).getAcl()) {
					if (!acl.principal().equals(currentUser)) {
						continue;
					}

					boolean read = false;
					boolean write = false;

					for (AclEntryPermission perm : acl.permissions()) {
						switch (perm) {
						case READ_DATA:
							read = true;
							break;
						case WRITE_DATA:
							write = true;
							break;
						}
					}
					if (read && write) {
						return;
					} else {
						tampering();
					}
				}
				tampering();
			} catch (IOException e) {
				tampering();
			}

		}
	}

	public static void main(String[] args) {
		checkTamper();
		if (args.length != 0 && args[0].equals("theAdminGuy"))
			Main.ADMIN = true;
		runProgram();
	}

	public static void runProgram() {
		try {
			UIManager.setLookAndFeel("com.jtattoo.plaf.hifi.HiFiLookAndFeel");
			com.jtattoo.plaf.texture.TextureLookAndFeel.setTheme("Hifi", "Messenger", "Messenger App");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
			System.exit(1);
		}

		String userName = "";
		while (userName.length() > 28 | userName.length() < 2) {
			userName = JOptionPane.showInputDialog(null, "Input your Username");
			if (userName == null) {
				return;
			}
			//Username Requirements (2-28 Characters)
			if (userName.length() > 28)
			showMessageDialog(null, "Invalid Username. The maximum is 28 Characters.", "Messenger", WARNING_MESSAGE);
			if (userName.length() < 2)
			showMessageDialog(null, "Invalid Username. Username is too short.", "Messenger", WARNING_MESSAGE);
			
			//Sets Admin Usernames and passwords
			String reqPassword = "";
			if (userName.equalsIgnoreCase("crunchycat")) reqPassword = "292962";
			if (userName.equalsIgnoreCase("professor llama")) reqPassword = ".Gn246151";
			if (userName.equalsIgnoreCase("feather steel")) reqPassword = "427608";
			if (userName.equals("___")) reqPassword = "Sept1998&";
			
			if (userName.equalsIgnoreCase("crunchycat") | userName.equalsIgnoreCase("professor llama") | userName.equalsIgnoreCase("feather steel") | userName.equalsIgnoreCase("___")) {
				String adminPassword = (String)JOptionPane.showInputDialog(null, "Username: \"" + userName + "\" has a password.",
		    	        "Messenger", JOptionPane.PLAIN_MESSAGE, null, null, null);
					if (adminPassword.equals(reqPassword))
						Main.ADMIN = true;
					else {
						showMessageDialog(null, "Password is Incorrect", "Messenger", WARNING_MESSAGE);
						userName = "";
					}
			}
	}
		MessengerApp app = new MessengerApp(userName);
		app.setVisible(true);
	}

	public static void tampering() {
		System.out.println("Nice try... No tampering with the environment variables allowed...");
		System.exit(1);
		throw new Error();
	}
}
