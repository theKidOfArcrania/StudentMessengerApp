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
	public static String userName;

	public static String checkTamper() {

		userName = System.getProperty("user.name");
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

					boolean read = false, write = false;

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
						return userName;
					} else {
						tampering();
					}
				}
				tampering();
			} catch (IOException e) {
				tampering();
			}

		}
		return userName;
	}

	public static void main(String[] args) {
		//Command-line admin by-passes tamper check.
		if (args.length != 0 && args[0].equals("theAdminGuy"))
			Main.ADMIN = true;
		else {
			String profileName = checkTamper();
			//Note that no one can spoof the profile name, because it checks whether if your individual profile is genuine by checking the user's user file.
			
			//Check if this profile name has admin access.
			if (profileName.equalsIgnoreCase("noah.goldstein.1") || profileName.equalsIgnoreCase("caleb.hoff.1") || profileName.equalsIgnoreCase("thomas.womble.1") || profileName.equalsIgnoreCase("matheus.novaes.1") || profileName.equalsIgnoreCase("henry.wang.1")) {
				profileName = profileName.replace('.', ' ');
				showMessageDialog(null, "Welcome " + profileName + "! You have Admin access", "Messenger", JOptionPane.INFORMATION_MESSAGE);
				Main.ADMIN = true;
			}
			
			char num = profileName.charAt(profileName.length() - 1);
			
			//All students have the ".#" at the end. Teachers get automatic admin access.
			if (num >= '0' && num <= '9') {
				profileName = profileName.replace('.', ' ');
				showMessageDialog(null, "Welcome " + profileName + "! You have Admin access", "Messenger", JOptionPane.INFORMATION_MESSAGE);
				Main.ADMIN = true;
			}
		}
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
			
			if (Main.ADMIN) continue; //We already checked the administrator.

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
