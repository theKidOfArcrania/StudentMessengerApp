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
			com.jtattoo.plaf.texture.TextureLookAndFeel.setTheme("Hifi", "Messenger App", "Messenger App");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
			System.exit(1);
		}

		String userName = "";
		while (userName.isEmpty()) {
			userName = JOptionPane.showInputDialog(null, "Input your username.");
			if (userName == null) {
				return;
			}
			if (userName.isEmpty()) {
				showMessageDialog(null, "Invalid Username", "Messenger", WARNING_MESSAGE);
			}
		}
		
		MessengerApp app = new MessengerApp(userName);
		app.setVisible(true);
	}
}
