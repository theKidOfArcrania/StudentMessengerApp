package messenger;

import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import messenger.ui.MessengerApp;

public class Main {

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
		while (userName.length() > 30| userName.length() < 2) {
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
